package org.alshar.lib.partition.coarsening.stop_rules;

import org.alshar.lib.partition.PartitionConfig;

public abstract class StopRule {

    public StopRule() {}

    public abstract boolean stop(int numberOfFinerVertices, int numberOfCoarserVertices);
}

