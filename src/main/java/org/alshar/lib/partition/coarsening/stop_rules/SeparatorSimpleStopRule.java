package org.alshar.lib.partition.coarsening.stop_rules;

import org.alshar.lib.partition.PartitionConfig;

public class SeparatorSimpleStopRule extends StopRule {
    private final int numStop;

    public SeparatorSimpleStopRule(PartitionConfig config, int numberOfNodes) {
        this.numStop = config.getSepNumVertStop();

        if (config.isDisableMaxVertexWeightConstraint()) {
            config.setMaxVertexWeight(config.getUpperBoundPartition());
        } else {
            config.setMaxVertexWeight((int) (1.5 * config.getWorkLoad() / numStop));
        }
    }

    @Override
    public boolean stop(int numberOfFinerVertices, int numberOfCoarserVertices) {
        double contractionRate = 1.0 * numberOfFinerVertices / numberOfCoarserVertices;
        return contractionRate >= 1.1 && numberOfCoarserVertices >= numStop;
    }
}
