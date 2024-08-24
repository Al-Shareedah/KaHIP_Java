package org.alshar.app;

import org.alshar.lib.enums.*;
import org.alshar.lib.partition.PartitionConfig;

import java.util.Arrays;

public class Configuration {

    public Configuration() {
    }

    public void strong(PartitionConfig partitionConfig) {
        standard(partitionConfig);
        partitionConfig.setMatchingType(MatchingType.MATCHING_GPA);
        partitionConfig.setPermutationQuality(PermutationQuality.PERMUTATION_QUALITY_GOOD);
        partitionConfig.setPermutationDuringRefinement(PermutationQuality.PERMUTATION_QUALITY_GOOD);
        partitionConfig.setEdgeRatingTiebreaking(true);
        partitionConfig.setFmSearchLimit(5);
        partitionConfig.setBankAccountFactor(3);
        partitionConfig.setEdgeRating(EdgeRating.EXPANSIONSTAR2);
        partitionConfig.setRefinementSchedulingAlgorithm(RefinementSchedulingAlgorithm.REFINEMENT_SCHEDULING_ACTIVE_BLOCKS_REF_KWAY);
        partitionConfig.setRefinementType(RefinementType.REFINEMENT_TYPE_FM_FLOW);
        partitionConfig.setGlobalCycleIterations(2);
        partitionConfig.setFlowRegionFactor(8);
        partitionConfig.setCornerRefinementEnabled(true);
        partitionConfig.setKwayStopRule(KWayStopRule.KWAY_ADAPTIVE_STOP_RULE);
        partitionConfig.setKwayAdaptiveLimitsAlpha(10);
        partitionConfig.setKwayRounds(10);
        partitionConfig.setRateFirstLevelInnerOuter(true);
        partitionConfig.setUseWcycles(false);
        partitionConfig.setNoNewInitialPartitioning(true);
        partitionConfig.setUseFullMultigrid(true);
        partitionConfig.setMostBalancedMinimumCuts(true);
        partitionConfig.setLocalMultitryFmAlpha(10);
        partitionConfig.setLocalMultitryRounds(10);
        partitionConfig.setMhInitialPopulationFraction(10);
        partitionConfig.setMhFlipCoin(1);

        // Adjust based on mode
        if (!partitionConfig.isModeNodeSeparators()) {
            partitionConfig.setEpsilon(3);
            partitionConfig.setImbalance(3);
        } else {
            partitionConfig.setEpsilon(20);
            partitionConfig.setImbalance(20);
        }


        partitionConfig.setInitialPartitioningType(InitialPartitioningType.INITIAL_PARTITIONING_RECPARTITION);
        partitionConfig.setBipartitionTries(4);
        partitionConfig.setMinipreps(4);
        partitionConfig.setInitialPartitioningRepetitions(64);
        partitionConfig.setStrong(true);
    }

    public void eco(PartitionConfig partitionConfig) {
        standard(partitionConfig);
        partitionConfig.setEco(true);
        partitionConfig.setAggressiveRandomLevels(Math.max(2, (int) (7 - Math.log(partitionConfig.getK()) / Math.log(2))));
        partitionConfig.setKwayRounds(Math.min(5, (int) (Math.log(partitionConfig.getK()) / Math.log(2))));
        partitionConfig.setMatchingType(MatchingType.MATCHING_RANDOM_GPA);
        partitionConfig.setPermutationQuality(PermutationQuality.PERMUTATION_QUALITY_NONE);
        partitionConfig.setPermutationDuringRefinement(PermutationQuality.PERMUTATION_QUALITY_GOOD);
        partitionConfig.setEdgeRating(EdgeRating.EXPANSIONSTAR2);
        partitionConfig.setFmSearchLimit(1);
        partitionConfig.setRefinementType(RefinementType.REFINEMENT_TYPE_FM_FLOW);
        partitionConfig.setFlowRegionFactor(2);
        partitionConfig.setCornerRefinementEnabled(true);
        partitionConfig.setKwayStopRule(KWayStopRule.KWAY_SIMPLE_STOP_RULE);
        partitionConfig.setKwayFmSearchLimit(1);
        partitionConfig.setMhInitialPopulationFraction(50);
        partitionConfig.setMhFlipCoin(1);
        partitionConfig.setInitialPartitioningType(InitialPartitioningType.INITIAL_PARTITIONING_RECPARTITION);
        partitionConfig.setBipartitionTries(4);
        partitionConfig.setMinipreps(4);
        partitionConfig.setInitialPartitioningRepetitions(16);
    }

