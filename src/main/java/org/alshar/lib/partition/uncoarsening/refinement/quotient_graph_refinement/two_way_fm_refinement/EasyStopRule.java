package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.two_way_fm_refinement;

public class EasyStopRule extends StopRule {
    public EasyStopRule() {}

    @Override
    public boolean searchShouldStop(int minCutIdx, int curIdx, int searchLimit) {
        return curIdx - minCutIdx > searchLimit;
    }
}
