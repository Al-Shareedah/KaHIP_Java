package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.flow_refinement;
import org.alshar.lib.algorithms.PushRelabel;
import org.alshar.lib.data_structure.FlowGraph;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.data_structure.GraphAccess;

import java.util.*;

public class CutFlowProblemSolver {

    public CutFlowProblemSolver() {
    }
    private static final int BOUNDARY_STRIPE_NODE = Integer.MAX_VALUE;
    public int regionsNoEdges(GraphAccess G,
                              List<Integer> lhsBoundaryStripe,
                              List<Integer> rhsBoundaryStripe,
                              int lhs,
                              int rhs,
                              List<Integer> outerLhsBoundaryNodes,
                              List<Integer> outerRhsBoundaryNodes) {

        int noOfEdges = 0;
        int idx = 0;

        for (int i = 0; i < lhsBoundaryStripe.size(); i++, idx++) {
            int node = lhsBoundaryStripe.get(i);
            boolean isOuterBoundary = false;
            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                if (G.getPartitionIndex(G.getEdgeTarget(e)) == BOUNDARY_STRIPE_NODE) {
                    noOfEdges++;
                } else {
                    isOuterBoundary = true;
                }
            }
            if (isOuterBoundary) {
                outerLhsBoundaryNodes.add(idx);
            }
        }