    public void fast(PartitionConfig partitionConfig) {
        standard(partitionConfig);
        partitionConfig.setFast(true);

        if (partitionConfig.getK() > 8) {
            partitionConfig.setQuotientGraphRefinementDisabled(true);
            partitionConfig.setKwayFmSearchLimit(0);
            partitionConfig.setKwayStopRule(KWayStopRule.KWAY_SIMPLE_STOP_RULE);
            partitionConfig.setCornerRefinementEnabled(true);
        } else {
            partitionConfig.setCornerRefinementEnabled(false);
        }

        partitionConfig.setPermutationQuality(PermutationQuality.PERMUTATION_QUALITY_FAST);
        partitionConfig.setPermutationDuringRefinement(PermutationQuality.PERMUTATION_QUALITY_NONE);
        partitionConfig.setMatchingType(MatchingType.MATCHING_RANDOM_GPA);
        partitionConfig.setAggressiveRandomLevels(4);
        partitionConfig.setRefinementSchedulingAlgorithm(RefinementSchedulingAlgorithm.REFINEMENT_SCHEDULING_FAST);
        partitionConfig.setEdgeRating(EdgeRating.EXPANSIONSTAR2);
        partitionConfig.setFmSearchLimit(0);
        partitionConfig.setBankAccountFactor(1);

        partitionConfig.setInitialPartitioningType(InitialPartitioningType.INITIAL_PARTITIONING_RECPARTITION);
        partitionConfig.setBipartitionTries(4);
        partitionConfig.setMinipreps(1);
        partitionConfig.setInitialPartitioningRepetitions(0);
    }

    // Additional methods like strong_separator, eco_separator, fast_separator,
    // standardsnw, fastsocial, ecosocial, strongsocial, etc. can be similarly translated

