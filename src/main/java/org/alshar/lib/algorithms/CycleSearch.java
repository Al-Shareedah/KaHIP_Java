package org.alshar.lib.algorithms;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.tools.RandomFunctions;
import org.alshar.lib.tools.Timer;

import java.util.*;

public class CycleSearch {
    public static double totalTime = 0;

    public CycleSearch() {
    }

    public void findRandomCycle(GraphAccess G, List<Integer> cycle) {
        // First, perform a BFS starting from a random node and build the parent array
        Deque<Integer> bfsQueue = new ArrayDeque<>();
        int v = RandomFunctions.nextInt(0, G.numberOfNodes() - 1);
        bfsQueue.add(v);

        boolean[] touched = new boolean[G.numberOfNodes()];
        int[] parent = new int[G.numberOfNodes()];
        Arrays.fill(parent, -1);
        List<Integer> leafes = new ArrayList<>();
        touched[v] = true;
        parent[v] = v;

        while (!bfsQueue.isEmpty()) {
            int source = bfsQueue.poll();

            boolean isLeaf = true;
            for (int e = G.getFirstEdge(source); e < G.getFirstInvalidEdge(source); e++) {
                int target = G.getEdgeTarget(e);
                if (!touched[target]) {
                    isLeaf = false;
                    touched[target] = true;
                    parent[target] = source;
                    bfsQueue.add(target);
                }
            }

            if (isLeaf) {
                leafes.add(source);
            }
        }

        int[] sources = new int[G.numberOfEdges()];
        int[] targets = new int[G.numberOfEdges()];
        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                int target = G.getEdgeTarget(e);
                sources[e] = node;
                targets[e] = target;
            }
        }

        // Now find two random leaves
        int v1 = 0, v2 = 0;
        int rIdx = RandomFunctions.nextInt(0, G.numberOfEdges() - 1);
        while (true) {
            int source = sources[rIdx];
            int target = targets[rIdx];
            if (parent[source] != target && parent[target] != source) {
                // Found a non-tree edge
                v1 = source;
                v2 = target;
                break;
            }

            rIdx = RandomFunctions.nextInt(0, G.numberOfEdges() - 1);
        }

        // Climb up the parent array stepwise left and right
        List<Integer> lhsPath = new ArrayList<>();
        List<Integer> rhsPath = new ArrayList<>();
        lhsPath.add(v1);
        rhsPath.add(v2);

        boolean[] touchedNodes = new boolean[G.numberOfNodes()];
        int[] index = new int[G.numberOfNodes()];
        index[v1] = 0;
        index[v2] = 0;
        touchedNodes[v1] = true;
        touchedNodes[v2] = true;

        int curLhs = v1, curRhs = v2;
        int counter = 0;
        boolean breakLhs = false;
        while (true) {
            counter++;
            if (curLhs != parent[curLhs]) {
                if (touchedNodes[parent[curLhs]]) {
                    breakLhs = true;
                    lhsPath.add(parent[curLhs]);
                    break;
                } else {
                    curLhs = parent[curLhs];
                    touchedNodes[curLhs] = true;
                    lhsPath.add(curLhs);
                    index[curLhs] = counter;
                }
            }
            if (curRhs != parent[curRhs]) {
                if (touchedNodes[parent[curRhs]]) {
                    rhsPath.add(parent[curRhs]);
                    break;
                } else {
                    curRhs = parent[curRhs];
                    touchedNodes[curRhs] = true;
                    rhsPath.add(curRhs);
                    index[curRhs] = counter;
                }
            }
        }

        if (breakLhs) {
            cycle.addAll(lhsPath);

            int connectingVertex = cycle.get(cycle.size() - 1);
            for (int i = index[connectingVertex] - 1; i >= 0; i--) {
                cycle.add(rhsPath.get(i));
            }
        } else {
            cycle.addAll(rhsPath);

            int connectingVertex = cycle.get(cycle.size() - 1);
            for (int i = index[connectingVertex] - 1; i >= 0; i--) {
                cycle.add(lhsPath.get(i));
            }
        }

        cycle.add(cycle.get(0));
    }

    public boolean findShortestPath(GraphAccess G, int start, int dest, List<Integer> cycle) {
        List<Integer> parent = new ArrayList<>(Collections.nCopies(G.numberOfNodes(), Integer.MAX_VALUE));
        List<Integer> distance = new ArrayList<>(Collections.nCopies(G.numberOfNodes(), Integer.MAX_VALUE / 2));

        boolean negativeCycleDetected = negativeCycleDetection(G, start, distance, parent, cycle);

        if (!negativeCycleDetected) {
            cycle.clear();
            cycle.add(dest);
            int cur = dest;
            while (cur != start) {
                cur = parent.get(cur);
                cycle.add(cur);
            }
            Collections.reverse(cycle);
        }
        return negativeCycleDetected;
    }

    public boolean findNegativeCycle(GraphAccess G, int start, List<Integer> cycle) {
        List<Integer> parent = new ArrayList<>(Collections.nCopies(G.numberOfNodes(), Integer.MAX_VALUE));
        List<Integer> distance = new ArrayList<>(Collections.nCopies(G.numberOfNodes(), Integer.MAX_VALUE / 2));

        return negativeCycleDetection(G, start, distance, parent, cycle);
    }

    public boolean findZeroWeightCycle(GraphAccess G, int start, List<Integer> cycle) {
        List<Integer> distance = new ArrayList<>(Collections.nCopies(G.numberOfNodes(), Integer.MAX_VALUE / 2));
        List<Integer> parent = new ArrayList<>(Collections.nCopies(G.numberOfNodes(), Integer.MAX_VALUE));

        boolean negativeWeightCycle = negativeCycleDetection(G, start, distance, parent, cycle);
        if (!negativeWeightCycle) {
            // Now we try to return a random directed zero weight gain cycle
            GraphAccess W = new GraphAccess();
            W.startConstruction(G.numberOfNodes(), G.numberOfEdges());

            for (int node = 0; node < G.numberOfNodes(); node++) {
                int shadowNode = W.newNode();
                W.setNodeWeight(shadowNode, G.getNodeWeight(node));
                for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                    int target = G.getEdgeTarget(e);
                    int modifiedEdgeWeight = G.getEdgeWeight(e) + distance.get(node) - distance.get(target);
                    if (modifiedEdgeWeight == 0) {
                        int shadowEdge = W.newEdge(shadowNode, target);
                        W.setEdgeWeight(shadowEdge, 0);
                    }
                }
            }
            W.finishConstruction();

            StronglyConnectedComponents scc = new StronglyConnectedComponents();
            int[] compNum = new int[W.numberOfNodes()];
            scc.strongComponents(W, compNum);

            // First, check whether there are components with more than one vertex
            int[] compCount = new int[W.numberOfNodes()];
            for (int node = 0; node < W.numberOfNodes(); node++) {
                compCount[compNum[node]]++;
            }

            List<Integer> candidates = new ArrayList<>();
            for (int node = 0; node < W.numberOfNodes(); node++) {
                if (compCount[compNum[node]] > 1) {
                    candidates.add(node);
                }
            }

            if (candidates.isEmpty()) {
                return false;
            }

            // Now pick a random start vertex
            int startVertexIdx = RandomFunctions.nextInt(0, candidates.size() - 1);
            int startVertex = candidates.get(startVertexIdx);
            boolean[] seen = new boolean[W.numberOfNodes()];
            List<Integer> list = new ArrayList<>();

            int successor = startVertex;
            int compOfSv = compNum[startVertex];
            do {
                seen[successor] = true;
                list.add(successor);

                List<Integer> sameCompNeighbors = new ArrayList<>();
                for (int e = W.getFirstEdge(successor); e < W.getFirstInvalidEdge(successor); e++) {
                    int target = W.getEdgeTarget(e);
                    if (compNum[target] == compOfSv) {
                        sameCompNeighbors.add(target);
                    }
                }

                int succId = RandomFunctions.nextInt(0, sameCompNeighbors.size() - 1);
                successor = sameCompNeighbors.get(succId);
            } while (!seen[successor]);

            int startIdx = 0;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) == successor) {
                    startIdx = i;
                    break;
                }
            }

            for (int i = startIdx; i < list.size(); i++) {
                cycle.add(list.get(i));
            }
            cycle.add(successor);

            return true;
        }
        return false;
    }

    private boolean negativeCycleDetection(GraphAccess G, int start, List<Integer> distance, List<Integer> parent, List<Integer> cycle) {
        Timer timer = new Timer();

        int w = bellmanFordWithSubtreeDisassemblyAndUpdates(G, start, distance, parent, cycle);

        if (w >= 0) { // found a cycle
            int t = parent.get(w);
            int u = t;

            boolean[] seen = new boolean[G.numberOfNodes()];
            seen[u] = true;
            int predecessor = parent.get(u);
            int startVertex;

            while (true) {
                if (seen[predecessor]) {
                    startVertex = predecessor;
                    break;
                }
                seen[predecessor] = true;
                predecessor = parent.get(predecessor);
            }

            cycle.add(startVertex);
            predecessor = parent.get(startVertex);
            while (predecessor != startVertex) {
                cycle.add(predecessor);
                predecessor = parent.get(predecessor);
            }
            cycle.add(startVertex);
            Collections.reverse(cycle);

            totalTime += timer.elapsed();
            return true;
        }

        totalTime += timer.elapsed();
        return false;
    }

    private int bellmanFordWithSubtreeDisassemblyAndUpdates(GraphAccess G, int start, List<Integer> distance, List<Integer> parent, List<Integer> cycle) {
        // Goldberg spc-1.2 similar implementation using our data structures
        final int NULL_NODE = -1;
        distance.set(start, 0);
        Queue<Integer> L = new LinkedList<>();

        // Doubly linked list of shortest path tree in preorder
        final short OUT_OF_QUEUE = 0;
        final short INACTIVE = 1;
        final short ACTIVE = 2;
        final short IN_QUEUE = 2;

        int[] before = new int[G.numberOfNodes()];
        int[] after = new int[G.numberOfNodes()];
        int[] degree = new int[G.numberOfNodes()];
        short[] status = new short[G.numberOfNodes()];
        Arrays.fill(before, NULL_NODE);

        L.add(start);

        after[start] = start;
        before[start] = start;
        degree[start] = -1;
        status[start] = IN_QUEUE;

        while (!L.isEmpty()) {
            int v = L.poll();

            short currentStatus = status[v];
            status[v] = OUT_OF_QUEUE;

            if (currentStatus == INACTIVE) continue;

            for (int e = G.getFirstEdge(v); e < G.getFirstInvalidEdge(v); e++) {
                int w = G.getEdgeTarget(e);
                int delta = distance.get(w) - distance.get(v) - G.getEdgeWeight(e);
                if (delta > 0) {
                    // Disassemble subtree
                    int newDistance = distance.get(w) - delta;

                    int x = before[w];
                    int y = w;
                    if (x != NULL_NODE) {
                        // w is already in the tree, remove it/disassemble the subtree
                        for (int totalDegree = 0; totalDegree >= 0; y = after[y]) {
                            if (y == v) {
                                parent.set(w, v);
                                return w; // since parent[w] = v
                            } else {
                                distance.set(y, distance.get(y) - delta);
                                before[y] = NULL_NODE;
                                totalDegree += degree[y];

                                if (status[y] == ACTIVE) {
                                    status[y] = INACTIVE;
                                }
                            }
                        }

                        degree[parent.get(w)]--;

                        after[x] = after[y];
                        before[after[y]] = x;
                    }
                    distance.set(w, newDistance);
                    parent.set(w, v);
                }

                if (before[w] == NULL_NODE && parent.get(w) == v) {
                    degree[v]++;
                    degree[w] = -1;

                    int afterV = after[v];

                    after[v] = w;
                    before[w] = v;
                    after[w] = afterV;
                    before[afterV] = w;

                    if (status[w] == OUT_OF_QUEUE) {
                        L.add(w);
                        status[w] = IN_QUEUE;
                    } else {
                        status[w] = ACTIVE;
                    }
                }
            }
        }
        return NULL_NODE;
    }
}