        for (int i = 0; i < rhsBoundaryStripe.size(); i++, idx++) {
            int node = rhsBoundaryStripe.get(i);
            boolean isOuterBoundary = false;
            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                if (G.getPartitionIndex(G.getEdgeTarget(e)) == BOUNDARY_STRIPE_NODE) {
                    noOfEdges++;
                } else {
                    isOuterBoundary = true;
                }
            }
            if (isOuterBoundary) {
                outerRhsBoundaryNodes.add(idx);
            }
        }

        return noOfEdges;
    }

    public boolean convertDS(PartitionConfig config,
                             GraphAccess G,
                             int lhs,
                             int rhs,
                             List<Integer> lhsBoundaryStripe,
                             List<Integer> rhsBoundaryStripe,
                             List<Integer> newToOldIds,
                             FlowGraph fG) {

        // Building up the graph as in parse.h of hi_pr code
        int idx = 0;
        newToOldIds.clear();
        newToOldIds.addAll(Collections.nCopies(lhsBoundaryStripe.size() + rhsBoundaryStripe.size(), 0));
        Map<Integer, Integer> oldToNew = new HashMap<>();

        for (int i = 0; i < lhsBoundaryStripe.size(); i++) {
            G.setPartitionIndex(lhsBoundaryStripe.get(i), BOUNDARY_STRIPE_NODE);
            newToOldIds.set(idx, lhsBoundaryStripe.get(i));
            oldToNew.put(lhsBoundaryStripe.get(i), idx++);
        }

        for (int i = 0; i < rhsBoundaryStripe.size(); i++) {
            G.setPartitionIndex(rhsBoundaryStripe.get(i), BOUNDARY_STRIPE_NODE);
            newToOldIds.set(idx, rhsBoundaryStripe.get(i));
            oldToNew.put(rhsBoundaryStripe.get(i), idx++);
        }

        List<Integer> outerLhsBoundary = new ArrayList<>();
        List<Integer> outerRhsBoundary = new ArrayList<>();

        regionsNoEdges(G, lhsBoundaryStripe, rhsBoundaryStripe, lhs, rhs, outerLhsBoundary, outerRhsBoundary);

        // If either outer boundary is empty, return false as in the C++ code
        if (outerLhsBoundary.isEmpty() || outerRhsBoundary.isEmpty()) {
            return false;
        }

        int n = lhsBoundaryStripe.size() + rhsBoundaryStripe.size() + 2; // +source and target
        int estimatedEdges = (lhsBoundaryStripe.size() + rhsBoundaryStripe.size()) * 2; // Example estimate
        fG.startConstruction(n, estimatedEdges);

        int source = n - 2;
        int sink = n - 1;
        idx = 0;

        // Add LHS stripe to flow problem
        for (int i = 0; i < lhsBoundaryStripe.size(); i++, idx++) {
            int node = lhsBoundaryStripe.get(i);
            int sourceID = idx;
            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                if (G.getPartitionIndex(G.getEdgeTarget(e)) == BOUNDARY_STRIPE_NODE) {
                    int targetID = oldToNew.get(G.getEdgeTarget(e));
                    fG.newEdge(sourceID, targetID, G.getEdgeWeight(e));
                }
            }
        }

        // Add RHS stripe to flow problem
        for (int i = 0; i < rhsBoundaryStripe.size(); i++, idx++) {
            int node = rhsBoundaryStripe.get(i);
            int sourceID = idx;
            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                if (G.getPartitionIndex(G.getEdgeTarget(e)) == BOUNDARY_STRIPE_NODE) {
                    int targetID = oldToNew.get(G.getEdgeTarget(e));
                    fG.newEdge(sourceID, targetID, G.getEdgeWeight(e));
                }
            }
        }

        // Connect source and target with outer boundary nodes
        int maxCapacity = Integer.MAX_VALUE;
        for (int targetID : outerLhsBoundary) {
            fG.newEdge(source, targetID, maxCapacity);
        }

        for (int sourceID : outerRhsBoundary) {
            fG.newEdge(sourceID, sink, maxCapacity);
        }

        return true; // Successfully completed the conversion
    }


    public int getMinFlowMaxCut(PartitionConfig config,
                                GraphAccess G,
                                int lhs,
                                int rhs,
                                List<Integer> lhsBoundaryStripe,
                                List<Integer> rhsBoundaryStripe,
                                List<Integer> newToOldIds,
                                int initialCut,
                                int rhsPartWeight,
                                int rhsStripeWeight,
                                List<Integer> newRhsNodes) {

        FlowGraph fG = new FlowGraph();
        boolean doSth = convertDS(config, G, lhs, rhs, lhsBoundaryStripe, rhsBoundaryStripe, newToOldIds, fG);

        if (!doSth) return initialCut;

        PushRelabel pr = new PushRelabel();
        int source = fG.numberOfNodes() - 2;
        int sink = fG.numberOfNodes() - 1;
        List<Integer> sourceSet = new ArrayList<>();
        long flowValue = pr.solveMaxFlowMinCut(fG, source, sink, true, sourceSet);

        boolean[] newRhsFlag = new boolean[fG.numberOfNodes()];
        Arrays.fill(newRhsFlag, true);

        for (int i = 0; i < sourceSet.size(); i++) {
            newRhsFlag[sourceSet.get(i)] = false;
        }

        if (!config.isMostBalancedMinimumCuts()) {
            for (int node = 0; node < fG.numberOfNodes(); node++) {
                if (newRhsFlag[node] && node < fG.numberOfNodes() - 2) {
                    newRhsNodes.add(node);
                }
            }
        } else {
            GraphAccess residualGraph = new GraphAccess();
            residualGraph.startConstruction(fG.numberOfNodes(), fG.numberOfEdges());

            for (int node = 0; node < fG.numberOfNodes(); node++) {
                int residualNode = residualGraph.newNode(); // For each node here, create a new node

                if (residualNode < fG.numberOfNodes() - 2) {
                    residualGraph.setNodeWeight(residualNode, G.getNodeWeight(newToOldIds.get(residualNode)));
                }

                for (int e = fG.getFirstEdge(node); e < fG.getFirstInvalidEdge(node); e++) {
                    int target = fG.getEdgeTarget(node, e);
                    if (fG.getEdgeCapacity(node, e) > 0) {
                        if (fG.getEdgeFlow(node, e) < fG.getEdgeCapacity(node, e)) {
                            residualGraph.newEdge(residualNode, target);
                        } else {
                            for (int eBar = fG.getFirstEdge(target); eBar < fG.getFirstInvalidEdge(target); eBar++) {
                                int targetPrime = fG.getEdgeTarget(target, eBar);
                                if (targetPrime == node && fG.getEdgeFlow(target,eBar) > 0) {
                                    residualGraph.newEdge(residualNode, target);
                                }
                            }
                        }
                    }
                }
            }

            residualGraph.setNodeWeight(source, 0);
            residualGraph.setNodeWeight(sink, 0);
            residualGraph.finishConstruction();

            int averagePartitionWeight = (int) Math.ceil((double) config.getWorkLoad() / config.getK());
            int perfectRhsStripeWeight = Math.abs(averagePartitionWeight - rhsPartWeight + rhsStripeWeight);

            MostBalancedMinimumCuts mbmc = new MostBalancedMinimumCuts();
            mbmc.computeGoodBalancedMinCut(residualGraph, config, perfectRhsStripeWeight, newRhsNodes);
        }

        return (int) flowValue;  // Cast the long to int before returning
    }

}