package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.two_way_fm_refinement;

public abstract class StopRule {
    public StopRule() {}

    public abstract boolean searchShouldStop(int minCutIdx, int curIdx, int searchLimit);
}