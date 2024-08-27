package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.two_way_fm_refinement;

import org.alshar.lib.partition.PartitionConfig;

public class IPPartitionAcceptRule extends PartitionAcceptRule {
    private int bestCut;
    private int curLhsOverload;
    private int curRhsOverload;

    public IPPartitionAcceptRule(PartitionConfig config,
                                 int initialCut,
                                 int initialLhsPartWeight,
                                 int initialRhsPartWeight,
                                 int lhs,
                                 int rhs) {
        this.bestCut = initialCut;
        this.curLhsOverload = Math.max(initialLhsPartWeight - config.getTargetWeights().get(lhs), 0);
        this.curRhsOverload = Math.max(initialRhsPartWeight - config.getTargetWeights().get(rhs), 0);
    }

    @Override
    public boolean acceptPartition(PartitionConfig config,
                                   int edgeCut,
                                   int lhsPartWeight,
                                   int rhsPartWeight,
                                   int lhs,
                                   int rhs,
                                   boolean rebalance) {
        boolean betterCutWithinBalance = edgeCut <= bestCut;

        int actLhsOverload = Math.max(lhsPartWeight - config.getTargetWeights().get(lhs), 0);
        int actRhsOverload = Math.max(rhsPartWeight - config.getTargetWeights().get(rhs), 0);

        betterCutWithinBalance = betterCutWithinBalance &&
                actLhsOverload == 0 && actRhsOverload == 0;

        if (actRhsOverload == 0 && actLhsOverload == 0) {
            config.setRebalance(false);
        }

        if (config.isRebalance()) {
            if (actRhsOverload + actLhsOverload < curLhsOverload + curRhsOverload
                    || (actRhsOverload + actLhsOverload <= curLhsOverload + curRhsOverload
                    && edgeCut < bestCut)) {
                bestCut = edgeCut;
                curLhsOverload = actLhsOverload;
                curRhsOverload = actRhsOverload;
                return true;
            }
        } else {
            if ((betterCutWithinBalance ||
                    (actRhsOverload + actLhsOverload < curLhsOverload + curRhsOverload && edgeCut == bestCut))
                    && lhsPartWeight > 0 && rhsPartWeight > 0) {
                bestCut = edgeCut;
                curLhsOverload = actLhsOverload;
                curRhsOverload = actRhsOverload;
                return true;
            }
        }
        return false;
    }
}
