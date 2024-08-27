package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.two_way_fm_refinement;

import org.alshar.lib.data_structure.priority_queues.PriorityQueueInterface;
import org.alshar.lib.partition.PartitionConfig;

public class QueueSelectionTopGain extends QueueSelectionStrategy {
    public QueueSelectionTopGain(PartitionConfig config) {
        super(config);
    }

    @Override
    public void selectQueue(int lhsPartWeight, int rhsPartWeight,
                            int lhs, int rhs,
                            int[] from, int[] to,
                            PriorityQueueInterface lhsQueue, PriorityQueueInterface rhsQueue,
                            PriorityQueueInterface[] fromQueue, PriorityQueueInterface[] toQueue) {

        if (lhsQueue.isEmpty()) {
            fromQueue[0] = rhsQueue;
            toQueue[0] = lhsQueue;
            from[0] = rhs;
            to[0] = lhs;
            return;
        }
        if (rhsQueue.isEmpty()) {
            fromQueue[0] = lhsQueue;
            toQueue[0] = rhsQueue;
            from[0] = lhs;
            to[0] = rhs;
            return;
        }

        int lhsGain = lhsQueue.maxValue();
        int rhsGain = rhsQueue.maxValue();

        if (lhsGain > rhsGain) {
            fromQueue[0] = lhsQueue;
            toQueue[0] = rhsQueue;
            from[0] = lhs;
            to[0] = rhs;
        } else {
            fromQueue[0] = rhsQueue;
            toQueue[0] = lhsQueue;
            from[0] = rhs;
            to[0] = lhs;
        }
    }
}