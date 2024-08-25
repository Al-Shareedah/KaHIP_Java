package org.alshar.lib.partition.initial_partitioning;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;

public abstract class InitialPartitioner {

    public InitialPartitioner() {
        // Constructor logic if needed
    }

    // Abstract method to be implemented by subclasses
    public abstract void initialPartition(PartitionConfig config,
                                          int seed,
                                          GraphAccess G,
                                          int[] xadj,
                                          int[] adjncy,
                                          int[] vwgt,
                                          int[] adjwgt,
                                          int[] partitionMap);

    // Abstract method to be implemented by subclasses
    public abstract void initialPartition(PartitionConfig config,
                                          int seed,
                                          GraphAccess G,
                                          int[] partitionMap);
}

