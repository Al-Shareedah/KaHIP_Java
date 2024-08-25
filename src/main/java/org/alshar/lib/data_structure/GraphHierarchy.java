package org.alshar.lib.data_structure;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GraphHierarchy {
    private Stack<GraphAccess> graphHierarchyStack;
    private Stack<List<Integer>> mappingsStack;  // CoarseMapping
    private List<List<Integer>> mappingsToDelete;  // CoarseMapping
    private List<GraphAccess> hierarchiesToDelete;
    private GraphAccess currentCoarserGraph;
    private GraphAccess coarsestGraph;
    private List<Integer> currentCoarseMapping;  // CoarseMapping

    public GraphHierarchy() {
        this.graphHierarchyStack = new Stack<>();
        this.mappingsStack = new Stack<>();
        this.mappingsToDelete = new ArrayList<>();
        this.hierarchiesToDelete = new ArrayList<>();
        this.currentCoarserGraph = null;
        this.currentCoarseMapping = null;
    }

    public void pushBack(GraphAccess G, List<Integer> coarseMapping) {  // CoarseMapping
        this.graphHierarchyStack.push(G);
        this.mappingsStack.push(coarseMapping);
        this.mappingsToDelete.add(coarseMapping);
        this.coarsestGraph = G;
    }

    public GraphAccess popFinerAndProject() {
        GraphAccess finer = popCoarsest();

        List<Integer> coarseMapping = mappingsStack.pop();  // CoarseMapping

        if (finer == coarsestGraph) {
            currentCoarserGraph = finer;
            finer = popCoarsest();
            finer.setPartitionCount(currentCoarserGraph.getPartitionCount());

            coarseMapping = mappingsStack.pop();  // CoarseMapping
        }

        assert graphHierarchyStack.size() == mappingsStack.size();

        // Perform projection
        for (int n = 0; n < finer.numberOfNodes(); n++) {
            int coarserNode = coarseMapping.get(n);
            int coarserPartitionID = currentCoarserGraph.getPartitionIndex(coarserNode);
            finer.setPartitionIndex(n, coarserPartitionID);
        }

        currentCoarseMapping = coarseMapping;
        finer.setPartitionCount(currentCoarserGraph.getPartitionCount());
        currentCoarserGraph = finer;

        return finer;
    }

    public List<Integer> getMappingOfCurrentFiner() {  // CoarseMapping
        return currentCoarseMapping;
    }

    public GraphAccess getCoarsest() {
        return coarsestGraph;
    }

    private GraphAccess popCoarsest() {
        return graphHierarchyStack.pop();
    }

    public boolean isEmpty() {
        assert graphHierarchyStack.size() == mappingsStack.size();
        return graphHierarchyStack.isEmpty();
    }

    public int size() {
        return graphHierarchyStack.size();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            for (List<Integer> mapping : mappingsToDelete) {  // CoarseMapping
                if (mapping != null) {
                    mapping = null;
                }
            }
            for (int i = 0; i + 1 < hierarchiesToDelete.size(); i++) {
                if (hierarchiesToDelete.get(i) != null) {
                    hierarchiesToDelete.set(i, null);
                }
            }
        } finally {
            super.finalize();
        }
    }
}



