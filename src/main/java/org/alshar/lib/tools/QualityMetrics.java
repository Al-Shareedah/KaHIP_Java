package org.alshar.lib.tools;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.matrix.Matrix;
import org.alshar.lib.data_structure.UnionFind;
import org.alshar.lib.partition.PartitionConfig;

import java.util.*;

public class QualityMetrics {

    public QualityMetrics() {
    }

    public int edgeCut(GraphAccess G) {
        int edgeCut = 0;
        for (int n = 0; n < G.numberOfNodes(); n++) {
            int partitionIDSource = G.getPartitionIndex(n);
            for (int e = G.getFirstEdge(n); e < G.getFirstInvalidEdge(n); e++) {
                int targetNode = G.getEdgeTarget(e);
                int partitionIDTarget = G.getPartitionIndex(targetNode);

                if (partitionIDSource != partitionIDTarget) {
                    edgeCut += G.getEdgeWeight(e);
                }
            }
        }
        return edgeCut / 2;
    }

    public int edgeCut(GraphAccess G, int[] partitionMap) {
        int edgeCut = 0;
        for (int n = 0; n < G.numberOfNodes(); n++) {
            int partitionIDSource = partitionMap[n];
            for (int e = G.getFirstEdge(n); e < G.getFirstInvalidEdge(n); e++) {
                int targetNode = G.getEdgeTarget(e);
                int partitionIDTarget = partitionMap[targetNode];

                if (partitionIDSource != partitionIDTarget) {
                    edgeCut += G.getEdgeWeight(e);
                }
            }
        }
        return edgeCut / 2;
    }

    public int edgeCut(GraphAccess G, int lhs, int rhs) {
        int edgeCut = 0;
        for (int n = 0; n < G.numberOfNodes(); n++) {
            int partitionIDSource = G.getPartitionIndex(n);
            if (partitionIDSource != lhs) continue;
            for (int e = G.getFirstEdge(n); e < G.getFirstInvalidEdge(n); e++) {
                int targetNode = G.getEdgeTarget(e);
                int partitionIDTarget = G.getPartitionIndex(targetNode);

                if (partitionIDTarget == rhs) {
                    edgeCut += G.getEdgeWeight(e);
                }
            }
        }
        return edgeCut;
    }

