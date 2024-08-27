package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.cycle_improvements;
import org.alshar.lib.algorithms.CycleSearch;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.BoundaryLookup;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import org.alshar.lib.tools.RandomFunctions;


import java.util.*;

public class AdvancedModels {

    public static long conflicts = 0;

    public AdvancedModels() {
    }

    public boolean computeVertexMovementsRebalanceUltra(PartitionConfig config,
                                                        GraphAccess G,
                                                        CompleteBoundary boundary,
                                                        AugmentedQGraph aqg,
                                                        int steps) {

        GraphAccess GBar = new GraphAccess();
        boundary.getUnderlyingQuotientGraph(GBar);

        aqg.prepare(config, G, GBar, steps);

        List<Boolean> feasibleEdge = new ArrayList<>();
        List<Integer> idMapping = new ArrayList<>();
        int s = 0, t = 0;

        do {
            GraphAccess cycleProblem = new GraphAccess();
            buildRebalanceModel(config, G, GBar, boundary, aqg, feasibleEdge, steps, cycleProblem, s, t, idMapping);

            CycleSearch cs = new CycleSearch();
            List<Integer> path = new ArrayList<>();
            cs.findShortestPath(cycleProblem, s, t, path);

            boolean conflictDetected = handleUltraModelConflicts(config, cycleProblem, boundary, idMapping, feasibleEdge, path, s, aqg, true);

            if (!conflictDetected) {
                performAugmentedMove(config, G, boundary, path, s, t, aqg);
                return true;
            }
        } while (true);
    }

    public boolean computeVertexMovementsRebalance(PartitionConfig config,
                                                   GraphAccess G,
                                                   CompleteBoundary boundary,
                                                   AugmentedQGraph aqg,
                                                   int steps) {

        GraphAccess cycleProblem = new GraphAccess();
        GraphAccess GBar = new GraphAccess();
        boundary.getUnderlyingQuotientGraph(GBar);

        aqg.prepare(config, G, GBar, steps);

        int numberOfNodes = GBar.numberOfNodes() * steps + 2;
        int numberOfEdges = GBar.numberOfEdges() * steps + 2 * numberOfNodes;

        cycleProblem.startConstruction(numberOfNodes, numberOfEdges);
        int s = numberOfNodes - 2;
        int t = numberOfNodes - 1;

        for (int sIdx = 0; sIdx < steps; sIdx++) {
            for (int lhs = 0; lhs < GBar.numberOfNodes(); lhs++) {  // Iterating over nodes
                int curNode = cycleProblem.newNode();
                for (int e = GBar.getFirstEdge(lhs); e < GBar.getFirstInvalidEdge(lhs); e++) {  // Iterating over out-edges
                    int rhs = GBar.getEdgeTarget(e);

                    BoundaryLookup.BoundaryPair bp = new BoundaryLookup.BoundaryPair();
                    bp.k = config.getK();
                    bp.lhs = lhs;
                    bp.rhs = rhs;

                    int loadDifference = sIdx + 1;
                    if (aqg.existsVmovementsOfDiff(bp, loadDifference)) {
                        int eBar = cycleProblem.newEdge(curNode, sIdx * config.getK() + rhs);
                        cycleProblem.setEdgeWeight(eBar, -aqg.getGainOfVmovements(bp, loadDifference));
                    }
                }

                if (boundary.getBlockWeight(lhs) + sIdx < config.getUpperBoundPartition()) {
                    int eBar = cycleProblem.newEdge(curNode, t);
                    cycleProblem.setEdgeWeight(eBar, 0);
                }
            }
        }

        s = cycleProblem.newNode();

        for (int sIdx = 0; sIdx < steps; sIdx++) {
            for (int node = 0; node < GBar.numberOfNodes(); node++) {  // Iterating over nodes again
                if (boundary.getBlockWeight(node) > config.getUpperBoundPartition()) {
                    int e = cycleProblem.newEdge(s, sIdx * config.getK() + node);
                    cycleProblem.setEdgeWeight(e, 0);
                }
            }
        }

        t = cycleProblem.newNode();
        cycleProblem.finishConstruction();

        CycleSearch cs = new CycleSearch();
        List<Integer> path = new ArrayList<>();
        cs.findShortestPath(cycleProblem, s, t, path);

        performAugmentedMove(config, G, boundary, path, s, t, aqg);

        return true;
    }


