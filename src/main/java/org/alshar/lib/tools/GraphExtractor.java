package org.alshar.lib.tools;

import org.alshar.lib.data_structure.GraphAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphExtractor {

    public GraphExtractor() {
    }

    public void extractBlock(GraphAccess G, GraphAccess extractedBlock, int block, List<Integer> mapping) {
        // Build reverse mapping
        List<Integer> reverseMapping = new ArrayList<>();
        int nodes = 0;
        int dummyNode = G.numberOfNodes() + 1;

        for (int node = 0; node < G.numberOfNodes(); node++) {
            if (G.getPartitionIndex(node) == block) {
                reverseMapping.add(nodes++);
            } else {
                reverseMapping.add(dummyNode);
            }
        }

        extractedBlock.startConstruction(nodes, G.numberOfEdges());

        for (int node = 0; node < G.numberOfNodes(); node++) {
            if (G.getPartitionIndex(node) == block) {
                int newNode = extractedBlock.newNode();
                mapping.add(node);
                extractedBlock.setNodeWeight(newNode, G.getNodeWeight(node));

                for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                    int target = G.getEdgeTarget(e);
                    if (G.getPartitionIndex(target) == block) {
                        int newEdge = extractedBlock.newEdge(newNode, reverseMapping.get(target));
                        extractedBlock.setEdgeWeight(newEdge, G.getEdgeWeight(e));
                    }
                }
            }
        }

        extractedBlock.finishConstruction();
    }

    public void extractTwoBlocks(GraphAccess G, GraphAccess extractedBlockLhs, GraphAccess extractedBlockRhs,
                                 List<Integer> mappingLhs, List<Integer> mappingRhs,
                                 int[] partitionWeights) {

        int lhs = 0;
        int rhs = 1;

        // Build reverse mapping
        List<Integer> reverseMappingLhs = new ArrayList<>();
        List<Integer> reverseMappingRhs = new ArrayList<>();
        int nodesLhs = 0;
        int nodesRhs = 0;
        partitionWeights[0] = 0;  // partitionWeightLhs
        partitionWeights[1] = 0;  // partitionWeightRhs
        int dummyNode = G.numberOfNodes() + 1;

        for (int node = 0; node < G.numberOfNodes(); node++) {
            if (G.getPartitionIndex(node) == lhs) {
                reverseMappingLhs.add(nodesLhs++);
                reverseMappingRhs.add(dummyNode);
                partitionWeights[0] += G.getNodeWeight(node);
            } else {
                reverseMappingRhs.add(nodesRhs++);
                reverseMappingLhs.add(dummyNode);
                partitionWeights[1] += G.getNodeWeight(node);
            }
        }

        extractedBlockLhs.startConstruction(nodesLhs, G.numberOfEdges());
        extractedBlockRhs.startConstruction(nodesRhs, G.numberOfEdges());

        for (int node = 0; node < G.numberOfNodes(); node++) {
            if (G.getPartitionIndex(node) == lhs) {
                int newNode = extractedBlockLhs.newNode();
                mappingLhs.add(node);
                extractedBlockLhs.setNodeWeight(newNode, G.getNodeWeight(node));

                for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                    int target = G.getEdgeTarget(e);
                    if (G.getPartitionIndex(target) == lhs) {
                        int newEdge = extractedBlockLhs.newEdge(newNode, reverseMappingLhs.get(target));
                        extractedBlockLhs.setEdgeWeight(newEdge, G.getEdgeWeight(e));
                    }
                }

            } else {
                int newNode = extractedBlockRhs.newNode();
                mappingRhs.add(node);
                extractedBlockRhs.setNodeWeight(newNode, G.getNodeWeight(node));

                for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                    int target = G.getEdgeTarget(e);
                    if (G.getPartitionIndex(target) == rhs) {
                        int newEdge = extractedBlockRhs.newEdge(newNode, reverseMappingRhs.get(target));
                        extractedBlockRhs.setEdgeWeight(newEdge, G.getEdgeWeight(e));
                    }
                }
            }
        }

        extractedBlockLhs.finishConstruction();
        extractedBlockRhs.finishConstruction();
    }

    public void extractTwoBlocksConnected(GraphAccess G, List<Integer> lhsNodes, List<Integer> rhsNodes,
                                          int lhs, int rhs, GraphAccess pair, List<Integer> mapping) {
        // Build reverse mapping
        Map<Integer, Integer> reverseMapping = new HashMap<>();
        int nodes = 0;
        int edges = 0; // Upper bound for number of edges

        for (int node : lhsNodes) {
            reverseMapping.put(node, nodes);
            edges += G.getNodeDegree(node);
            nodes++;
        }

        for (int node : rhsNodes) {
            reverseMapping.put(node, nodes);
            edges += G.getNodeDegree(node);
            nodes++;
        }

        pair.startConstruction(nodes, edges);

        for (int node : lhsNodes) {
            int newNode = pair.newNode();
            mapping.add(node);

            pair.setNodeWeight(newNode, G.getNodeWeight(node));
            pair.setPartitionIndex(newNode, 0);

            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                int target = G.getEdgeTarget(e);
                if (G.getPartitionIndex(target) == lhs || G.getPartitionIndex(target) == rhs) {
                    int newEdge = pair.newEdge(newNode, reverseMapping.get(target));
                    pair.setEdgeWeight(newEdge, G.getEdgeWeight(e));
                }
            }
        }

        for (int node : rhsNodes) {
            int newNode = pair.newNode();
            mapping.add(node);

            pair.setNodeWeight(newNode, G.getNodeWeight(node));
            pair.setPartitionIndex(newNode, 1);

            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                int target = G.getEdgeTarget(e);
                if (G.getPartitionIndex(target) == lhs || G.getPartitionIndex(target) == rhs) {
                    int newEdge = pair.newEdge(newNode, reverseMapping.get(target));
                    pair.setEdgeWeight(newEdge, G.getEdgeWeight(e));
                }
            }
        }

        pair.finishConstruction();
    }
}
