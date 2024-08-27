package org.alshar.lib.partition.coarsening.matching;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.enums.MatchingType;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.tools.RandomFunctions;
import org.alshar.lib.data_structure.CoarseMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class RandomMatching extends Matching {

    public RandomMatching() {
        super();
    }

    @Override
    public void match(PartitionConfig partitionConfig,
                      GraphAccess G,
                      List<Integer> edgeMatching,
                      List<Integer> coarseMapping,
                      int noOfCoarseVertices,
                      List<Integer> permutation) {

        int numberOfNodes = G.numberOfNodes();
        permutation.clear();
        edgeMatching.clear();
        coarseMapping.clear();

        for (int i = 0; i < numberOfNodes; i++) {
            permutation.add(i);
            edgeMatching.add(i, i);
            coarseMapping.add(-1);
        }

        noOfCoarseVertices = 0;

        if (partitionConfig.getMatchingType() != MatchingType.MATCHING_RANDOM_GPA) {
            RandomFunctions.permutateEntries(partitionConfig, permutation, true);
        } else {
            Collections.shuffle(permutation);
        }

        if (partitionConfig.isGraphAlreadyPartitioned()) {
            for (int n : permutation) {
                int curNode = n;
                int curNodeWeight = G.getNodeWeight(curNode);

                if (edgeMatching.get(curNode) == curNode) {
                    int matchingPartner = curNode;
                    for (int e = G.getFirstEdge(curNode); e < G.getFirstInvalidEdge(curNode); e++) {
                        int target = G.getEdgeTarget(e);
                        int coarserWeight = G.getNodeWeight(target) + curNodeWeight;

                        if (edgeMatching.get(target) == target && coarserWeight <= partitionConfig.getMaxVertexWeight()) {
                            if (G.getPartitionIndex(curNode) != G.getPartitionIndex(target)) {
                                continue;
                            }

                            if (partitionConfig.isCombine()) {
                                if (G.getSecondPartitionIndex(curNode) != G.getSecondPartitionIndex(target)) {
                                    continue;
                                }
                            }

                            matchingPartner = target;
                            break;
                        }
                    }

                    coarseMapping.set(matchingPartner, noOfCoarseVertices);
                    coarseMapping.set(curNode, noOfCoarseVertices);

                    edgeMatching.set(matchingPartner, curNode);
                    edgeMatching.set(curNode, matchingPartner);

                    noOfCoarseVertices++;
                }
            }
        } else {
            for (int n : permutation) {
                int curNode = n;
                int curNodeWeight = G.getNodeWeight(curNode);

                if (edgeMatching.get(curNode) == curNode) {
                    int matchingPartner = curNode;
                    for (int e = G.getFirstEdge(curNode); e < G.getFirstInvalidEdge(curNode); e++) {
                        int target = G.getEdgeTarget(e);
                        int coarserWeight = G.getNodeWeight(target) + curNodeWeight;

                        if (edgeMatching.get(target) == target && coarserWeight <= partitionConfig.getMaxVertexWeight()) {
                            matchingPartner = target;
                            break;
                        }
                    }

                    coarseMapping.set(matchingPartner, noOfCoarseVertices);
                    coarseMapping.set(curNode, noOfCoarseVertices);

                    edgeMatching.set(matchingPartner, curNode);
                    edgeMatching.set(curNode, matchingPartner);

                    noOfCoarseVertices++;
                }
            }
        }

        System.out.println("log> no of coarse nodes: " + noOfCoarseVertices);
    }
}