    public boolean computeVertexMovementsUltraModel(PartitionConfig config,
                                                    GraphAccess G,
                                                    CompleteBoundary boundary,
                                                    AugmentedQGraph aqg,
                                                    int steps,
                                                    boolean zeroWeightCycle) {

        // Step 1: Create an instance of GraphAccess GBar and get the underlying quotient graph from boundary
        GraphAccess GBar = new GraphAccess();
        boundary.getUnderlyingQuotientGraph(GBar);

        // Step 2: Prepare the augmented quotient graph (aqg) if not in zeroWeightCycle mode
        if (!zeroWeightCycle) {
            aqg.prepare(config, G, GBar, steps);
        }

        // Step 3: Initialize variables for the loop
        boolean foundSome;
        List<Boolean> feasibleEdge = new ArrayList<>();
        List<Integer> idMapping = new ArrayList<>();
        int s = 0; // Start vertex

        do {
            // Step 4: Build the ultra model
            GraphAccess cycleProblem = new GraphAccess();
            buildUltraModel(config, G, GBar, boundary, aqg, feasibleEdge, steps, cycleProblem, s, idMapping);

            // Step 5: Initialize cycle search and the cycle container
            CycleSearch cs = new CycleSearch();
            List<Integer> cycle = new ArrayList<>();

            // Step 6: Depending on the zeroWeightCycle flag, find the appropriate cycle
            if (zeroWeightCycle) {
                foundSome = cs.findZeroWeightCycle(cycleProblem, s, cycle);
            } else {
                foundSome = cs.findNegativeCycle(cycleProblem, s, cycle);
            }

            // Step 7: If a cycle was found, handle potential conflicts
            if (foundSome) {
                boolean conflictDetected = handleUltraModelConflicts(config, cycleProblem, boundary, idMapping, feasibleEdge, cycle, s, aqg, true);

                // Step 8: If no conflict was detected, perform the augmented move and return true
                if (!conflictDetected) {
                    performAugmentedMove(config, G, boundary, cycle, s, s, aqg);
                    return true;
                }
            }
        } while (foundSome);

        // Step 9: If no valid move was found after processing all cycles, return false
        return false;
    }


