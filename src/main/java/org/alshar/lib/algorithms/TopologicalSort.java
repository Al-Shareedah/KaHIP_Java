package org.alshar.lib.algorithms;
import org.alshar.lib.data_structure.GraphAccess;

import java.util.*;

public class TopologicalSort {

    public TopologicalSort() {
    }

    public void sort(GraphAccess G, List<Integer> sortedSequence) {
        int numNodes = G.numberOfNodes();
        int[] dfsNum = new int[numNodes];
        Arrays.fill(dfsNum, -1);
        int dfsCount = 0;

        List<Integer> nodes = new ArrayList<>(numNodes);
        for (int i = 0; i < numNodes; i++) {
            nodes.add(i);
        }
        Collections.shuffle(nodes);

        for (int node : nodes) {
            if (dfsNum[node] == -1) {
                sortDfs(node, G, dfsNum, dfsCount, sortedSequence);
            }
        }

        Collections.reverse(sortedSequence);
    }

    private void sortDfs(int node, GraphAccess G, int[] dfsNum, int dfsCount, List<Integer> sortedSequence) {
        dfsNum[node] = dfsCount++;

        for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
            int target = G.getEdgeTarget(e);
            if (dfsNum[target] == -1) {
                sortDfs(target, G, dfsNum, dfsCount, sortedSequence);
            }
        }

        sortedSequence.add(node);
    }
}
