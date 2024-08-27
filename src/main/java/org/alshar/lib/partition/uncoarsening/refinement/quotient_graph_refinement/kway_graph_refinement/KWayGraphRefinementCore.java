package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.kway_graph_refinement;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.priority_queues.BucketPQ;
import org.alshar.lib.data_structure.priority_queues.MaxNodeHeap;
import org.alshar.lib.data_structure.priority_queues.PriorityQueueInterface;
import org.alshar.lib.enums.KWayStopRule;
import org.alshar.lib.enums.PermutationQuality;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.BoundaryLookup;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import org.alshar.lib.tools.RandomFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KWayGraphRefinementCore {

    private KWayGraphRefinementCommons commons;

    public KWayGraphRefinementCore() {
        this.commons = null;
    }

    public int singleKWayRefinementRound(PartitionConfig config,
                                         GraphAccess G,
                                         CompleteBoundary boundary,
                                         List<Integer> startNodes,
                                         int stepLimit,
                                         VertexMovedHashtable movedIdx) {
        Map<Integer, Integer> touchedBlocks = new HashMap<>();
        return singleKWayRefinementRoundInternal(config, G, boundary, startNodes,
                stepLimit, movedIdx, false, touchedBlocks);
    }

    public int singleKWayRefinementRound(PartitionConfig config,
                                         GraphAccess G,
                                         CompleteBoundary boundary,
                                         List<Integer> startNodes,
                                         int stepLimit,
                                         VertexMovedHashtable movedIdx,
                                         Map<Integer, Integer> touchedBlocks) {

        return singleKWayRefinementRoundInternal(config, G, boundary, startNodes,
                stepLimit, movedIdx, true, touchedBlocks);
    }

    private int singleKWayRefinementRoundInternal(PartitionConfig config,
                                                  GraphAccess G,
                                                  CompleteBoundary boundary,
                                                  List<Integer> startNodes,
                                                  int stepLimit,
                                                  VertexMovedHashtable movedIdx,
                                                  boolean computeTouchedPartitions,
                                                  Map<Integer, Integer> touchedBlocks) {

        if (commons == null) {
            commons = new KWayGraphRefinementCommons(config);
        }

        PriorityQueueInterface queue;
        if (config.isUseBucketQueues()) {
            int maxDegree = G.getMaxDegree();
            queue = new BucketPQ(maxDegree);  // This should now work
        } else {
            queue = new MaxNodeHeap();  // This should now work
        }
        initQueueWithBoundary(config, G, startNodes, queue, movedIdx);

        if (queue.isEmpty()) {
            queue = null;
            return 0;
        }

        List<Integer> transpositions = new ArrayList<>();
        List<Integer> fromPartitions = new ArrayList<>();
        List<Integer> toPartitions = new ArrayList<>();

        int maxNumberOfSwaps = G.numberOfNodes();
        int minCutIndex = -1;

        int cut = Integer.MAX_VALUE / 2;
        int initialCut = cut;

        int bestCut = cut;
        int numberOfSwaps = 0;
        int movements = 0;

        KWaySimpleStopRule stoppingRule = null;
        switch (config.getKwayStopRule()) {
            case KWAY_SIMPLE_STOP_RULE:
                stoppingRule = new KWaySimpleStopRule(config);
                break;
            case KWAY_ADAPTIVE_STOP_RULE:
                stoppingRule = new KWaySimpleStopRule(config);
                break;
        }

        for (numberOfSwaps = 0, movements = 0; movements < maxNumberOfSwaps; movements++, numberOfSwaps++) {
            if (queue.isEmpty()) break;
            if (stoppingRule.searchShouldStop(minCutIndex, numberOfSwaps, stepLimit)) break;

            int gain = queue.maxValue();
            int node = queue.deleteMax();

            int from = G.getPartitionIndex(node);
            boolean successful = moveNode(config, G, node, movedIdx, queue, boundary);

            if (successful) {
                cut -= gain;
                stoppingRule.pushStatistics(gain);

                boolean acceptEqual = RandomFunctions.nextBool();
                if (cut < bestCut || (cut == bestCut && acceptEqual)) {
                    bestCut = cut;
                    minCutIndex = numberOfSwaps;
                    if (cut < bestCut)
                        stoppingRule.resetStatistics();
                }

                fromPartitions.add(from);
                toPartitions.add(G.getPartitionIndex(node));
                transpositions.add(node);
            } else {
                numberOfSwaps--;
            }
            movedIdx.put(node, VertexMovedHashtable.MOVED);
        }

        // Roll backwards
        for (numberOfSwaps--; numberOfSwaps > minCutIndex; numberOfSwaps--) {
            int node = transpositions.remove(transpositions.size() - 1);
            int to = fromPartitions.remove(fromPartitions.size() - 1);
            toPartitions.remove(toPartitions.size() - 1);

            moveNodeBack(config, G, node, to, movedIdx, queue, boundary);
        }

        if (computeTouchedPartitions) {
            for (int i = 0; i < fromPartitions.size(); i++) {
                touchedBlocks.put(fromPartitions.get(i), fromPartitions.get(i));
                touchedBlocks.put(toPartitions.get(i), toPartitions.get(i));
            }
        }

        queue = null;
        stoppingRule = null;
        return initialCut - bestCut;
    }

    private void initQueueWithBoundary(PartitionConfig config,
                                       GraphAccess G,
                                       List<Integer> bndNodes,
                                       PriorityQueueInterface queue, VertexMovedHashtable movedIdx) {

        if (config.getPermutationDuringRefinement() == PermutationQuality.PERMUTATION_QUALITY_FAST) {
            RandomFunctions.permutateVectorFast(bndNodes, false);
        } else if (config.getPermutationDuringRefinement() == PermutationQuality.PERMUTATION_QUALITY_GOOD) {
            RandomFunctions.permutateVectorGood(bndNodes, false);
        }

        for (int node : bndNodes) {
            if (!movedIdx.containsKey(node)) {
                int[] maxGainer = new int[1]; // Use an array to allow modification in the method
                int[] extDegree = new int[1]; // Use an array to allow modification in the method
                int gain = commons.computeGain(G, node, maxGainer, extDegree);
                queue.insert(node, gain);
                movedIdx.put(node, new VertexMovedHashtable.MovedIndex());
            }
        }
    }


    private boolean moveNode(PartitionConfig config,
                             GraphAccess G,
                             int node,
                             VertexMovedHashtable movedIdx,
                             PriorityQueueInterface queue,
                             CompleteBoundary boundary) {

        int from = G.getPartitionIndex(node);
        int[] to = new int[1];
        int[] nodeExtDeg = new int[1];
        commons.computeGain(G, node, to, nodeExtDeg);

        int thisNodesWeight = G.getNodeWeight(node);
        if (boundary.getBlockWeight(to[0]) + thisNodesWeight >= config.getUpperBoundPartition())
            return false;

        if (boundary.getBlockNoNodes(from) - 1 == 0)
            return false;

        G.setPartitionIndex(node, to[0]);

        BoundaryLookup.BoundaryPair pair = new BoundaryLookup.BoundaryPair(config.getK(), from, to[0]);

        boundary.postMovedBoundaryNodeUpdates(node, pair, true, true);

        boundary.setBlockNoNodes(from, boundary.getBlockNoNodes(from) - 1);
        boundary.setBlockNoNodes(to[0], boundary.getBlockNoNodes(to[0]) + 1);
        boundary.setBlockWeight(from, boundary.getBlockWeight(from) - thisNodesWeight);
        boundary.setBlockWeight(to[0], boundary.getBlockWeight(to[0]) + thisNodesWeight);

        // Update gain of neighbors
        for (int e = G.getFirstEdge(node), end = G.getFirstInvalidEdge(node); e < end; e++) {
            int target = G.getEdgeTarget(e);
            int[] targetsMaxGainer = new int[1];
            int[] extDegree = new int[1];
            int gain = commons.computeGain(G, target, targetsMaxGainer, extDegree);

            if (queue.contains(target)) {
                if (extDegree[0] > 0) {
                    queue.changeKey(target, gain);
                } else {
                    queue.deleteNode(target);
                }
            } else {
                if (extDegree[0] > 0 && !movedIdx.containsKey(target)) {
                    queue.insert(target, gain);
                    VertexMovedHashtable.MovedIndex movedIndex = new VertexMovedHashtable.MovedIndex();
                    movedIndex.index = VertexMovedHashtable.NOT_MOVED;
                    movedIdx.put(target, movedIndex);
                }
            }
        }

        return true;
    }

    private void moveNodeBack(PartitionConfig config,
                              GraphAccess G,
                              int node,
                              int to,
                              VertexMovedHashtable movedIdx,
                              PriorityQueueInterface queue,
                              CompleteBoundary boundary) {

        int from = G.getPartitionIndex(node);
        G.setPartitionIndex(node, to);

        BoundaryLookup.BoundaryPair pair = new BoundaryLookup.BoundaryPair(config.getK(), from, to);

        boundary.postMovedBoundaryNodeUpdates(node, pair, true, true);

        int thisNodesWeight = G.getNodeWeight(node);
        boundary.setBlockNoNodes(from, boundary.getBlockNoNodes(from) - 1);
        boundary.setBlockNoNodes(to, boundary.getBlockNoNodes(to) + 1);
        boundary.setBlockWeight(from, boundary.getBlockWeight(from) - thisNodesWeight);
        boundary.setBlockWeight(to, boundary.getBlockWeight(to) + thisNodesWeight);
    }
}