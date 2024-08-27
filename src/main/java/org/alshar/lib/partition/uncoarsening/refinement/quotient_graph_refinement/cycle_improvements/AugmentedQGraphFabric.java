package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.cycle_improvements;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.priority_queues.BucketPQ;
import org.alshar.lib.data_structure.priority_queues.PriorityQueueInterface;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.BoundaryLookup;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.PartialBoundary;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.kway_graph_refinement.KWayGraphRefinementCommons;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.kway_graph_refinement.KWaySimpleStopRule;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.kway_graph_refinement.KWayStopRule;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.two_way_fm_refinement.TwoWayFM;
import org.alshar.lib.tools.RandomFunctions;
import java.util.*;

import static org.alshar.lib.enums.MLSRule.*;

public class AugmentedQGraphFabric {
    private TwoWayFM mTwfm;
    private List<Boolean> mEligible;
    private List<Integer> mToMakeEligible;
    private KWayGraphRefinementCommons commons;

    public AugmentedQGraphFabric() {
        mTwfm = new TwoWayFM();
        mEligible = new ArrayList<>();
        mToMakeEligible = new ArrayList<>();
    }

    public boolean buildAugmentedQuotientGraph(PartitionConfig config,
                                               GraphAccess G,
                                               CompleteBoundary boundary,
                                               AugmentedQGraph aqg,
                                               int s, boolean rebalance, boolean plus) {

        GraphAccess GBar = new GraphAccess();
        boundary.getUnderlyingQuotientGraph(GBar);

        if (mEligible.size() != G.numberOfNodes()) {
            mEligible = new ArrayList<>(Collections.nCopies(G.numberOfNodes(), true));
        } else {
            cleanupEligible();
        }

        if (!rebalance) {
            List<BlockPairDifference> vecBpd = new ArrayList<>();
            for (int lhs = 0; lhs < GBar.numberOfNodes(); lhs++) {
                for (int e = GBar.getFirstEdge(lhs); e < GBar.getFirstInvalidEdge(lhs); e++) {
                    int rhs = GBar.getEdgeTarget(e);

                    BlockPairDifference bpd = new BlockPairDifference(lhs, rhs, 0);
                    vecBpd.add(bpd);
                }
            }

            for (int j = 0; j < config.getKabaPackingIterations(); j++) {
                RandomFunctions.permutateVectorGoodSmall(vecBpd);
                boolean variantToUse = plus;
                for (BlockPairDifference bpd : vecBpd) {
                    BoundaryLookup.BoundaryPair bp = new BoundaryLookup.BoundaryPair(config.getK(), bpd.getLhs(), bpd.getRhs());

                    if (plus && config.isKabaFlipPackings()) {
                        variantToUse = RandomFunctions.nextBool();
                    }

                    localSearch(config, variantToUse, G, boundary, aqg, bp, s);
                }
            }
        } else {
            List<Integer> candidates = new ArrayList<>();
            int[] parent = new int[GBar.numberOfNodes()];
            boolean graphModelWillBeFeasible = false;

            Arrays.fill(parent, -1);

            for (int lhs = 0; lhs < GBar.numberOfNodes(); lhs++) {
                for (int e = GBar.getFirstEdge(lhs); e < GBar.getFirstInvalidEdge(lhs); e++) {
                    int rhs = GBar.getEdgeTarget(e);

                    if (boundary.getBlockWeight(lhs) > config.getUpperBoundPartition()
                            && boundary.getBlockWeight(rhs) < config.getUpperBoundPartition()
                            && !graphModelWillBeFeasible) {

                        BoundaryLookup.BoundaryPair bp = new BoundaryLookup.BoundaryPair(config.getK(), lhs, rhs);

                        boolean success = localSearch(config, false, G, boundary, aqg, bp, s);
                        if (success) {
                            graphModelWillBeFeasible = true;
                        }
                    }
                }
            }

            if (!graphModelWillBeFeasible) {
                // Use the fallback method with adjusted parameters
                rebalanceFallback(config, G, GBar, boundary, candidates, parent, aqg);
                return true;
            } else {
                // Run Kaba packing iterations
                runKabaPackingIterations(config, G, GBar, boundary, aqg, s, new ArrayList<>(), new int[G.numberOfNodes()]);
            }
        }
        return false;
    }




