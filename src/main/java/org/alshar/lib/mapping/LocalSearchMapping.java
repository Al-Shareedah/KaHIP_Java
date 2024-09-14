package org.alshar.lib.mapping;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.matrix.Matrix;
import org.alshar.lib.data_structure.Pair;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.tools.QualityMetrics;
import org.alshar.lib.tools.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class LocalSearchMapping {

    private List<Integer> nodeContribution;
    private int totalVolume;
    private QualityMetrics qm;

    public LocalSearchMapping() {
        this.qm = new QualityMetrics();
        this.nodeContribution = new ArrayList<>();
        this.totalVolume = 0;
    }

    public <T extends SearchSpace> void performLocalSearch(Class<T> searchSpaceClass, PartitionConfig config, GraphAccess C, Matrix D, List<Integer> permRank) {
        Timer t = new Timer();
        t.restart();

        // Compute total metric
        totalVolume = 0;
        nodeContribution = new ArrayList<>(Collections.nCopies(C.numberOfNodes(), 0));

        for (int node = 0; node < C.numberOfNodes(); node++) {
            for (int e = C.getFirstEdge(node); e < C.getFirstInvalidEdge(node); e++) {
                int target = C.getEdgeTarget(e);
                int commVol = C.getEdgeWeight(e);
                int permRankNode = permRank.get(node);
                int permRankTarget = permRank.get(target);
                int curVol = commVol * D.getXY(permRankNode, permRankTarget);
                nodeContribution.set(node, nodeContribution.get(node) + curVol);
            }
            totalVolume += nodeContribution.get(node);
        }
        System.out.println("J(C,D,Pi) = " + totalVolume);

        T fss;
        try {
            fss = searchSpaceClass.getDeclaredConstructor(PartitionConfig.class, int.class).newInstance(config, C.numberOfNodes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate search space class", e);
        }

        fss.setGraphRef(C);

        while (!fss.done()) {
            Pair<Integer, Integer> curPair = fss.nextPair();

            int swapLhs = curPair.getFirst();
            int swapRhs = curPair.getSecond();

            if (D.getXY(permRank.get(swapLhs), permRank.get(swapRhs)) == config.getDistances().get(0)) {
                fss.commitStatus(false);
                continue; // skipping swaps inside nodes
            }

            if (!performSingleSwap(C, D, permRank, swapLhs, swapRhs)) {
                fss.commitStatus(false);
            } else {
                fss.commitStatus(true);
            }
        }

        if (totalVolume != qm.totalQap(C, D, permRank)) {
            System.out.println("Objective function mismatch");
            System.exit(0);
        }
    }



    private boolean performSingleSwap(GraphAccess C, Matrix D, List<Integer> permRank, int swapLhs, int swapRhs) {
        int oldVolume = totalVolume;
        int oldLhsContrib = nodeContribution.get(swapLhs);
        int oldRhsContrib = nodeContribution.get(swapRhs);

        // We multiply by two since contributions are on both sides
        totalVolume -= 2 * nodeContribution.get(swapLhs);
        totalVolume -= 2 * nodeContribution.get(swapRhs);

        for (int e = C.getFirstEdge(swapLhs); e < C.getFirstInvalidEdge(swapLhs); e++) {
            int target = C.getEdgeTarget(e);
            if (target == swapRhs) {
                int commVol = C.getEdgeWeight(e);
                int permRankNode = permRank.get(swapLhs);
                int permRankTarget = permRank.get(swapRhs);
                int curVol = commVol * D.getXY(permRankNode, permRankTarget);
                totalVolume += 2 * curVol;
                break;
            }
        }

        nodeContribution.set(swapLhs, 0);
        nodeContribution.set(swapRhs, 0);

        Collections.swap(permRank, swapLhs, swapRhs);
        updateNodeContribution(C, D, permRank, swapLhs, swapRhs);

        totalVolume += 2 * nodeContribution.get(swapLhs);
        totalVolume += 2 * nodeContribution.get(swapRhs);

        for (int e = C.getFirstEdge(swapLhs); e < C.getFirstInvalidEdge(swapLhs); e++) {
            int target = C.getEdgeTarget(e);
            if (target == swapRhs) {
                int commVol = C.getEdgeWeight(e);
                int permRankNode = permRank.get(swapLhs);
                int permRankTarget = permRank.get(swapRhs);
                int curVol = commVol * D.getXY(permRankNode, permRankTarget);
                totalVolume -= 2 * curVol;
                break;
            }
        }

        if (totalVolume < oldVolume) {
            //System.out.println("Log> Improvement: " + totalVolume + " " + oldVolume);
            return true;
        } else {
            Collections.swap(permRank, swapLhs, swapRhs);
            updateNodeContribution(C, D, permRank, swapLhs, swapRhs);
            nodeContribution.set(swapLhs, oldLhsContrib);
            nodeContribution.set(swapRhs, oldRhsContrib);
            totalVolume = oldVolume;
            return false;
        }
    }

    private void updateNodeContribution(GraphAccess C, Matrix D, List<Integer> permRank, int swapLhs, int swapRhs) {
        for (int e = C.getFirstEdge(swapLhs); e < C.getFirstInvalidEdge(swapLhs); e++) {
            int target = C.getEdgeTarget(e);
            int commVol = C.getEdgeWeight(e);
            int permRankNode = permRank.get(swapLhs);
            int permRankTarget = permRank.get(target);
            int curVol = commVol * D.getXY(permRankNode, permRankTarget);
            nodeContribution.set(swapLhs, nodeContribution.get(swapLhs) + curVol);

            if (target != swapRhs) {
                nodeContribution.set(target,
                        nodeContribution.get(target)
                                - commVol * D.getXY(permRank.get(swapRhs), permRankTarget)
                                + curVol);
            }
        }
        for (int e = C.getFirstEdge(swapRhs); e < C.getFirstInvalidEdge(swapRhs); e++) {
            int target = C.getEdgeTarget(e);
            int commVol = C.getEdgeWeight(e);
            int permRankNode = permRank.get(swapRhs);
            int permRankTarget = permRank.get(target);
            int curVol = commVol * D.getXY(permRankNode, permRankTarget);
            nodeContribution.set(swapRhs, nodeContribution.get(swapRhs) + curVol);

            if (target != swapLhs) {
                nodeContribution.set(target,
                        nodeContribution.get(target)
                                - commVol * D.getXY(permRank.get(swapLhs), permRankTarget)
                                + curVol);
            }
        }
    }
}
