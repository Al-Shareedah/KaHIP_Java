package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.Refinement;
import org.alshar.lib.quotient_graph_refinement.CompleteBoundary;
import org.alshar.lib.quotient_graph_refinement.BoundaryPair;
import org.alshar.lib.quotient_graph_scheduling.ActiveBlockQuotientGraphScheduler;
import org.alshar.lib.quotient_graph_scheduling.QuotientGraphScheduling;
import org.alshar.lib.quotient_graph_scheduling.SimpleQuotientGraphScheduler;
import org.alshar.lib.quality_metrics.QualityMetrics;
import org.alshar.lib.uncoarsening.refinement.twoway.TwoWayFMRefinement;
import org.alshar.lib.uncoarsening.refinement.twoway.TwoWayFlowRefinement;

import java.util.*;

public class QuotientGraphRefinement extends Refinement {

    public QuotientGraphRefinement() {
    }

    @Override
    public int performRefinement(PartitionConfig config, GraphAccess G, CompleteBoundary boundary) {

        assert boundary.assertBNodesInBoundaries();
        assert boundary.assertBoundariesAreBNodes();

        List<BoundaryPair> qgraphEdges = boundary.getQuotientGraphEdges();
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

            boolean somethingChanged = false;
            int improvement = performTwoWayRefinement(config, G, boundary, bp, lhs, rhs, lhsPartWeight, rhsPartWeight, initialCutValue, somethingChanged);

            overallImprovement += improvement;

            if (config.getRefinementSchedulingAlgorithm() == PartitionConfig.REFINEMENT_SCHEDULING_ACTIVE_BLOCKS_REF_KWAY) {
                // Multitry refinement logic (omitted for brevity)
            }

            assert qm.edgeCut(G, lhs, rhs) == initialCutValue - improvement;

            assert boundary.assertBNodesInBoundaries();
            assert boundary.assertBoundariesAreBNodes();
            assert boundary.getBlockNoNodes(lhs) > 0;
            assert boundary.getBlockNoNodes(rhs) > 0;

        } while (!scheduler.hasFinished());

        return overallImprovement;
    }

    private int performTwoWayRefinement(PartitionConfig config, GraphAccess G, CompleteBoundary boundary, BoundaryPair bp, int lhs, int rhs, int lhsPartWeight, int rhsPartWeight, int initialCutValue, boolean somethingChanged) {
        TwoWayFMRefinement pairWiseRefinement = new TwoWayFMRefinement();
        TwoWayFlowRefinement pairWiseFlow = new TwoWayFlowRefinement();

        List<Integer> lhsBndNodes = setupStartNodes(G, lhs, bp, boundary);
        List<Integer> rhsBndNodes = setupStartNodes(G, rhs, bp, boundary);

        somethingChanged = false;
        int improvement = 0;

        QualityMetrics qm = new QualityMetrics();
        if (config.getRefinementType() == PartitionConfig.REFINEMENT_TYPE_FM_FLOW || config.getRefinementType() == PartitionConfig.REFINEMENT_TYPE_FM) {
            improvement = pairWiseRefinement.performRefinement(config, G, boundary, lhsBndNodes, rhsBndNodes, bp, lhsPartWeight, rhsPartWeight, initialCutValue, somethingChanged);
            assert improvement >= 0 || config.isRebalance();
        }

        if (config.getRefinementType() == PartitionConfig.REFINEMENT_TYPE_FM_FLOW || config.getRefinementType() == PartitionConfig.REFINEMENT_TYPE_FLOW) {
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
        int curIdx = 0;

        int lhs = bp.getLhs();
        int rhs = bp.getRhs();
        List<Integer> lhsBoundaryNodes = boundary.getDirectedBoundary(partition, lhs, rhs);

        for (int node : lhsBoundaryNodes) {
            assert G.getPartitionIndex(node) == partition;
            startNodes.add(curIdx++, node);
        }

        return startNodes;
    }
}