    private void runKabaPackingIterations(PartitionConfig config,
                                          GraphAccess G,
                                          GraphAccess GBar,  // Pass GBar as an additional parameter
                                          CompleteBoundary boundary,
                                          AugmentedQGraph aqg,
                                          int s,
                                          List<Integer> candidates,
                                          int[] parent) {
        for (int j = 0; j < config.getKabaPackingIterations(); j++) {
            RandomFunctions.permutateVectorGoodSmall(candidates);

            for (int candidate : candidates) {
                int curBlock = candidate;
                if (parent[curBlock] == -1) {
                    continue;
                }

                BoundaryLookup.BoundaryPair bp = new BoundaryLookup.BoundaryPair(config.getK(), parent[curBlock], curBlock);

                boolean success = localSearch(config, false, G, boundary, aqg, bp, s);

                if (!success) {
                    rebalanceFallback(config, G, GBar, boundary, candidates, parent, aqg);
                    return;
                }
            }
        }
    }



    private void rebalanceFallback(PartitionConfig config,
                                   GraphAccess G,
                                   GraphAccess GBar,
                                   CompleteBoundary boundary,
                                   List<Integer> candidates,
                                   int[] parent,
                                   AugmentedQGraph aqg) {

        // List to track eligibility of nodes
        List<Boolean> eligible = new ArrayList<>(Collections.nCopies(G.numberOfNodes(), true));

        // Permutate the candidate list
        RandomFunctions.permutateVectorGoodSmall(candidates);

        // Lists to hold start vertices and other candidates
        List<Integer> startVertices = new ArrayList<>();

        // Initialize parent array and startVertices list
        Arrays.fill(parent, -1);
        for (int lhs = 0; lhs < GBar.numberOfNodes(); lhs++) {
            if (boundary.getBlockWeight(lhs) > config.getUpperBoundPartition()) {
                startVertices.add(lhs);
            } else if (boundary.getBlockWeight(lhs) < config.getUpperBoundPartition()) {
                candidates.add(lhs);
            }
        }

        // Permutate start vertices and initialize BFS queue
        RandomFunctions.permutateVectorGoodSmall(startVertices);
        Deque<Integer> bfsQueue = new ArrayDeque<>(startVertices);
        for (int startVertex : startVertices) {
            parent[startVertex] = startVertex;
        }

        // Perform BFS to populate the parent array
        while (!bfsQueue.isEmpty()) {
            int lhs = bfsQueue.poll();
            for (int e = GBar.getFirstEdge(lhs); e < GBar.getFirstInvalidEdge(lhs); e++) {
                int rhs = GBar.getEdgeTarget(e);
                if (parent[rhs] == -1 && boundary.getDirectedBoundary(lhs, lhs, rhs).size() > 0) {
                    parent[rhs] = lhs;
                    bfsQueue.add(rhs);
                }
            }
        }

        // Main loop to handle rebalancing
        int curBlock = -1;
        int startBlock;
        boolean candidateSetWasEmpty = false;
        List<Integer> tmpCandidates = new ArrayList<>(candidates);

        do {
            if (candidates.isEmpty()) {
                candidateSetWasEmpty = true;
                break;
            }
            int rIdx = RandomFunctions.nextInt(0, candidates.size() - 1);
            curBlock = candidates.get(rIdx);
            Collections.swap(candidates, rIdx, candidates.size() - 1);
            candidates.remove(candidates.size() - 1);
        } while (parent[curBlock] == -1);

        if (candidateSetWasEmpty) {
            int rIdx = RandomFunctions.nextInt(0, tmpCandidates.size() - 1);
            int curBlockTmp = tmpCandidates.get(rIdx);

            do {
                int node = RandomFunctions.nextInt(0, G.numberOfNodes() - 1);
                int nodeBlock = G.getPartitionIndex(node);
                if (nodeBlock != curBlockTmp && boundary.getBlockWeight(nodeBlock) > config.getUpperBoundPartition()) {
                    performSimpleMove(config, G, boundary, node, nodeBlock, curBlockTmp);
                    return;
                }
            } while (true);
        }

        startBlock = curBlock;

        while (boundary.getBlockWeight(curBlock) <= config.getUpperBoundPartition()) {
            BoundaryLookup.BoundaryPair bp = new BoundaryLookup.BoundaryPair(config.getK(), parent[curBlock], curBlock);
            curBlock = parent[curBlock];

            boolean success = localSearch(config, false, G, boundary, aqg, bp, 1);

            if (!success) {
                candidates.add(startBlock);
                // Recursively call rebalanceFallback if no success
                rebalanceFallback(config, G, GBar, boundary, candidates, parent, aqg);
                return;
            }
        }
    }



