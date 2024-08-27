package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.two_way_fm_refinement;
import org.alshar.lib.data_structure.priority_queues.PriorityQueueInterface;
import org.alshar.lib.partition.PartitionConfig;

public class QueueSelectionDiffusionBlockTargets extends QueueSelectionStrategy {
    private final QueueSelectionTopGainDiffusion qdiff;

    public QueueSelectionDiffusionBlockTargets(PartitionConfig config) {
        super(config);
        this.qdiff = new QueueSelectionTopGainDiffusion(config);
    }

    @Override
    public void selectQueue(int lhsPartWeight, int rhsPartWeight,
                            int lhs, int rhs,
                            int[] from, int[] to,
                            PriorityQueueInterface lhsQueue, PriorityQueueInterface rhsQueue,
                            PriorityQueueInterface[] fromQueue, PriorityQueueInterface[] toQueue) {

        int lhsOverload = Math.max(lhsPartWeight - config.getTargetWeights().get(0), 0);
        int rhsOverload = Math.max(rhsPartWeight - config.getTargetWeights().get(1), 0);

        if (lhsOverload == 0 && rhsOverload == 0) {
            qdiff.selectQueue(lhsPartWeight, rhsPartWeight, lhs, rhs, from, to, lhsQueue, rhsQueue, fromQueue, toQueue);
        } else {
            if (lhsOverload > rhsOverload) {
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
}