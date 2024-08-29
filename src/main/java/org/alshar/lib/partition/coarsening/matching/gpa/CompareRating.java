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
        double leftRating = G.getEdgeRating(left);
        double rightRating = G.getEdgeRating(right);

        if (leftRating > rightRating) {
            return -1;  // left should come before right (higher rating first)
        } else if (leftRating < rightRating) {
            return 1;  // right should come before left (lower rating first)
        } else {
            return 0;  // Ratings are equal, no tie-breaking
        }
    }
}