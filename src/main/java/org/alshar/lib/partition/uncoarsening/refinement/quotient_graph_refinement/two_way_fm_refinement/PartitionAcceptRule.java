package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.two_way_fm_refinement;

import org.alshar.lib.partition.PartitionConfig;

public abstract class PartitionAcceptRule {
    public PartitionAcceptRule() {}

    public abstract boolean acceptPartition(PartitionConfig config,
                                            int edgeCut,
                                            int lhsPartWeight,
                                            int rhsPartWeight,
                                            int lhs,
                                            int rhs,
                                            boolean rebalance);
}
