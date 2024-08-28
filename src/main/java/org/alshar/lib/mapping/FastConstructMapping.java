package org.alshar.lib.mapping;
import org.alshar.app.BalanceConfiguration;
import org.alshar.app.Configuration;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.matrix.Matrix;
import org.alshar.lib.partition.GraphPartitioner;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.cycle_improvements.CycleRefinement;
import org.alshar.lib.tools.GraphExtractor;

import java.io.*;
import java.util.*;

public class FastConstructMapping {

    private int mTmpNumNodes;

    public FastConstructMapping() {
    }

    public void constructInitialMappingBottomUp(PartitionConfig config, GraphAccess C, Matrix D, List<Integer> permRank) {
        mTmpNumNodes = C.numberOfNodes();
        constructInitialMappingBottomUpInternal(config, C, D, 0, permRank);
    }

    private void constructInitialMappingBottomUpInternal(PartitionConfig config, GraphAccess C, Matrix D, int idx, List<Integer> permRank) {
        int numParts = C.numberOfNodes() / config.getGroupSizes().get(idx);
        partitionCPerfectlyBalanced(config, C, numParts);

        if (idx == config.getGroupSizes().size() - 1) {
            // Build initial offsets
            int nodesPerBlock = mTmpNumNodes / config.getGroupSizes().get(idx);
            permRank.set(0, 0);
            for (int block = 1; block < permRank.size(); block++) {
                permRank.set(block, permRank.get(block - 1) + nodesPerBlock);
            }
        } else {
            // Contract partitioned graph
            GraphAccess Q = new GraphAccess();
            CompleteBoundary bnd = new CompleteBoundary(C);
            bnd.build();
            bnd.getUnderlyingQuotientGraph(Q);

            List<Integer> recRanks = new ArrayList<>(Collections.nCopies(numParts, 0));
            constructInitialMappingBottomUpInternal(config, Q, D, idx + 1, recRanks);

            // Recompute offsets
            for (int node = 0; node < C.numberOfNodes(); node++) {
                int block = C.getPartitionIndex(node);
                permRank.set(node, recRanks.get(block));
                recRanks.set(block, recRanks.get(block) + C.getNodeWeight(node));
            }
        }
    }

    public void constructInitialMappingTopDown(PartitionConfig config, GraphAccess C, Matrix D, List<Integer> permRank) {
        List<Integer> mMapping = new ArrayList<>(C.numberOfNodes());
        for (int node = 0; node < C.numberOfNodes(); node++) {
            mMapping.add(node);
        }

        constructInitialMappingTopDownInternal(config, C, new ArrayList<>(config.getGroupSizes()), 0, mMapping, permRank);
    }

    private void constructInitialMappingTopDownInternal(PartitionConfig config, GraphAccess C, List<Integer> groupSizes, int startId, List<Integer> mapToOriginal, List<Integer> permRank) {
        int numParts = groupSizes.get(groupSizes.size() - 1);
        if (numParts == 1) {
            if (groupSizes.size() == 1) return;
            groupSizes.remove(groupSizes.size() - 1);
            constructInitialMappingTopDownInternal(config, C, groupSizes, 0, mapToOriginal, permRank);
            return;
        }

        partitionCPerfectlyBalanced(config, C, numParts);

        int nodesPerBlock = C.numberOfNodes() / numParts;
        List<Integer> count = new ArrayList<>(Collections.nCopies(numParts, startId));
        for (int block = 1; block < numParts; block++) {
            count.set(block, count.get(block - 1) + nodesPerBlock);
        }

        int stopNumber = Math.max(2, config.getGroupSizes().size() - config.getMaxRecursionLevelsConstruction());
        if (groupSizes.size() == stopNumber) {
            for (int node = 0; node < C.numberOfNodes(); node++) {
                int block = C.getPartitionIndex(node);
                permRank.set(mapToOriginal.get(node), count.get(block));
                count.set(block, count.get(block) + 1);
            }
        } else {
            // Extract subgraphs and recurse on them
            groupSizes.remove(groupSizes.size() - 1);
            for (int block = 0; block < numParts; block++) {
                GraphExtractor ge = new GraphExtractor();
                GraphAccess Q = new GraphAccess();
                List<Integer> mapping = new ArrayList<>();
                ge.extractBlock(C, Q, block, mapping);

                for (int node = 0; node < Q.numberOfNodes(); node++) {
                    mapping.set(node, mapToOriginal.get(mapping.get(node)));
                }

                constructInitialMappingTopDownInternal(config, Q, groupSizes, count.get(block), mapping, permRank);
            }
        }
    }

    private void partitionCPerfectlyBalanced(PartitionConfig config, GraphAccess C, int blocks) {
        PrintStream originalOut = System.out;
        try (PrintStream nullStream = new PrintStream(new OutputStream() {
            public void write(int b) {
                // NO-OP
            }
        })) {
            System.setOut(nullStream);

            PartitionConfig partitionConfig = new PartitionConfig(config);
            Configuration cfg = new Configuration();
            switch (partitionConfig.getPreconfigurationMapping()) {
                case PRE_CONFIG_MAPPING_FAST:
                    cfg.fast(partitionConfig);
                    break;
                case PRE_CONFIG_MAPPING_ECO:
                    cfg.eco(partitionConfig);
                    break;
                case PRE_CONFIG_MAPPING_STRONG:
                    cfg.strong(partitionConfig);
                    break;
                default:
                    cfg.fast(partitionConfig);
            }

            partitionConfig.setK(blocks);
            partitionConfig.setImbalance(0);
            partitionConfig.setEpsilon(0);

            List<Integer> weights = new ArrayList<>(C.numberOfNodes());
            for (int node = 0; node < C.numberOfNodes(); node++) {
                weights.add(C.getNodeWeight(node));
                C.setNodeWeight(node, 1);
            }

            GraphPartitioner partitioner = new GraphPartitioner();
            BalanceConfiguration bc = new BalanceConfiguration();
            bc.configurateBalance(partitionConfig, C);

            partitioner.performPartitioning(partitionConfig, C);

            CompleteBoundary boundary = new CompleteBoundary(C);
            boundary.build();

            CycleRefinement cr = new CycleRefinement();
            partitionConfig.setUpperBoundPartition((int) Math.ceil(C.numberOfNodes() / (double) partitionConfig.getK()));
            cr.performRefinement(partitionConfig, C, boundary);

            for (int node = 0; node < C.numberOfNodes(); node++) {
                C.setNodeWeight(node, weights.get(node));
            }
        } finally {
            System.setOut(originalOut);
        }
    }
}