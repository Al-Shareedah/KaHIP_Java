package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement;

import java.util.HashMap;
import java.util.Map;

public class PartialBoundary {

    // Define the nested structures similar to the C++ structs
    public static class CompareNodesContains {
        public boolean compare(int lhs, int rhs) {  // NodeID in C++ corresponds to int in Java
            return lhs == rhs;
        }
    }

    public static class IsBoundary {
        public boolean contains;

        public IsBoundary() {
            this.contains = false;
        }
    }

    public static class HashBoundaryNodes {
        public int hash(int idx) {
            return idx;
        }
    }

    // HashMap equivalent to is_boundary_node_hashtable in C++
    private Map<Integer, IsBoundary> internalBoundary;

    public PartialBoundary() {
        this.internalBoundary = new HashMap<>();
    }

    public boolean contains(int node) {
        return internalBoundary.containsKey(node);
    }

    public void insert(int node) {
        internalBoundary.put(node, new IsBoundary());
    }

    public void deleteNode(int node) {
        internalBoundary.remove(node);
    }

    public int size() {
        return internalBoundary.size();
    }

    public void clear() {
        internalBoundary.clear();
    }

    // Iterator for boundary nodes, similar to C++ forall_boundary_nodes macro
    public void forAllBoundaryNodes(BoundaryNodeConsumer consumer) {
        for (Map.Entry<Integer, IsBoundary> entry : internalBoundary.entrySet()) {
            consumer.accept(entry.getKey());
        }
    }

    // Functional interface to mimic C++ iterator behavior
    @FunctionalInterface
    public interface BoundaryNodeConsumer {
        void accept(int node);
    }
}
