package org.alshar.lib.partition.coarsening;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.enums.MatchingType;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.coarsening.matching.Matching;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Contraction {

    public Contraction() {}

    public void contract(PartitionConfig partitionConfig,
                         GraphAccess finer,
                         GraphAccess coarser,
                         List<Integer> edgeMatching,
                         List<Integer> coarseMapping,
                         int noOfCoarseVertices,
                         List<Integer> permutation) {

        if (partitionConfig.getMatchingType() == MatchingType.CLUSTER_COARSENING) {
            contractClustering(partitionConfig, finer, coarser, edgeMatching, coarseMapping, noOfCoarseVertices, permutation);
            return;
        }

        if (partitionConfig.isCombine()) {
            coarser.resizeSecondPartitionIndex(noOfCoarseVertices);
        }

        List<Integer> newEdgeTargets = new ArrayList<>(finer.numberOfEdges());
        for (int e = 0; e < finer.numberOfEdges(); e++) {
            newEdgeTargets.add(coarseMapping.get(finer.getEdgeTarget(e)));
        }

        List<Integer> edgePositions = new ArrayList<>(Collections.nCopies(noOfCoarseVertices, -1)); // -1 represents UNDEFINED_EDGE

        coarser.startConstruction(noOfCoarseVertices, finer.numberOfEdges());

        int curNoVertices = 0;

        for (int n = 0; n < finer.numberOfNodes(); n++) {
            int node = permutation.get(n);
            if (!coarseMapping.get(node).equals(curNoVertices)) {
                continue;
            }

            int coarseNode = coarser.newNode();
            coarser.setNodeWeight(coarseNode, finer.getNodeWeight(node));

            if (partitionConfig.isCombine()) {
                coarser.setSecondPartitionIndex(coarseNode, finer.getSecondPartitionIndex(node));
            }

            for (int e = finer.getFirstEdge(node); e < finer.getFirstInvalidEdge(node); e++) {
                visitEdge(finer, coarser, edgePositions, coarseNode, e, newEdgeTargets);
            }

            int matchedNeighbor = edgeMatching.get(node);
            if (node != matchedNeighbor) {
                int newCoarseWeight = finer.getNodeWeight(node) + finer.getNodeWeight(matchedNeighbor);
                coarser.setNodeWeight(coarseNode, newCoarseWeight);

                for (int e = finer.getFirstEdge(matchedNeighbor); e < finer.getFirstInvalidEdge(matchedNeighbor); e++) {
                    visitEdge(finer, coarser, edgePositions, coarseNode, e, newEdgeTargets);
                }
            }

            for (int e = coarser.getFirstEdge(coarseNode); e < coarser.getFirstInvalidEdge(coarseNode); e++) {
                edgePositions.set(coarser.getEdgeTarget(e), -1); // Reset edge position to UNDEFINED_EDGE
            }

            curNoVertices++;
        }

        // Assertions and finalization
        assert curNoVertices == noOfCoarseVertices;
        coarser.finishConstruction();
    }

    private void visitEdge(GraphAccess G,
                           GraphAccess coarser,
                           List<Integer> edgePositions,
                           int coarseNode,
                           int e,
                           List<Integer> newEdgeTargets) {

        int newCoarseEdgeTarget = newEdgeTargets.get(e);
        if (newCoarseEdgeTarget == coarseNode) return; // this is the matched edge ... return

        int edgePos = edgePositions.get(newCoarseEdgeTarget);
        if (edgePos == -1) { // UNDEFINED_EDGE
            int coarseEdge = coarser.newEdge(coarseNode, newCoarseEdgeTarget);
            coarser.setEdgeWeight(coarseEdge, G.getEdgeWeight(e));
            edgePositions.set(newCoarseEdgeTarget, coarseEdge);
        } else {
            int newEdgeWeight = coarser.getEdgeWeight(edgePos) + G.getEdgeWeight(e);
            coarser.setEdgeWeight(edgePos, newEdgeWeight);
        }
    }

    public void contractClustering(PartitionConfig partitionConfig,
                                   GraphAccess finer,
                                   GraphAccess coarser,
                                   List<Integer> edgeMatching,
                                   List<Integer> coarseMapping,
                                   int noOfCoarseVertices,
                                   List<Integer> permutation) {

        if (partitionConfig.isCombine()) {
            coarser.resizeSecondPartitionIndex(noOfCoarseVertices);
        }

        List<Integer> partitionMap = new ArrayList<>(finer.numberOfNodes());
        int k = finer.getPartitionCount();

        for (int node = 0; node < finer.numberOfNodes(); node++) {
            partitionMap.add(finer.getPartitionIndex(node));
            finer.setPartitionIndex(node, coarseMapping.get(node));
        }

        finer.setPartitionCount(noOfCoarseVertices);

        CompleteBoundary boundary = new CompleteBoundary(finer);
        boundary.fastComputeQuotientGraph(coarser, noOfCoarseVertices);

        finer.setPartitionCount(k);
        for (int node = 0; node < finer.numberOfNodes(); node++) {
            finer.setPartitionIndex(node, partitionMap.get(node));
            coarser.setPartitionIndex(coarseMapping.get(node), finer.getPartitionIndex(node));

            if (partitionConfig.isCombine()) {
                coarser.setSecondPartitionIndex(coarseMapping.get(node), finer.getSecondPartitionIndex(node));
            }
        }
    }

    public void contractPartitioned(PartitionConfig partitionConfig,
                                    GraphAccess finer,
                                    GraphAccess coarser,
                                    List<Integer> edgeMatching,
                                    List<Integer> coarseMapping,
                                    int noOfCoarseVertices,
                                    List<Integer> permutation) {

        if (partitionConfig.getMatchingType() == MatchingType.CLUSTER_COARSENING) {
            contractClustering(partitionConfig, finer, coarser, edgeMatching, coarseMapping, noOfCoarseVertices, permutation);
            return;
        }

        List<Integer> newEdgeTargets = new ArrayList<>(finer.numberOfEdges());
        for (int e = 0; e < finer.numberOfEdges(); e++) {
            newEdgeTargets.add(coarseMapping.get(finer.getEdgeTarget(e)));
        }

        List<Integer> edgePositions = new ArrayList<>(Collections.nCopies(noOfCoarseVertices, -1)); // UNDEFINED_EDGE

        coarser.setPartitionCount(finer.getPartitionCount());
        coarser.startConstruction(noOfCoarseVertices, finer.numberOfEdges());

        if (partitionConfig.isCombine()) {
            coarser.resizeSecondPartitionIndex(noOfCoarseVertices);
        }

        int curNoVertices = 0;

        for (int n = 0; n < finer.numberOfNodes(); n++) {
            int node = permutation.get(n);
            if (!coarseMapping.get(node).equals(curNoVertices)) {
                continue;
            }

            int coarseNode = coarser.newNode();
            coarser.setNodeWeight(coarseNode, finer.getNodeWeight(node));
            coarser.setPartitionIndex(coarseNode, finer.getPartitionIndex(node));

            if (partitionConfig.isCombine()) {
                coarser.setSecondPartitionIndex(coarseNode, finer.getSecondPartitionIndex(node));
            }

            for (int e = finer.getFirstEdge(node); e < finer.getFirstInvalidEdge(node); e++) {
                visitEdge(finer, coarser, edgePositions, coarseNode, e, newEdgeTargets);
            }

            int matchedNeighbor = edgeMatching.get(node);
            if (node != matchedNeighbor) {
                int newCoarseWeight = finer.getNodeWeight(node) + finer.getNodeWeight(matchedNeighbor);
                coarser.setNodeWeight(coarseNode, newCoarseWeight);

                for (int e = finer.getFirstEdge(matchedNeighbor); e < finer.getFirstInvalidEdge(matchedNeighbor); e++) {
                    visitEdge(finer, coarser, edgePositions, coarseNode, e, newEdgeTargets);
                }
            }

            for (int e = coarser.getFirstEdge(coarseNode); e < coarser.getFirstInvalidEdge(coarseNode); e++) {
                edgePositions.set(coarser.getEdgeTarget(e), -1); // Reset edge position to UNDEFINED_EDGE
            }

            curNoVertices++;
        }

        // Assertions and finalization
        assert curNoVertices == noOfCoarseVertices;
        coarser.finishConstruction();
    }
}