    private boolean buildRebalanceModel(PartitionConfig config,
                                        GraphAccess G,
                                        GraphAccess GBar,
                                        CompleteBoundary boundary,
                                        AugmentedQGraph aqg,
                                        List<Boolean> feasibleEdge,
                                        int steps,
                                        GraphAccess cycleProblem,
                                        int s,
                                        int t,
                                        List<Integer> idMapping) {

        int maxVertexWeightDifference = aqg.getMaxVertexWeightDifference();
        int numberOfNodes = GBar.numberOfNodes() * maxVertexWeightDifference + 2;

        int maxDiffToUB = 0;
        for (int i = 0; i < config.getK(); i++) {
            int diff = config.getUpperBoundPartition() - boundary.getBlockWeight(i);
            if (diff > maxDiffToUB) {
                maxDiffToUB = diff;
            }
        }
        int square = steps * (maxDiffToUB + 1);
        int numberOfEdges = GBar.numberOfEdges() * square + 4 * numberOfNodes;
        s = numberOfNodes - 2;
        t = numberOfNodes - 1;

        if (feasibleEdge.isEmpty()) {
            for (int e = 0; e < numberOfEdges; e++) {
                feasibleEdge.add(true);
            }
        }

        if (idMapping.isEmpty()) {
            for (int i = 0; i < numberOfEdges; i++) {
                idMapping.add(0);
            }
        }

        cycleProblem.startConstruction(numberOfNodes, numberOfEdges);
        int edgeCounter = 0;

        for (int sIdx = 0; sIdx < maxVertexWeightDifference; sIdx++) {
            for (int lhs = 0; lhs < GBar.numberOfNodes(); lhs++) {  // Modified loop
                int curNode = cycleProblem.newNode();
                for (int e = GBar.getFirstEdge(lhs); e < GBar.getFirstInvalidEdge(lhs); e++) {
                    int rhs = GBar.getEdgeTarget(e);

                    BoundaryLookup.BoundaryPair bp = new BoundaryLookup.BoundaryPair();
                    bp.k = config.getK();
                    bp.lhs = lhs;
                    bp.rhs = rhs;

                    int loadDifference = sIdx + 1;
                    if (aqg.existsVmovementsOfDiff(bp, loadDifference)) {
                        int curTarget = sIdx * config.getK() + rhs;
                        int curEdgeWeight = -aqg.getGainOfVmovements(bp, loadDifference);

                        if (feasibleEdge.get(edgeCounter)) {
                            int eBar = cycleProblem.newEdge(curNode, curTarget);
                            cycleProblem.setEdgeWeight(eBar, curEdgeWeight);
                            idMapping.set(eBar, edgeCounter);
                        }
                        edgeCounter++;

                        int curBlockWeight = boundary.getBlockWeight(rhs);
                        if (sIdx != 0) {
                            for (int j = sIdx, possibleOverload = 1; j > 0; j--, possibleOverload++) {
                                curTarget -= config.getK();
                                if (curBlockWeight + possibleOverload < config.getUpperBoundPartition()) {
                                    if (feasibleEdge.get(edgeCounter)) {
                                        int eBar = cycleProblem.newEdge(curNode, curTarget);
                                        cycleProblem.setEdgeWeight(eBar, curEdgeWeight);
                                        idMapping.set(eBar, edgeCounter);
                                    }
                                    edgeCounter++;
                                }
                            }
                        }
                    }
                }

                if (boundary.getBlockWeight(lhs) + sIdx + 1 <= config.getUpperBoundPartition()) {
                    if (feasibleEdge.get(edgeCounter)) {
                        int eBar = cycleProblem.newEdge(curNode, t);
                        cycleProblem.setEdgeWeight(eBar, 0);
                    }
                    edgeCounter++;
                }

                if (sIdx != maxVertexWeightDifference - 1) {
                    if (feasibleEdge.get(edgeCounter)) {
                        int eBar = cycleProblem.newEdge(curNode, curNode + config.getK());
                        cycleProblem.setEdgeWeight(eBar, 0);
                        idMapping.set(eBar, edgeCounter);
                    }
                    edgeCounter++;
                }
            }
        }

        s = cycleProblem.newNode();

        for (int sIdx = 0; sIdx < maxVertexWeightDifference; sIdx++) {
            for (int node = 0; node < GBar.numberOfNodes(); node++) {  // Modified loop
                if (boundary.getBlockWeight(node) > config.getUpperBoundPartition() && feasibleEdge.get(edgeCounter)) {
                    int e = cycleProblem.newEdge(s, sIdx * config.getK() + node);
                    cycleProblem.setEdgeWeight(e, 0);
                    idMapping.set(e, edgeCounter);
                }
                edgeCounter++;
            }
        }

        t = cycleProblem.newNode();
        cycleProblem.finishConstruction();

        return false;
    }


