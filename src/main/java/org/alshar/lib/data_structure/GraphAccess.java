package org.alshar.lib.data_structure;
import java.util.ArrayList;
import java.util.List;

class Node {
    int firstEdge;
    int weight;
}

class Edge {
    int target;
    int weight;
}

class RefinementNode {
    int partitionIndex;
}

class CoarseningEdge {
    float rating;
}

class BasicGraph {
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

}

public class GraphAccess {
    private BasicGraph graphRef;
    private boolean maxDegreeComputed;
    private int partitionCount;
    private int maxDegree;
    private int separatorBlockID;
    private List<Integer> secondPartitionIndex;

    public GraphAccess() {
        this.graphRef = new BasicGraph();
        this.maxDegreeComputed = false;
        this.maxDegree = 0;
        this.separatorBlockID = 2;
        this.secondPartitionIndex = new ArrayList<>();
    }

    public void startConstruction(int nodes, int edges) {
        graphRef.startConstruction(nodes, edges);
    }

    public int newNode() {
        return graphRef.newNode();
    }

    public int newEdge(int source, int target) {
        return graphRef.newEdge(source, target);
    }

    public void finishConstruction() {
        graphRef.finishConstruction();
    }

    public int numberOfNodes() {
        return graphRef.numberOfNodes();
    }

    public int numberOfEdges() {
        return graphRef.numberOfEdges();
    }

    public void resizeSecondPartitionIndex(int noNodes) {
        secondPartitionIndex = new ArrayList<>(noNodes);
    }

    public int getFirstEdge(int node) {
        return graphRef.getFirstEdge(node);
    }

    public int getFirstInvalidEdge(int node) {
        return graphRef.getFirstInvalidEdge(node);
    }

    public int getPartitionCount() {
        return partitionCount;
    }

    public void setPartitionCount(int count) {
        this.partitionCount = count;
    }

    public int getSeparatorBlock() {
        return separatorBlockID;
    }

    public void setSeparatorBlock(int id) {
        this.separatorBlockID = id;
    }

    public int getPartitionIndex(int node) {
        return graphRef.refinementNodeProps.get(node).partitionIndex;
    }

    public void setPartitionIndex(int node, int id) {
        graphRef.refinementNodeProps.get(node).partitionIndex = id;
    }

    public int getSecondPartitionIndex(int node) {
        return secondPartitionIndex.get(node);
    }

    public void setSecondPartitionIndex(int node, int id) {
        secondPartitionIndex.set(node, id);
    }

    public int getNodeWeight(int node) {
        return graphRef.nodes.get(node).weight;
    }

    public void setNodeWeight(int node, int weight) {
        graphRef.nodes.get(node).weight = weight;
    }

    public int getEdgeWeight(int edge) {
        return graphRef.edges.get(edge).weight;
    }

    public void setEdgeWeight(int edge, int weight) {
        graphRef.edges.get(edge).weight = weight;
    }

    public int getEdgeTarget(int edge) {
        return graphRef.edges.get(edge).target;
    }
    public List<Integer> getOutEdges(int node) {
        List<Integer> outEdges = new ArrayList<>();
        for (int e = getFirstEdge(node); e < getFirstInvalidEdge(node); e++) {
            outEdges.add(e);
        }
        return outEdges;
    }


    public float getEdgeRating(int edge) {
        return graphRef.coarseningEdgeProps.get(edge).rating;
    }

    public void setEdgeRating(int edge, float rating) {
        graphRef.coarseningEdgeProps.get(edge).rating = rating;
    }

    public int getNodeDegree(int node) {
        return graphRef.getFirstInvalidEdge(node) - graphRef.getFirstEdge(node);
    }

    public int getWeightedNodeDegree(int node) {
        int degree = 0;
        for (int e = graphRef.getFirstEdge(node); e < graphRef.getFirstInvalidEdge(node); e++) {
            degree += getEdgeWeight(e);
        }
        return degree;
    }

    public int getMaxDegree() {
        if (!maxDegreeComputed) {
            maxDegree = 0;
            for (int node = 0; node < numberOfNodes(); node++) {
                int curDegree = getWeightedNodeDegree(node);
                if (curDegree > maxDegree) {
                    maxDegree = curDegree;
                }
            }
            maxDegreeComputed = true;
        }
        return maxDegree;
    }

    public int[] unsafeMetisStyleXadjArray() {
        int[] xadj = new int[graphRef.numberOfNodes() + 1];
        for (int n = 0; n < graphRef.numberOfNodes(); n++) {
            xadj[n] = graphRef.nodes.get(n).firstEdge;
        }
        xadj[graphRef.numberOfNodes()] = graphRef.nodes.get(graphRef.numberOfNodes()).firstEdge;
        return xadj;
    }

    public int[] unsafeMetisStyleAdjncyArray() {
        int[] adjncy = new int[graphRef.numberOfEdges()];
        for (int e = 0; e < graphRef.numberOfEdges(); e++) {
            adjncy[e] = graphRef.edges.get(e).target;
        }
        return adjncy;
    }

    public int[] unsafeMetisStyleVwgtArray() {
        int[] vwgt = new int[graphRef.numberOfNodes()];
        for (int n = 0; n < graphRef.numberOfNodes(); n++) {
            vwgt[n] = graphRef.nodes.get(n).weight;
        }
        return vwgt;
    }

    public int[] unsafeMetisStyleAdjwgtArray() {
        int[] adjwgt = new int[graphRef.numberOfEdges()];
        for (int e = 0; e < graphRef.numberOfEdges(); e++) {
            adjwgt[e] = graphRef.edges.get(e).weight;
        }
        return adjwgt;
    }

    public int buildFromMetis(int n, int[] xadj, int[] adjncy) {
        graphRef = new BasicGraph();
        startConstruction(n, xadj[n]);

        for (int i = 0; i < n; i++) {
            int node = newNode();
            setNodeWeight(node, 1);
            setPartitionIndex(node, 0);

            for (int e = xadj[i]; e < xadj[i + 1]; e++) {
                int eBar = newEdge(node, adjncy[e]);
                setEdgeWeight(eBar, 1);
            }
        }

        finishConstruction();
        return 0;
    }

    public int buildFromMetisWeighted(int n, int[] xadj, int[] adjncy, int[] vwgt, int[] adjwgt) {
        graphRef = new BasicGraph();
        startConstruction(n, xadj[n]);

        for (int i = 0; i < n; i++) {
            int node = newNode();
            setNodeWeight(node, vwgt[i]);
            setPartitionIndex(node, 0);

            for (int e = xadj[i]; e < xadj[i + 1]; e++) {
                int eBar = newEdge(node, adjncy[e]);
                setEdgeWeight(eBar, adjwgt[e]);
            }
        }

        finishConstruction();
        return 0;
    }

    public void copy(GraphAccess Gcopy) {
        Gcopy.startConstruction(numberOfNodes(), numberOfEdges());

        for (int node = 0; node < numberOfNodes(); node++) {
            int shadowNode = Gcopy.newNode();
            Gcopy.setNodeWeight(shadowNode, getNodeWeight(node));
            for (int e = getFirstEdge(node); e < getFirstInvalidEdge(node); e++) {
                int target = getEdgeTarget(e);
                int shadowEdge = Gcopy.newEdge(shadowNode, target);
                Gcopy.setEdgeWeight(shadowEdge, getEdgeWeight(e));
            }
        }

        Gcopy.finishConstruction();
    }
}
