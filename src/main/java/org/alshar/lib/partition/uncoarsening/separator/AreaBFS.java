package org.alshar.lib.partition.uncoarsening.separator;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.tools.RandomFunctions;

import java.util.*;

public class AreaBFS {

    private static List<Integer> depth;
    private static int round = 0;

    public AreaBFS() {
        depth = new ArrayList<>();
    }

    public void performBFS(PartitionConfig config, GraphAccess G, List<Integer> inputSeparator, int block,
                           List<Integer> blockWeights, List<Integer> reachedNodes) {

        // For correctness, in practice will almost never be called
        if (round == Integer.MAX_VALUE) {
            round = 0;
            Collections.fill(depth, 0);
        }

        round++;
        Queue<Integer> nodeQueue = new LinkedList<>();

        RandomFunctions.permutateVectorGood(inputSeparator, false);

        /***************************
         * Initialize the Queue
         ***************************/
        for (int node : inputSeparator) {
            nodeQueue.offer(node);
            depth.set(node, round);
        }

        int sizeLHS = blockWeights.get(0);
        int sizeRHS = blockWeights.get(1);
        int sizeSep = blockWeights.get(2);

        int accumulatedWeight = 0;
        int upperBoundNoNodes;

        if (block == 0) {
            upperBoundNoNodes = Math.max((int) (config.getRegionFactorNodeSeparators() * config.getUpperBoundPartition() - sizeRHS - sizeSep), 0);
        } else {
            upperBoundNoNodes = Math.max((int) (config.getRegionFactorNodeSeparators() * config.getUpperBoundPartition() - sizeLHS - sizeSep), 0);
        }
        upperBoundNoNodes = Math.min(upperBoundNoNodes, blockWeights.get(block) - 1);

        /***************************
         * Do the BFS
         ***************************/
        while (!nodeQueue.isEmpty()) {
            if (accumulatedWeight >= upperBoundNoNodes) break;
            int n = nodeQueue.poll();

            for (int e = G.getFirstEdge(n); e < G.getFirstInvalidEdge(n); e++) {
                int target = G.getEdgeTarget(e);
                if (depth.get(target) != round && G.getPartitionIndex(target) == block &&
                        accumulatedWeight + G.getNodeWeight(target) <= upperBoundNoNodes) {
                    depth.set(target, round);
                    nodeQueue.offer(target);
                    reachedNodes.add(target);
                    accumulatedWeight += G.getNodeWeight(target);
                }
            }
        }
    }
}