    private boolean buildUltraModel(PartitionConfig config,
                                    GraphAccess G,
                                    GraphAccess GBar,
                                    CompleteBoundary boundary,
                                    AugmentedQGraph aqg,
                                    List<Boolean> feasibleEdge,
                                    int steps,
                                    GraphAccess cycleProblem,
                                    int s,
                                    List<Integer> idMapping) {

        int maxVertexWeightDifference = aqg.getMaxVertexWeightDifference();
        int numberOfNodes = GBar.numberOfNodes() * maxVertexWeightDifference + 1;

        int maxDiffToUB = 0;
        for (int i = 0; i < config.getK(); i++) {
            int diff = config.getUpperBoundPartition() - boundary.getBlockWeight(i);
            if (diff > maxDiffToUB) {
                maxDiffToUB = diff;
            }
        }
        int square = steps * (maxDiffToUB + 1);
        int numberOfEdges = GBar.numberOfEdges() * square + 4 * numberOfNodes;
        s = numberOfNodes - 1;

        if (feasibleEdge.isEmpty()) {
            for (int e = 0; e < numberOfEdges; e++) {
                feasibleEdge.add(true);
            }
        }

        if (idMapping.isEmpty()) {
            for (int i = 0; i < numberOfEdges; i++) {
                idMapping.add(0);
            }
        }

        cycleProblem.startConstruction(numberOfNodes, numberOfEdges);
        int edgeCounter = 0;

        for (int sIdx = 0; sIdx < maxVertexWeightDifference; sIdx++) {
            for (int lhs = 0; lhs < GBar.numberOfNodes(); lhs++) {  // Modified loop
                int curNode = cycleProblem.newNode();
                for (int e = GBar.getFirstEdge(lhs); e < GBar.getFirstInvalidEdge(lhs); e++) {
                    int rhs = GBar.getEdgeTarget(e);

                    BoundaryLookup.BoundaryPair bp = new BoundaryLookup.BoundaryPair();
                    bp.k = config.getK();
                    bp.lhs = lhs;
                    bp.rhs = rhs;

                    int loadDifference = sIdx + 1;
                    if (aqg.existsVmovementsOfDiff(bp, loadDifference)) {
                        int curTarget = sIdx * config.getK() + rhs;
                        int curEdgeWeight = -aqg.getGainOfVmovements(bp, loadDifference);

                        if (feasibleEdge.get(edgeCounter)) {
                            int eBar = cycleProblem.newEdge(curNode, curTarget);
                            cycleProblem.setEdgeWeight(eBar, curEdgeWeight);
                            idMapping.set(eBar, edgeCounter);
                        }
                        edgeCounter++;

                        int curBlockWeight = boundary.getBlockWeight(rhs);
                        if (sIdx != 0) {
                            for (int j = sIdx, possibleOverload = 1; j > 0; j--, possibleOverload++) {
                                curTarget -= config.getK();
                                if (curBlockWeight + possibleOverload < config.getUpperBoundPartition()) {
                                    if (feasibleEdge.get(edgeCounter)) {
                                        int eBar = cycleProblem.newEdge(curNode, curTarget);
                                        cycleProblem.setEdgeWeight(eBar, curEdgeWeight);
                                        idMapping.set(eBar, edgeCounter);
                                    }
                                    edgeCounter++;
                                }
                            }
                        }
                    }
                }

                if (boundary.getBlockWeight(lhs) + sIdx < config.getUpperBoundPartition()) {
                    if (feasibleEdge.get(edgeCounter)) {
                        int eBar = cycleProblem.newEdge(curNode, s);
                        cycleProblem.setEdgeWeight(eBar, 0);
                    }
                    edgeCounter++;
                }

                if (sIdx != maxVertexWeightDifference - 1) {
                    if (feasibleEdge.get(edgeCounter)) {
                        int eBar = cycleProblem.newEdge(curNode, curNode + config.getK());
                        idMapping.set(eBar, edgeCounter);
                        cycleProblem.setEdgeWeight(eBar, 0);
                    }
                    edgeCounter++;
                }
            }
        }

        s = cycleProblem.newNode();

        for (int sIdx = 0; sIdx < maxVertexWeightDifference; sIdx++) {
            for (int node = 0; node < GBar.numberOfNodes(); node++) {  // Modified loop
                if (feasibleEdge.get(edgeCounter)) {
                    int e = cycleProblem.newEdge(s, sIdx * config.getK() + node);
                    idMapping.set(e, edgeCounter);
                    cycleProblem.setEdgeWeight(e, 0);
                }
                edgeCounter++;
            }
        }

        cycleProblem.finishConstruction();

        return false;
    }


