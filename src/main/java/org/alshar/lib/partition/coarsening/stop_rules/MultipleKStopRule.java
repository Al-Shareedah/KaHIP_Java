package org.alshar.lib.partition.coarsening.stop_rules;

import org.alshar.lib.partition.PartitionConfig;

public class MultipleKStopRule extends StopRule {
    private final int numStop;

    public MultipleKStopRule(PartitionConfig config, int numberOfNodes) {
        this.numStop = config.getNumVertStopFactor() * config.getK();

        if (config.isDisableMaxVertexWeightConstraint()) {
            config.setMaxVertexWeight(config.getUpperBoundPartition());
        } else {
            if (config.isInitialPartitioning()) {
                config.setMaxVertexWeight((int) (1.5 * config.getWorkLoad() / (2 * config.getNumVertStopFactor())));
            } else {
                config.setMaxVertexWeight((int) (1.5 * config.getWorkLoad() / numStop));
            }
        }
    }

    @Override
    public boolean stop(int numberOfFinerVertices, int numberOfCoarserVertices) {
        double contractionRate = 1.0 * numberOfFinerVertices / numberOfCoarserVertices;
        return contractionRate >= 1.1 && numberOfCoarserVertices >= numStop;
    }
}
