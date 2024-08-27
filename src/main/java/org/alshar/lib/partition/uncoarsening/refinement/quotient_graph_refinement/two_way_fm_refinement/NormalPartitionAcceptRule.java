package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.two_way_fm_refinement;

import org.alshar.lib.partition.PartitionConfig;

public class NormalPartitionAcceptRule extends PartitionAcceptRule {
    private int bestCut;
    private int curLhsPartWeight;
    private int curRhsPartWeight;
    private int difference;

    public NormalPartitionAcceptRule(PartitionConfig config,
                                     int initialCut,
                                     int initialLhsPartWeight,
                                     int initialRhsPartWeight) {
        this.bestCut = initialCut;
        this.curLhsPartWeight = initialLhsPartWeight;
        this.curRhsPartWeight = initialRhsPartWeight;
        this.difference = Math.abs(curLhsPartWeight - curRhsPartWeight);
    }

    @Override
    public boolean acceptPartition(PartitionConfig config,
                                   int edgeCut,
                                   int lhsPartWeight,
                                   int rhsPartWeight,
                                   int lhs,
                                   int rhs,
                                   boolean rebalance) {
        int curDiff = Math.abs(lhsPartWeight - rhsPartWeight);
        boolean betterCutWithinBalance = edgeCut < bestCut;

        if (config.isSoftRebalance()) {
            betterCutWithinBalance = edgeCut <= bestCut;
        }

        betterCutWithinBalance = betterCutWithinBalance &&
                lhsPartWeight < config.getUpperBoundPartition() &&
                rhsPartWeight < config.getUpperBoundPartition();

        if ((betterCutWithinBalance || (curDiff < difference && edgeCut == bestCut))
                && lhsPartWeight > 0 && rhsPartWeight > 0) {
            bestCut = edgeCut;
            difference = curDiff;
            rebalance = false;
            return true;
        } else if (rebalance) {
            if (curDiff < difference || (curDiff <= difference && edgeCut < bestCut)) {
                bestCut = edgeCut;
                difference = curDiff;
                return true;
            }
        }
        return false;
    }
}