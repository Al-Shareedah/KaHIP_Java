package org.alshar.lib.algorithms;

import org.alshar.lib.data_structure.GraphAccess;

import java.util.*;

public class StronglyConnectedComponents {

    private int dfsCount;
    private int compCount;

    private int[] dfsNum;
    private int[] compNum;
    private Deque<Integer> unfinished;
    private Deque<Integer> roots;
    private Deque<Map.Entry<Integer, Integer>> iterationStack;

    public StronglyConnectedComponents() {
    }

    public int strongComponents(GraphAccess G, int[] compNum) {
        int numNodes = G.numberOfNodes();
        dfsNum = new int[numNodes];
        this.compNum = new int[numNodes];
        unfinished = new ArrayDeque<>();
        roots = new ArrayDeque<>();
        iterationStack = new ArrayDeque<>();

        dfsCount = 0;
        compCount = 0;

        Arrays.fill(this.compNum, -1);
        Arrays.fill(dfsNum, -1);

        for (int node = 0; node < numNodes; node++) {
            if (dfsNum[node] == -1) {
                explicitSccDfs(node, G);
            }
        }

        System.arraycopy(this.compNum, 0, compNum, 0, numNodes);
        return compCount;
    }

    private void explicitSccDfs(int node, GraphAccess G) {
        iterationStack.push(new AbstractMap.SimpleEntry<>(node, G.getFirstEdge(node)));

        dfsNum[node] = dfsCount++;
        unfinished.push(node);
        roots.push(node);

        while (!iterationStack.isEmpty()) {
            Map.Entry<Integer, Integer> top = iterationStack.pop();
            int currentNode = top.getKey();
            int currentEdge = top.getValue();

            for (int e = currentEdge; e < G.getFirstInvalidEdge(currentNode); e++) {
                int target = G.getEdgeTarget(e);

                if (dfsNum[target] == -1) {
                    iterationStack.push(new AbstractMap.SimpleEntry<>(currentNode, e));
                    iterationStack.push(new AbstractMap.SimpleEntry<>(target, G.getFirstEdge(target)));

                    dfsNum[target] = dfsCount++;
                    unfinished.push(target);
                    roots.push(target);
                    break;
                } else if (compNum[target] == -1) {
                    while (dfsNum[roots.peek()] > dfsNum[target]) roots.pop();
                }
            }

            if (currentNode == roots.peek()) {
                int w;
                do {
                    w = unfinished.pop();
                    compNum[w] = compCount;
                } while (w != currentNode);
                compCount++;
                roots.pop();
            }
        }
    }
}