    private boolean handleUltraModelConflicts(PartitionConfig config,
                                              GraphAccess cycleProblem,
                                              CompleteBoundary boundary,
                                              List<Integer> idMapping,
                                              List<Boolean> feasibleEdge,
                                              List<Integer> cycle,
                                              int s,
                                              AugmentedQGraph aqg,
                                              boolean removeOnlyBetweenLayers) {

        boolean conflictDetected = cycleOrPathHasConflicts(config, boundary, cycle, s, aqg);
        if (conflictDetected) {
            int blockedEdge;

            if (removeOnlyBetweenLayers) {
                List<Integer> eligibleIdx = new ArrayList<>();
                for (int i = 0; i < cycle.size() - 2; i++) {
                    if (cycle.get(i) < s) {
                        int lhsTmp = cycle.get(i) / config.getK();
                        int rhsTmp = cycle.get(i + 1) / config.getK();
                        if (lhsTmp != rhsTmp) {
                            eligibleIdx.add(i);
                        }
                    }
                }
                blockedEdge = RandomFunctions.nextInt(0, eligibleIdx.size() - 1); // Corrected index range
            } else {
                blockedEdge = RandomFunctions.nextInt(0, cycle.size() - 2);
            }

            for (int node = 0; node < cycleProblem.numberOfNodes(); node++) {
                for (int e = cycleProblem.getFirstEdge(node); e < cycleProblem.getFirstInvalidEdge(node); e++) {
                    int target = cycleProblem.getEdgeTarget(e);
                    if (cycle.get(blockedEdge) == node && cycle.get(blockedEdge + 1) == target) {
                        feasibleEdge.set(idMapping.get(e), false);
                        break;
                    }
                }
            }

            conflicts++;
            return true;
        }

        return false;
    }



    private boolean cycleOrPathHasConflicts(PartitionConfig config,
                                            CompleteBoundary boundary,
                                            List<Integer> cycleOrPath,
                                            int s,
                                            AugmentedQGraph aqg) {

        List<Boolean> blockSeen = new ArrayList<>(Collections.nCopies(config.getK(), false));
        boolean conflictDetected = false;

        for (int i = 0; i < cycleOrPath.size() - 1; i++) {
            if (cycleOrPath.get(i).equals(s)) {
                continue;
            }
            if (cycleOrPath.get(i).equals(cycleOrPath.get(i + 1) + config.getK()) ||
                    cycleOrPath.get(i) + config.getK() == cycleOrPath.get(i + 1)) {
                continue;
            }

            if (cycleOrPath.get(i + 1).equals(s)) {
                if (cycleOrPath.size() > 3) {
                    boolean posFound = false;
                    int rhsPos = i;
                    int curPos = i;
                    int rhsTmp = cycleOrPath.get(i) / config.getK();
                    int numVertForRhs = 0;

                    do {
                        int lhsPos = curPos >= 1 ? curPos - 1 : cycleOrPath.size() - 2;
                        if (lhsPos == rhsPos) break;

                        int lhsTmp = cycleOrPath.get(lhsPos) / config.getK();
                        if (lhsTmp != rhsTmp) {
                            posFound = true;
                            numVertForRhs = lhsTmp + 1;
                            break;
                        } else {
                            if (curPos != 0) {
                                curPos--;
                            } else {
                                curPos = cycleOrPath.size();
                            }
                        }
                    } while (true);

                    if (!posFound) continue;

                    if (!(boundary.getBlockWeight(rhsTmp) + numVertForRhs <= config.getUpperBoundPartition())) {
                        conflictDetected = true;
                        break;
                    }
                }
            }

            int lhsTmp = cycleOrPath.get(i) % config.getK();
            int block = lhsTmp;

            if (blockSeen.get(block)) {
                conflictDetected = true;
                break;
            } else {
                blockSeen.set(block, true);
            }
        }

        if (!conflictDetected) {
            boolean sContained = false;
            for (int i : cycleOrPath) {
                if (i == s) {
                    sContained = true;
                    break;
                }
            }

            if (!sContained) {
                int lhs = 0, rhs = 0;
                int counter = 0;

                for (int i = 0; i < config.getK(); i++) {
                    if (blockSeen.get(i)) {
                        if (counter == 0) {
                            lhs = i;
                        } else if (counter == 1) {
                            rhs = i;
                        }
                        counter++;
                    }
                    if (counter > 2) break;
                }

                if (counter == 2) {
                    int layerLhs = 0, layerRhs = 0;
                    for (int i = 0; i < cycleOrPath.size(); i++) {
                        int tmp = cycleOrPath.get(i) / config.getK();
                        if (tmp == lhs) {
                            if (tmp > layerLhs) {
                                layerLhs = tmp;
                            }
                        } else if (tmp == rhs) {
                            if (tmp > layerRhs) {
                                layerRhs = tmp;
                            }
                        }
                    }
                    conflictDetected = aqg.checkConflict(config, lhs, rhs, layerLhs + 1, layerRhs + 1);
                }
            }
        }

        if (!conflictDetected) {
            int idx = 0;
            for (int i = 0; i < cycleOrPath.size() - 1; i++) {
                if (cycleOrPath.get(i).equals(s)) {
                    idx = i;
                    break;
                }
            }

            int entry;
            if (idx == cycleOrPath.size() - 1) {
                if (!cycleOrPath.get(0).equals(s)) {
                    entry = cycleOrPath.get(0);
                } else {
                    entry = cycleOrPath.get(1);
                }
            } else {
                entry = cycleOrPath.get(idx + 1);
            }

            int blockTmp = entry % config.getK();
            int block = blockTmp;
            int numberOfVerticesToMove = blockTmp + 1;

            for (int i = 0; i < cycleOrPath.size(); i++) {
                int tmp = cycleOrPath.get(i) % config.getK();
                if (tmp == block && (tmp + 1) > numberOfVerticesToMove) {
                    numberOfVerticesToMove = tmp + 1;
                }
            }

            if (boundary.getBlockWeight(block) == numberOfVerticesToMove) {
                conflictDetected = true;
            }
        }

        return conflictDetected;
    }


