package org.alshar.lib.partition.coarsening.stop_rules;

import org.alshar.lib.partition.PartitionConfig;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleStopRule extends StopRule {
    private final int numStop;

    public SimpleStopRule(PartitionConfig config, int numberOfNodes) {
        double x = 60;
        this.numStop = (int) Math.max(numberOfNodes / (2.0 * x * config.getK()), 60.0 * config.getK());

        if (config.isDisableMaxVertexWeightConstraint()) {
            config.setMaxVertexWeight(config.getUpperBoundPartition());
        } else {
            config.setMaxVertexWeight((int) (1.5 * config.getWorkLoad() / numStop));
        }
    }

    @Override
    public boolean stop(int numberOfFinerVertices, AtomicInteger numberOfCoarserVertices) {
        double contractionRate = 1.0 * numberOfFinerVertices / numberOfCoarserVertices.get();
        return contractionRate >= 1.1 && numberOfCoarserVertices.get() >= numStop;
    }
}
