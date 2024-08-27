package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.kway_graph_refinement;

import org.alshar.lib.partition.PartitionConfig;

public class KWaySimpleStopRule extends KWayStopRule {
    public KWaySimpleStopRule(PartitionConfig config) {
        super(config);
    }

    @Override
    public void pushStatistics(int gain) {
    }

    @Override
    public void resetStatistics() {
    }

    @Override
    public boolean searchShouldStop(int minCutIdx, int curIdx, int searchLimit) {
        return curIdx - minCutIdx > searchLimit;
    }
}
