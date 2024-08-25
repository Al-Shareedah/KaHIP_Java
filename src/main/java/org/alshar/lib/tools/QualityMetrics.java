package org.alshar.lib.tools;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.Matrix;
import org.alshar.lib.data_structure.UnionFind;
import org.alshar.lib.partition.PartitionConfig;

import java.util.*;
import java.util.stream.Collectors;

public class QualityMetrics {

    public QualityMetrics() {
    }

    public long edgeCut(GraphAccess G) {
        long edgeCut = 0;
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

    public long edgeCut(GraphAccess G, int[] partitionMap) {
        long edgeCut = 0;
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

    public long edgeCut(GraphAccess G, int lhs, int rhs) {
        long edgeCut = 0;
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

    public long edgeCutConnected(GraphAccess G, int[] partitionMap) {
        long edgeCut = 0;
        long sumEW = 0;
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

        Map<Integer, Integer> sizeRight = new HashMap<>();
        for (int node = 0; node < G.numberOfNodes(); node++) {
            sizeRight.put(uf.find(node), 1);
        }

        System.out.println("number of connected components: " + sizeRight.size());
        if (sizeRight.size() == G.getPartitionCount()) {
            return edgeCut / 2;
        } else {
            return edgeCut / 2 + sumEW * sizeRight.size();
        }
    }

    public long maxCommunicationVolume(GraphAccess G, int[] partitionMap) {
        long[] blockVolume = new long[G.getPartitionCount()];
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

    public long maxCommunicationVolume(GraphAccess G) {
        long[] blockVolume = new long[G.getPartitionCount()];
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

    public int boundaryNodes(GraphAccess G) {
        int boundaryNodesCount = 0;
        for (int n = 0; n < G.numberOfNodes(); n++) {
            int partitionIDSource = G.getPartitionIndex(n);
            for (int e = G.getFirstEdge(n); e < G.getFirstInvalidEdge(n); e++) {
                int targetNode = G.getEdgeTarget(e);
                int partitionIDTarget = G.getPartitionIndex(targetNode);

                if (partitionIDSource != partitionIDTarget) {
                    boundaryNodesCount++;
                    break;
                }
            }
        }
        return boundaryNodesCount;
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

    public long objective(PartitionConfig config, GraphAccess G, int[] partitionMap) {
        if (config.isMhOptimizeCommunicationVolume()) {
            return maxCommunicationVolume(G, partitionMap);
        } else if (config.isMhPenaltyForUnconnected()) {
            return edgeCutConnected(G, partitionMap);
        } else {
            return edgeCut(G, partitionMap);
        }
    }

    public long totalQap(GraphAccess C, Matrix D, List<Integer> rankAssign) {
        long totalVolume = 0;
        for (int node = 0; node < C.numberOfNodes(); node++) {
            for (int e = C.getFirstEdge(node); e < C.getFirstInvalidEdge(node); e++) {
                int target = C.getEdgeTarget(e);
                int commVol = C.getEdgeWeight(e);
                int permRankNode = rankAssign.get(node);
                int permRankTarget = rankAssign.get(target);
                totalVolume += commVol * D.getXY(permRankNode, permRankTarget);
            }
        }
        return totalVolume;
    }

    public long totalQap(Matrix C, Matrix D, List<Integer> rankAssign) {
        long totalVolume = 0;
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

