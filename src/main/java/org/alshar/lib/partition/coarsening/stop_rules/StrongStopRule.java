package org.alshar.lib.partition.coarsening.stop_rules;

import org.alshar.lib.partition.PartitionConfig;

public class StrongStopRule extends StopRule {
    private final int numStop;

    public StrongStopRule(PartitionConfig config, int numberOfNodes) {
        this.numStop = config.getK();
        config.setMaxVertexWeight(config.getUpperBoundPartition());
    }

    @Override
    public boolean stop(int numberOfFinerVertices, int numberOfCoarserVertices) {
        double contractionRate = 1.0 * numberOfFinerVertices / numberOfCoarserVertices;
        return contractionRate >= 1.1 && numberOfCoarserVertices >= numStop;
    }
}
