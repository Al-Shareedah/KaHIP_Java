package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.kway_graph_refinement;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.Refinement;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.PartialBoundary;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.BoundaryLookup.BoundaryPair;

import java.util.*;

public class KwayGraphRefinement extends Refinement {

    public KwayGraphRefinement() {}

    @Override
    public int performRefinement(PartitionConfig config, GraphAccess G, CompleteBoundary boundary) {
        KWayGraphRefinementCore refinementCore = new KWayGraphRefinementCore();
        int overallImprovement = 0;
        int maxNumberOfSwaps = G.numberOfNodes();
        boolean sthChanged = config.isNoChangeConvergence();

        for (int i = 0; i < config.getKwayRounds() || sthChanged; i++) {
            int improvement = 0;

            List<Integer> startNodes = new ArrayList<>();
            setupStartNodes(config, G, boundary, startNodes);

            if (startNodes.isEmpty()) return 0; // nothing to refine

            // Metis step limit
            int stepLimit = (int) ((config.getKwayFmSearchLimit() / 100.0) * maxNumberOfSwaps);
            stepLimit = Math.max(stepLimit, 15);

            VertexMovedHashtable movedIdx = new VertexMovedHashtable();
            improvement += refinementCore.singleKWayRefinementRound(config, G, boundary, startNodes, stepLimit, movedIdx);

            sthChanged = improvement != 0 && config.isNoChangeConvergence();
            if (improvement == 0) break;
            overallImprovement += improvement;
        }

        assert overallImprovement >= 0;

        return overallImprovement;
    }

    public void setupStartNodes(PartitionConfig config, GraphAccess G, CompleteBoundary boundary, List<Integer> startNodes) {
        List<BoundaryPair> quotientGraphEdges = new ArrayList<>();
        boundary.getQuotientGraphEdges(quotientGraphEdges);
        Map<Integer, Boolean> alreadyContained = new HashMap<>();

        for (BoundaryPair retValue : quotientGraphEdges) {
            int lhs = retValue.lhs;
            int rhs = retValue.rhs;

            PartialBoundary partialBoundaryLhs = boundary.getDirectedBoundary(lhs, lhs, rhs);
            partialBoundaryLhs.forAllBoundaryNodes(curBndNode -> {
                assert G.getPartitionIndex(curBndNode) == lhs;
                if (!alreadyContained.containsKey(curBndNode)) {
                    startNodes.add(curBndNode);
                    alreadyContained.put(curBndNode, true);
                }
            });

            PartialBoundary partialBoundaryRhs = boundary.getDirectedBoundary(rhs, lhs, rhs);
            partialBoundaryRhs.forAllBoundaryNodes(curBndNode -> {
                assert G.getPartitionIndex(curBndNode) == rhs;
                if (!alreadyContained.containsKey(curBndNode)) {
                    startNodes.add(curBndNode);
                    alreadyContained.put(curBndNode, true);
                }
            });
        }
    }

}
