package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.flow_refinement;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.tools.RandomFunctions;

import java.util.*;

public class BoundaryBFS {

    public BoundaryBFS() {
    }

    public boolean boundaryBFSSearch(GraphAccess G,
                                     List<Integer> startNodes,
                                     int partition,
                                     int upperBoundNoNodes,
                                     List<Integer> reachedNodes,
                                     int[] stripeWeight,
                                     boolean flowTiebreaking) {

        Queue<Integer> nodeQueue = new LinkedList<>();
        int[] depth = new int[G.numberOfNodes()];
        Arrays.fill(depth, -1);
        int curDepth = 0;

        if (flowTiebreaking) {
            RandomFunctions.permutateVectorGood(startNodes, false);
        }

        // Initialize the Queue
        int accumulatedWeight = 0;
        for (int i = 0; i < startNodes.size(); i++) {
            int node = startNodes.get(i);
            nodeQueue.add(node);
            assert G.getPartitionIndex(node) == partition;
            depth[node] = curDepth;
            reachedNodes.add(node);
            accumulatedWeight += G.getNodeWeight(node);
        }
        curDepth++;

        if (accumulatedWeight >= upperBoundNoNodes) {
            stripeWeight[0] = accumulatedWeight;
            return false;
        }

        // Do the BFS
        while (!nodeQueue.isEmpty()) {
            if (accumulatedWeight >= upperBoundNoNodes) break;
            int n = nodeQueue.poll();

            if (depth[n] == curDepth) {
                curDepth++;
            }
            for (int e = G.getFirstEdge(n); e < G.getFirstInvalidEdge(n); e++) {
                int t = G.getEdgeTarget(e);
                if (depth[t] == -1 && G.getPartitionIndex(t) == partition
                        && accumulatedWeight + G.getNodeWeight(t) <= upperBoundNoNodes) {
                    depth[t] = curDepth;
                    nodeQueue.add(t);
                    reachedNodes.add(t);
                    accumulatedWeight += G.getNodeWeight(t);
                }
            }
        }
        boolean someToDo = stripeWeight[0] != accumulatedWeight;
        stripeWeight[0] = accumulatedWeight;
        return someToDo;
    }
}