    private void performAugmentedMove(PartitionConfig config,
                                      GraphAccess G,
                                      CompleteBoundary boundary,
                                      List<Integer> cycleOrPath,
                                      int s, int t,
                                      AugmentedQGraph aqg) {

        for (int i = 0; i < cycleOrPath.size() - 1; i++) {
            if (cycleOrPath.get(i) == s || cycleOrPath.get(i + 1) == s || cycleOrPath.get(i) == t || cycleOrPath.get(i + 1) == t) {
                continue;
            }
            if (cycleOrPath.get(i) == cycleOrPath.get(i + 1) + config.getK() ||
                    cycleOrPath.get(i) + config.getK() == cycleOrPath.get(i + 1)) {
                continue;
            }

            int lhsTmp = cycleOrPath.get(i) / config.getK();
            int rhsTmp = cycleOrPath.get(i + 1) / config.getK();
            int loadDiff = lhsTmp + 1;

            BoundaryLookup.BoundaryPair pair = new BoundaryLookup.BoundaryPair();
            pair.k = config.getK();
            pair.lhs = lhsTmp;
            pair.rhs = rhsTmp;

            List<Integer> verticesOfMove = new ArrayList<>();
            List<Integer> blocksOfMove = new ArrayList<>();
            aqg.getAssociatedVertices(pair, loadDiff, verticesOfMove);
            aqg.getAssociatedBlocks(pair, loadDiff, blocksOfMove);

            int diff = 0;
            for (int j = 0; j < verticesOfMove.size(); j++) {
                int node = verticesOfMove.get(j);

                int from = pair.lhs == blocksOfMove.get(j) ? pair.rhs : pair.lhs;
                int to = blocksOfMove.get(j);

                G.setPartitionIndex(node, to);

                boundary.postMovedBoundaryNodeUpdates(node, pair, true, true);

                int thisNodesWeight = G.getNodeWeight(node);
                boundary.setBlockNoNodes(from, boundary.getBlockNoNodes(from) - 1);
                boundary.setBlockNoNodes(to, boundary.getBlockNoNodes(to) + 1);
                boundary.setBlockWeight(from, boundary.getBlockWeight(from) - thisNodesWeight);
                boundary.setBlockWeight(to, boundary.getBlockWeight(to) + thisNodesWeight);

                diff += thisNodesWeight;
            }
        }
    }
}
