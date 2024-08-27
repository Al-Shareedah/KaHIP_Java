package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.kway_graph_refinement;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.enums.KWayStopRule;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import org.alshar.lib.tools.RandomFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
public class MultiTryKWayFM {

    private KWayGraphRefinementCommons commons;

    public MultiTryKWayFM() {
        this.commons = null;
    }

    public int performRefinement(PartitionConfig config, GraphAccess G, CompleteBoundary boundary, int rounds, boolean initNeighbors, int alpha) {
        if (commons == null) {
            commons = new KWayGraphRefinementCommons(config);
        }

        double tmpAlpha = config.getKwayAdaptiveLimitsAlpha();
        KWayStopRule tmpStop = config.getKwayStopRule();
        config.setKwayAdaptiveLimitsAlpha(alpha);
        config.setKwayStopRule(KWayStopRule.KWAY_ADAPTIVE_STOP_RULE);

        int overallImprovement = 0;
        for (int i = 0; i < rounds; i++) {
            List<Integer> startNodes = new ArrayList<>();
            boundary.setupStartNodesAll(G, startNodes);
            if (startNodes.size() == 0) {
                return 0; // nothing to refine
            }

            List<Integer> todoList = new ArrayList<>(startNodes);

            Map<Integer, Integer> touchedBlocks = new HashMap<>();
            int improvement = startMoreLocalizedSearch(config, G, boundary, initNeighbors, false, touchedBlocks, todoList);
            if (improvement == 0) break;

            overallImprovement += improvement;
        }

        assert overallImprovement >= 0;

        config.setKwayAdaptiveLimitsAlpha(tmpAlpha);
        config.setKwayStopRule(tmpStop);

        return overallImprovement;
    }

    public int performRefinementAroundParts(PartitionConfig config, GraphAccess G, CompleteBoundary boundary, boolean initNeighbors, int alpha, int lhs, int rhs, Map<Integer, Integer> touchedBlocks) {
        if (commons == null) {
            commons = new KWayGraphRefinementCommons(config);
        }

        double tmpAlpha = config.getKwayAdaptiveLimitsAlpha();
        KWayStopRule tmpStop = config.getKwayStopRule();
        config.setKwayAdaptiveLimitsAlpha(alpha);
        config.setKwayStopRule(KWayStopRule.KWAY_ADAPTIVE_STOP_RULE);
        int overallImprovement = 0;

        for (int i = 0; i < config.getLocalMultitryRounds(); i++) {
            List<Integer> startNodes = new ArrayList<>();
            boundary.setupStartNodesAroundBlocks(G, lhs, rhs, startNodes);

            if (startNodes.size() == 0) {
                return 0; // nothing to refine
            }

            List<Integer> todoList = new ArrayList<>(startNodes);

            int improvement = startMoreLocalizedSearch(config, G, boundary, initNeighbors, true, touchedBlocks, todoList);
            if (improvement == 0) break;

            overallImprovement += improvement;
        }

        config.setKwayAdaptiveLimitsAlpha(tmpAlpha);
        config.setKwayStopRule(tmpStop);

        assert overallImprovement >= 0;
        return overallImprovement;
    }

    private int startMoreLocalizedSearch(PartitionConfig config, GraphAccess G, CompleteBoundary boundary, boolean initNeighbors, boolean computeTouchedBlocks, Map<Integer, Integer> touchedBlocks, List<Integer> todoList) {

        RandomFunctions.permutateVectorGood(todoList, false);
        if (commons == null) {
            commons = new KWayGraphRefinementCommons(config);
        }

        KWayGraphRefinementCore refinementCore = new KWayGraphRefinementCore();
        int localStepLimit = 0;

        VertexMovedHashtable movedIdx = new VertexMovedHashtable();
        int idx = todoList.size() - 1;
        int overallImprovement = 0;

        while (!todoList.isEmpty()) {
            int randomIdx = RandomFunctions.nextInt(0, idx);
            int node = todoList.get(randomIdx);

            int[] maxGainer = new int[1];
            int[] extDeg = new int[1];
            commons.computeGain(G, node, maxGainer, extDeg);

            if (!movedIdx.containsKey(node) && extDeg[0] > 0) {
                List<Integer> realStartNodes = new ArrayList<>();
                realStartNodes.add(node);

                if (initNeighbors) {
                    for (int e = G.getFirstEdge(node), end = G.getFirstInvalidEdge(node); e < end; e++) {
                        int target = G.getEdgeTarget(e);
                        if (!movedIdx.containsKey(target)) {
                            extDeg[0] = 0;
                            commons.computeGain(G, target, maxGainer, extDeg);
                            if (extDeg[0] > 0) {
                                realStartNodes.add(target);
                            }
                        }
                    }
                }

                int improvement = 0;
                if (computeTouchedBlocks) {
                    improvement = refinementCore.singleKWayRefinementRound(config, G, boundary, realStartNodes, localStepLimit, movedIdx, touchedBlocks);
                } else {
                    improvement = refinementCore.singleKWayRefinementRound(config, G, boundary, realStartNodes, localStepLimit, movedIdx);
                }

                overallImprovement += improvement;
            }

            if (movedIdx.size() > 0.05 * G.numberOfNodes()) break;
            Collections.swap(todoList, randomIdx, idx--);
            todoList.remove(todoList.size() - 1);
        }

        return overallImprovement;
    }

}