    private void fallbackHelper(PartitionConfig config,
                                GraphAccess G,
                                CompleteBoundary boundary,
                                List<Integer> candidates,
                                int[] parent,
                                AugmentedQGraph aqg) {
        int curBlock;
        int startBlock;

        while (true) {
            if (candidates.isEmpty()) {
                randomSimpleMove(config, G, boundary, candidates);
                return;
            }

            int rIdx = RandomFunctions.nextInt(0, candidates.size() - 1);
            curBlock = candidates.get(rIdx);
            Collections.swap(candidates, rIdx, candidates.size() - 1);
            candidates.remove(candidates.size() - 1);

            if (parent[curBlock] != -1) break;
        }

        startBlock = curBlock;

        while (boundary.getBlockWeight(curBlock) <= config.getUpperBoundPartition()) {
            BoundaryLookup.BoundaryPair bp = new BoundaryLookup.BoundaryPair(config.getK(), parent[curBlock], curBlock);
            curBlock = parent[curBlock];
            boolean success = localSearch(config, false, G, boundary, aqg, bp, 1);

            if (!success) {
                candidates.add(startBlock);
                rebalanceFallback(config, G, new GraphAccess(), boundary, candidates, parent, aqg);
                return;
            }
        }
    }

    private void randomSimpleMove(PartitionConfig config,
                                  GraphAccess G,
                                  CompleteBoundary boundary,
                                  List<Integer> candidates) {
        int rIdx = RandomFunctions.nextInt(0, candidates.size() - 1);
        int curBlock = candidates.get(rIdx);

        while (true) {
            int node = RandomFunctions.nextInt(0, G.numberOfNodes() - 1);
            int nodeBlock = G.getPartitionIndex(node);

            if (nodeBlock != curBlock && boundary.getBlockWeight(nodeBlock) > config.getUpperBoundPartition()) {
                int from = nodeBlock;
                int to = curBlock;
                performSimpleMove(config, G, boundary, node, from, to);
                return;
            }
        }
    }

    private boolean localSearch(PartitionConfig config,
                                boolean plus,
                                GraphAccess G,
                                CompleteBoundary boundary,
                                AugmentedQGraph aqg,
                                BoundaryLookup.BoundaryPair pair,
                                int s) {
        return constructLocalSearchesOnQGraphEdge(config, G, boundary, aqg, pair, s, plus);
    }

    private boolean constructLocalSearchesOnQGraphEdge(PartitionConfig config,
                                                       GraphAccess G,
                                                       CompleteBoundary boundary,
                                                       AugmentedQGraph aqg,
                                                       BoundaryLookup.BoundaryPair pair,
                                                       int s,
                                                       boolean plus) {
        int lhs = pair.getLhs();
        int rhs = pair.getRhs();

        PartialBoundary lhsB = boundary.getDirectedBoundary(lhs, lhs, rhs);
        List<Integer> lhsBoundary = new ArrayList<>();

        lhsB.forAllBoundaryNodes(node -> {
            if (mEligible.get(node)) {
                lhsBoundary.add(node);
            }
        });

        if (lhsBoundary.isEmpty()) {
            return false;
        }

        for (int i = 0; i < 1; i++) {
            if (lhsBoundary.isEmpty()) return false;

            PairwiseLocalSearch pls = new PairwiseLocalSearch();

            int startNode = lhsBoundary.get(0);
            findEligibleStartNode(G, lhs, rhs, lhsBoundary, mEligible, startNode);

            if (!mEligible.get(startNode)) return false;

            if (plus) {
                moreLocalizedSearch(config, G, boundary, lhs, rhs, startNode, s, pls);
            } else {
                directedMoreLocalizedSearch(config, G, boundary, lhs, rhs, startNode, s, pls);
            }

            aqg.commitPairwiseLocalSearch(pair, pls);

            if (plus) {
                BoundaryLookup.BoundaryPair oppPair = new BoundaryLookup.BoundaryPair(pair.getK(), rhs, lhs);
                aqg.commitPairwiseLocalSearch(oppPair, pls);
            }
        }
        return true;
    }

