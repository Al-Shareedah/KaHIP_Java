package org.alshar.lib.data_structure;
import java.util.ArrayList;
import java.util.List;

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
    public BasicGraph getGraphRef() {
        return graphRef;
    }
    public void setGraphRef(BasicGraph graphRef) {
        this.graphRef = graphRef;
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


    public double getEdgeRating(int edge) {
        return graphRef.coarseningEdgeProps.get(edge).rating;
    }

    public void setEdgeRating(int edge, double rating) {
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
