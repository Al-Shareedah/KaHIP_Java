package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.flow_refinement;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.priority_queues.PriorityQueueInterface;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.BoundaryLookup.BoundaryPair;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.TwoWayRefinement;

import java.util.ArrayList;
import java.util.List;

public class TwoWayFlowRefinement extends TwoWayRefinement {
    private static final int BOUNDARY_STRIPE_NODE = Integer.MAX_VALUE;

    public TwoWayFlowRefinement() {}

    @Override
    public int performRefinement(PartitionConfig config,
                                 GraphAccess G,
                                 CompleteBoundary boundary,
                                 List<Integer> lhsPQStartNodes,
                                 List<Integer> rhsPQStartNodes,
                                 BoundaryPair refinementPair,
                                 int lhsPartWeight,
                                 int rhsPartWeight,
                                 int cut,
                                 boolean[] somethingChanged) {

        int retval = iterativeFlowIteration(config, G, boundary, lhsPQStartNodes, rhsPQStartNodes,
                refinementPair, lhsPartWeight, rhsPartWeight, cut, somethingChanged);

        if (retval > 0) {
            somethingChanged[0] = true;
        }

        return retval;
    }

    private int iterativeFlowIteration(PartitionConfig config,
                                       GraphAccess G,
                                       CompleteBoundary boundary,
                                       List<Integer> lhsPQStartNodes,
                                       List<Integer> rhsPQStartNodes,
                                       BoundaryPair refinementPair,
                                       int lhsPartWeight,
                                       int rhsPartWeight,
                                       int cut,
                                       boolean[] somethingChanged) {

        if (lhsPQStartNodes.isEmpty() || rhsPQStartNodes.isEmpty()) return 0;

        assert lhsPartWeight < config.getUpperBoundPartition() && rhsPartWeight < config.getUpperBoundPartition();

        int lhs = refinementPair.lhs;
        int rhs = refinementPair.rhs;
        BoundaryBFS bfsRegionSearcher = new BoundaryBFS();

        double regionFactor = config.getFlowRegionFactor();
        int maxIterations = config.getMaxFlowIterations();
        int iteration = 0;

        List<Integer> lhsNodes = new ArrayList<>();
        List<Integer> rhsNodes = new ArrayList<>();

        int curImprovement = 1;
        int bestCut = cut;
        boolean sumOverWeight = lhsPartWeight + rhsPartWeight > 2 * config.getUpperBoundPartition();
        if (sumOverWeight) {
            return 0;
        }

        int averagePartitionWeight = (int) Math.ceil(config.getWorkLoad() / config.getK());
        while (curImprovement > 0 && iteration < maxIterations) {
            int upperBoundNoLhs = (int) Math.max((100.0 + regionFactor * config.getImbalance()) / 100.0 * averagePartitionWeight - rhsPartWeight, 0.0);
            int upperBoundNoRhs = (int) Math.max((100.0 + regionFactor * config.getImbalance()) / 100.0 * averagePartitionWeight - lhsPartWeight, 0.0);

            upperBoundNoLhs = Math.min(lhsPartWeight - 1, upperBoundNoLhs);
            upperBoundNoRhs = Math.min(rhsPartWeight - 1, upperBoundNoRhs);

            List<Integer> lhsBoundaryStripe = new ArrayList<>();
            int[] lhsStripeWeight = new int[]{0};
            if (!bfsRegionSearcher.boundaryBFSSearch(G, lhsPQStartNodes, lhs, upperBoundNoLhs, lhsBoundaryStripe, lhsStripeWeight, true)) {
                int improvement = cut - bestCut;
                cut = bestCut;
                return improvement;
            }

            List<Integer> rhsBoundaryStripe = new ArrayList<>();
            int[] rhsStripeWeight = new int[]{0};
            if (!bfsRegionSearcher.boundaryBFSSearch(G, rhsPQStartNodes, rhs, upperBoundNoRhs, rhsBoundaryStripe, rhsStripeWeight, true)) {
                int improvement = cut - bestCut;
                cut = bestCut;
                return improvement;
            }

            List<Integer> newRhsNodes = new ArrayList<>();
            List<Integer> newToOldIds = new ArrayList<>();

            CutFlowProblemSolver fsolve = new CutFlowProblemSolver();
            int newCut = fsolve.getMinFlowMaxCut(config, G, lhs, rhs, lhsBoundaryStripe, rhsBoundaryStripe, newToOldIds, bestCut, rhsPartWeight, rhsStripeWeight[0], newRhsNodes);

            int newLhsPartWeight = 0;
            int newRhsPartWeight = 0;
            int newLhsStripeWeight = 0;
            int newRhsStripeWeight = 0;
            int noNodesFlowGraph = lhsBoundaryStripe.size() + rhsBoundaryStripe.size();

            for (int i = 0; i < newRhsNodes.size(); i++) {
                int newRhsNode = newRhsNodes.get(i);
                if (newRhsNode < noNodesFlowGraph) {
                    int oldNodeId = newToOldIds.get(newRhsNode);
                    newRhsStripeWeight += G.getNodeWeight(oldNodeId);
                    G.setPartitionIndex(oldNodeId, BOUNDARY_STRIPE_NODE - 1);
                }
            }

            for (int i = 0; i < lhsBoundaryStripe.size(); i++) {
                if (G.getPartitionIndex(lhsBoundaryStripe.get(i)) == BOUNDARY_STRIPE_NODE) {
                    newLhsStripeWeight += G.getNodeWeight(lhsBoundaryStripe.get(i));
                }
            }

            for (int i = 0; i < rhsBoundaryStripe.size(); i++) {
                if (G.getPartitionIndex(rhsBoundaryStripe.get(i)) == BOUNDARY_STRIPE_NODE) {
                    newLhsStripeWeight += G.getNodeWeight(rhsBoundaryStripe.get(i));
                }
            }
            newLhsPartWeight = boundary.getBlockWeight(lhs) + (newLhsStripeWeight - lhsStripeWeight[0]);
            newRhsPartWeight = boundary.getBlockWeight(rhs) + (newRhsStripeWeight - rhsStripeWeight[0]);


            boolean partitionIsFeasible = false;
            if (config.isMostBalancedMinimumCuts()) {
                partitionIsFeasible = newLhsPartWeight < config.getUpperBoundPartition()
                        && newRhsPartWeight < config.getUpperBoundPartition()
                        && (newCut < bestCut || Math.abs(newLhsPartWeight - newRhsPartWeight) < Math.abs(lhsPartWeight - rhsPartWeight));
            } else {
                partitionIsFeasible = newLhsPartWeight < config.getUpperBoundPartition()
                        && newRhsPartWeight < config.getUpperBoundPartition() && newCut < bestCut;
            }

            if (partitionIsFeasible) {
                applyPartitionAndUpdateBoundary(config, G, refinementPair, lhs, rhs, boundary,
                        lhsBoundaryStripe, rhsBoundaryStripe, lhsStripeWeight[0], rhsStripeWeight[0], newToOldIds, newRhsNodes);

                boundary.setEdgeCut(refinementPair, newCut);

                lhsPartWeight = boundary.getBlockWeight(lhs);
                rhsPartWeight = boundary.getBlockWeight(rhs);
                assert lhsPartWeight < config.getUpperBoundPartition() && rhsPartWeight < config.getUpperBoundPartition();

                curImprovement = bestCut - newCut;
                bestCut = newCut;

                if (2 * regionFactor < config.getFlowRegionFactor()) {
                    regionFactor *= 2;
                } else {
                    regionFactor = config.getFlowRegionFactor();
                }
                if (regionFactor == config.getFlowRegionFactor()) {
                    break;
                }
                if (iteration + 1 < maxIterations) {
                    lhsPQStartNodes.clear();
                    boundary.setupStartNodes(G, lhs, refinementPair, lhsPQStartNodes);

                    rhsPQStartNodes.clear();
                    boundary.setupStartNodes(G, rhs, refinementPair, rhsPQStartNodes);
                }
            } else {
                for (int i = 0; i < lhsBoundaryStripe.size(); i++) {
                    G.setPartitionIndex(lhsBoundaryStripe.get(i), lhs);
                }
                for (int i = 0; i < rhsBoundaryStripe.size(); i++) {
                    G.setPartitionIndex(rhsBoundaryStripe.get(i), rhs);
                }

                regionFactor = Math.max(regionFactor / 2, 1.0);
                if (newCut == bestCut) {
                    break;
                }
            }
            iteration++;
        }

        assert lhsPartWeight < config.getUpperBoundPartition() && rhsPartWeight < config.getUpperBoundPartition();
        int improvement = cut - bestCut;
        cut = bestCut;
        return improvement;
    }