    public int edgeCutConnected(GraphAccess G, int[] partitionMap) {
        int edgeCut = 0;
        int sumEW = 0;
        for (int n = 0; n < G.numberOfNodes(); n++) {
            int partitionIDSource = partitionMap[n];
            for (int e = G.getFirstEdge(n); e < G.getFirstInvalidEdge(n); e++) {
                int targetNode = G.getEdgeTarget(e);
                int partitionIDTarget = partitionMap[targetNode];

                if (partitionIDSource != partitionIDTarget) {
                    edgeCut += G.getEdgeWeight(e);
                }
                sumEW += G.getEdgeWeight(e);
            }
        }

        UnionFind uf = new UnionFind(G.numberOfNodes());
        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                int target = G.getEdgeTarget(e);
                if (partitionMap[node] == partitionMap[target]) {
                    uf.union(node, target);
                }
            }
        }

        Set<Integer> sizeRight = new HashSet<>();
        for (int node = 0; node < G.numberOfNodes(); node++) {
            sizeRight.add(uf.find(node));
        }

        System.out.println("Number of connected components: " + sizeRight.size());
        if (sizeRight.size() == G.getPartitionCount()) {
            return edgeCut / 2;
        } else {
            return edgeCut / 2 + sumEW * sizeRight.size();
        }
    }

    public int maxCommunicationVolume(GraphAccess G, int[] partitionMap) {
        int[] blockVolume = new int[G.getPartitionCount()];
        for (int node = 0; node < G.numberOfNodes(); node++) {
            int block = partitionMap[node];
            boolean[] blockIncident = new boolean[G.getPartitionCount()];
            blockIncident[block] = true;

            int numIncidentBlocks = 0;

            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                int target = G.getEdgeTarget(e);
                int targetBlock = partitionMap[target];
                if (!blockIncident[targetBlock]) {
                    blockIncident[targetBlock] = true;
                    numIncidentBlocks++;
                }
            }
            blockVolume[block] += numIncidentBlocks;
        }

        return Arrays.stream(blockVolume).max().orElse(0);
    }

    public int minCommunicationVolume(GraphAccess G) {
        int[] blockVolume = new int[G.getPartitionCount()];
        for (int node = 0; node < G.numberOfNodes(); node++) {
            int block = G.getPartitionIndex(node);
            boolean[] blockIncident = new boolean[G.getPartitionCount()];
            blockIncident[block] = true;
            int numIncidentBlocks = 0;

            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                int target = G.getEdgeTarget(e);
                int targetBlock = G.getPartitionIndex(target);
                if (!blockIncident[targetBlock]) {
                    blockIncident[targetBlock] = true;
                    numIncidentBlocks++;
                }
            }
            blockVolume[block] += numIncidentBlocks;
        }

        return Arrays.stream(blockVolume).min().orElse(0);
    }

    public int maxCommunicationVolume(GraphAccess G) {
        int[] blockVolume = new int[G.getPartitionCount()];
        for (int node = 0; node < G.numberOfNodes(); node++) {
            int block = G.getPartitionIndex(node);
            boolean[] blockIncident = new boolean[G.getPartitionCount()];
            blockIncident[block] = true;
            int numIncidentBlocks = 0;

            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                int target = G.getEdgeTarget(e);
                int targetBlock = G.getPartitionIndex(target);
                if (!blockIncident[targetBlock]) {
                    blockIncident[targetBlock] = true;
                    numIncidentBlocks++;
                }
            }
            blockVolume[block] += numIncidentBlocks;
        }

        return Arrays.stream(blockVolume).max().orElse(0);
    }

    public int totalCommunicationVolume(GraphAccess G) {
        int[] blockVolume = new int[G.getPartitionCount()];
        for (int node = 0; node < G.numberOfNodes(); node++) {
            int block = G.getPartitionIndex(node);
            boolean[] blockIncident = new boolean[G.getPartitionCount()];
            blockIncident[block] = true;
            int numIncidentBlocks = 0;

            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                int target = G.getEdgeTarget(e);
                int targetBlock = G.getPartitionIndex(target);
                if (!blockIncident[targetBlock]) {
                    blockIncident[targetBlock] = true;
                    numIncidentBlocks++;
                }
            }
            blockVolume[block] += numIncidentBlocks;
        }

        return Arrays.stream(blockVolume).sum();
    }

    public int boundaryNodes(GraphAccess G) {
        int noOfBoundaryNodes = 0;
        for (int n = 0; n < G.numberOfNodes(); n++) {
            int partitionIDSource = G.getPartitionIndex(n);

            for (int e = G.getFirstEdge(n); e < G.getFirstInvalidEdge(n); e++) {
                int targetNode = G.getEdgeTarget(e);
                int partitionIDTarget = G.getPartitionIndex(targetNode);

                if (partitionIDSource != partitionIDTarget) {
                    noOfBoundaryNodes++;
                    break;
                }
            }
        }
        return noOfBoundaryNodes;
    }

    public double balanceSeparator(GraphAccess G) {
        int[] partWeights = new int[G.getPartitionCount()];

        double overallWeight = 0;

        for (int n = 0; n < G.numberOfNodes(); n++) {
            int curPartition = G.getPartitionIndex(n);
            partWeights[curPartition] += G.getNodeWeight(n);
            overallWeight += G.getNodeWeight(n);
        }

        double balancePartWeight = Math.ceil(overallWeight / (G.getPartitionCount() - 1));
        double curMax = -1;

        int separatorBlock = G.getSeparatorBlock();
        for (int p = 0; p < G.getPartitionCount(); p++) {
            if (p == separatorBlock) continue;
            double cur = partWeights[p];
            if (cur > curMax) {
                curMax = cur;
            }
        }

        return curMax / balancePartWeight;
    }

    public int separatorWeight(GraphAccess G) {
        int separatorSize = 0;
        int separatorID = G.getSeparatorBlock();
        for (int node = 0; node < G.numberOfNodes(); node++) {
            if (G.getPartitionIndex(node) == separatorID) {
                separatorSize += G.getNodeWeight(node);
            }
        }

        return separatorSize;
    }

    public double balance(GraphAccess G) {
        int[] partWeights = new int[G.getPartitionCount()];

        double overallWeight = 0;

        for (int n = 0; n < G.numberOfNodes(); n++) {
            int curPartition = G.getPartitionIndex(n);
            partWeights[curPartition] += G.getNodeWeight(n);
            overallWeight += G.getNodeWeight(n);
        }

        double balancePartWeight = Math.ceil(overallWeight / G.getPartitionCount());
        double curMax = -1;

        for (int p = 0; p < G.getPartitionCount(); p++) {
            double cur = partWeights[p];
            if (cur > curMax) {
                curMax = cur;
            }
        }

        return curMax / balancePartWeight;
    }

    public double edgeBalance(GraphAccess G, List<Integer> edgePartition) {
        int[] partWeights = new int[G.getPartitionCount()];

        double overallWeight = 0;

        for (int e = 0; e < G.numberOfEdges(); e++) {
            int curPartition = edgePartition.get(e);
            partWeights[curPartition]++;
            overallWeight++;
        }

        double balancePartWeight = Math.ceil(overallWeight / G.getPartitionCount());
        double curMax = -1;

        for (int p = 0; p < G.getPartitionCount(); p++) {
            double cur = partWeights[p];
            if (cur > curMax) {
                curMax = cur;
            }
        }

        return curMax / balancePartWeight;
    }

    public double balanceEdges(GraphAccess G) {
        int[] partWeights = new int[G.getPartitionCount()];

        double overallWeight = 0;

        for (int n = 0; n < G.numberOfNodes(); n++) {
            int curPartition = G.getPartitionIndex(n);
            partWeights[curPartition] += G.getNodeDegree(n);
            overallWeight += G.getNodeDegree(n);
        }

        double balancePartWeight = Math.ceil(overallWeight / G.getPartitionCount());
        double curMax = -1;

        for (int p = 0; p < G.getPartitionCount(); p++) {
            double cur = partWeights[p];
            if (cur > curMax) {
                curMax = cur;
            }
        }

        return curMax / balancePartWeight;
    }

    public int objective(PartitionConfig config, GraphAccess G, int[] partitionMap) {
        if (config.isMhOptimizeCommunicationVolume()) {
            return maxCommunicationVolume(G, partitionMap);
        } else if (config.isMhPenaltyForUnconnected()) {
            return edgeCutConnected(G, partitionMap);
        } else {
            return edgeCut(G, partitionMap);
        }
    }

    public int totalQap(GraphAccess C, Matrix D, List<Integer> rankAssign) {
        int totalVolume = 0;
        for (int node = 0; node < C.numberOfNodes(); node++) {
            for (int e = C.getFirstEdge(node); e < C.getFirstInvalidEdge(node); e++) {
                int target = C.getEdgeTarget(e);
                int commVol = C.getEdgeWeight(e);
                int permRankNode = rankAssign.get(node);
                int permRankTarget = rankAssign.get(target);
                int curVol = commVol * D.getXY(permRankNode, permRankTarget);
                totalVolume += curVol;
            }
        }
        return totalVolume;
    }

    public int totalQap(Matrix C, Matrix D, List<Integer> rankAssign) {
        int totalVolume = 0;
        for (int i = 0; i < C.getXDim(); i++) {
            for (int j = 0; j < C.getYDim(); j++) {
                int permRankNode = rankAssign.get(i);
                int permRankTarget = rankAssign.get(j);
                totalVolume += C.getXY(i, j) * D.getXY(permRankNode, permRankTarget);
            }
        }
        return totalVolume;
    }
}

