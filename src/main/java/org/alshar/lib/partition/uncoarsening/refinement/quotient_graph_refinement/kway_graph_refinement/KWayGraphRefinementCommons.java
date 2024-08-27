package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.kway_graph_refinement;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.tools.RandomFunctions;

import java.util.ArrayList;
import java.util.List;

public class KWayGraphRefinementCommons {
    public static final int INVALID_PARTITION = Integer.MAX_VALUE;
    // Internal structure for efficient computation of internal and external degrees
    private static class RoundStruct {
        int round;
        int localDegree;

        public RoundStruct() {
            this.round = 0;
            this.localDegree = 0;
        }
    }

    private List<RoundStruct> localDegrees;
    private int round;

    // Constructor
    public KWayGraphRefinementCommons(PartitionConfig config) {
        init(config);
    }

    // Destructor (Java uses garbage collection, so no need to implement)
    protected void finalize() throws Throwable {
        super.finalize();
    }

    // Initialize the structure based on the number of partitions
    public void init(PartitionConfig config) {
        localDegrees = new ArrayList<>(config.getK());
        for (int i = 0; i < config.getK(); i++) {
            localDegrees.add(new RoundStruct());
        }
        round = 0; // needed for the computation of internal and external degrees
    }

    // Returns the number of partitions (K)
    public int getUnderlyingK() {
        return localDegrees.size();
    }

    // Check if a node is incident to more than two partitions
    public boolean incidentToMoreThanTwoPartitions(GraphAccess G, int node) {
        boolean retValue = false;
        int ownPartition = G.getPartitionIndex(node);
        int secondPartition = INVALID_PARTITION;

        for (int e = G.getFirstEdge(node), end = G.getFirstInvalidEdge(node); e < end; e++) {
            int target = G.getEdgeTarget(e);
            int targetPartition = G.getPartitionIndex(target);
            if (targetPartition != ownPartition) {
                if (secondPartition == INVALID_PARTITION) {
                    secondPartition = targetPartition;
                } else if (targetPartition != secondPartition) {
                    retValue = true;
                    break;
                }
            }
        }
        return retValue;
    }

    // Compute internal and external degrees for a node with respect to two partitions (lhs, rhs)
    public boolean intExtDegree(GraphAccess G, int node, int lhs, int rhs, int[] intDegree, int[] extDegree) {

        assert lhs == G.getPartitionIndex(node);

        intDegree[0] = 0;
        extDegree[0] = 0;
        boolean updateIsDifficult = false;

        for (int e = G.getFirstEdge(node), end = G.getFirstInvalidEdge(node); e < end; e++) {
            int target = G.getEdgeTarget(e);
            int targetPartition = G.getPartitionIndex(target);

            if (targetPartition == lhs) {
                intDegree[0] += G.getEdgeWeight(e);
            } else if (targetPartition == rhs) {
                extDegree[0] += G.getEdgeWeight(e);
            }

            if (targetPartition != lhs && targetPartition != rhs) {
                updateIsDifficult = true;
            }
        }

        return updateIsDifficult;
    }

    // Compute the gain of moving a node to another partition
    public int computeGain(GraphAccess G, int node, int[] maxGainer, int[] extDegree) {
        int sourcePartition = G.getPartitionIndex(node);
        int maxDegree = 0;
        maxGainer[0] = INVALID_PARTITION;

        round++; // can become zero again
        for (int e = G.getFirstEdge(node), end = G.getFirstInvalidEdge(node); e < end; e++) {
            int target = G.getEdgeTarget(e);
            int targetPartition = G.getPartitionIndex(target);

            if (localDegrees.get(targetPartition).round == round) {
                localDegrees.get(targetPartition).localDegree += G.getEdgeWeight(e);
            } else {
                localDegrees.get(targetPartition).localDegree = G.getEdgeWeight(e);
                localDegrees.get(targetPartition).round = round;
            }

            if (localDegrees.get(targetPartition).localDegree >= maxDegree && targetPartition != sourcePartition) {
                if (localDegrees.get(targetPartition).localDegree > maxDegree) {
                    maxDegree = localDegrees.get(targetPartition).localDegree;
                    maxGainer[0] = targetPartition;
                } else {
                    // break ties randomly
                    boolean accept = RandomFunctions.nextBool();
                    if (accept) {
                        maxDegree = localDegrees.get(targetPartition).localDegree;
                        maxGainer[0] = targetPartition;
                    }
                }
            }
        }

        if (maxGainer[0] != INVALID_PARTITION) {
            extDegree[0] = maxDegree;
        } else {
            extDegree[0] = 0;
        }

        if (localDegrees.get(sourcePartition).round != round) {
            localDegrees.get(sourcePartition).localDegree = 0;
        }

        return maxDegree - localDegrees.get(sourcePartition).localDegree;
    }
}
