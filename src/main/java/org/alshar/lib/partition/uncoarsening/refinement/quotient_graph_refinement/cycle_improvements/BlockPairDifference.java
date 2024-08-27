package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.cycle_improvements;

public class BlockPairDifference {
    private int lhs;
    private int rhs;
    private int weightDifference;

    public BlockPairDifference(int lhs, int rhs, int weightDifference) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.weightDifference = weightDifference;
    }

    public int getLhs() {
        return lhs;
    }

    public int getRhs() {
        return rhs;
    }

    public int getWeightDifference() {
        return weightDifference;
    }

    public void setWeightDifference(int weightDifference) {
        this.weightDifference = weightDifference;
    }
}
