package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.flow_refinement;
import org.alshar.lib.algorithms.StronglyConnectedComponents;
import org.alshar.lib.algorithms.TopologicalSort;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;

import java.util.*;
public class MostBalancedMinimumCuts {

    public MostBalancedMinimumCuts() {
    }

    public void computeGoodBalancedMinCut(GraphAccess residualGraph,
                                          PartitionConfig config,
                                          int perfectRhsWeight,
                                          List<Integer> newRhsNodes) {

        StronglyConnectedComponents scc = new StronglyConnectedComponents();
        int[] components = new int[residualGraph.numberOfNodes()];
        int compCount = scc.strongComponents(residualGraph, components);

        List<List<Integer>> compNodes = new ArrayList<>(compCount);
        int[] compWeights = new int[compCount];

        for (int i = 0; i < compCount; i++) {
            compNodes.add(new ArrayList<>());
        }

        for (int node = 0; node < residualGraph.numberOfNodes(); node++) {
            compNodes.get(components[node]).add(node);
            compWeights[components[node]] += residualGraph.getNodeWeight(node);
        }

        int s = residualGraph.numberOfNodes() - 2;
        int t = residualGraph.numberOfNodes() - 1;
        int compOfS = components[s];
        int compOfT = components[t];

        GraphAccess sccGraph = new GraphAccess();
        buildInternalSccGraph(residualGraph, components, compCount, sccGraph);

        List<Integer> compForRhs = new ArrayList<>();
        computeNewRhs(sccGraph, config, compWeights, compOfS, compOfT, perfectRhsWeight, compForRhs);

        if (!config.modeNodeSeparators) {
            for (int curComponent : compForRhs) {
                if (curComponent != compOfS && curComponent != compOfT) {
                    newRhsNodes.addAll(compNodes.get(curComponent));
                }
            }
        } else {
            for (int curComponent : compForRhs) {
                if (curComponent != compOfS) {
                    for (int node : compNodes.get(curComponent)) {
                        if (node != t) {
                            newRhsNodes.add(node);
                        }
                    }
                }
            }
        }
    }

    private void computeNewRhs(GraphAccess sccGraph,
                               PartitionConfig config,
                               int[] compWeights,
                               int compOfS,
                               int compOfT,
                               int optimalRhsStripeWeight,
                               List<Integer> compForRhs) {

        boolean[] validToAdd = new boolean[sccGraph.numberOfNodes()];
        Arrays.fill(validToAdd, true);

        Queue<Integer> nodeQueue = new LinkedList<>();
        nodeQueue.add(compOfS);
        validToAdd[compOfS] = false;

        while (!nodeQueue.isEmpty()) {
            int node = nodeQueue.poll();
            for (int e = sccGraph.getFirstEdge(node); e < sccGraph.getFirstInvalidEdge(node); e++) {
                int target = sccGraph.getEdgeTarget(e);
                if (validToAdd[target]) {
                    validToAdd[target] = false;
                    nodeQueue.add(target);
                }
            }
        }

        List<Integer> tmpCompForRhs = new ArrayList<>();
        int bestDiff = Integer.MAX_VALUE;

        for (int i = 0; i < config.toposortIterations; i++) {
            TopologicalSort ts = new TopologicalSort();
            List<Integer> sortedSequence = new ArrayList<>();
            ts.sort(sccGraph, sortedSequence);

            tmpCompForRhs.clear();

            boolean tContained = false;
            int curRhsWeight = 0;
            int diff = Integer.MAX_VALUE;

            for (int curComponent : sortedSequence) {
                if (curComponent == compOfT) {
                    tContained = true;
                    tmpCompForRhs.add(curComponent);
                    continue;
                }

                if (validToAdd[curComponent]) {
                    int tmpDiff = optimalRhsStripeWeight - curRhsWeight - compWeights[curComponent];
                    boolean wouldBreak = tmpDiff <= 0 && tContained;
                    if (!wouldBreak) {
                        tmpCompForRhs.add(curComponent);
                        curRhsWeight += compWeights[curComponent];
                    } else {
                        if (Math.abs(tmpDiff) < Math.abs(diff)) {
                            tmpCompForRhs.add(curComponent);
                            curRhsWeight += compWeights[curComponent];
                            diff = optimalRhsStripeWeight - curRhsWeight;
                        }
                        break;
                    }
                }

                diff = optimalRhsStripeWeight - curRhsWeight;
                if (diff <= 0 && tContained) {
                    break;
                }
            }

            if (Math.abs(diff) < bestDiff) {
                bestDiff = Math.abs(diff);
                compForRhs.clear();
                compForRhs.addAll(tmpCompForRhs);
            }
        }
    }

    private void buildInternalSccGraph(GraphAccess residualGraph,
                                       int[] components,
                                       int compCount,
                                       GraphAccess sccGraph) {

        List<Set<Integer>> edges = new ArrayList<>(compCount);
        for (int i = 0; i < compCount; i++) {
            edges.add(new HashSet<>());
        }

        int edgeCount = 0;

        for (int node = 0; node < residualGraph.numberOfNodes(); node++) {
            for (int e = residualGraph.getFirstEdge(node); e < residualGraph.getFirstInvalidEdge(node); e++) {
                int target = residualGraph.getEdgeTarget(e);
                if (components[node] != components[target]) {
                    edges.get(components[node]).add(components[target]);
                    edgeCount++;
                }
            }
        }

        sccGraph.startConstruction(compCount, edgeCount);
        for (int i = 0; i < compCount; i++) {
            int node = sccGraph.newNode();
            for (int target : edges.get(i)) {
                sccGraph.newEdge(node, target);
            }
        }

        sccGraph.finishConstruction();
    }
}
