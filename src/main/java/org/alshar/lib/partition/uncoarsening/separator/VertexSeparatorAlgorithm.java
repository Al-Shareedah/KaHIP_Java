package org.alshar.lib.partition.uncoarsening.separator;
import org.alshar.lib.algorithms.PushRelabel;
import org.alshar.lib.data_structure.FlowGraph;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.BoundaryLookup;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.PartialBoundary;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.flow_refinement.MostBalancedMinimumCuts;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.quotient_graph_scheduling.QuotientGraphScheduling;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.quotient_graph_scheduling.SimpleQuotientGraphScheduler;
import org.alshar.lib.tools.QualityMetrics;

import java.util.*;

public class VertexSeparatorAlgorithm {

    public VertexSeparatorAlgorithm() {
    }

    public void buildFlowProblem(PartitionConfig config,
                                 GraphAccess G,
                                 List<Integer> lhsNodes,
                                 List<Integer> rhsNodes,
                                 List<Integer> separatorNodes,
                                 FlowGraph rG,
                                 List<Integer> forwardMapping,
                                 int source, int sink) {

        List<Integer> outerLhsBoundaryNodes = new ArrayList<>();
        List<Integer> outerRhsBoundaryNodes = new ArrayList<>();

        for (int node : lhsNodes) {
            G.setPartitionIndex(node, 3);
        }

        for (int node : rhsNodes) {
            G.setPartitionIndex(node, 3);
        }

        for (int node : separatorNodes) {
            G.setPartitionIndex(node, 3);
        }

        for (int node : lhsNodes) {
            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                int target = G.getEdgeTarget(e);
                if (G.getPartitionIndex(target) == 0 || G.getPartitionIndex(target) == 1) {
                    outerLhsBoundaryNodes.add(node);
                    break;
                }
            }
        }

