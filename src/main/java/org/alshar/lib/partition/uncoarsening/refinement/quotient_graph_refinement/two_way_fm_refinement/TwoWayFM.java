package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.two_way_fm_refinement;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.priority_queues.BucketPQ;
import org.alshar.lib.data_structure.priority_queues.MaxNodeHeap;
import org.alshar.lib.data_structure.priority_queues.PriorityQueueInterface;
import org.alshar.lib.enums.PermutationQuality;
import org.alshar.lib.enums.StopRule;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.BoundaryLookup;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.PartialBoundary;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.TwoWayRefinement;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.kway_graph_refinement.VertexMovedHashtable;
import org.alshar.lib.tools.QualityMetrics;
import org.alshar.lib.tools.RandomFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TwoWayFM extends TwoWayRefinement {

    public TwoWayFM() {
    }

    @Override
    public int performRefinement(PartitionConfig config,
                                 GraphAccess G,
                                 CompleteBoundary boundary,
                                 List<Integer> lhsStartNodes,
                                 List<Integer> rhsStartNodes,
                                 BoundaryLookup.BoundaryPair pair,
                                 int lhsPartWeight,
                                 int rhsPartWeight,
                                 int cut,
                                 boolean[] somethingChanged) {

        if (lhsStartNodes.isEmpty() || rhsStartNodes.isEmpty()) {
            return 0; // nothing to refine
        }

        QualityMetrics qm = new QualityMetrics();
        assert pair.lhs != pair.rhs;
        assert assertDirectedBoundaryCondition(G, boundary, pair.lhs, pair.rhs);
        assert cut == qm.edgeCut(G, pair.lhs, pair.rhs);

        PriorityQueueInterface lhsQueue;
        PriorityQueueInterface rhsQueue;
        if (config.isUseBucketQueues()) {
            int maxDegree = G.getMaxDegree();
            lhsQueue = new BucketPQ(maxDegree);
            rhsQueue = new BucketPQ(maxDegree);
        } else {
            lhsQueue = new MaxNodeHeap();
            rhsQueue = new MaxNodeHeap();
        }

        initQueueWithBoundary(config, G, lhsStartNodes, lhsQueue, pair.lhs, pair.rhs);
        initQueueWithBoundary(config, G, rhsStartNodes, rhsQueue, pair.rhs, pair.lhs);

        QueueSelectionStrategy topgainQueueSelect = new QueueSelectionTopGain(config);
        QueueSelectionStrategy diffusionQueueSelect = new QueueSelectionDiffusion(config);
        QueueSelectionStrategy diffusionQueueSelectBlockTarget = new QueueSelectionDiffusionBlockTargets(config);

        VertexMovedHashtable movedIdx = new VertexMovedHashtable();
        List<Integer> transpositions = new ArrayList<>();

        int initialCut = cut;
        int maxNumberOfSwaps = boundary.getBlockNoNodes(pair.lhs) + boundary.getBlockNoNodes(pair.rhs);
        int stepLimit = Math.max((int) ((config.getFmSearchLimit() / 100.0) * maxNumberOfSwaps), 15);
        int minCutIndex = -1;

        PriorityQueueInterface[] fromQueue = new PriorityQueueInterface[1];
        PriorityQueueInterface[] toQueue = new PriorityQueueInterface[1];

        int[] from = new int[]{0};
        int[] to = new int[]{0};

        int[] fromPartWeight = {lhsPartWeight};
        int[] toPartWeight = {rhsPartWeight};

        EasyStopRule stRule = new EasyStopRule();
        PartitionAcceptRule acceptPartition;
        if (config.isInitialBipartitioning()) {
            acceptPartition = new IPPartitionAcceptRule(config, cut, lhsPartWeight, rhsPartWeight, pair.lhs, pair.rhs);
        } else {
            acceptPartition = new NormalPartitionAcceptRule(config, cut, lhsPartWeight, rhsPartWeight);
        }
        QueueSelectionStrategy qSelect;

        if (config.isSoftRebalance() || config.isRebalance() || config.isInitialBipartitioning()) {
            qSelect = config.isInitialBipartitioning() ? diffusionQueueSelectBlockTarget : diffusionQueueSelect;
        } else {
            qSelect = topgainQueueSelect;
        }

        // roll forwards
        int bestCut = cut;
        int numberOfSwaps = 0;
        for (numberOfSwaps = 0; numberOfSwaps < maxNumberOfSwaps; numberOfSwaps++) {
            if (stRule.searchShouldStop(minCutIndex, numberOfSwaps, stepLimit)) break;

            if (lhsQueue.isEmpty() && rhsQueue.isEmpty()) {
                break;
            }


            qSelect.selectQueue(lhsPartWeight, rhsPartWeight,
                    pair.lhs, pair.rhs,
                    from, to,
                    lhsQueue, rhsQueue,
                    fromQueue, toQueue);


            if (fromQueue[0] != null && !fromQueue[0].isEmpty()) {
                int gain = fromQueue[0].maxValue();
                int node = fromQueue[0].deleteMax();

                assert movedIdx.get(node).index == VertexMovedHashtable.NOT_MOVED;

                boundary.setBlockNoNodes(from[0], boundary.getBlockNoNodes(from[0]) - 1);
                boundary.setBlockNoNodes(to[0], boundary.getBlockNoNodes(to[0]) + 1);

                if (from[0] == pair.lhs) {
                    fromPartWeight[0] = lhsPartWeight;
                    toPartWeight[0] = rhsPartWeight;
                } else {
                    fromPartWeight[0] = rhsPartWeight;
                    toPartWeight[0] = lhsPartWeight;
                }

                moveNode(config, G, node, movedIdx,
                        fromQueue[0], toQueue[0],  // Pass the first element
                        from[0], to[0],            // Pass the first element
                        pair,
                        fromPartWeight, toPartWeight,
                        boundary);

                cut -= gain;

                if (acceptPartition.acceptPartition(config, cut, lhsPartWeight, rhsPartWeight, pair.lhs, pair.rhs, config.isRebalance())) {
                    assert cut <= bestCut || config.isRebalance();
                    if (cut < bestCut) {
                        somethingChanged[0] = true;
                    }
                    bestCut = cut;
                    minCutIndex = numberOfSwaps;
                }
                transpositions.add(node);
                // Safely check and update the moved index
                VertexMovedHashtable.MovedIndex movedIndex = movedIdx.get(node);
                if (movedIndex == null) {
                    // Initialize the movedIndex if it's not already in the map
                    movedIndex = new VertexMovedHashtable.MovedIndex();
                    movedIdx.put(node, movedIndex);
                }
                movedIndex.index = VertexMovedHashtable.MOVED.index;
            } else {
                break;
            }
        }

        assert assertDirectedBoundaryCondition(G, boundary, pair.lhs, pair.rhs);
        assert cut == qm.edgeCut(G, pair.lhs, pair.rhs);

        // roll backwards
        for (numberOfSwaps--; numberOfSwaps > minCutIndex; numberOfSwaps--) {
            assert transpositions.size() > 0;

            int node = transpositions.remove(transpositions.size() - 1);
            int nodesPartition = G.getPartitionIndex(node);

            if (nodesPartition == pair.lhs) {
                fromQueue[0] = lhsQueue;
                toQueue[0] = rhsQueue;
                from[0] = pair.lhs;
                to[0] = pair.rhs;
                fromPartWeight[0] = lhsPartWeight;
                toPartWeight[0] = rhsPartWeight;
            } else {
                fromQueue[0] = rhsQueue;
                toQueue[0] = lhsQueue;
                from[0] = pair.rhs;
                to[0] = pair.lhs;
                fromPartWeight[0] = rhsPartWeight;
                toPartWeight[0] = lhsPartWeight;
            }

            boundary.setBlockNoNodes(from[0], boundary.getBlockNoNodes(from[0]) - 1);
            boundary.setBlockNoNodes(to[0], boundary.getBlockNoNodes(to[0]) + 1);

            moveNodeBack(config, G, node, movedIdx,
                    fromQueue[0], toQueue[0],
                    from[0], to[0],
                    pair,
                    fromPartWeight,
                    toPartWeight,
                    boundary);
        }

        // clean up
        cut = bestCut;

        boundary.setEdgeCut(pair, bestCut);
        boundary.setBlockWeight(pair.lhs, lhsPartWeight);
        boundary.setBlockWeight(pair.rhs, rhsPartWeight);

        lhsQueue = null;
        rhsQueue = null;
        topgainQueueSelect = null;
        diffusionQueueSelect = null;
        diffusionQueueSelectBlockTarget = null;
        stRule = null;
        acceptPartition = null;

        assert cut == qm.edgeCut(G, pair.lhs, pair.rhs);
        assert assertDirectedBoundaryCondition(G, boundary, pair.lhs, pair.rhs);
        assert initialCut - bestCut >= 0 || config.isRebalance();
        // the computed partition shouldn't have an edge cut which is worse than the initial one
        return initialCut - bestCut;
    }

    private void moveNode(PartitionConfig config,
                          GraphAccess G,
                          int node,
                          VertexMovedHashtable movedIdx,
                          PriorityQueueInterface fromQueue,
                          PriorityQueueInterface toQueue,
                          int from,
                          int to,
                          BoundaryLookup.BoundaryPair pair,
                          int[] fromPartWeight,
                          int[] toPartWeight,
                          CompleteBoundary boundary) {

        // move node
        G.setPartitionIndex(node, to);
        boundary.deleteNode(node, from, pair);

        int[] intDegreeNode = new int[]{0};
        int[] extDegreeNode = new int[]{0};
        boolean difficultUpdate = intExtDegree(G, node, to, from, intDegreeNode, extDegreeNode);

        if (extDegreeNode[0] > 0) {
            boundary.insert(node, to, pair);
        }

        if (difficultUpdate) {
            boundary.postMovedBoundaryNodeUpdates(node, pair, true, false);
        }

        int thisNodeWeight = G.getNodeWeight(node);
        fromPartWeight[0] -= thisNodeWeight;
        toPartWeight[0] += thisNodeWeight;

        // update neighbors
        for (int e = G.getFirstEdge(node), end = G.getFirstInvalidEdge(node); e < end; e++) {
            int target = G.getEdgeTarget(e);
            int targetPartition = G.getPartitionIndex(target);

            if (targetPartition != from && targetPartition != to) {
                continue;
            }

            int[] intDegree = new int[]{0};
            int[] extDegree = new int[]{0};

            int otherPartition = targetPartition == from ? to : from;
            intExtDegree(G, target, targetPartition, otherPartition, intDegree, extDegree);

            PriorityQueueInterface queueToUpdate = targetPartition == from ? fromQueue : toQueue;

            int gain = extDegree[0] - intDegree[0];
            boolean found = queueToUpdate.contains(target);
            if (found) {
                if (extDegree[0] == 0) {
                    queueToUpdate.deleteNode(target);
                    boundary.deleteNode(target, targetPartition, pair);
                } else {
                    queueToUpdate.changeKey(target, gain);
                }
            } else {
                if (extDegree[0] > 0) {
                    VertexMovedHashtable.MovedIndex movedIndex = movedIdx.get(target);
                    if (movedIndex == null || movedIndex.index == VertexMovedHashtable.NOT_MOVED) {
                        queueToUpdate.insert(target, gain);
                        movedIdx.put(target, VertexMovedHashtable.MOVED);
                    }
                    boundary.insert(target, targetPartition, pair);
                } else {
                    boundary.deleteNode(target, targetPartition, pair);
                }
            }
        }
    }

    private void moveNodeBack(PartitionConfig config,
                              GraphAccess G,
                              int node,
                              VertexMovedHashtable movedIdx,
                              PriorityQueueInterface fromQueue,
                              PriorityQueueInterface toQueue,
                              int from,
                              int to,
                              BoundaryLookup.BoundaryPair pair,
                              int[] fromPartWeight,
                              int[] toPartWeight,
                              CompleteBoundary boundary) {

        assert from != to;
        assert from == G.getPartitionIndex(node);

        // move node
        G.setPartitionIndex(node, to);
        boundary.deleteNode(node, from, pair);

        int[] intDegreeNode = new int[]{0};
        int[] extDegreeNode = new int[]{0};
        boolean updateDifficult = intExtDegree(G, node, to, from, intDegreeNode, extDegreeNode);

        if (extDegreeNode[0] > 0) {
            boundary.insert(node, to, pair);
        }

        if (updateDifficult) {
            boundary.postMovedBoundaryNodeUpdates(node, pair, true, false);
        }

        int thisNodeWeight = G.getNodeWeight(node);
        fromPartWeight[0] -= thisNodeWeight;
        toPartWeight[0] += thisNodeWeight;

        // update neighbors
        for (int e = G.getFirstEdge(node), end = G.getFirstInvalidEdge(node); e < end; e++) {
            int target = G.getEdgeTarget(e);
            int targetPartition = G.getPartitionIndex(target);

            if (targetPartition != from && targetPartition != to) {
                continue;
            }

            int[] intDegree = new int[]{0};
            int[] extDegree = new int[]{0};

            int otherPartition = targetPartition == from ? to : from;
            intExtDegree(G, target, targetPartition, otherPartition, intDegree, extDegree);

            if (boundary.contains(target, targetPartition, pair)) {
                if (extDegree[0] == 0) {
                    boundary.deleteNode(target, targetPartition, pair);
                }
            } else {
                if (extDegree[0] > 0) {
                    boundary.insert(target, targetPartition, pair);
                }
            }
        }
    }

    private void initQueueWithBoundary(PartitionConfig config,
                                       GraphAccess G,
                                       List<Integer> bndNodes,
                                       PriorityQueueInterface queue,
                                       int partitionOfBoundary,
                                       int other) {

        if (config.getPermutationDuringRefinement() == PermutationQuality.PERMUTATION_QUALITY_FAST) {
            RandomFunctions.permutateVectorFast(bndNodes, false);
        } else if (config.getPermutationDuringRefinement() == PermutationQuality.PERMUTATION_QUALITY_GOOD) {
            RandomFunctions.permutateVectorGood(bndNodes, false);
        }

        for (int curBndNode : bndNodes) {
            int[] intDegree = new int[]{0};
            int[] extDegree = new int[]{0};

            intExtDegree(G, curBndNode, partitionOfBoundary, other, intDegree, extDegree);

            int gain = extDegree[0] - intDegree[0];
            queue.insert(curBndNode, gain);
            assert extDegree[0] > 0;
            assert partitionOfBoundary == G.getPartitionIndex(curBndNode);
        }
    }

    public boolean intExtDegree(GraphAccess G,
                                int node,
                                int lhs,
                                int rhs,
                                int[] intDegree,
                                int[] extDegree) {

        assert lhs == G.getPartitionIndex(node);

        intDegree[0] = 0;
        extDegree[0] = 0;
        boolean updateIsDifficult = false;

        for (int e = G.getFirstEdge(node), end = G.getFirstInvalidEdge(node); e < end; e++) {
            int target = G.getEdgeTarget(e);
            int targetsPartition = G.getPartitionIndex(target);

            if (targetsPartition == lhs) {
                intDegree[0] += G.getEdgeWeight(e);
            } else if (targetsPartition == rhs) {
                extDegree[0] += G.getEdgeWeight(e);
            }

            if (targetsPartition != lhs && targetsPartition != rhs) {
                updateIsDifficult = true;
            }
        }

        return updateIsDifficult;
    }

    private boolean assertDirectedBoundaryCondition(GraphAccess G, CompleteBoundary boundary,
                                                    int lhs, int rhs) {
        return assertOnlyBoundaryNodes(G, boundary.getDirectedBoundary(lhs, lhs, rhs), lhs, rhs)
                && assertOnlyBoundaryNodes(G, boundary.getDirectedBoundary(rhs, lhs, rhs), rhs, lhs)
                && assertEveryBoundaryNode(G, boundary.getDirectedBoundary(lhs, lhs, rhs), lhs, rhs)
                && assertEveryBoundaryNode(G, boundary.getDirectedBoundary(rhs, lhs, rhs), rhs, lhs);
    }

    private boolean assertOnlyBoundaryNodes(GraphAccess G, PartialBoundary lhsBoundary, int lhs, int rhs) {
        lhsBoundary.forAllBoundaryNodes(curBndNode -> {
            int[] intDegree = {0};
            int[] extDegree = {0};

            intExtDegree(G, curBndNode, lhs, rhs, intDegree, extDegree);

            assert G.getPartitionIndex(curBndNode) == lhs;
            assert extDegree[0] > 0;
        });
        return true;
    }

    private boolean assertEveryBoundaryNode(GraphAccess G, PartialBoundary lhsBoundary, int lhs, int rhs) {
        for (int n = 0; n < G.numberOfNodes(); n++) {
            int[] intDegree = {0};
            int[] extDegree = {0};
            if (G.getPartitionIndex(n) == lhs) {
                intExtDegree(G, n, lhs, rhs, intDegree, extDegree);

                if (extDegree[0] > 0) {
                    assert lhsBoundary.contains(n);
                }
            }
        }
        return true;
    }
}

