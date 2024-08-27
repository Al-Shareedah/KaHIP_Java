package org.alshar.lib.data_structure;
import java.util.ArrayList;
import java.util.List;

public class FlowGraph {
    // Structure to represent an edge in the residual graph
    static class REdge {
        int source;
        int target;
        long capacity;
        long flow;
        int reverseEdgeIndex;

        REdge(int source, int target, long capacity, long flow, int reverseEdgeIndex) {
            this.source = source;
            this.target = target;
            this.capacity = capacity;
            this.flow = flow;
            this.reverseEdgeIndex = reverseEdgeIndex;
        }
    }

    private List<List<REdge>> adjacencyLists;
    private int numNodes;
    private int numEdges;

    public FlowGraph() {
        this.numEdges = 0;
        this.numNodes = 0;
        this.adjacencyLists = new ArrayList<>();
    }

    public void startConstruction(int nodes, int edges) {
        adjacencyLists = new ArrayList<>(nodes);
        for (int i = 0; i < nodes; i++) {
            adjacencyLists.add(new ArrayList<>());
        }
        this.numNodes = nodes;
        this.numEdges = edges;
    }

    public void finishConstruction() {
        // Any finalization if needed
    }

    public int numberOfNodes() {
        return numNodes;
    }

    public int numberOfEdges() {
        return numEdges;
    }

    public int getEdgeTarget(int source, int e) {
        return adjacencyLists.get(source).get(e).target;
    }

    public long getEdgeCapacity(int source, int e) {
        return adjacencyLists.get(source).get(e).capacity;
    }

    public long getEdgeFlow(int source, int e) {
        return adjacencyLists.get(source).get(e).flow;
    }

    public void setEdgeFlow(int source, int e, long flow) {
        adjacencyLists.get(source).get(e).flow = flow;
    }

    public int getReverseEdge(int source, int e) {
        return adjacencyLists.get(source).get(e).reverseEdgeIndex;
    }

    public void newEdge(int source, int target, long capacity) {
        // Add the forward edge
        adjacencyLists.get(source).add(new REdge(source, target, capacity, 0, adjacencyLists.get(target).size()));
        // Add the reverse edge with capacity 0
        adjacencyLists.get(target).add(new REdge(target, source, 0, 0, adjacencyLists.get(source).size() - 1));
        numEdges += 2;
    }

    public int getFirstEdge(int node) {
        return 0;
    }

    public int getFirstInvalidEdge(int node) {
        return adjacencyLists.get(node).size();
    }
}
