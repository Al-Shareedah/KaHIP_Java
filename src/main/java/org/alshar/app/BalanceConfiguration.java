package org.alshar.app;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;

public class BalanceConfiguration {

    public BalanceConfiguration() {}

    public void configurateBalance(PartitionConfig partitionConfig, GraphAccess G) {
        int largestGraphWeight = 0;
        for (int node = 0; node < G.numberOfNodes(); node++) {
            largestGraphWeight += G.getNodeWeight(node);
        }

        int edgeWeights = 0;
        if (partitionConfig.balanceEdges && partitionConfig.imbalance != 0) {
            // Balancing edges is disabled for the perfectly balanced case since this case requires uniform node weights
            for (int node = 0; node < G.numberOfNodes(); node++) {
                int weightedDegree = 0;
                for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                    weightedDegree += G.getEdgeWeight(e);
                }

                edgeWeights += weightedDegree;
                G.setNodeWeight(node, G.getNodeWeight(node) + weightedDegree);
            }
        }

        double epsilon = partitionConfig.imbalance / 100.0;
        if (partitionConfig.imbalance == 0 && !partitionConfig.kaffpaE) {
            partitionConfig.setUpperBoundPartition((int) Math.ceil((1 + epsilon + 0.01) * (largestGraphWeight / (double) partitionConfig.getK())));
            partitionConfig.setKaffpaPerfectlyBalance(true);
        } else {
            int load = largestGraphWeight + edgeWeights;
            partitionConfig.setUpperBoundPartition((int) Math.ceil((1 + epsilon) * (load / (double) partitionConfig.getK())));
        }

        partitionConfig.setLargestGraphWeight(largestGraphWeight);
        partitionConfig.setGraphAlreadyPartitioned(false);
        partitionConfig.setKwayAdaptiveLimitsBeta(Math.log(G.numberOfNodes()));
        partitionConfig.setWorkLoad(largestGraphWeight + edgeWeights);

        // System.out.println("block weight upper bound " + partitionConfig.getUpperBoundPartition());
    }
}