    private void directedMoreLocalizedSearch(PartitionConfig config,
                                             GraphAccess G,
                                             CompleteBoundary boundary,
                                             int lhs,
                                             int rhs,
                                             int startNode,
                                             int numberOfSwaps,
                                             PairwiseLocalSearch pls) {

        int maxDegree = G.getMaxDegree();
        PriorityQueueInterface queue = new BucketPQ(maxDegree);

        int[] intDegree = new int[]{0};
        int[] extDegree = new int[]{0};
        mTwfm.intExtDegree(G, startNode, lhs, rhs, intDegree, extDegree);

        int gain = extDegree[0] - intDegree[0];
        queue.insert(startNode, gain);

        if (queue.isEmpty()) {
            return;
        }

        int movements = 0;
        int overallGain = 0;
        KWayStopRule stoppingRule = new KWaySimpleStopRule(config);

        int minCutIndex = 0;
        int stepLimit = 200;
        int inputCut = boundary.getEdgeCut(lhs, rhs);
        int minCut = inputCut;
        int from = lhs;
        int to = rhs;

        for (movements = 0; movements < numberOfSwaps; movements++) {
            if (queue.isEmpty()) {
                break;
            }
            if (stoppingRule.searchShouldStop(minCutIndex, movements, stepLimit)) break;

            gain = queue.maxValue();
            int node = queue.deleteMax();

            moveNode(config, G, node, queue, boundary, from, to);

            overallGain += gain;
            inputCut -= gain;

            stoppingRule.pushStatistics(gain);

            if (inputCut < minCut) {
                minCutIndex = movements;
                minCut = inputCut;
            }

            pls.getVertexMovements().add(node);
            pls.getBlockMovements().add(to);
            pls.getGains().add(overallGain);
            mToMakeEligible.add(node);
        }

        rollbackMoves(G, config, lhs, rhs, pls, queue, boundary);
    }

