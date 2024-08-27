package org.alshar.lib.data_structure;

import java.util.ArrayList;
import java.util.List;

public class BasicGraph {
    public List<Node> nodes = new ArrayList<>();
    public List<Edge> edges = new ArrayList<>();
    public List<RefinementNode> refinementNodeProps = new ArrayList<>();
    public List<CoarseningEdge> coarseningEdgeProps = new ArrayList<>();
    private List<Integer> contractionOffset = new ArrayList<>();
    private boolean buildingGraph = false;
    private int lastSource = -1;
    private int node = 0;
    private int e = 0;


    public int numberOfEdges() {
        return edges.size();
    }

    public int numberOfNodes() {
        return nodes.size() - 1;
    }

    public int getFirstEdge(int node) {
        return nodes.get(node).firstEdge;
    }

    public int getFirstInvalidEdge(int node) {
        return nodes.get(node + 1).firstEdge;
    }

    public void startConstruction(int n, int m) {
        buildingGraph = true;
        node = 0;
        e = 0;
        lastSource = -1;

        nodes.clear();
        refinementNodeProps.clear();
        edges.clear();
        coarseningEdgeProps.clear();
        contractionOffset.clear();

        for (int i = 0; i <= n; i++) {
            nodes.add(new Node());
            refinementNodeProps.add(new RefinementNode());
            contractionOffset.add(0);
        }
        for (int i = 0; i < m; i++) {
            edges.add(new Edge());
            coarseningEdgeProps.add(new CoarseningEdge());
        }

        nodes.get(node).firstEdge = e;
    }

    public int newEdge(int source, int target) {
        if (!buildingGraph) throw new IllegalStateException("Graph is not in building mode");
        if (e >= edges.size()) throw new IllegalStateException("Too many edges");

        edges.get(e).target = target;
        int eBar = e++;
        nodes.get(source + 1).firstEdge = e;

        if (lastSource + 1 < source) {
            for (int i = source; i > lastSource + 1; i--) {
                nodes.get(i).firstEdge = nodes.get(lastSource + 1).firstEdge;
            }
        }
        lastSource = source;
        return eBar;
    }

    public int newNode() {
        if (!buildingGraph) throw new IllegalStateException("Graph is not in building mode");
        return node++;
    }

    public void finishConstruction() {
        // Resize the lists to ensure they have exactly `node + 1` elements
        ensureSize(nodes, node + 1, new Node());
        ensureSize(refinementNodeProps, node + 1, new RefinementNode());
        ensureSize(contractionOffset, node + 1, 0);

        // Trim the edges and coarseningEdgeProps lists to size `e`
        edges = edges.subList(0, e);
        coarseningEdgeProps = coarseningEdgeProps.subList(0, e);

        buildingGraph = false;

        // Handle isolated nodes, as in the C++ code
        if (lastSource != node - 1) {
            for (int i = node; i > lastSource + 1; i--) {
                nodes.get(i).firstEdge = nodes.get(lastSource + 1).firstEdge;
            }
        }
    }

    // Utility method to ensure list size and initialize new elements if needed
    private <T> void ensureSize(List<T> list, int size, T defaultValue) {
        while (list.size() < size) {
            list.add(defaultValue);
        }
        while (list.size() > size) {
            list.remove(list.size() - 1);
        }
    }
    public void clear() {
        nodes.clear();
        edges.clear();
        refinementNodeProps.clear();
        coarseningEdgeProps.clear();
        contractionOffset.clear();
        buildingGraph = false;
        lastSource = -1;
        node = 0;
        e = 0;
    }

}
