package org.alshar.lib.partition.uncoarsening.refinement;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.QuotientGraphRefinement;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.cycle_improvements.CycleRefinement;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.kway_graph_refinement.KwayGraphRefinement;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.kway_graph_refinement.MultiTryKWayFM;

public class MixedRefinement extends Refinement {

    public MixedRefinement() {
        super();
    }

    @Override
    public int performRefinement(PartitionConfig config, GraphAccess G, CompleteBoundary boundary) {
        Refinement refine = new QuotientGraphRefinement();
        Refinement kway = new KwayGraphRefinement();
        MultiTryKWayFM multitryKway = new MultiTryKWayFM();
        CycleRefinement cycleRefine = new CycleRefinement();

        int overallImprovement = 0;

        if (config.isNoChangeConvergence()) {
            boolean sthChanged = true;
            while (sthChanged) {
                int improvement = 0;
                if (config.isCornerRefinementEnabled()) {
                    improvement += kway.performRefinement(config, G, boundary);
                }

                if (!config.isQuotientGraphRefinementDisabled()) {
                    improvement += refine.performRefinement(config, G, boundary);
                }

                overallImprovement += improvement;
                sthChanged = improvement != 0;
            }
        } else {
            if (config.isCornerRefinementEnabled()) {
                overallImprovement += kway.performRefinement(config, G, boundary);
            }

            if (!config.isQuotientGraphRefinementDisabled()) {
                overallImprovement += refine.performRefinement(config, G, boundary);
            }

            if (config.isKaffpaPerfectlyBalancedRefinement()) {
                overallImprovement += cycleRefine.performRefinement(config, G, boundary);
            }
        }

        // Clean up resources
        refine = null;
        kway = null;
        multitryKway = null;
        cycleRefine = null;

        return overallImprovement;
    }
}