    public void moreLocalizedSearch(PartitionConfig config, GraphAccess G, CompleteBoundary boundary,
                                    int lhs, int rhs, int startNode, int numberOfSwaps, PairwiseLocalSearch pls) {

        // Create priority queues for lhs and rhs partitions
        PriorityQueueInterface queueLhs = new BucketPQ(G.getMaxDegree());
        PriorityQueueInterface queueRhs = new BucketPQ(G.getMaxDegree());

        // Calculate internal and external degrees for the start node
        int[] intDegree = new int[1];
        int[] extDegree = new int[1];
        mTwfm.intExtDegree(G, startNode, lhs, rhs, intDegree, extDegree);

        int gain = extDegree[0] - intDegree[0];
        queueLhs.insert(startNode, gain);

        // Find a start node for the rhs queue
        int startNodeRhs = startNode; // Dummy initialization
        int maxGain = Integer.MIN_VALUE;
        for (int e = G.getFirstEdge(startNode); e < G.getFirstInvalidEdge(startNode); e++) {
            int target = G.getEdgeTarget(e);
            if (G.getPartitionIndex(target) == rhs && mEligible.get(target)) {
                mTwfm.intExtDegree(G, target, rhs, lhs, intDegree, extDegree);
                int currentGain = extDegree[0] - intDegree[0];
                if (currentGain > maxGain) {
                    maxGain = currentGain;
                    startNodeRhs = target;
                }
            }
        }

        if (mEligible.get(startNodeRhs) && startNodeRhs != startNode) {
            queueRhs.insert(startNodeRhs, maxGain);
        }

        if (queueLhs.isEmpty() || queueRhs.isEmpty()) {
            queueLhs.clear();
            queueRhs.clear();
            return;
        }

        // Roll forwards
        int movements = 0;
        int overallGain = 0;

        KWayStopRule stoppingRule = new KWaySimpleStopRule(config);

        int minCutIndex = 0;
        int stepLimit = 200;
        int inputCut = boundary.getEdgeCut(lhs, rhs);
        int minCut = inputCut;
        int from = lhs;
        int to = rhs;

        PriorityQueueInterface queue = null;
        PriorityQueueInterface toQueue = null;

        int diff = 0;

        for (movements = 0; movements < numberOfSwaps; movements++) {
            if (queueLhs.isEmpty() || queueRhs.isEmpty()) {
                break;
            }
            if (stoppingRule.searchShouldStop(minCutIndex, movements, stepLimit)) break;

            int gainLhs = queueLhs.maxValue();
            int gainRhs = queueRhs.maxValue();

            boolean coin = false;
            switch (config.getKabaLsearchP()) {
                case COIN_DIFFTIE:
                    coin = RandomFunctions.nextBool();
                    if (coin) {
                        queue = gainRhs > gainLhs ? queueRhs : queueLhs;
                    } else {
                        queue = queueLhs;
                    }
                    break;

                case COIN_RNDTIE:
                    coin = RandomFunctions.nextBool();
                    if (coin) {
                        if (gainRhs > gainLhs) {
                            queue = queueRhs;
                        } else if (gainRhs < gainLhs) {
                            queue = queueLhs;
                        } else {
                            queue = RandomFunctions.nextBool() ? queueRhs : queueLhs;
                        }
                    } else {
                        queue = queueLhs;
                    }
                    break;

                case NOCOIN_DIFFTIE:
                    queue = gainRhs > gainLhs ? queueRhs : queueLhs;
                    break;

                case NOCOIN_RNDTIE:
                    if (gainRhs > gainLhs) {
                        queue = queueRhs;
                    } else if (gainRhs < gainLhs) {
                        queue = queueLhs;
                    } else {
                        queue = RandomFunctions.nextBool() ? queueRhs : queueLhs;
                    }
                    break;
            }

            int node = queue.deleteMax();
            if (queue == queueRhs) {
                from = rhs;
                to = lhs;
                gain = gainRhs;
                toQueue = queueLhs;
                diff += G.getNodeWeight(node);
            } else {
                from = lhs;
                to = rhs;
                gain = gainLhs;
                toQueue = queueRhs;
                diff -= G.getNodeWeight(node);
            }

            moveNode(config, G, node, queue, toQueue, boundary, from, to);

            overallGain += gain;
            inputCut -= gain;

            stoppingRule.pushStatistics(gain);

            if (inputCut < minCut && diff == 0) {
                minCut = inputCut;
                pls.clearMovements();
            } else {
                pls.addMovement(node, to, overallGain);
            }
            mToMakeEligible.add(node);
        }

        // Roll backwards
        int idx = pls.getMovementsSize() - 1;
        for (; idx >= 0; idx--) {
            int node = pls.getVertexMovement(idx);
            int currentFrom = G.getPartitionIndex(node);
            int currentTo = currentFrom == lhs ? rhs : lhs;
            performSimpleMove(config, G, boundary, node, currentFrom, currentTo);

            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                int target = G.getEdgeTarget(e);
                if (mEligible.get(target)) mToMakeEligible.add(target);
                mEligible.set(target, false);
            }
        }

