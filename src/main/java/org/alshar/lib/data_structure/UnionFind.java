package org.alshar.lib.data_structure;
import java.util.ArrayList;
import java.util.List;

public class UnionFind {
    private List<Integer> parent;
    private List<Integer> rank;
    private int numSets;

    // Constructor to initialize Union-Find with n elements
    public UnionFind(int n) {
        parent = new ArrayList<>(n);
        rank = new ArrayList<>(n);
        numSets = n;

        // Initialize each element to be its own parent and rank 0
        for (int i = 0; i < n; i++) {
            parent.add(i);
            rank.add(0);
        }
    }

    // Find the root of the set containing element with path compression
    public int find(int element) {
        if (parent.get(element) != element) {
            int root = find(parent.get(element));
            parent.set(element, root);  // path compression
            return root;
        }
        return element;
    }

    // Union two sets containing lhs and rhs
    public void union(int lhs, int rhs) {
        int setLhs = find(lhs);
        int setRhs = find(rhs);

        if (setLhs != setRhs) {
            // Union by rank
            if (rank.get(setLhs) < rank.get(setRhs)) {
                parent.set(setLhs, setRhs);
            } else {
                parent.set(setRhs, setLhs);
                if (rank.get(setLhs).equals(rank.get(setRhs))) {
                    rank.set(setLhs, rank.get(setLhs) + 1);
                }
            }
            numSets--;
        }
    }

    // Get the number of disjoint sets
    public int getNumSets() {
        return numSets;
    }
}

