package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.kway_graph_refinement;

import java.util.HashMap;
import java.util.Map;

public class VertexMovedHashtable {

    public static final int NOT_MOVED = -1;
    public static final MovedIndex MOVED = new MovedIndex(); // Represents the MOVED state

    static {
        MOVED.index = 1; // Assuming '1' represents MOVED; you can change this value as needed
    }

    public static class MovedIndex {
        public int index;

        public MovedIndex() {
            this.index = NOT_MOVED;
        }
    }

    public static class CompareNodes {
        public boolean compare(int lhs, int rhs) {
            return lhs == rhs;
        }
    }

    public static class HashNodes {
        public int hash(int idx) {
            return idx;
        }
    }

    // The Java equivalent of std::unordered_map<NodeID, moved_index, hash_nodes, compare_nodes>
    private final Map<Integer, MovedIndex> internalMap;

    public VertexMovedHashtable() {
        this.internalMap = new HashMap<>();
    }

    public MovedIndex get(int nodeID) {
        return internalMap.get(nodeID);
    }

    public void put(int nodeID, MovedIndex movedIndex) {
        internalMap.put(nodeID, movedIndex);
    }

    public boolean containsKey(int nodeID) {
        return internalMap.containsKey(nodeID);
    }

    public MovedIndex find(int nodeID) {
        return internalMap.getOrDefault(nodeID, new MovedIndex());
    }

    public int size() {
        return internalMap.size();
    }

    public void clear() {
        internalMap.clear();
    }
}
