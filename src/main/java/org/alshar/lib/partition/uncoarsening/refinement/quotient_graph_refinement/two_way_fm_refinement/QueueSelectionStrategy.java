package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.two_way_fm_refinement;

import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.data_structure.priority_queues.PriorityQueueInterface;

public abstract class QueueSelectionStrategy {
    protected PartitionConfig config;

    public QueueSelectionStrategy(PartitionConfig config) {
        this.config = config;
    }

    public abstract void selectQueue(int lhsPartWeight, int rhsPartWeight,
                                     int lhs, int rhs,
                                     int[] from, int[] to,
                                     PriorityQueueInterface lhsQueue, PriorityQueueInterface rhsQueue,
                                     PriorityQueueInterface[] fromQueue, PriorityQueueInterface[] toQueue);
}