    private void applyPartitionAndUpdateBoundary(PartitionConfig config,
                                                 GraphAccess G,
                                                 BoundaryPair refinementPair,
                                                 int lhs,
                                                 int rhs,
                                                 CompleteBoundary boundary,
                                                 List<Integer> lhsBoundaryStripe,
                                                 List<Integer> rhsBoundaryStripe,
                                                 int lhsStripeWeight,
                                                 int rhsStripeWeight,
                                                 List<Integer> newToOldIds,
                                                 List<Integer> newRhsNodes) {

        int noNodesFlowGraph = lhsBoundaryStripe.size() + rhsBoundaryStripe.size();
        int newLhsStripeWeight = 0;
        int newRhsStripeWeight = 0;

        int newRhsStripeNoNodes = 0;
        int newLhsStripeNoNodes = 0;

        for (int i = 0; i < newRhsNodes.size(); i++) {
            int newRhsNode = newRhsNodes.get(i);
            if (newRhsNode < noNodesFlowGraph) {
                int oldNodeId = newToOldIds.get(newRhsNode);
                G.setPartitionIndex(oldNodeId, rhs);
                newRhsStripeWeight += G.getNodeWeight(oldNodeId);
                newRhsStripeNoNodes++;
            }
        }

        for (int i = 0; i < lhsBoundaryStripe.size(); i++) {
            if (G.getPartitionIndex(lhsBoundaryStripe.get(i)) == BOUNDARY_STRIPE_NODE) {
                G.setPartitionIndex(lhsBoundaryStripe.get(i), lhs);
                newLhsStripeWeight += G.getNodeWeight(lhsBoundaryStripe.get(i));
                newLhsStripeNoNodes++;
            }
        }

        for (int i = 0; i < rhsBoundaryStripe.size(); i++) {
            if (G.getPartitionIndex(rhsBoundaryStripe.get(i)) == BOUNDARY_STRIPE_NODE) {
                G.setPartitionIndex(rhsBoundaryStripe.get(i), lhs);
                newLhsStripeWeight += G.getNodeWeight(rhsBoundaryStripe.get(i));
                newLhsStripeNoNodes++;
            }
        }

        boundary.setBlockWeight(lhs, boundary.getBlockWeight(lhs) + (newLhsStripeWeight - lhsStripeWeight));
        boundary.setBlockWeight(rhs, boundary.getBlockWeight(rhs) + (newRhsStripeWeight - rhsStripeWeight));

        boundary.setBlockNoNodes(lhs, boundary.getBlockNoNodes(lhs) + (newLhsStripeNoNodes - lhsBoundaryStripe.size()));
        boundary.setBlockNoNodes(rhs, boundary.getBlockNoNodes(rhs) + (newRhsStripeNoNodes - rhsBoundaryStripe.size()));

        for (int i = 0; i < lhsBoundaryStripe.size(); i++) {
            boundary.postMovedBoundaryNodeUpdates(lhsBoundaryStripe.get(i), refinementPair, false, true);
        }

        for (int i = 0; i < rhsBoundaryStripe.size(); i++) {
            boundary.postMovedBoundaryNodeUpdates(rhsBoundaryStripe.get(i), refinementPair, false, true);
        }
    }
}