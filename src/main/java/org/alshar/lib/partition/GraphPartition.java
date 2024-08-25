package org.alshar.lib.partition;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.enums.EdgeRating;
import org.alshar.lib.enums.StopRule;
import org.alshar.lib.partition.initial_partitioning.InitialPartitioning;
import org.alshar.lib.tools.QualityMetrics;
import org.alshar.lib.tools.RandomFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GraphPartitioner {

    private int globalK;
    private int globalUpperBound;
    private int rndBal;

    public GraphPartitioner() {
    }

    public void performPartitioning(PartitionConfig config, GraphAccess G) {
        if (config.isOnlyFirstLevel()) {
            if (!config.isGraphAlreadyPartitioned()) {
                InitialPartitioning initPart = new InitialPartitioning();
                initPart.performInitialPartitioning(config, G);
            }

            if (!config.isMhNoMh()) {
                CompleteBoundary boundary = new CompleteBoundary(G);
                boundary.build();
                Refinement refine = new MixedRefinement();
                refine.performRefinement(config, G, boundary);
            }
            return;
        }

        if (config.getRepetitions() == 1) {
            singleRun(config, G);
        } else {
            QualityMetrics qm = new QualityMetrics();
            // Currently only for ecosocial
            int bestCut = Integer.MAX_VALUE;
            List<Integer> bestMap = new ArrayList<>(G.numberOfNodes());
            for (int i = 0; i < config.getRepetitions(); i++) {
                for (int node = 0; node < G.numberOfNodes(); node++) {
                    G.setPartitionIndex(node, 0);
                }
                PartitionConfig workingConfig = new PartitionConfig(config);
                singleRun(workingConfig, G);

                int curCut = qm.edgeCut(G);
                if (curCut < bestCut) {
                    for (int node = 0; node < G.numberOfNodes(); node++) {
                        bestMap.add(G.getPartitionIndex(node));
                    }
                    bestCut = curCut;
                }
            }

            for (int node = 0; node < G.numberOfNodes(); node++) {
                G.setPartitionIndex(node, bestMap.get(node));
            }
        }
    }

    public void performRecursivePartitioning(PartitionConfig config, GraphAccess G) {
        globalK = config.getK();
        globalUpperBound = config.getUpperBoundPartition();
        rndBal = RandomFunctions.nextInt(1, 2);
        performRecursivePartitioningInternal(config, G, 0, config.getK() - 1);
    }

    public void performPartitioningKRecHierarchy(PartitionConfig config, GraphAccess G) {
        globalK = config.getK();
        globalUpperBound = config.getUpperBoundPartition();
        rndBal = RandomFunctions.nextInt(1, 2);
        performRecursivePartitioningKModelInternal(config, G, config.getGroupSizes());
    }

    private void performRecursivePartitioningInternal(PartitionConfig config, GraphAccess G, int lb, int ub) {
        G.setPartitionCount(2);

        PartitionConfig bipartConfig = new PartitionConfig(config);
        bipartConfig.setK(2);
        bipartConfig.setStopRule(StopRule.STOP_RULE_MULTIPLE_K);
        bipartConfig.setNumVertStopFactor(100);
        double epsilon = 0;
        bipartConfig.setRebalance(false);
        bipartConfig.setSoftRebalance(true);

        if (config.getK() < 64) {
            epsilon = rndBal / 100.0;
            bipartConfig.setRebalance(false);
            bipartConfig.setSoftRebalance(false);
        } else {
            epsilon = 1 / 100.0;
        }
        if (globalK == 2) {
            epsilon = 3.0 / 100.0;
        }

        bipartConfig.setUpperBoundPartition((int) Math.ceil((1 + epsilon) * config.getWorkLoad() / bipartConfig.getK()));
        bipartConfig.setCornerRefinementEnabled(false);
        bipartConfig.setQuotientGraphRefinementDisabled(false);
        bipartConfig.setRefinementSchedulingAlgorithm(RefinementSchedulingAlgorithm.ACTIVE_BLOCKS);
        bipartConfig.setKwayAdaptiveLimitsBeta(Math.log(G.numberOfNodes()));

        int newUbLhs = (int) Math.floor((lb + ub) / 2.0);
        int newLbRhs = (int) Math.floor((lb + ub) / 2.0 + 1);
        int numBlocksLhs = newUbLhs - lb + 1;
        int numBlocksRhs = ub - newLbRhs + 1;

        if (config.getK() % 2 != 0) {
            bipartConfig.getTargetWeights().clear();
            bipartConfig.getTargetWeights().add((1 + epsilon) * numBlocksLhs / (double) (numBlocksLhs + numBlocksRhs) * config.getWorkLoad());
            bipartConfig.getTargetWeights().add((1 + epsilon) * numBlocksRhs / (double) (numBlocksLhs + numBlocksRhs) * config.getWorkLoad());
            bipartConfig.setInitialBipartitioning(true);
            bipartConfig.setRefinementType(RefinementType.FM); // flows not supported for odd block weights
        } else {
            bipartConfig.getTargetWeights().clear();
            bipartConfig.getTargetWeights().add(bipartConfig.getUpperBoundPartition());
            bipartConfig.getTargetWeights().add(bipartConfig.getUpperBoundPartition());
            bipartConfig.setInitialBipartitioning(false);
        }

        bipartConfig.setGrowTarget((int) Math.ceil(numBlocksLhs / (double) (numBlocksLhs + numBlocksRhs) * config.getWorkLoad()));

        performPartitioning(bipartConfig, G);

        if (config.getK() > 2) {
            GraphExtractor extractor = new GraphExtractor();

            GraphAccess extractedBlockLhs = new GraphAccess();
            GraphAccess extractedBlockRhs = new GraphAccess();
            List<Integer> mappingExtractedToGLhs = new ArrayList<>();
            List<Integer> mappingExtractedToGRhs = new ArrayList<>();

            int weightLhsBlock = 0;
            int weightRhsBlock = 0;

            extractor.extractTwoBlocks(G, extractedBlockLhs, extractedBlockRhs, mappingExtractedToGLhs, mappingExtractedToGRhs, weightLhsBlock, weightRhsBlock);

            PartitionConfig recConfig = new PartitionConfig(config);
            if (numBlocksLhs > 1) {
                recConfig.setK(numBlocksLhs);

                recConfig.setLargestGraphWeight(weightLhsBlock);
                recConfig.setWorkLoad(weightLhsBlock);
                performRecursivePartitioningInternal(recConfig, extractedBlockLhs, lb, newUbLhs);

                for (int node = 0; node < extractedBlockLhs.numberOfNodes(); node++) {
                    G.setPartitionIndex(mappingExtractedToGLhs.get(node), extractedBlockLhs.getPartitionIndex(node));
                }

            } else {
                for (int node = 0; node < extractedBlockLhs.numberOfNodes(); node++) {
                    G.setPartitionIndex(mappingExtractedToGLhs.get(node), lb);
                }
            }

            if (numBlocksRhs > 1) {
                recConfig.setK(numBlocksRhs);
                recConfig.setLargestGraphWeight(weightRhsBlock);
                recConfig.setWorkLoad(weightRhsBlock);
                performRecursivePartitioningInternal(recConfig, extractedBlockRhs, newLbRhs, ub);

                for (int node = 0; node < extractedBlockRhs.numberOfNodes(); node++) {
                    G.setPartitionIndex(mappingExtractedToGRhs.get(node), extractedBlockRhs.getPartitionIndex(node));
                }

            } else {
                for (int node = 0; node < extractedBlockRhs.numberOfNodes(); node++) {
                    G.setPartitionIndex(mappingExtractedToGRhs.get(node), ub);
                }
            }

        } else {
            for (int node = 0; node < G.numberOfNodes(); node++) {
                if (G.getPartitionIndex(node) == 0) {
                    G.setPartitionIndex(node, lb);
                } else {
                    G.setPartitionIndex(node, ub);
                }
            }
        }

        G.setPartitionCount(config.getK());
    }

    private void performRecursivePartitioningKModelInternal(PartitionConfig config, GraphAccess G, List<Integer> groupSizes) {
        int numParts = groupSizes.get(groupSizes.size() - 1);
        if (numParts == 1) {
            if (groupSizes.size() == 1) return;
            groupSizes.remove(groupSizes.size() - 1);
            performRecursivePartitioningKModelInternal(config, G, groupSizes);
            return;
        }

        G.setPartitionCount(numParts);

        PartitionConfig kpartConfig = new PartitionConfig(config);
        kpartConfig.setK(numParts);
        kpartConfig.setStopRule(StopRule.STOP_RULE_MULTIPLE_K);
        kpartConfig.setNumVertStopFactor(100);
        kpartConfig.setWorkLoad(G.numberOfNodes());
        double epsilon = 0;
        kpartConfig.setRebalance(false);
        kpartConfig.setSoftRebalance(true);

        if (config.getK() < 64) {
            epsilon = rndBal / 100.0;
            kpartConfig.setRebalance(false);
            kpartConfig.setSoftRebalance(false);
        } else {
            epsilon = 1 / 100.0;
        }
        if (globalK == 2) {
            epsilon = 3.0 / 100.0;
        }

        kpartConfig.setUpperBoundPartition((int) Math.ceil((1 + epsilon) * G.numberOfNodes() / (double) kpartConfig.getK()));
        kpartConfig.setKwayAdaptiveLimitsBeta(Math.log(G.numberOfNodes()));

        performPartitioning(kpartConfig, G);
        GraphExtractor extractor = new GraphExtractor();
        groupSizes.remove(groupSizes.size() - 1);
        int remainingK = 1;
        for (int size : groupSizes) {
            remainingK *= size;
        }
        if (remainingK > 1) {
            List<Integer> partitionIds = new ArrayList<>(G.numberOfNodes());
            for (int block = 0; block < numParts; block++) {
                GraphExtractor ge = new GraphExtractor();
                GraphAccess Q = new GraphAccess();
                List<Integer> mapping = new ArrayList<>();
                ge.extractBlock(G, Q, block, mapping);
                performRecursivePartitioningKModelInternal(config, Q, groupSizes);

                Q.setPartitionCount(remainingK);
                for (int node = 0; node < Q.numberOfNodes(); node++) {
                    partitionIds.set(mapping.get(node), Q.getPartitionIndex(node) + block * remainingK);
                }
            }
            for (int node = 0; node < G.numberOfNodes(); node++) {
                G.setPartitionIndex(node, partitionIds.get(node));
            }
        }

        G.setPartitionCount(numParts * remainingK);
    }

    private void singleRun(PartitionConfig config, GraphAccess G) {
        for (int i = 1; i <= config.getGlobalCycleIterations(); i++) {
            System.out.println("vcycle " + i + " of " + config.getGlobalCycleIterations());

            if (config.isUseWcycles() || config.isUseFullMultigrid()) {
                WCyclePartitioner wPartitioner = new WCyclePartitioner();
                wPartitioner.performPartitioning(config, G);
            } else {
                Coarsening coarsen = new Coarsening();
                InitialPartitioning initPart = new InitialPartitioning();
                Uncoarsening uncoarsen = new Uncoarsening();

                GraphHierarchy hierarchy = new GraphHierarchy();

                if (config.isModeNodeSeparators()) {
                    int rnd = RandomFunctions.nextInt(0, 3);
                    switch (rnd) {
                        case 0:
                            config.setEdgeRating(EdgeRating.SEPARATOR_MULTX);
                            break;
                        case 1:
                            config.setEdgeRating(EdgeRating.WEIGHT);
                            break;
                        case 2:
                            config.setEdgeRating(EdgeRating.SEPARATOR_MAX);
                            break;
                        case 3:
                            config.setEdgeRating(EdgeRating.SEPARATOR_LOG);
                            break;
                    }
                }

                coarsen.performCoarsening(config, G, hierarchy);
                initPart.performInitialPartitioning(config, hierarchy);
                uncoarsen.performUncoarsening(config, hierarchy);
            }

            config.setGraphAlreadyPartitioned(true);
            config.setBalanceFactor(0);
        }
    }
}

