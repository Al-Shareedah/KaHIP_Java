package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.cycle_improvements;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.BoundaryLookup;

import java.util.*;

public class AugmentedQGraph {
    private Map<BoundaryLookup.BoundaryPair, SetPairwiseLocalSearches> aqg;
    private int maxVertexWeightDifference;

    public AugmentedQGraph() {
        this.aqg = new HashMap<>();
        this.maxVertexWeightDifference = 0;
    }

    public void prepare(PartitionConfig config, GraphAccess G, GraphAccess GBar, int steps) {
        maxVertexWeightDifference = 0;

        for (int lhs = 0; lhs < GBar.numberOfNodes(); lhs++) {
            for (int e = GBar.getFirstEdge(lhs); e < GBar.getFirstInvalidEdge(lhs); e++) {
                int rhs = GBar.getEdgeTarget(e);

                BoundaryLookup.BoundaryPair bp = new BoundaryLookup.BoundaryPair();
                bp.k = config.getK();
                bp.lhs = lhs;
                bp.rhs = rhs;

                SetPairwiseLocalSearches localSearches = aqg.get(bp);
                if (localSearches == null || localSearches.localSearches.isEmpty()) continue;

                int maxDifference = Integer.MIN_VALUE;
                for (int i = 0; i < localSearches.localSearches.size(); i++) {
                    PairwiseLocalSearch pls = localSearches.localSearches.get(i);
                    int localSearchSize = pls.vertexMovements.size();
                    pls.loadDifference = new int[localSearchSize];

                    int curDifference = 0;
                    for (int j = 0; j < localSearchSize; j++) {
                        int node = pls.vertexMovements.get(j);
                        if (G.getPartitionIndex(node) == lhs) {
                            curDifference += G.getNodeWeight(node);
                        } else {
                            curDifference -= G.getNodeWeight(node);
                        }
                        pls.loadDifference[j] = curDifference;

                        if (curDifference > maxDifference) {
                            maxDifference = curDifference;
                        }
                    }
                }

                if (maxDifference <= 0) continue;
                if (maxDifference > maxVertexWeightDifference) {
                    maxVertexWeightDifference = maxDifference;
                }

                localSearches.searchToUse = new int[maxDifference];
                localSearches.searchGain = new int[maxDifference];
                localSearches.searchNumMoves = new int[maxDifference];
                Arrays.fill(localSearches.searchToUse, -1);
                Arrays.fill(localSearches.searchGain, Integer.MIN_VALUE);
                Arrays.fill(localSearches.searchNumMoves, -1);

                for (int i = 0; i < localSearches.localSearches.size(); i++) {
                    PairwiseLocalSearch pls = localSearches.localSearches.get(i);
                    for (int j = 0; j < pls.vertexMovements.size(); j++) {
                        int loadDiff = pls.loadDifference[j];
                        if (loadDiff <= 0) continue;

                        int internalIdx = loadDiff - 1;
                        if (localSearches.searchGain[internalIdx] < pls.gains.get(j)) {
                            localSearches.searchNumMoves[internalIdx] = j;
                            localSearches.searchGain[internalIdx] = pls.gains.get(j);
                            localSearches.searchToUse[internalIdx] = i;
                        }
                    }
                }
            }
        }
    }


    public boolean existsVmovementsOfDiff(BoundaryLookup.BoundaryPair bp, int diff) {
        int internalIdx = diff - 1;
        SetPairwiseLocalSearches searches = aqg.get(bp);
        if (searches != null && searches.localSearches.size() > 0) {
            if (searches.searchToUse.length > internalIdx) {
                return searches.searchToUse[internalIdx] != -1;
            }
        }
        return false;
    }

    public int getGainOfVmovements(BoundaryLookup.BoundaryPair bp, int diff) {
        int internalIdx = diff - 1;
        return aqg.get(bp).searchGain[internalIdx];
    }

    public void getAssociatedVertices(BoundaryLookup.BoundaryPair pair, int loadDiff, List<Integer> verticesOfMove) {
        int internalIdx = loadDiff - 1;
        SetPairwiseLocalSearches searches = aqg.get(pair);
        int searchToUse = searches.searchToUse[internalIdx];
        int searchNumMoves = searches.searchNumMoves[internalIdx];
        for (int j = 0; j <= searchNumMoves; j++) {
            verticesOfMove.add(searches.localSearches.get(searchToUse).vertexMovements.get(j));
        }
    }

    public void getAssociatedBlocks(BoundaryLookup.BoundaryPair pair, int loadDiff, List<Integer> blocksOfMove) {
        int internalIdx = loadDiff - 1;
        SetPairwiseLocalSearches searches = aqg.get(pair);
        int searchToUse = searches.searchToUse[internalIdx];
        int searchNumMoves = searches.searchNumMoves[internalIdx];
        for (int j = 0; j <= searchNumMoves; j++) {
            blocksOfMove.add(searches.localSearches.get(searchToUse).blockMovements.get(j));
        }
    }

    public void commitPairwiseLocalSearch(BoundaryLookup.BoundaryPair pair, PairwiseLocalSearch pls) {
        aqg.computeIfAbsent(pair, k -> new SetPairwiseLocalSearches()).localSearches.add(pls);
    }

    public boolean checkConflict(PartitionConfig config, int lhs, int rhs, int forwardLoadDiff, int backwardLoadDiff) {
        BoundaryLookup.BoundaryPair bp = new BoundaryLookup.BoundaryPair();
        bp.k = config.getK();
        bp.lhs = lhs;
        bp.rhs = rhs;

        int internalIdx = forwardLoadDiff - 1;
        SetPairwiseLocalSearches searches = aqg.get(bp);
        int localSearchToUse = searches.searchToUse[internalIdx];
        int node = searches.localSearches.get(localSearchToUse).vertexMovements.get(0);

        bp.lhs = rhs;
        bp.rhs = lhs;
        internalIdx = backwardLoadDiff - 1;
        localSearchToUse = aqg.get(bp).searchToUse[internalIdx];

        return node == aqg.get(bp).localSearches.get(localSearchToUse).vertexMovements.get(0);
    }

    public int getMaxVertexWeightDifference() {
        return maxVertexWeightDifference;
    }
}

