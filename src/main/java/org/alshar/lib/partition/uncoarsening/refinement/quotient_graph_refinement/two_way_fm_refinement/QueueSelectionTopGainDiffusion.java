package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.two_way_fm_refinement;

import org.alshar.lib.data_structure.priority_queues.PriorityQueueInterface;
import org.alshar.lib.partition.PartitionConfig;

public class QueueSelectionTopGainDiffusion extends QueueSelectionStrategy {
    private final QueueSelectionDiffusion qdiff;

    public QueueSelectionTopGainDiffusion(PartitionConfig config) {
        super(config);
        this.qdiff = new QueueSelectionDiffusion(config);
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

        if (lhsGain == rhsGain) {
            qdiff.selectQueue(lhsPartWeight, rhsPartWeight, lhs, rhs, from, to, lhsQueue, rhsQueue, fromQueue, toQueue);
        } else if (lhsGain > rhsGain && false) {
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