        if (!lhsNodes.isEmpty()) {
            for (int node : separatorNodes) {
                for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                    int target = G.getEdgeTarget(e);
                    if (G.getPartitionIndex(target) == 0) {
                        outerLhsBoundaryNodes.add(node);
                        break;
                    }
                }
            }
        } else {
            outerLhsBoundaryNodes = separatorNodes;
        }

        for (int node : rhsNodes) {
            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                int target = G.getEdgeTarget(e);
                if (G.getPartitionIndex(target) == 0 || G.getPartitionIndex(target) == 1) {
                    outerRhsBoundaryNodes.add(node);
                    break;
                }
            }
        }

        if (!rhsNodes.isEmpty()) {
            for (int node : separatorNodes) {
                for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                    int target = G.getEdgeTarget(e);
                    if (G.getPartitionIndex(target) == 1) {
                        outerRhsBoundaryNodes.add(node);
                        break;
                    }
                }
            }
        } else {
            outerRhsBoundaryNodes = separatorNodes;
        }

        int n = 2 * (lhsNodes.size() + rhsNodes.size() + separatorNodes.size()) + 2; // source and sink

        Map<Integer, Integer> backwardMapping = new HashMap<>();
        forwardMapping.clear();
        forwardMapping.addAll(Collections.nCopies(n - 2, 0));

        int nodeCount = 0;
        for (int v : lhsNodes) {
            backwardMapping.put(v, nodeCount);
            forwardMapping.set(nodeCount++, v);
            forwardMapping.set(nodeCount++, v);
        }

        for (int v : rhsNodes) {
            backwardMapping.put(v, nodeCount);
            forwardMapping.set(nodeCount++, v);
            forwardMapping.set(nodeCount++, v);
        }

        for (int v : separatorNodes) {
            backwardMapping.put(v, nodeCount);
            forwardMapping.set(nodeCount++, v);
            forwardMapping.set(nodeCount++, v);
        }

        source = n - 2;
        sink = n - 1;
        int infinite = Integer.MAX_VALUE / 2;
        rG.startConstruction(n, 0);

        for (int v : outerLhsBoundaryNodes) {
            rG.newEdge(source, backwardMapping.get(v), infinite);
        }

        for (int v : outerRhsBoundaryNodes) {
            rG.newEdge(backwardMapping.get(v) + 1, sink, infinite);
        }

        for (int v : lhsNodes) {
            rG.newEdge(backwardMapping.get(v), backwardMapping.get(v) + 1, G.getNodeWeight(v));
            for (int e = G.getFirstEdge(v); e < G.getFirstInvalidEdge(v); e++) {
                int target = G.getEdgeTarget(e);
                if (G.getPartitionIndex(target) != 3) continue; // not part of the flow problem
                rG.newEdge(backwardMapping.get(target) + 1, backwardMapping.get(v), infinite);
            }
        }

        for (int v : rhsNodes) {
            rG.newEdge(backwardMapping.get(v), backwardMapping.get(v) + 1, G.getNodeWeight(v));
            for (int e = G.getFirstEdge(v); e < G.getFirstInvalidEdge(v); e++) {
                int target = G.getEdgeTarget(e);
                if (G.getPartitionIndex(target) != 3) continue; // not part of the flow problem
                rG.newEdge(backwardMapping.get(target) + 1, backwardMapping.get(v), infinite);
            }
        }

        for (int v : separatorNodes) {
            rG.newEdge(backwardMapping.get(v), backwardMapping.get(v) + 1, G.getNodeWeight(v));
            for (int e = G.getFirstEdge(v); e < G.getFirstInvalidEdge(v); e++) {
                int target = G.getEdgeTarget(e);
                if (G.getPartitionIndex(target) != 3) continue; // not part of the flow problem
                rG.newEdge(backwardMapping.get(target) + 1, backwardMapping.get(v), infinite);
            }
        }

        rG.finishConstruction();
    }

    public int improveVertexSeparator(PartitionConfig config,
                                      GraphAccess G,
                                      List<Integer> blockWeights,
                                      PartialBoundary separator) {

        G.setPartitionCount(3);

        double currentRegionFactor = config.getRegionFactorNodeSeparators();
        double iteration = 0;
        int prevSolutionValue = blockWeights.get(2);

        int solutionValue;
        boolean solutionImbalanced;
        List<Integer> oldBlockWeights = new ArrayList<>(blockWeights);

        do {
            PartitionConfig cfg = new PartitionConfig(config);
            cfg.setRegionFactorNodeSeparators(1 + currentRegionFactor);
            solutionImbalanced = false;

            List<Integer> oldLhs = new ArrayList<>();
            List<Integer> oldRhs = new ArrayList<>();
            List<Integer> oldSep = new ArrayList<>();

            solutionValue = improveVertexSeparatorInternal(cfg, G, blockWeights, separator, oldLhs, oldRhs, oldSep);

            if (solutionValue == prevSolutionValue) {
                int curDiff = Math.abs(blockWeights.get(1) - blockWeights.get(0));
                int oldDiff = Math.abs(oldBlockWeights.get(1) - oldBlockWeights.get(0));
                if (curDiff > oldDiff) {
                    applyVectors(G, oldLhs, oldRhs, oldSep);
                    separator.clear();
                    for (int node : oldSep) {
                        separator.insert(node);
                    }

                    blockWeights.clear();
                    blockWeights.addAll(oldBlockWeights);
                }
                return 0;
            }

            if (blockWeights.get(0) > config.getUpperBoundPartition() || blockWeights.get(1) > config.getUpperBoundPartition()) {
                solutionImbalanced = true;
                currentRegionFactor /= 2;

                applyVectors(G, oldLhs, oldRhs, oldSep);
                separator.clear();
                for (int node : oldSep) {
                    separator.insert(node);
                }

                blockWeights.clear();
                blockWeights.addAll(oldBlockWeights);
            }
            iteration++;
        } while (solutionImbalanced && iteration < 10);

        if (solutionImbalanced) {
            PartitionConfig cfg = new PartitionConfig(config);
            cfg.setRegionFactorNodeSeparators(1);
            solutionValue = improveVertexSeparatorInternal(cfg, G, blockWeights, separator, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        return prevSolutionValue - solutionValue;
    }

    private int improveVertexSeparatorInternal(PartitionConfig config,
                                               GraphAccess G,
                                               List<Integer> blockWeights,
                                               PartialBoundary separator,
                                               List<Integer> lhsNodes,
                                               List<Integer> rhsNodes,
                                               List<Integer> startNodes) {

        int lhsPartWeight = blockWeights.get(0);
        int rhsPartWeight = blockWeights.get(1);
        int separatorWeight = blockWeights.get(2);

        AreaBFS abfs = new AreaBFS();

        // Iterate over boundary nodes using forAllBoundaryNodes
        separator.forAllBoundaryNodes(node -> startNodes.add(node));

        abfs.performBFS(config, G, startNodes, 0, blockWeights, lhsNodes);
        abfs.performBFS(config, G, startNodes, 1, blockWeights, rhsNodes);

        FlowGraph rG = new FlowGraph();
        int source = 0, sink = 0;
        List<Integer> forwardMapping = new ArrayList<>();
        buildFlowProblem(config, G, lhsNodes, rhsNodes, startNodes, rG, forwardMapping, source, sink);

        PushRelabel mfmcSolver = new PushRelabel();
        List<Integer> sourceSet = new ArrayList<>();
        boolean computeSourceSet = !config.isMostBalancedMinimumCutsNodeSep();
        long value = mfmcSolver.solveMaxFlowMinCut(rG, source, sink, computeSourceSet, sourceSet);

        List<Boolean> isInSourceSet = new ArrayList<>(Collections.nCopies(rG.numberOfNodes(), config.isMostBalancedMinimumCutsNodeSep()));
        if (!config.isMostBalancedMinimumCutsNodeSep()) {
            for (int v : sourceSet) {
                isInSourceSet.set(v, true);
            }
        } else {
            GraphAccess residualGraph = new GraphAccess();
            convertResidualGraph(G, forwardMapping, source, sink, rG, residualGraph);

            int rhsStripeWeight = rhsNodes.stream().mapToInt(G::getNodeWeight).sum();

            int overallWeight = lhsPartWeight + separatorWeight + rhsPartWeight;
            int idealNewBlockWeight = (overallWeight - (int) value) / 2;
            int amountToBeAdded = idealNewBlockWeight - rhsPartWeight;
            int perfectRhsStripeWeight = Math.max(2 * amountToBeAdded + (int) value, 0);

            PartitionConfig tmpConfig = new PartitionConfig(config);
            tmpConfig.setModeNodeSeparators(true);

            MostBalancedMinimumCuts mbmc = new MostBalancedMinimumCuts();
            List<Integer> rhsSet = new ArrayList<>();
            mbmc.computeGoodBalancedMinCut(residualGraph, tmpConfig, perfectRhsStripeWeight, rhsSet);

            for (int v : rhsSet) {
                isInSourceSet.set(v, false);
            }
        }

        applyVectors(G, lhsNodes, rhsNodes, startNodes);

        for (int node = 0; node < rG.numberOfNodes(); node++) {
            if (node == sink || node == source) continue;
            int v = forwardMapping.get(node);
            int toSet = isInSourceSet.get(node) ? 0 : 1;
            boolean isFrontierNode = isInSourceSet.get(node) && !isInSourceSet.get(node + 1);
            if (!isFrontierNode) {
                blockWeights.set(G.getPartitionIndex(v), blockWeights.get(G.getPartitionIndex(v)) - G.getNodeWeight(v));
                G.setPartitionIndex(v, toSet);
                blockWeights.set(G.getPartitionIndex(v), blockWeights.get(G.getPartitionIndex(v)) + G.getNodeWeight(v));
            } else {
                blockWeights.set(G.getPartitionIndex(v), blockWeights.get(G.getPartitionIndex(v)) - G.getNodeWeight(v));
            }
            node++;
        }

        separator.clear();
        blockWeights.set(2, (int) value);
        for (int node = 0; node < rG.numberOfNodes(); node++) {
            if (node == sink || node == source) continue;
            if (isInSourceSet.get(node) && !isInSourceSet.get(node + 1)) {
                int v = forwardMapping.get(node);
                separator.insert(v);
                G.setPartitionIndex(v, 2);
            }
            node++;
        }

        return (int) value;
    }

    public int improveVertexSeparator(PartitionConfig config,
                                      GraphAccess G,
                                      List<Integer> inputSeparator,
                                      List<Integer> outputSeparator) {

        List<Integer> currentSolution = new ArrayList<>(G.numberOfNodes());
        for (int node = 0; node < G.numberOfNodes(); node++) {
            currentSolution.add(G.getPartitionIndex(node));
        }

        boolean solutionImbalanced;
        int solutionValue;
        double currentRegionFactor = config.getRegionFactorNodeSeparators();
        double iteration = 0;
        QualityMetrics qm = new QualityMetrics();
        do {
            PartitionConfig cfg = new PartitionConfig(config);
            cfg.setRegionFactorNodeSeparators(1 + currentRegionFactor);
            solutionImbalanced = false;
            solutionValue = improveVertexSeparatorInternal(cfg, G, inputSeparator, outputSeparator);
            G.setPartitionCount(3);
            double balance = qm.balanceSeparator(G);
            if (balance > (1 + config.getEpsilon() / 100.0)) {
                solutionImbalanced = true;
                currentRegionFactor /= 2;

                for (int node = 0; node < G.numberOfNodes(); node++) {
                    G.setPartitionIndex(node, currentSolution.get(node));
                }
            }
            iteration++;
        } while (solutionImbalanced && iteration < 10);

        if (solutionImbalanced) {
            PartitionConfig cfg = new PartitionConfig(config);
            cfg.setRegionFactorNodeSeparators(1);
            solutionValue = improveVertexSeparatorInternal(cfg, G, inputSeparator, outputSeparator);
        }

        return solutionValue;
    }
    public void computeVertexSeparatorSimple(PartitionConfig config,
                                             GraphAccess G,
                                             CompleteBoundary boundary,
                                             List<Integer> overallSeparator) {

        PartitionConfig cfg = new PartitionConfig(config);
        cfg.setBankAccountFactor(1);

        ArrayList<BoundaryLookup.BoundaryPair> qgraphEdges = new ArrayList<>();
        boundary.getQuotientGraphEdges(qgraphEdges);

        if (qgraphEdges.isEmpty()) {
            return;
        }

        QuotientGraphScheduling scheduler = new SimpleQuotientGraphScheduler(cfg, qgraphEdges, qgraphEdges.size());

        Map<Integer, Boolean> alreadySeparator = new HashMap<>();

        do {
            BoundaryLookup.BoundaryPair bp = scheduler.getNext();
            int lhs = bp.getLhs();
            int rhs = bp.getRhs();

            ArrayList<Integer> startNodesLhs = new ArrayList<>();
            ArrayList<Integer> startNodesRhs = new ArrayList<>();

            PartialBoundary lhsB = boundary.getDirectedBoundary(lhs, lhs, rhs);
            PartialBoundary rhsB = boundary.getDirectedBoundary(rhs, lhs, rhs);

            if (lhsB.size() < rhsB.size()) {
                lhsB.forAllBoundaryNodes(curBndNode -> {
                    if (!alreadySeparator.containsKey(curBndNode)) {
                        alreadySeparator.put(curBndNode, true);
                    }
                });
            } else {
                rhsB.forAllBoundaryNodes(curBndNode -> {
                    if (!alreadySeparator.containsKey(curBndNode)) {
                        alreadySeparator.put(curBndNode, true);
                    }
                });
            }

        } while (!scheduler.hasFinished());

        for (Map.Entry<Integer, Boolean> entry : alreadySeparator.entrySet()) {
            overallSeparator.add(entry.getKey());
            G.setPartitionIndex(entry.getKey(), G.getSeparatorBlock());
        }
    }
    public void computeVertexSeparatorSimpler(PartitionConfig config,
                                              GraphAccess G,
                                              CompleteBoundary boundary,
                                              List<Integer> overallSeparator) {

        PartitionConfig cfg = new PartitionConfig(config);
        cfg.setBankAccountFactor(1);

        ArrayList<BoundaryLookup.BoundaryPair> qgraphEdges = new ArrayList<>();
        boundary.getQuotientGraphEdges(qgraphEdges);

        if (qgraphEdges.isEmpty()) {
            return;
        }

        QuotientGraphScheduling scheduler = new SimpleQuotientGraphScheduler(cfg, qgraphEdges, qgraphEdges.size());

        Map<Integer, Boolean> alreadySeparator = new HashMap<>();

        do {
            BoundaryLookup.BoundaryPair bp = scheduler.getNext();
            int lhs = bp.getLhs();
            int rhs = bp.getRhs();

            PartialBoundary lhsB = boundary.getDirectedBoundary(lhs, lhs, rhs);
            PartialBoundary rhsB = boundary.getDirectedBoundary(rhs, lhs, rhs);

            lhsB.forAllBoundaryNodes(curBndNode -> {
                if (!alreadySeparator.containsKey(curBndNode)) {
                    alreadySeparator.put(curBndNode, true);
                }
            });

            rhsB.forAllBoundaryNodes(curBndNode -> {
                if (!alreadySeparator.containsKey(curBndNode)) {
                    alreadySeparator.put(curBndNode, true);
                }
            });

        } while (!scheduler.hasFinished());

        for (Map.Entry<Integer, Boolean> entry : alreadySeparator.entrySet()) {
            overallSeparator.add(entry.getKey());
            G.setPartitionIndex(entry.getKey(), G.getSeparatorBlock());
        }
    }

    private int improveVertexSeparatorInternal(PartitionConfig config,
                                               GraphAccess G,
                                               List<Integer> inputSeparator,
                                               List<Integer> outputSeparator) {

        int lhsPartWeight = 0;
        int rhsPartWeight = 0;
        int separatorWeight = 0;
        for (int node = 0; node < G.numberOfNodes(); node++) {
            if (G.getPartitionIndex(node) == 1) {
                rhsPartWeight += G.getNodeWeight(node);
            } else if (G.getPartitionIndex(node) == 0) {
                lhsPartWeight += G.getNodeWeight(node);
            } else if (G.getPartitionIndex(node) == 2) {
                separatorWeight += G.getNodeWeight(node);
            }
        }

        int oldSeparatorWeight = separatorWeight;

        AreaBFS abfs = new AreaBFS();
        List<Integer> blockWeights = new ArrayList<>(Arrays.asList(lhsPartWeight, rhsPartWeight, oldSeparatorWeight));
        List<Integer> lhsNodes = new ArrayList<>();
        abfs.performBFS(config, G, inputSeparator, 0, blockWeights, lhsNodes);

        List<Integer> rhsNodes = new ArrayList<>();
        abfs.performBFS(config, G, inputSeparator, 1, blockWeights, rhsNodes);

        FlowGraph rG = new FlowGraph();
        int source = 0, sink = 0;
        List<Integer> forwardMapping = new ArrayList<>();
        buildFlowProblem(config, G, lhsNodes, rhsNodes, inputSeparator, rG, forwardMapping, source, sink);

        PushRelabel mfmcSolver = new PushRelabel();
        List<Integer> sourceSet = new ArrayList<>();
        boolean computeSourceSet = !config.isMostBalancedMinimumCutsNodeSep();
        long value = mfmcSolver.solveMaxFlowMinCut(rG, source, sink, computeSourceSet, sourceSet);

        List<Boolean> isInSourceSet = new ArrayList<>(Collections.nCopies(rG.numberOfNodes(), config.isMostBalancedMinimumCutsNodeSep()));
        if (!config.isMostBalancedMinimumCutsNodeSep()) {
            for (int v : sourceSet) {
                isInSourceSet.set(v, true);
            }
        } else {
            GraphAccess residualGraph = new GraphAccess();
            convertResidualGraph(G, forwardMapping, source, sink, rG, residualGraph);

            int rhsStripeWeight = rhsNodes.stream().mapToInt(G::getNodeWeight).sum();

            int overallWeight = lhsPartWeight + separatorWeight + rhsPartWeight;
            int idealNewBlockWeight = (overallWeight - (int) value) / 2;
            int amountToBeAdded = idealNewBlockWeight - rhsPartWeight;
            int perfectRhsStripeWeight = Math.max(2 * amountToBeAdded + (int) value, 0);

            PartitionConfig tmpConfig = new PartitionConfig(config);
            tmpConfig.setModeNodeSeparators(true);

            MostBalancedMinimumCuts mbmc = new MostBalancedMinimumCuts();
            List<Integer> rhsSet = new ArrayList<>();
            mbmc.computeGoodBalancedMinCut(residualGraph, tmpConfig, perfectRhsStripeWeight, rhsSet);

            for (int v : rhsSet) {
                isInSourceSet.set(v, false);
            }
        }

        for (int node = 0; node < rG.numberOfNodes(); node++) {
            if (node == sink || node == source) continue;
            if (isInSourceSet.get(node)) {
                G.setPartitionIndex(forwardMapping.get(node), 0);
            } else {
                G.setPartitionIndex(forwardMapping.get(node), 1);
            }
        }

        outputSeparator.clear();
        for (int node = 0; node < rG.numberOfNodes(); node++) {
            if (node == sink || node == source) continue;
            if (isInSourceSet.get(node) && !isInSourceSet.get(node + 1)) {
                outputSeparator.add(forwardMapping.get(node));
                G.setPartitionIndex(forwardMapping.get(node), 2);
            }
        }

        Map<Integer, Boolean> alreadySeparator = new HashMap<>();
        for (int node : outputSeparator) {
            alreadySeparator.put(node, true);
        }

        // Uncomment the following line if you want to enable separator validation.
        // isVertexSeparator(G, alreadySeparator);

        return oldSeparatorWeight - (int) value;
    }

    public void computeVertexSeparator(PartitionConfig config,
                                       GraphAccess G,
                                       CompleteBoundary boundary,
                                       List<Integer> overallSeparator) {

        PartitionConfig cfg = new PartitionConfig(config);
        cfg.setBankAccountFactor(1);

        Map<Integer, Boolean> alreadySeparator = new HashMap<>();

        ArrayList<BoundaryLookup.BoundaryPair> qgraphEdges = new ArrayList<>();
        boundary.getQuotientGraphEdges(qgraphEdges);

        if (qgraphEdges.isEmpty()) {
            return;
        }

        QuotientGraphScheduling scheduler = new SimpleQuotientGraphScheduler(cfg, qgraphEdges, qgraphEdges.size());

        do {
            BoundaryLookup.BoundaryPair bp = scheduler.getNext();
            int lhs = bp.getLhs();
            int rhs = bp.getRhs();

            ArrayList<Integer> startNodesLhs = new ArrayList<>();
            ArrayList<Integer> startNodesRhs = new ArrayList<>();

            PartialBoundary lhsB = boundary.getDirectedBoundary(lhs, lhs, rhs);
            PartialBoundary rhsB = boundary.getDirectedBoundary(rhs, lhs, rhs);

            lhsB.forAllBoundaryNodes(curBndNode -> {
                if (!alreadySeparator.containsKey(curBndNode)) {
                    startNodesLhs.add(curBndNode);
                }
            });

            rhsB.forAllBoundaryNodes(curBndNode -> {
                if (!alreadySeparator.containsKey(curBndNode)) {
                    startNodesRhs.add(curBndNode);
                }
            });

            VertexSeparatorFlowSolver vsfs = new VertexSeparatorFlowSolver();
            List<Integer> separator = new ArrayList<>();
            vsfs.findSeparator(config, G, lhs, rhs, startNodesLhs, startNodesRhs, separator);
            for (int node : separator) {
                alreadySeparator.put(node, true);
            }
        } while (!scheduler.hasFinished());

        for (Map.Entry<Integer, Boolean> entry : alreadySeparator.entrySet()) {
            overallSeparator.add(entry.getKey());
            G.setPartitionIndex(entry.getKey(), G.getSeparatorBlock());
        }
    }

    private void convertResidualGraph(GraphAccess G, List<Integer> forwardMapping,
                                      int source, int sink,
                                      FlowGraph rG, GraphAccess residualGraph) {

        residualGraph.startConstruction(rG.numberOfNodes(), rG.numberOfEdges());
        for (int node = 0; node < rG.numberOfNodes(); node++) {
            int newNode = residualGraph.newNode(); // for each node here create a new node
            if (node != sink && node != source) {
                residualGraph.setNodeWeight(newNode, G.getNodeWeight(forwardMapping.get(node)));
            }

            for (int e = rG.getFirstEdge(node); e < rG.getFirstInvalidEdge(node); e++) {
                int target = rG.getEdgeTarget(node, e);
                long resCap = rG.getEdgeCapacity(node, e) - rG.getEdgeFlow(node, e);
                if (resCap > 0) {
                    residualGraph.newEdge(node, target);
                } else {
                    int eBar = rG.getReverseEdge(node, e);
                    if (rG.getEdgeFlow(target, eBar) > 0) {
                        residualGraph.newEdge(node, target);
                    }
                }
            }
        }

        residualGraph.setNodeWeight(source, 0);
        residualGraph.setNodeWeight(sink, 0);
        residualGraph.finishConstruction();
    }

    private void applyVectors(GraphAccess G,
                              List<Integer> lhsNodes,
                              List<Integer> rhsNodes,
                              List<Integer> separator) {
        for (int node : lhsNodes) {
            G.setPartitionIndex(node, 0);
        }

        for (int node : rhsNodes) {
            G.setPartitionIndex(node, 1);
        }

        for (int node : separator) {
            G.setPartitionIndex(node, 2);
        }
    }

    private boolean isVertexSeparator(GraphAccess G, Map<Integer, Boolean> separator) {
        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                int target = G.getEdgeTarget(e);
                if (G.getPartitionIndex(node) != G.getPartitionIndex(target)) {

                    // In this case one of them has to be a separator
                    if (!separator.containsKey(node) && !separator.containsKey(target)) {
                        System.out.println("Not a separator! " + node + " " + target);
                        System.out.println("PartitionIndex node: " + G.getPartitionIndex(node));
                        System.out.println("PartitionIndex target: " + G.getPartitionIndex(target));
                        throw new AssertionError("Assertion failed");
                    }
                }
            }
        }
        return true;
    }
}
