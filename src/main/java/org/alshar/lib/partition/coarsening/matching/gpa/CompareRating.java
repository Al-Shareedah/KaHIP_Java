package org.alshar.lib.partition.coarsening.matching.gpa;

import org.alshar.lib.data_structure.GraphAccess;

import java.util.Comparator;


public class CompareRating implements Comparator<Integer> {
    private final GraphAccess G;

    public CompareRating(GraphAccess G) {
        this.G = G;
    }

    @Override
    public int compare(Integer left, Integer right) {
        // Fetch the ratings associated with the edge IDs `left` and `right`
        double leftRating = G.getEdgeRating(left);
        double rightRating = G.getEdgeRating(right);
        // Note: We use descending order, which matches the original C++ logic
        return Double.compare(rightRating, leftRating);
    }
}