    public void standard(PartitionConfig partitionConfig) {
        partitionConfig.setFilenameOutput("");
        partitionConfig.setUseMmapIo(false);
        partitionConfig.setSeed(0);
        partitionConfig.setFast(false);
        partitionConfig.setModeNodeSeparators(false);
        partitionConfig.setEco(false);
        partitionConfig.setStrong(false);
        partitionConfig.setFirstLevelRandomMatching(false);
        partitionConfig.setInitialPartitioningRepetitions(5);
        partitionConfig.setEdgeRatingTiebreaking(false);
        partitionConfig.setEdgeRating(EdgeRating.WEIGHT);
        partitionConfig.setMatchingType(MatchingType.MATCHING_RANDOM);
        partitionConfig.setPermutationQuality(PermutationQuality.PERMUTATION_QUALITY_FAST);
        partitionConfig.setInitialPartitioning(false);
        partitionConfig.setInitialPartitioningType(InitialPartitioningType.INITIAL_PARTITIONING_RECPARTITION);
        partitionConfig.setBipartitionTries(9);
        partitionConfig.setMinipreps(10);
        partitionConfig.setEnableOmp(false);
        partitionConfig.setCombine(false);

        if (!partitionConfig.isModeNodeSeparators()) {
            partitionConfig.setEpsilon(3);
            partitionConfig.setImbalance(3);
        } else {
            partitionConfig.setEpsilon(20);
            partitionConfig.setImbalance(20);
        }


        partitionConfig.setBuffoon(false);
        partitionConfig.setBalanceEdges(false);
        partitionConfig.setTimeLimit(0);
        partitionConfig.setMhPoolSize(5);
        partitionConfig.setMhPlainRepetitions(false);
        partitionConfig.setNoUnsucReps(10);
        partitionConfig.setLocalPartitioningRepetitions(1);

        partitionConfig.setMhDisableNcCombine(false);
        partitionConfig.setMhDisableCrossCombine(false);
        partitionConfig.setMhDisableCombine(false);
        partitionConfig.setMhEnableQuickstart(false);
        partitionConfig.setMhDisableDiversifyIslands(false);
        partitionConfig.setMhDiversify(true);
        partitionConfig.setMhDiversifyBest(false);
        partitionConfig.setMhCrossCombineOriginalK(false);
        partitionConfig.setMhEnableTournamentSelection(true);
        partitionConfig.setMhInitialPopulationFraction(10);
        partitionConfig.setMhFlipCoin(1);
        partitionConfig.setMhPrintLog(false);
        partitionConfig.setMhPenaltyForUnconnected(false);
        partitionConfig.setMhNoMh(false);
        partitionConfig.setMhOptimizeCommunicationVolume(false);
        partitionConfig.setUseBucketQueues(true);
        partitionConfig.setWalshawMhRepetitions(50);
        partitionConfig.setScalingFactor(1);
        partitionConfig.setScaleBack(false);
        partitionConfig.setInitialPartitionOptimizeFmLimits(20);
        partitionConfig.setInitialPartitionOptimizeMultitryFmAlpha(20);
        partitionConfig.setInitialPartitionOptimizeMultitryRounds(100);
        partitionConfig.setSuppressPartitionerOutput(false);

        if (partitionConfig.getK() <= 4) {
            partitionConfig.setBipartitionPostFmLimits(30);
            partitionConfig.setBipartitionPostMlLimits(6);
        } else {
            partitionConfig.setBipartitionPostFmLimits(25);
            partitionConfig.setBipartitionPostMlLimits(5);
        }

        partitionConfig.setDisableMaxVertexWeightConstraint(false);
        partitionConfig.setPermutationDuringRefinement(PermutationQuality.PERMUTATION_QUALITY_GOOD);
        partitionConfig.setFmSearchLimit(5);
        partitionConfig.setUseBucketQueues(false);
        partitionConfig.setBankAccountFactor(1.5);
        partitionConfig.setRefinementSchedulingAlgorithm(RefinementSchedulingAlgorithm.REFINEMENT_SCHEDULING_ACTIVE_BLOCKS);
        partitionConfig.setRateFirstLevelInnerOuter(false);
        partitionConfig.setMatchIslands(false);
        partitionConfig.setRefinementType(RefinementType.REFINEMENT_TYPE_FM);
        partitionConfig.setFlowRegionFactor(4.0f);
        partitionConfig.setAggressiveRandomLevels(3);
        partitionConfig.setRefinedBubbling(true);
        partitionConfig.setCornerRefinementEnabled(false);
        partitionConfig.setBubblingIterations(1);
        partitionConfig.setKwayRounds(1);
        partitionConfig.setQuotientGraphRefinementDisabled(false);
        partitionConfig.setKwayFmSearchLimit(3);
        partitionConfig.setGlobalCycleIterations(1);
        partitionConfig.setSoftRebalance(false);
        partitionConfig.setRebalance(false);
        partitionConfig.setUseWcycles(false);
        partitionConfig.setStopRule(StopRule.STOP_RULE_SIMPLE);
        partitionConfig.setNumVertStopFactor(20);
        partitionConfig.setLevelSplit(2);
        partitionConfig.setNoNewInitialPartitioning(true);
        partitionConfig.setOmitGivenPartitioning(false);
        partitionConfig.setUseFullMultigrid(false);
        partitionConfig.setKwayStopRule(KWayStopRule.KWAY_SIMPLE_STOP_RULE);
        partitionConfig.setKwayAdaptiveLimitsAlpha(1.0f);
        partitionConfig.setMaxFlowIterations(10);
        partitionConfig.setNoChangeConvergence(false);
        partitionConfig.setComputeVertexSeparator(false);
        partitionConfig.setToposortIterations(4);
        partitionConfig.setInitialPartitionOptimize(false);
        partitionConfig.setMostBalancedMinimumCuts(false);
        partitionConfig.setMostBalancedMinimumCutsNodeSep(false);
        partitionConfig.setGpaGrowPathsBetweenBlocks(true);

        partitionConfig.setBipartitionAlgorithm(BipartitionAlgorithm.BIPARTITION_BFS);
        partitionConfig.setLocalMultitryRounds(1);
        partitionConfig.setLocalMultitryFmAlpha(10);

        partitionConfig.setOnlyFirstLevel(false);
        partitionConfig.setUseBalanceSingletons(true);

        partitionConfig.setDisableHardRebalance(false);
        partitionConfig.setAmgIterations(5);
        partitionConfig.setKaffpaPerfectlyBalance(false);
        partitionConfig.setKaffpaE(false);

        partitionConfig.setRemoveNegativeCycles(false);
        partitionConfig.setCycleRefinementAlgorithm(CycleRefinementAlgorithm.CYCLE_REFINEMENT_ALGORITHM_ULTRA_MODEL);
        partitionConfig.setKabaEInternalBal(0.01f);
        partitionConfig.setKabaPackingIterations(20);
        partitionConfig.setKabaFlipPackings(false);
        partitionConfig.setKabaLsearchP(MLSRule.NOCOIN_RNDTIE);
        partitionConfig.setKaffpaPerfectlyBalancedRefinement(false);
        partitionConfig.setKabaEnableZeroWeightCycles(true);
        partitionConfig.setMhEnableGalCombine(false);
        partitionConfig.setMhEasyConstruction(false);
        partitionConfig.setFasterNs(false);

        partitionConfig.setMaxT(100);
        partitionConfig.setMaxIter(500000);

        if (partitionConfig.getK() <= 8) {
            partitionConfig.setKabaInternalNoAugStepsAug(15);
        } else {
            partitionConfig.setKabaInternalNoAugStepsAug(7);
        }

        partitionConfig.setKabaUnsuccIterations(6);
        partitionConfig.setInitialBipartitioning(false);
        partitionConfig.setKabapE(false);

        // Social networking parameters
        partitionConfig.setClusterCoarseningFactor(18);
        partitionConfig.setEnsembleClusterings(false);
        partitionConfig.setLabelIterations(10);
        partitionConfig.setLabelIterationsRefinement(25);
        partitionConfig.setNumberOfClusterings(1);
        partitionConfig.setLabelPropagationRefinement(false);
        partitionConfig.setBalanceFactor(0);
        partitionConfig.setClusterCoarseningDuringIp(false);
        partitionConfig.setSetUpperbound(true);
        partitionConfig.setRepetitions(1);
        partitionConfig.setNodeOrdering(NodeOrderingType.DEGREE_NODEORDERING);

        // Node separator parameters
        partitionConfig.setMaxFlowImprovSteps(5);
        partitionConfig.setMaxInitialNsTries(25);
        partitionConfig.setRegionFactorNodeSeparators(0.5f);
        partitionConfig.setSepFlowsDisabled(false);
        partitionConfig.setSepFmDisabled(false);
        partitionConfig.setSepGreedyDisabled(true);
        partitionConfig.setSepFmUnsuccSteps(2000);
        partitionConfig.setSepNumFmReps(200);
        partitionConfig.setSepLocFmDisabled(false);
        partitionConfig.setSepLocFmNoSnodes(20);
        partitionConfig.setSepLocFmUnsuccSteps(50);
        partitionConfig.setSepNumLocFmReps(25);
        partitionConfig.setSepNumVertStop(8000);
        partitionConfig.setSepFullBoundaryIp(false);
        partitionConfig.setSepEdgeRatingDuringIp(EdgeRating.SEPARATOR_MULTX);

        partitionConfig.setEnableMapping(false);
        partitionConfig.setLsNeighborhood(LsNeighborhoodType.COMMUNICATIONGRAPH);
        partitionConfig.setCommunicationNeighborhoodDist(10);
        partitionConfig.setConstructionAlgorithm(ConstructionAlgorithm.MAP_CONST_FASTHIERARCHY_TOPDOWN);
        partitionConfig.setDistanceConstructionAlgorithm(DistanceConstructionAlgorithm.DIST_CONST_HIERARCHY);
        partitionConfig.setSearchSpaceS(64);
        partitionConfig.setPreconfigurationMapping(PreConfigMapping.PRE_CONFIG_MAPPING_ECO);
        partitionConfig.setMaxRecursionLevelsConstruction(Integer.MAX_VALUE);

        partitionConfig.getGroupSizes().add(4);
        partitionConfig.getGroupSizes().add(8);
        partitionConfig.getGroupSizes().add(8);

        partitionConfig.getDistances().add(1);
        partitionConfig.getDistances().add(10);
        partitionConfig.getDistances().add(100);

        partitionConfig.setConvergenceFactor(1);
        partitionConfig.setReductionOrder(Arrays.asList(
                NestedDissectionReductionType.SIMPLICIAL_NODES,
                NestedDissectionReductionType.DEGREE_2_NODES
        ));

        partitionConfig.setDissectionRecLimit(120);
        partitionConfig.setDisableReductions(false);

        partitionConfig.setIlpMinGain(-1);
        partitionConfig.setIlpBfsDepth(2);
        partitionConfig.setIlpOverlapPresets(OverlapPresets.NOEQUAL);
        partitionConfig.setIlpLimitNonzeroes(5000000);
        partitionConfig.setIlpOverlapRuns(3);
        partitionConfig.setIlpTimeout(7200);
    }

    // Additional methods for other configuration types (strongsocial_separator, etc.) can be similarly translated
}
