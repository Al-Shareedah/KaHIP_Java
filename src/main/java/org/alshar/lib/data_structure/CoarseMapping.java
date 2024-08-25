package org.alshar.lib.data_structure;
import java.util.ArrayList;
import java.util.List;

public class CoarseMapping {
    private List<Integer> mapping;

    // Constructor
    public CoarseMapping(int size) {
        this.mapping = new ArrayList<>(size);
    }

    // Add a node ID to the mapping
    public void add(int nodeID) {
        mapping.add(nodeID);
    }

    // Get the node ID at a specific index
    public int get(int index) {
        return mapping.get(index);
    }

    // Set a node ID at a specific index
    public void set(int index, int nodeID) {
        mapping.set(index, nodeID);
    }

    // Get the size of the mapping
    public int size() {
        return mapping.size();
    }
}

