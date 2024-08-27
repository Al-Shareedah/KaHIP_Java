package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.enums.RefinementSchedulingAlgorithm;
import org.alshar.lib.enums.RefinementType;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.Refinement;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.BoundaryLookup.BoundaryPair;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.flow_refinement.TwoWayFlowRefinement;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.kway_graph_refinement.MultiTryKWayFM;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.quotient_graph_scheduling.ActiveBlockQuotientGraphScheduler;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.quotient_graph_scheduling.QuotientGraphScheduling;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.quotient_graph_scheduling.SimpleQuotientGraphScheduler;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.two_way_fm_refinement.TwoWayFM;
import org.alshar.lib.tools.QualityMetrics;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.TwoWayRefinement;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class QuotientGraphRefinement extends Refinement {

    public QuotientGraphRefinement() {
    }

    @Override
    public int performRefinement(PartitionConfig config, GraphAccess G, CompleteBoundary boundary) {

        assert boundary.assertBNodesInBoundaries();
        assert boundary.assertBoundariesAreBNodes();

        // Create a list to hold the quotient graph edges
        List<BoundaryPair> qgraphEdges = new ArrayList<>();

        // Call the method with the list as an argument
        boundary.getQuotientGraphEdges(qgraphEdges);
        QuotientGraphScheduling scheduler = null;

        int factor = (int) Math.ceil(config.getBankAccountFactor() * qgraphEdges.size());
        switch (config.getRefinementSchedulingAlgorithm()) {
            case REFINEMENT_SCHEDULING_FAST:
                scheduler = new SimpleQuotientGraphScheduler(config, qgraphEdges, factor);
                break;
            case REFINEMENT_SCHEDULING_ACTIVE_BLOCKS:
            case REFINEMENT_SCHEDULING_ACTIVE_BLOCKS_REF_KWAY:
                scheduler = new ActiveBlockQuotientGraphScheduler(config, qgraphEdges, factor);
                break;
        }

        int overallImprovement = 0;
        int noOfPairwiseImprovementSteps = 0;
        QualityMetrics qm = new QualityMetrics();

        do {
            noOfPairwiseImprovementSteps++;

            assert boundary.assertBNodesInBoundaries();
            assert boundary.assertBoundariesAreBNodes();

            if (scheduler.hasFinished()) break;

            BoundaryPair bp = scheduler.getNext();
            int lhs = bp.getLhs();
            int rhs = bp.getRhs();

            int lhsPartWeight = boundary.getBlockWeight(lhs);
            int rhsPartWeight = boundary.getBlockWeight(rhs);

            int initialCutValue = boundary.getEdgeCut(bp);
            if (initialCutValue < 0) continue;

            boolean[] somethingChanged = new boolean[]{false};
            int improvement = performTwoWayRefinement(config, G, boundary, bp, lhs, rhs, lhsPartWeight, rhsPartWeight, initialCutValue, somethingChanged);

            overallImprovement += improvement;

            if (config.getRefinementSchedulingAlgorithm() == RefinementSchedulingAlgorithm.REFINEMENT_SCHEDULING_ACTIVE_BLOCKS_REF_KWAY) {
                // Multitry refinement logic
                MultiTryKWayFM kwayRef = new MultiTryKWayFM();
                Map<Integer, Integer> touchedBlocks = new HashMap<>();

                int multitryImprovement = kwayRef.performRefinementAroundParts(config, G, boundary, true, config.getLocalMultitryFmAlpha(), lhs, rhs, touchedBlocks);

                if (multitryImprovement > 0 && scheduler instanceof ActiveBlockQuotientGraphScheduler) {
                    ((ActiveBlockQuotientGraphScheduler) scheduler).activateBlocks(touchedBlocks);
                }
            }
            QuotientGraphScheduling.QGraphEdgeStatistics stat = new QuotientGraphScheduling.QGraphEdgeStatistics(improvement, bp, somethingChanged[0]);
            scheduler.pushStatistics(stat);
            assert qm.edgeCut(G, lhs, rhs) == initialCutValue - improvement;

            assert boundary.assertBNodesInBoundaries();
            assert boundary.assertBoundariesAreBNodes();
            assert boundary.getBlockNoNodes(lhs) > 0;
            assert boundary.getBlockNoNodes(rhs) > 0;

        } while (!scheduler.hasFinished());

        return overallImprovement;
    }

    private int performTwoWayRefinement(PartitionConfig config, GraphAccess G, CompleteBoundary boundary, BoundaryPair bp, int lhs, int rhs, int lhsPartWeight, int rhsPartWeight, int initialCutValue, boolean[] somethingChanged) {
        TwoWayFM pairWiseRefinement = new TwoWayFM();
        TwoWayFlowRefinement pairWiseFlow = new TwoWayFlowRefinement();

        List<Integer> lhsBndNodes = setupStartNodes(G, lhs, bp, boundary);
        List<Integer> rhsBndNodes = setupStartNodes(G, rhs, bp, boundary);

        somethingChanged = new boolean[]{false};
        int improvement = 0;

        QualityMetrics qm = new QualityMetrics();
        if (config.getRefinementType() == RefinementType.REFINEMENT_TYPE_FM_FLOW || config.getRefinementType() == RefinementType.REFINEMENT_TYPE_FM) {
            improvement = pairWiseRefinement.performRefinement(config, G, boundary, lhsBndNodes, rhsBndNodes, bp, lhsPartWeight, rhsPartWeight, initialCutValue, somethingChanged);
            assert improvement >= 0 || config.isRebalance();
        }

        if (config.getRefinementType() == RefinementType.REFINEMENT_TYPE_FM_FLOW || config.getRefinementType() == RefinementType.REFINEMENT_TYPE_FLOW) {
            lhsBndNodes = setupStartNodes(G, lhs, bp, boundary);
            rhsBndNodes = setupStartNodes(G, rhs, bp, boundary);

            int flowImprovement = pairWiseFlow.performRefinement(config, G, boundary, lhsBndNodes, rhsBndNodes, bp, lhsPartWeight, rhsPartWeight, initialCutValue, somethingChanged);
            assert flowImprovement >= 0 || config.isRebalance();
            improvement += flowImprovement;
        }

        // Additional logic for handling overloaded blocks

        return improvement;
    }

    private List<Integer> setupStartNodes(GraphAccess G, int partition, BoundaryPair bp, CompleteBoundary boundary) {
        List<Integer> startNodes = new ArrayList<>();

        int lhs = bp.getLhs();
        int rhs = bp.getRhs();
        PartialBoundary lhsBoundaryNodes = boundary.getDirectedBoundary(partition, lhs, rhs);

        lhsBoundaryNodes.forAllBoundaryNodes(node -> {
            assert G.getPartitionIndex(node) == partition;
            startNodes.add(node);
        });

        return startNodes;
    }

}