        queueLhs.clear();
        queueRhs.clear();
        stoppingRule.resetStatistics();
    }


    private boolean randomizeQueueSelection(PartitionConfig config, PriorityQueueInterface queueLhs, PriorityQueueInterface queueRhs, int gainLhs, int gainRhs) {
        boolean coin;
        switch (config.getKabaLsearchP()) {
            case COIN_DIFFTIE:
                coin = RandomFunctions.nextBool();
                break;
            case COIN_RNDTIE:
                coin = randomCoinTie(gainLhs, gainRhs);
                break;
            case NOCOIN_DIFFTIE:
                coin = gainRhs > gainLhs;
                break;
            case NOCOIN_RNDTIE:
                coin = gainRhs > gainLhs || randomCoinTie(gainLhs, gainRhs);
                break;
            default:
                throw new IllegalArgumentException("Unexpected value: " + config.getKabaLsearchP());
        }
        return coin;
    }

    private boolean randomCoinTie(int gainLhs, int gainRhs) {
        boolean coin;
        coin = RandomFunctions.nextBool();
        if (coin) {
            if (gainRhs > gainLhs) {
                return true;
            } else if (gainRhs < gainLhs) {
                return false;
            } else {
                return RandomFunctions.nextBool();
            }
        }
        return false;
    }

    private void rollbackMoves(GraphAccess G, PartitionConfig config, int lhs, int rhs, PairwiseLocalSearch pls, PriorityQueueInterface queue, CompleteBoundary boundary) {
        int idx = pls.getVertexMovements().size() - 1;
        for (; idx >= 0; idx--) {
            int node = pls.getVertexMovements().get(idx);
            moveNode(config, G, node, queue, boundary, rhs, lhs);

            // block the neighboring nodes to avoid conflicts
            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                int target = G.getEdgeTarget(e);
                if (mEligible.get(target)) mToMakeEligible.add(target);
                mEligible.set(target, false);
            }
        }
    }

    private int findStartNodeRhs(GraphAccess G, PartitionConfig config, int lhs, int rhs, int startNode) {
        int startNodeRhs = startNode;
        int maxGain = Integer.MIN_VALUE;
        for (int e = G.getFirstEdge(startNode); e < G.getFirstInvalidEdge(startNode); e++) {
            int target = G.getEdgeTarget(e);
            if (G.getPartitionIndex(target) == rhs && mEligible.get(target)) {
                int[] intDegree = new int[]{0};
                int[] extDegree = new int[]{0};
                mTwfm.intExtDegree(G, target, rhs, lhs, intDegree, extDegree);
                int gain = extDegree[0] - intDegree[0];
                if (gain > maxGain) {
                    maxGain = gain;
                    startNodeRhs = target;
                }
            }
        }
        return startNodeRhs;
    }

    private void moveNode(PartitionConfig config,
                          GraphAccess G,
                          int node,
                          PriorityQueueInterface queue,
                          CompleteBoundary boundary,
                          int from,
                          int to) {

        G.setPartitionIndex(node, to);
        mEligible.set(node, false);

        BoundaryLookup.BoundaryPair pair = new BoundaryLookup.BoundaryPair(config.getK(), from, to);
        boundary.postMovedBoundaryNodeUpdates(node, pair, true, true);

        int thisNodesWeight = G.getNodeWeight(node);
        boundary.setBlockNoNodes(from, boundary.getBlockNoNodes(from) - 1);
        boundary.setBlockNoNodes(to, boundary.getBlockNoNodes(to) + 1);
        boundary.setBlockWeight(from, boundary.getBlockWeight(from) - thisNodesWeight);
        boundary.setBlockWeight(to, boundary.getBlockWeight(to) + thisNodesWeight);

        updateNeighborGains(G, node, queue, boundary, from, to);
    }

    private void moveNode(PartitionConfig config,
                          GraphAccess G,
                          int node,
                          PriorityQueueInterface queue,
                          PriorityQueueInterface toQueue,
                          CompleteBoundary boundary,
                          int from,
                          int to) {

        G.setPartitionIndex(node, to);
        mEligible.set(node, false);

        BoundaryLookup.BoundaryPair pair = new BoundaryLookup.BoundaryPair(config.getK(), from, to);
        boundary.postMovedBoundaryNodeUpdates(node, pair, true, true);

        int thisNodesWeight = G.getNodeWeight(node);
        boundary.setBlockNoNodes(from, boundary.getBlockNoNodes(from) - 1);
        boundary.setBlockNoNodes(to, boundary.getBlockNoNodes(to) + 1);
        boundary.setBlockWeight(from, boundary.getBlockWeight(from) - thisNodesWeight);
        boundary.setBlockWeight(to, boundary.getBlockWeight(to) + thisNodesWeight);

        updateNeighborGains(G, node, queue, toQueue, boundary, from, to);
    }

    private void updateNeighborGains(GraphAccess G,
                                     int node,
                                     PriorityQueueInterface queue,
                                     PriorityQueueInterface toQueue,
                                     CompleteBoundary boundary,
                                     int from,
                                     int to) {
        for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
            int target = G.getEdgeTarget(e);
            PriorityQueueInterface curQueue;

            if (G.getPartitionIndex(target) == from) {
                curQueue = queue;
            } else if (G.getPartitionIndex(target) == to) {
                curQueue = toQueue;
            } else {
                continue;
            }

            int[] intDegree = new int[]{0};
            int[] extDegree = new int[]{0};
            int targetPid = G.getPartitionIndex(target);
            int otherPid = targetPid == from ? to : from;
            mTwfm.intExtDegree(G, target, targetPid, otherPid, intDegree, extDegree);
            int gain = extDegree[0] - intDegree[0];

            if (curQueue.contains(target)) {
                if (extDegree[0] > 0) {
                    curQueue.changeKey(target, gain);
                } else {
                    curQueue.deleteNode(target);
                }
            } else {
                if (extDegree[0] > 0) {
                    if (mEligible.get(target)) {
                        curQueue.insert(target, gain);
                    }
                }
            }
        }
    }

    private void updateNeighborGains(GraphAccess G,
                                     int node,
                                     PriorityQueueInterface queue,
                                     CompleteBoundary boundary,
                                     int from,
                                     int to) {
        for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
            int target = G.getEdgeTarget(e);
            if (G.getPartitionIndex(target) != from) continue;

            int[] intDegree = new int[]{0};
            int[] extDegree = new int[]{0};
            mTwfm.intExtDegree(G, target, from, to, intDegree, extDegree);
            int gain = extDegree[0] - intDegree[0];

            if (queue.contains(target)) {
                if (extDegree[0] > 0) {
                    queue.changeKey(target, gain);
                } else {
                    queue.deleteNode(target);
                }
            } else {
                if (extDegree[0] > 0) {
                    if (mEligible.get(target)) {
                        queue.insert(target, gain);
                    }
                }
            }
        }
    }

    private void performSimpleMove(PartitionConfig config,
                                   GraphAccess G,
                                   CompleteBoundary boundary,
                                   int node,
                                   int from,
                                   int to) {

        G.setPartitionIndex(node, to);

        BoundaryLookup.BoundaryPair pair = new BoundaryLookup.BoundaryPair(config.getK(), from, to);
        boundary.postMovedBoundaryNodeUpdates(node, pair, true, true);

        int thisNodesWeight = G.getNodeWeight(node);
        boundary.setBlockNoNodes(from, boundary.getBlockNoNodes(from) - 1);
        boundary.setBlockNoNodes(to, boundary.getBlockNoNodes(to) + 1);
        boundary.setBlockWeight(from, boundary.getBlockWeight(from) - thisNodesWeight);
        boundary.setBlockWeight(to, boundary.getBlockWeight(to) + thisNodesWeight);
    }

    public void cleanupEligible() {
        for (int node : mToMakeEligible) {
            mEligible.set(node, true);
        }
        mToMakeEligible.clear();
    }

    private int findEligibleStartNode(GraphAccess G,
                                      int lhs,
                                      int rhs,
                                      List<Integer> lhsBoundary,
                                      List<Boolean> eligible,
                                      int startNode) {

        int maxIdx = lhsBoundary.size();
        int randomIdx;
        int maxGain = Integer.MIN_VALUE;

        do {
            maxGain = Integer.MIN_VALUE;
            randomIdx = RandomFunctions.nextInt(0, maxIdx - 1);
            for (int node : lhsBoundary) {
                if (eligible.get(node)) {
                    int[] intDegree = new int[]{0};
                    int[] extDegree = new int[]{0};
                    mTwfm.intExtDegree(G, node, lhs, rhs, intDegree, extDegree);
                    int gain = extDegree[0] - intDegree[0];
                    if (gain > maxGain) {
                        maxGain = gain;
                    }
                }
            }

            if (maxGain == Integer.MIN_VALUE) {
                break;
            }

            List<Integer> eligibles = new ArrayList<>();
            for (int node : lhsBoundary) {
                if (eligible.get(node)) {
                    int[] intDegree = new int[]{0};
                    int[] extDegree = new int[]{0};
                    mTwfm.intExtDegree(G, node, lhs, rhs, intDegree, extDegree);
                    if (extDegree[0] - intDegree[0] == maxGain) {
                        eligibles.add(node);
                    }
                }
            }

            randomIdx = RandomFunctions.nextInt(0, eligibles.size() - 1);
            startNode = eligibles.get(randomIdx);

            for (int i = 0; i < lhsBoundary.size(); i++) {
                if (lhsBoundary.get(i) == startNode) {
                    randomIdx = i;
                }
            }

            Collections.swap(lhsBoundary, randomIdx, maxIdx - 1);
            lhsBoundary.remove(maxIdx - 1);
            maxIdx--;
        } while (!mEligible.get(startNode) && maxIdx != 0);

        return maxGain;
    }
}
