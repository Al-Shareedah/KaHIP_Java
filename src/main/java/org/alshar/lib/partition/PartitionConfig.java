package org.alshar.lib.partition;
import java.util.ArrayList;
import java.util.List;

import org.alshar.lib.enums.*;

public class PartitionConfig {


    public PartitionConfig() {
        this.groupSizes = new ArrayList<>();
        this.distances = new ArrayList<>();
    }
    // Copy constructor
    public PartitionConfig(PartitionConfig other) {
        this.useMmapIo = other.useMmapIo;

        // Copying Matching related fields
        this.edgeRatingTiebreaking = other.edgeRatingTiebreaking;
        this.edgeRating = other.edgeRating;
        this.permutationQuality = other.permutationQuality;
        this.matchingType = other.matchingType;
        this.matchIslands = other.matchIslands;
        this.firstLevelRandomMatching = other.firstLevelRandomMatching;
        this.rateFirstLevelInnerOuter = other.rateFirstLevelInnerOuter;
        this.maxVertexWeight = other.maxVertexWeight;
        this.largestGraphWeight = other.largestGraphWeight;
        this.workLoad = other.workLoad;
        this.aggressiveRandomLevels = other.aggressiveRandomLevels;
        this.disableMaxVertexWeightConstraint = other.disableMaxVertexWeightConstraint;

        // Initial Partitioning fields
        this.initialPartitioningRepetitions = other.initialPartitioningRepetitions;
        this.minipreps = other.minipreps;
        this.refinedBubbling = other.refinedBubbling;
        this.initialPartitioningType = other.initialPartitioningType;
        this.initialPartitionOptimize = other.initialPartitionOptimize;
        this.bipartitionAlgorithm = other.bipartitionAlgorithm;
        this.initialPartitioning = other.initialPartitioning;
        this.bipartitionTries = other.bipartitionTries;
        this.bipartitionPostFmLimits = other.bipartitionPostFmLimits;
        this.bipartitionPostMlLimits = other.bipartitionPostMlLimits;

        // Refinement parameters
        this.cornerRefinementEnabled = other.cornerRefinementEnabled;
        this.useBucketQueues = other.useBucketQueues;
        this.refinementType = other.refinementType;
        this.permutationDuringRefinement = other.permutationDuringRefinement;
        this.imbalance = other.imbalance;
        this.bubblingIterations = other.bubblingIterations;
        this.kwayRounds = other.kwayRounds;
        this.quotientGraphRefinementDisabled = other.quotientGraphRefinementDisabled;
        this.kwayStopRule = other.kwayStopRule;
        this.kwayAdaptiveLimitsAlpha = other.kwayAdaptiveLimitsAlpha;
        this.kwayAdaptiveLimitsBeta = other.kwayAdaptiveLimitsBeta;
        this.maxFlowIterations = other.maxFlowIterations;
        this.localMultitryRounds = other.localMultitryRounds;
        this.localMultitryFmAlpha = other.localMultitryFmAlpha;
        this.graphAlreadyPartitioned = other.graphAlreadyPartitioned;
        this.fmSearchLimit = other.fmSearchLimit;
        this.kwayFmSearchLimit = other.kwayFmSearchLimit;
        this.upperBoundPartition = other.upperBoundPartition;
        this.bankAccountFactor = other.bankAccountFactor;
        this.refinementSchedulingAlgorithm = other.refinementSchedulingAlgorithm;
        this.mostBalancedMinimumCuts = other.mostBalancedMinimumCuts;
        this.mostBalancedMinimumCutsNodeSep = other.mostBalancedMinimumCutsNodeSep;
        this.toposortIterations = other.toposortIterations;
        this.softRebalance = other.softRebalance;
        this.rebalance = other.rebalance;
        this.flowRegionFactor = other.flowRegionFactor;
        this.gpaGrowPathsBetweenBlocks = other.gpaGrowPathsBetweenBlocks;

        // Global search parameters
        this.globalCycleIterations = other.globalCycleIterations;
        this.useWcycles = other.useWcycles;
        this.useFullMultigrid = other.useFullMultigrid;
        this.levelSplit = other.levelSplit;
        this.noNewInitialPartitioning = other.noNewInitialPartitioning;
        this.omitGivenPartitioning = other.omitGivenPartitioning;
        this.stopRule = other.stopRule;
        this.numVertStopFactor = other.numVertStopFactor;
        this.noChangeConvergence = other.noChangeConvergence;

        // Perfectly balanced partitioning
        this.removeNegativeCycles = other.removeNegativeCycles;
        this.kabaIncludeRemovalOfPaths = other.kabaIncludeRemovalOfPaths;
        this.kabaEnableZeroWeightCycles = other.kabaEnableZeroWeightCycles;
        this.kabaEInternalBal = other.kabaEInternalBal;
        this.cycleRefinementAlgorithm = other.cycleRefinementAlgorithm;
        this.kabaInternalNoAugStepsAug = other.kabaInternalNoAugStepsAug;
        this.kabaPackingIterations = other.kabaPackingIterations;
        this.kabaFlipPackings = other.kabaFlipPackings;
        this.kabaLsearchP = other.kabaLsearchP;
        this.kaffpaPerfectlyBalancedRefinement = other.kaffpaPerfectlyBalancedRefinement;
        this.kabaUnsuccIterations = other.kabaUnsuccIterations;

        // PAR_PSEUDOMH / MH
        this.timeLimit = other.timeLimit;
        this.epsilon = other.epsilon;
        this.noUnsucReps = other.noUnsucReps;
        this.localPartitioningRepetitions = other.localPartitioningRepetitions;
        this.mhPlainRepetitions = other.mhPlainRepetitions;
        this.mhEasyConstruction = other.mhEasyConstruction;
        this.mhEnableGalCombine = other.mhEnableGalCombine;
        this.mhNoMh = other.mhNoMh;
        this.mhPrintLog = other.mhPrintLog;
        this.mhFlipCoin = other.mhFlipCoin;
        this.mhInitialPopulationFraction = other.mhInitialPopulationFraction;
        this.mhDisableCrossCombine = other.mhDisableCrossCombine;
        this.mhCrossCombineOriginalK = other.mhCrossCombineOriginalK;
        this.mhDisableNcCombine = other.mhDisableNcCombine;
        this.mhDisableCombine = other.mhDisableCombine;
        this.mhEnableQuickstart = other.mhEnableQuickstart;
        this.mhDisableDiversifyIslands = other.mhDisableDiversifyIslands;
        this.mhDiversify = other.mhDiversify;
        this.mhDiversifyBest = other.mhDiversifyBest;
        this.mhEnableTournamentSelection = other.mhEnableTournamentSelection;
        this.mhOptimizeCommunicationVolume = other.mhOptimizeCommunicationVolume;
        this.mhNumNcsToCompute = other.mhNumNcsToCompute;
        this.mhPoolSize = other.mhPoolSize;
        this.combine = other.combine;
        this.initialPartitionOptimizeFmLimits = other.initialPartitionOptimizeFmLimits;
        this.initialPartitionOptimizeMultitryFmAlpha = other.initialPartitionOptimizeMultitryFmAlpha;
        this.initialPartitionOptimizeMultitryRounds = other.initialPartitionOptimizeMultitryRounds;
        this.walshawMhRepetitions = other.walshawMhRepetitions;
        this.scalingFactor = other.scalingFactor;
        this.scaleBack = other.scaleBack;
        this.suppressPartitionerOutput = other.suppressPartitionerOutput;
        this.maxT = other.maxT;
        this.maxIter = other.maxIter;

        // Buffoon
        this.disableHardRebalance = other.disableHardRebalance;
        this.buffoon = other.buffoon;
        this.kabapE = other.kabapE;
        this.mhPenaltyForUnconnected = other.mhPenaltyForUnconnected;

        // Miscellaneous
        this.inputPartition = other.inputPartition;
        this.seed = other.seed;
        this.fast = other.fast;
        this.eco = other.eco;
        this.strong = other.strong;
        this.kaffpaE = other.kaffpaE;
        this.balanceEdges = other.balanceEdges;
        this.k = other.k;
        this.computeVertexSeparator = other.computeVertexSeparator;
        this.onlyFirstLevel = other.onlyFirstLevel;
        this.useBalanceSingletons = other.useBalanceSingletons;
        this.amgIterations = other.amgIterations;
        this.graphFilename = other.graphFilename;
        this.filenameOutput = other.filenameOutput;
        this.kaffpaPerfectlyBalance = other.kaffpaPerfectlyBalance;
        this.modeNodeSeparators = other.modeNodeSeparators;

        // SNW Partitioning
        this.nodeOrdering = other.nodeOrdering;
        this.clusterCoarseningFactor = other.clusterCoarseningFactor;
        this.ensembleClusterings = other.ensembleClusterings;
        this.labelIterations = other.labelIterations;
        this.labelIterationsRefinement = other.labelIterationsRefinement;
        this.numberOfClusterings = other.numberOfClusterings;
        this.labelPropagationRefinement = other.labelPropagationRefinement;
        this.balanceFactor = other.balanceFactor;
        this.clusterCoarseningDuringIp = other.clusterCoarseningDuringIp;
        this.setUpperbound = other.setUpperbound;
        this.repetitions = other.repetitions;

        // Node Separator
        this.maxFlowImprovSteps = other.maxFlowImprovSteps;
        this.maxInitialNsTries = other.maxInitialNsTries;
        this.regionFactorNodeSeparators = other.regionFactorNodeSeparators;
        this.sepFlowsDisabled = other.sepFlowsDisabled;
        this.sepFmDisabled = other.sepFmDisabled;
        this.sepLocFmDisabled = other.sepLocFmDisabled;
        this.sepLocFmNoSnodes = other.sepLocFmNoSnodes;
        this.sepGreedyDisabled = other.sepGreedyDisabled;
        this.sepFmUnsuccSteps = other.sepFmUnsuccSteps;
        this.sepLocFmUnsuccSteps = other.sepLocFmUnsuccSteps;
        this.sepNumFmReps = other.sepNumFmReps;
        this.sepNumLocFmReps = other.sepNumLocFmReps;
        this.sepNumVertStop = other.sepNumVertStop;
        this.sepFullBoundaryIp = other.sepFullBoundaryIp;
        this.fasterNs = other.fasterNs;
        this.sepEdgeRatingDuringIp = other.sepEdgeRatingDuringIp;

        // Label Propagation
        this.clusterUpperbound = other.clusterUpperbound;

        // Initial Partitioning
        this.targetWeights = new ArrayList<>(other.targetWeights);
        this.initialBipartitioning = other.initialBipartitioning;
        this.growTarget = other.growTarget;

        // ILP Local Search
        this.ilpMode = other.ilpMode;
        this.ilpMinGain = other.ilpMinGain;
        this.ilpBfsDepth = other.ilpBfsDepth;
        this.ilpBfsMinGain = other.ilpBfsMinGain;
        this.ilpOverlapPresets = other.ilpOverlapPresets;
        this.ilpLimitNonzeroes = other.ilpLimitNonzeroes;
        this.ilpOverlapRuns = other.ilpOverlapRuns;
        this.ilpTimeout = other.ilpTimeout;

        // QAP
        this.communicationNeighborhoodDist = other.communicationNeighborhoodDist;
        this.lsNeighborhood = other.lsNeighborhood;
        this.constructionAlgorithm = other.constructionAlgorithm;
        this.distanceConstructionAlgorithm = other.distanceConstructionAlgorithm;
        this.groupSizes = new ArrayList<>(other.groupSizes);
        this.distances = new ArrayList<>(other.distances);
        this.searchSpaceS = other.searchSpaceS;
        this.preconfigurationMapping = other.preconfigurationMapping;
        this.maxRecursionLevelsConstruction = other.maxRecursionLevelsConstruction;
        this.enableMapping = other.enableMapping;

        // Node Ordering
        this.dissectionRecLimit = other.dissectionRecLimit;
        this.disableReductions = other.disableReductions;
        this.reductionOrder = new ArrayList<>(other.reductionOrder);
        this.convergenceFactor = other.convergenceFactor;
        this.maxSimplicialDegree = other.maxSimplicialDegree;

        // Shared Mem OMP
        this.enableOmp = other.enableOmp;
    }


    public boolean useMmapIo;

    // ============================================================
    // =======================MATCHING=============================
    // ============================================================
    public boolean edgeRatingTiebreaking;

    public EdgeRating edgeRating;

    public PermutationQuality permutationQuality;

    public MatchingType matchingType;

    public boolean matchIslands;

    public boolean firstLevelRandomMatching;

    public boolean rateFirstLevelInnerOuter;

    public int maxVertexWeight;

    public int largestGraphWeight;

    public int workLoad;

    public int aggressiveRandomLevels;

    public boolean disableMaxVertexWeightConstraint;

    // ============================================================
    // ===================INITIAL PARTITIONING=====================
    // ============================================================
    public int initialPartitioningRepetitions;

    public int minipreps;

    public boolean refinedBubbling;

    public InitialPartitioningType initialPartitioningType;

    public boolean initialPartitionOptimize;

    public BipartitionAlgorithm bipartitionAlgorithm;

    public boolean initialPartitioning;

    public int bipartitionTries;

    public int bipartitionPostFmLimits;

    public int bipartitionPostMlLimits;

    // ============================================================
    // ====================REFINEMENT PARAMETERS===================
    // ============================================================
    public boolean cornerRefinementEnabled;

    public boolean useBucketQueues;

    public RefinementType refinementType;

    public PermutationQuality permutationDuringRefinement;

    public double imbalance;

    public int bubblingIterations;

    public int kwayRounds;

    public boolean quotientGraphRefinementDisabled;

    public KWayStopRule kwayStopRule;

    public double kwayAdaptiveLimitsAlpha;

    public double kwayAdaptiveLimitsBeta;

    public int maxFlowIterations;

    public int localMultitryRounds;

    public int localMultitryFmAlpha;

    public boolean graphAlreadyPartitioned;

    public int fmSearchLimit;

    public int kwayFmSearchLimit;

    public int upperBoundPartition;

    public double bankAccountFactor;

    public RefinementSchedulingAlgorithm refinementSchedulingAlgorithm;

    public boolean mostBalancedMinimumCuts;

    public boolean mostBalancedMinimumCutsNodeSep;

    public int toposortIterations;

    public boolean softRebalance;

    public boolean rebalance;

    public double flowRegionFactor;

    public boolean gpaGrowPathsBetweenBlocks;

    // ===================================================
    // ==========GLOBAL SEARCH PARAMETERS=================
    // ===================================================
    public int globalCycleIterations;

    public boolean useWcycles;

    public boolean useFullMultigrid;

    public int levelSplit;

    public boolean noNewInitialPartitioning;

    public boolean omitGivenPartitioning;

    public StopRule stopRule;

    public int numVertStopFactor;

    public boolean noChangeConvergence;

    // ===================================================
    // ===PERFECTLY BALANCED PARTITIONING=================
    // ===================================================
    public boolean removeNegativeCycles;

    public boolean kabaIncludeRemovalOfPaths;

    public boolean kabaEnableZeroWeightCycles;

    public double kabaEInternalBal;

    public CycleRefinementAlgorithm cycleRefinementAlgorithm;

    public int kabaInternalNoAugStepsAug;

    public int kabaPackingIterations;

    public boolean kabaFlipPackings;

    public MLSRule kabaLsearchP; // more localized search pseudo directed

    public boolean kaffpaPerfectlyBalancedRefinement;

    public int kabaUnsuccIterations;

    // ===================================================
    // =============PAR_PSEUDOMH / MH=====================
    // ===================================================
    public double timeLimit;

    public double epsilon;

    public int noUnsucReps;

    public int localPartitioningRepetitions;

    public boolean mhPlainRepetitions;

    public boolean mhEasyConstruction;

    public boolean mhEnableGalCombine;

    public boolean mhNoMh;

    public boolean mhPrintLog;

    public int mhFlipCoin;

    public int mhInitialPopulationFraction;

    public boolean mhDisableCrossCombine;

    public boolean mhCrossCombineOriginalK;

    public boolean mhDisableNcCombine;

    public boolean mhDisableCombine;

    public boolean mhEnableQuickstart;

    public boolean mhDisableDiversifyIslands;

    public boolean mhDiversify;

    public boolean mhDiversifyBest;

    public boolean mhEnableTournamentSelection;

    public boolean mhOptimizeCommunicationVolume;

    public int mhNumNcsToCompute;

    public int mhPoolSize;

    public boolean combine; // in this case, the second index is filled and edges between both partitions are not contracted

    public int initialPartitionOptimizeFmLimits;

    public int initialPartitionOptimizeMultitryFmAlpha;

    public int initialPartitionOptimizeMultitryRounds;

    public int walshawMhRepetitions;

    public int scalingFactor;

    public boolean scaleBack;

    public boolean suppressPartitionerOutput;

    public int maxT;

    public int maxIter;

    // ===================================================
    // ===============BUFFOON=============================
    // ===================================================
    public boolean disableHardRebalance;

    public boolean buffoon;

    public boolean kabapE;

    public boolean mhPenaltyForUnconnected;

    // ===================================================
    // ==================MISC=============================
    // ===================================================
    public String inputPartition;

    public int seed;

    public boolean fast;

    public boolean eco;

    public boolean strong;

    public boolean kaffpaE;

    public boolean balanceEdges;

    // number of blocks the graph should be partitioned in
    public int k;

    public boolean computeVertexSeparator;

    public boolean onlyFirstLevel;

    public boolean useBalanceSingletons;

    public int amgIterations;

    public String graphFilename;

    public String filenameOutput;

    public boolean kaffpaPerfectlyBalance;

    public boolean modeNodeSeparators;

    // ===================================================
    // ===========SNW PARTITIONING========================
    // ===================================================
    public NodeOrderingType nodeOrdering;

    public int clusterCoarseningFactor;

    public boolean ensembleClusterings;

    public int labelIterations;

    public int labelIterationsRefinement;

    public int numberOfClusterings;

    public boolean labelPropagationRefinement;

    public double balanceFactor;

    public boolean clusterCoarseningDuringIp;

    public boolean setUpperbound;

    public int repetitions;

    // ===================================================
    // ===========NODE SEPARATOR==========================
    // ===================================================
    public int maxFlowImprovSteps;

    public int maxInitialNsTries;

    public double regionFactorNodeSeparators;

    public boolean sepFlowsDisabled;

    public boolean sepFmDisabled;

    public boolean sepLocFmDisabled;

    public int sepLocFmNoSnodes;

    public boolean sepGreedyDisabled;

    public int sepFmUnsuccSteps;

    public int sepLocFmUnsuccSteps;

    public int sepNumFmReps;

    public int sepNumLocFmReps;

    public int sepNumVertStop;

    public boolean sepFullBoundaryIp;

    public boolean fasterNs;

    public EdgeRating sepEdgeRatingDuringIp;

    // ===================================================
    // =========LABEL PROPAGATION=========================
    // ===================================================
    public int clusterUpperbound;

    // ===================================================
    // =========INITIAL PARTITIONING======================
    // ===================================================

    // variables controlling the size of the blocks during
    // multilevel recursive bisection
    // (for the case where k is not a power of 2)
    public List<Integer> targetWeights;

    public boolean initialBipartitioning;

    public int growTarget;

    // ===================================================
    // =========ILP LOCAL SEARCH==========================
    // ===================================================
    public OptimizationMode ilpMode;

    public int ilpMinGain;

    public int ilpBfsDepth;

    public int ilpBfsMinGain;

    public OverlapPresets ilpOverlapPresets;

    public int ilpLimitNonzeroes;

    public int ilpOverlapRuns;

    public int ilpTimeout;

    // ===================================================
    // ===============QAP=================================
    // ===================================================
    public int communicationNeighborhoodDist;

    public LsNeighborhoodType lsNeighborhood;

    public ConstructionAlgorithm constructionAlgorithm;

    public DistanceConstructionAlgorithm distanceConstructionAlgorithm;

    public List<Integer> groupSizes;

    public List<Integer> distances;

    public int searchSpaceS;

    public PreConfigMapping preconfigurationMapping;

    public int maxRecursionLevelsConstruction;

    public boolean enableMapping;

    // ===================================================
    // ========NODE ORDERING==============================
    // ===================================================
    public int dissectionRecLimit;

    public boolean disableReductions;

    public List<NestedDissectionReductionType> reductionOrder;

    public double convergenceFactor;

    public int maxSimplicialDegree;

    // ===================================================
    // ===============Shared Mem OMP======================
    // ===================================================
    public boolean enableOmp;


    public void logDump(java.io.File out) {
        // Implementation for logging the config if needed
    }

    public void setUseMmapIo(boolean useMmapIo) {
        this.useMmapIo = useMmapIo;
    }

    public void setEdgeRatingTiebreaking(boolean edgeRatingTiebreaking) {
        this.edgeRatingTiebreaking = edgeRatingTiebreaking;
    }

    public void setEdgeRating(EdgeRating edgeRating) {
        this.edgeRating = edgeRating;
    }

    public void setPermutationQuality(PermutationQuality permutationQuality) {
        this.permutationQuality = permutationQuality;
    }

    public void setMatchingType(MatchingType matchingType) {
        this.matchingType = matchingType;
    }

    public void setMatchIslands(boolean matchIslands) {
        this.matchIslands = matchIslands;
    }

    public void setFirstLevelRandomMatching(boolean firstLevelRandomMatching) {
        this.firstLevelRandomMatching = firstLevelRandomMatching;
    }

    public void setRateFirstLevelInnerOuter(boolean rateFirstLevelInnerOuter) {
        this.rateFirstLevelInnerOuter = rateFirstLevelInnerOuter;
    }

    public void setMaxVertexWeight(int maxVertexWeight) {
        this.maxVertexWeight = maxVertexWeight;
    }

    public void setLargestGraphWeight(int largestGraphWeight) {
        this.largestGraphWeight = largestGraphWeight;
    }

    public void setWorkLoad(int workLoad) {
        this.workLoad = workLoad;
    }

    public void setAggressiveRandomLevels(int aggressiveRandomLevels) {
        this.aggressiveRandomLevels = aggressiveRandomLevels;
    }

    public void setDisableMaxVertexWeightConstraint(boolean disableMaxVertexWeightConstraint) {
        this.disableMaxVertexWeightConstraint = disableMaxVertexWeightConstraint;
    }

    public void setInitialPartitioningRepetitions(int initialPartitioningRepetitions) {
        this.initialPartitioningRepetitions = initialPartitioningRepetitions;
    }

    public void setMinipreps(int minipreps) {
        this.minipreps = minipreps;
    }

    public void setRefinedBubbling(boolean refinedBubbling) {
        this.refinedBubbling = refinedBubbling;
    }

    public void setInitialPartitioningType(InitialPartitioningType initialPartitioningType) {
        this.initialPartitioningType = initialPartitioningType;
    }

    public void setInitialPartitionOptimize(boolean initialPartitionOptimize) {
        this.initialPartitionOptimize = initialPartitionOptimize;
    }

    public void setBipartitionAlgorithm(BipartitionAlgorithm bipartitionAlgorithm) {
        this.bipartitionAlgorithm = bipartitionAlgorithm;
    }

    public void setInitialPartitioning(boolean initialPartitioning) {
        this.initialPartitioning = initialPartitioning;
    }

    public void setBipartitionTries(int bipartitionTries) {
        this.bipartitionTries = bipartitionTries;
    }

    public void setBipartitionPostFmLimits(int bipartitionPostFmLimits) {
        this.bipartitionPostFmLimits = bipartitionPostFmLimits;
    }

    public void setBipartitionPostMlLimits(int bipartitionPostMlLimits) {
        this.bipartitionPostMlLimits = bipartitionPostMlLimits;
    }

    public void setCornerRefinementEnabled(boolean cornerRefinementEnabled) {
        this.cornerRefinementEnabled = cornerRefinementEnabled;
    }

    public void setUseBucketQueues(boolean useBucketQueues) {
        this.useBucketQueues = useBucketQueues;
    }

    public void setRefinementType(RefinementType refinementType) {
        this.refinementType = refinementType;
    }

    public void setPermutationDuringRefinement(PermutationQuality permutationDuringRefinement) {
        this.permutationDuringRefinement = permutationDuringRefinement;
    }

    public void setImbalance(double imbalance) {
        this.imbalance = imbalance;
    }

    public void setBubblingIterations(int bubblingIterations) {
        this.bubblingIterations = bubblingIterations;
    }

    public void setKwayRounds(int kwayRounds) {
        this.kwayRounds = kwayRounds;
    }

    public void setQuotientGraphRefinementDisabled(boolean quotientGraphRefinementDisabled) {
        this.quotientGraphRefinementDisabled = quotientGraphRefinementDisabled;
    }

    public void setKwayStopRule(KWayStopRule kwayStopRule) {
        this.kwayStopRule = kwayStopRule;
    }

    public void setKwayAdaptiveLimitsAlpha(double kwayAdaptiveLimitsAlpha) {
        this.kwayAdaptiveLimitsAlpha = kwayAdaptiveLimitsAlpha;
    }

    public void setKwayAdaptiveLimitsBeta(double kwayAdaptiveLimitsBeta) {
        this.kwayAdaptiveLimitsBeta = kwayAdaptiveLimitsBeta;
    }

    public void setMaxFlowIterations(int maxFlowIterations) {
        this.maxFlowIterations = maxFlowIterations;
    }

    public void setLocalMultitryRounds(int localMultitryRounds) {
        this.localMultitryRounds = localMultitryRounds;
    }

    public void setLocalMultitryFmAlpha(int localMultitryFmAlpha) {
        this.localMultitryFmAlpha = localMultitryFmAlpha;
    }

    public void setGraphAlreadyPartitioned(boolean graphAlreadyPartitioned) {
        this.graphAlreadyPartitioned = graphAlreadyPartitioned;
    }

    public void setFmSearchLimit(int fmSearchLimit) {
        this.fmSearchLimit = fmSearchLimit;
    }

    public void setKwayFmSearchLimit(int kwayFmSearchLimit) {
        this.kwayFmSearchLimit = kwayFmSearchLimit;
    }

    public void setUpperBoundPartition(int upperBoundPartition) {
        this.upperBoundPartition = upperBoundPartition;
    }

    public void setBankAccountFactor(double bankAccountFactor) {
        this.bankAccountFactor = bankAccountFactor;
    }

    public void setRefinementSchedulingAlgorithm(RefinementSchedulingAlgorithm refinementSchedulingAlgorithm) {
        this.refinementSchedulingAlgorithm = refinementSchedulingAlgorithm;
    }

    public void setMostBalancedMinimumCuts(boolean mostBalancedMinimumCuts) {
        this.mostBalancedMinimumCuts = mostBalancedMinimumCuts;
    }

    public void setMostBalancedMinimumCutsNodeSep(boolean mostBalancedMinimumCutsNodeSep) {
        this.mostBalancedMinimumCutsNodeSep = mostBalancedMinimumCutsNodeSep;
    }

    public void setToposortIterations(int toposortIterations) {
        this.toposortIterations = toposortIterations;
    }

    public void setSoftRebalance(boolean softRebalance) {
        this.softRebalance = softRebalance;
    }

    public void setRebalance(boolean rebalance) {
        this.rebalance = rebalance;
    }

    public void setFlowRegionFactor(double flowRegionFactor) {
        this.flowRegionFactor = flowRegionFactor;
    }

    public void setGpaGrowPathsBetweenBlocks(boolean gpaGrowPathsBetweenBlocks) {
        this.gpaGrowPathsBetweenBlocks = gpaGrowPathsBetweenBlocks;
    }

    public void setGlobalCycleIterations(int globalCycleIterations) {
        this.globalCycleIterations = globalCycleIterations;
    }

    public void setUseWcycles(boolean useWcycles) {
        this.useWcycles = useWcycles;
    }

    public void setUseFullMultigrid(boolean useFullMultigrid) {
        this.useFullMultigrid = useFullMultigrid;
    }

    public void setLevelSplit(int levelSplit) {
        this.levelSplit = levelSplit;
    }

    public void setNoNewInitialPartitioning(boolean noNewInitialPartitioning) {
        this.noNewInitialPartitioning = noNewInitialPartitioning;
    }

    public void setOmitGivenPartitioning(boolean omitGivenPartitioning) {
        this.omitGivenPartitioning = omitGivenPartitioning;
    }

    public void setStopRule(StopRule stopRule) {
        this.stopRule = stopRule;
    }

    public void setNumVertStopFactor(int numVertStopFactor) {
        this.numVertStopFactor = numVertStopFactor;
    }

    public void setNoChangeConvergence(boolean noChangeConvergence) {
        this.noChangeConvergence = noChangeConvergence;
    }

    public void setRemoveNegativeCycles(boolean removeNegativeCycles) {
        this.removeNegativeCycles = removeNegativeCycles;
    }

    public void setKabaIncludeRemovalOfPaths(boolean kabaIncludeRemovalOfPaths) {
        this.kabaIncludeRemovalOfPaths = kabaIncludeRemovalOfPaths;
    }

    public void setKabaEnableZeroWeightCycles(boolean kabaEnableZeroWeightCycles) {
        this.kabaEnableZeroWeightCycles = kabaEnableZeroWeightCycles;
    }

    public void setKabaEInternalBal(double kabaEInternalBal) {
        this.kabaEInternalBal = kabaEInternalBal;
    }

    public void setCycleRefinementAlgorithm(CycleRefinementAlgorithm cycleRefinementAlgorithm) {
        this.cycleRefinementAlgorithm = cycleRefinementAlgorithm;
    }

    public void setKabaInternalNoAugStepsAug(int kabaInternalNoAugStepsAug) {
        this.kabaInternalNoAugStepsAug = kabaInternalNoAugStepsAug;
    }

    public void setKabaPackingIterations(int kabaPackingIterations) {
        this.kabaPackingIterations = kabaPackingIterations;
    }

    public void setKabaFlipPackings(boolean kabaFlipPackings) {
        this.kabaFlipPackings = kabaFlipPackings;
    }

    public void setKabaLsearchP(MLSRule kabaLsearchP) {
        this.kabaLsearchP = kabaLsearchP;
    }

    public void setKaffpaPerfectlyBalancedRefinement(boolean kaffpaPerfectlyBalancedRefinement) {
        this.kaffpaPerfectlyBalancedRefinement = kaffpaPerfectlyBalancedRefinement;
    }

    public void setKabaUnsuccIterations(int kabaUnsuccIterations) {
        this.kabaUnsuccIterations = kabaUnsuccIterations;
    }

    public void setTimeLimit(double timeLimit) {
        this.timeLimit = timeLimit;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public void setNoUnsucReps(int noUnsucReps) {
        this.noUnsucReps = noUnsucReps;
    }

    public void setLocalPartitioningRepetitions(int localPartitioningRepetitions) {
        this.localPartitioningRepetitions = localPartitioningRepetitions;
    }

    public void setMhPlainRepetitions(boolean mhPlainRepetitions) {
        this.mhPlainRepetitions = mhPlainRepetitions;
    }

    public void setMhEasyConstruction(boolean mhEasyConstruction) {
        this.mhEasyConstruction = mhEasyConstruction;
    }

    public void setMhEnableGalCombine(boolean mhEnableGalCombine) {
        this.mhEnableGalCombine = mhEnableGalCombine;
    }

    public void setMhNoMh(boolean mhNoMh) {
        this.mhNoMh = mhNoMh;
    }

    public void setMhPrintLog(boolean mhPrintLog) {
        this.mhPrintLog = mhPrintLog;
    }

    public void setMhFlipCoin(int mhFlipCoin) {
        this.mhFlipCoin = mhFlipCoin;
    }

    public void setMhInitialPopulationFraction(int mhInitialPopulationFraction) {
        this.mhInitialPopulationFraction = mhInitialPopulationFraction;
    }

    public void setMhDisableCrossCombine(boolean mhDisableCrossCombine) {
        this.mhDisableCrossCombine = mhDisableCrossCombine;
    }

    public void setMhCrossCombineOriginalK(boolean mhCrossCombineOriginalK) {
        this.mhCrossCombineOriginalK = mhCrossCombineOriginalK;
    }

    public void setMhDisableNcCombine(boolean mhDisableNcCombine) {
        this.mhDisableNcCombine = mhDisableNcCombine;
    }

    public void setMhDisableCombine(boolean mhDisableCombine) {
        this.mhDisableCombine = mhDisableCombine;
    }

    public void setMhEnableQuickstart(boolean mhEnableQuickstart) {
        this.mhEnableQuickstart = mhEnableQuickstart;
    }

    public void setMhDisableDiversifyIslands(boolean mhDisableDiversifyIslands) {
        this.mhDisableDiversifyIslands = mhDisableDiversifyIslands;
    }

    public void setMhDiversify(boolean mhDiversify) {
        this.mhDiversify = mhDiversify;
    }

    public void setMhDiversifyBest(boolean mhDiversifyBest) {
        this.mhDiversifyBest = mhDiversifyBest;
    }

    public void setMhEnableTournamentSelection(boolean mhEnableTournamentSelection) {
        this.mhEnableTournamentSelection = mhEnableTournamentSelection;
    }

    public void setMhOptimizeCommunicationVolume(boolean mhOptimizeCommunicationVolume) {
        this.mhOptimizeCommunicationVolume = mhOptimizeCommunicationVolume;
    }

    public void setMhNumNcsToCompute(int mhNumNcsToCompute) {
        this.mhNumNcsToCompute = mhNumNcsToCompute;
    }

    public void setMhPoolSize(int mhPoolSize) {
        this.mhPoolSize = mhPoolSize;
    }

    public void setCombine(boolean combine) {
        this.combine = combine;
    }

    public void setInitialPartitionOptimizeFmLimits(int initialPartitionOptimizeFmLimits) {
        this.initialPartitionOptimizeFmLimits = initialPartitionOptimizeFmLimits;
    }

    public void setInitialPartitionOptimizeMultitryFmAlpha(int initialPartitionOptimizeMultitryFmAlpha) {
        this.initialPartitionOptimizeMultitryFmAlpha = initialPartitionOptimizeMultitryFmAlpha;
    }

    public void setInitialPartitionOptimizeMultitryRounds(int initialPartitionOptimizeMultitryRounds) {
        this.initialPartitionOptimizeMultitryRounds = initialPartitionOptimizeMultitryRounds;
    }

    public void setWalshawMhRepetitions(int walshawMhRepetitions) {
        this.walshawMhRepetitions = walshawMhRepetitions;
    }

    public void setScalingFactor(int scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    public void setScaleBack(boolean scaleBack) {
        this.scaleBack = scaleBack;
    }

    public void setSuppressPartitionerOutput(boolean suppressPartitionerOutput) {
        this.suppressPartitionerOutput = suppressPartitionerOutput;
    }

    public void setMaxT(int maxT) {
        this.maxT = maxT;
    }

    public void setMaxIter(int maxIter) {
        this.maxIter = maxIter;
    }

    public void setDisableHardRebalance(boolean disableHardRebalance) {
        this.disableHardRebalance = disableHardRebalance;
    }

    public void setBuffoon(boolean buffoon) {
        this.buffoon = buffoon;
    }

    public void setKabapE(boolean kabapE) {
        this.kabapE = kabapE;
    }

    public void setMhPenaltyForUnconnected(boolean mhPenaltyForUnconnected) {
        this.mhPenaltyForUnconnected = mhPenaltyForUnconnected;
    }

    public void setInputPartition(String inputPartition) {
        this.inputPartition = inputPartition;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public void setFast(boolean fast) {
        this.fast = fast;
    }

    public void setEco(boolean eco) {
        this.eco = eco;
    }

    public void setStrong(boolean strong) {
        this.strong = strong;
    }

    public void setKaffpaE(boolean kaffpaE) {
        this.kaffpaE = kaffpaE;
    }

    public void setBalanceEdges(boolean balanceEdges) {
        this.balanceEdges = balanceEdges;
    }

    public void setK(int k) {
        this.k = k;
    }

    public void setComputeVertexSeparator(boolean computeVertexSeparator) {
        this.computeVertexSeparator = computeVertexSeparator;
    }

    public void setOnlyFirstLevel(boolean onlyFirstLevel) {
        this.onlyFirstLevel = onlyFirstLevel;
    }

    public void setUseBalanceSingletons(boolean useBalanceSingletons) {
        this.useBalanceSingletons = useBalanceSingletons;
    }

    public void setAmgIterations(int amgIterations) {
        this.amgIterations = amgIterations;
    }

    public void setGraphFilename(String graphFilename) {
        this.graphFilename = graphFilename;
    }

    public void setFilenameOutput(String filenameOutput) {
        this.filenameOutput = filenameOutput;
    }

    public void setKaffpaPerfectlyBalance(boolean kaffpaPerfectlyBalance) {
        this.kaffpaPerfectlyBalance = kaffpaPerfectlyBalance;
    }

    public void setModeNodeSeparators(boolean modeNodeSeparators) {
        this.modeNodeSeparators = modeNodeSeparators;
    }

    public void setNodeOrdering(NodeOrderingType nodeOrdering) {
        this.nodeOrdering = nodeOrdering;
    }

    public void setClusterCoarseningFactor(int clusterCoarseningFactor) {
        this.clusterCoarseningFactor = clusterCoarseningFactor;
    }

    public void setEnsembleClusterings(boolean ensembleClusterings) {
        this.ensembleClusterings = ensembleClusterings;
    }

    public void setLabelIterations(int labelIterations) {
        this.labelIterations = labelIterations;
    }

    public void setLabelIterationsRefinement(int labelIterationsRefinement) {
        this.labelIterationsRefinement = labelIterationsRefinement;
    }

    public void setNumberOfClusterings(int numberOfClusterings) {
        this.numberOfClusterings = numberOfClusterings;
    }

    public void setLabelPropagationRefinement(boolean labelPropagationRefinement) {
        this.labelPropagationRefinement = labelPropagationRefinement;
    }

    public void setBalanceFactor(double balanceFactor) {
        this.balanceFactor = balanceFactor;
    }

    public void setClusterCoarseningDuringIp(boolean clusterCoarseningDuringIp) {
        this.clusterCoarseningDuringIp = clusterCoarseningDuringIp;
    }

    public void setSetUpperbound(boolean setUpperbound) {
        this.setUpperbound = setUpperbound;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public void setMaxFlowImprovSteps(int maxFlowImprovSteps) {
        this.maxFlowImprovSteps = maxFlowImprovSteps;
    }

    public void setMaxInitialNsTries(int maxInitialNsTries) {
        this.maxInitialNsTries = maxInitialNsTries;
    }

    public void setRegionFactorNodeSeparators(double regionFactorNodeSeparators) {
        this.regionFactorNodeSeparators = regionFactorNodeSeparators;
    }

    public void setSepFlowsDisabled(boolean sepFlowsDisabled) {
        this.sepFlowsDisabled = sepFlowsDisabled;
    }

    public void setSepFmDisabled(boolean sepFmDisabled) {
        this.sepFmDisabled = sepFmDisabled;
    }

    public void setSepLocFmDisabled(boolean sepLocFmDisabled) {
        this.sepLocFmDisabled = sepLocFmDisabled;
    }

    public void setSepLocFmNoSnodes(int sepLocFmNoSnodes) {
        this.sepLocFmNoSnodes = sepLocFmNoSnodes;
    }

    public void setSepGreedyDisabled(boolean sepGreedyDisabled) {
        this.sepGreedyDisabled = sepGreedyDisabled;
    }

    public void setSepFmUnsuccSteps(int sepFmUnsuccSteps) {
        this.sepFmUnsuccSteps = sepFmUnsuccSteps;
    }

    public void setSepLocFmUnsuccSteps(int sepLocFmUnsuccSteps) {
        this.sepLocFmUnsuccSteps = sepLocFmUnsuccSteps;
    }

    public void setSepNumFmReps(int sepNumFmReps) {
        this.sepNumFmReps = sepNumFmReps;
    }

    public void setSepNumLocFmReps(int sepNumLocFmReps) {
        this.sepNumLocFmReps = sepNumLocFmReps;
    }

    public void setSepNumVertStop(int sepNumVertStop) {
        this.sepNumVertStop = sepNumVertStop;
    }

    public void setSepFullBoundaryIp(boolean sepFullBoundaryIp) {
        this.sepFullBoundaryIp = sepFullBoundaryIp;
    }

    public void setFasterNs(boolean fasterNs) {
        this.fasterNs = fasterNs;
    }

    public void setSepEdgeRatingDuringIp(EdgeRating sepEdgeRatingDuringIp) {
        this.sepEdgeRatingDuringIp = sepEdgeRatingDuringIp;
    }

    public void setClusterUpperbound(int clusterUpperbound) {
        this.clusterUpperbound = clusterUpperbound;
    }

    public void setTargetWeights(List<Integer> targetWeights) {
        this.targetWeights = targetWeights;
    }

    public void setInitialBipartitioning(boolean initialBipartitioning) {
        this.initialBipartitioning = initialBipartitioning;
    }

    public void setGrowTarget(int growTarget) {
        this.growTarget = growTarget;
    }

    public void setIlpMode(OptimizationMode ilpMode) {
        this.ilpMode = ilpMode;
    }

    public void setIlpMinGain(int ilpMinGain) {
        this.ilpMinGain = ilpMinGain;
    }

    public void setIlpBfsDepth(int ilpBfsDepth) {
        this.ilpBfsDepth = ilpBfsDepth;
    }

    public void setIlpBfsMinGain(int ilpBfsMinGain) {
        this.ilpBfsMinGain = ilpBfsMinGain;
    }

    public void setIlpOverlapPresets(OverlapPresets ilpOverlapPresets) {
        this.ilpOverlapPresets = ilpOverlapPresets;
    }

    public void setIlpLimitNonzeroes(int ilpLimitNonzeroes) {
        this.ilpLimitNonzeroes = ilpLimitNonzeroes;
    }

    public void setIlpOverlapRuns(int ilpOverlapRuns) {
        this.ilpOverlapRuns = ilpOverlapRuns;
    }

    public void setIlpTimeout(int ilpTimeout) {
        this.ilpTimeout = ilpTimeout;
    }

    public void setCommunicationNeighborhoodDist(int communicationNeighborhoodDist) {
        this.communicationNeighborhoodDist = communicationNeighborhoodDist;
    }

    public void setLsNeighborhood(LsNeighborhoodType lsNeighborhood) {
        this.lsNeighborhood = lsNeighborhood;
    }

    public void setConstructionAlgorithm(ConstructionAlgorithm constructionAlgorithm) {
        this.constructionAlgorithm = constructionAlgorithm;
    }

    public void setDistanceConstructionAlgorithm(DistanceConstructionAlgorithm distanceConstructionAlgorithm) {
        this.distanceConstructionAlgorithm = distanceConstructionAlgorithm;
    }

    public void setGroupSizes(List<Integer> groupSizes) {
        this.groupSizes = groupSizes;
    }

    public void setDistances(List<Integer> distances) {
        this.distances = distances;
    }

    public void setSearchSpaceS(int searchSpaceS) {
        this.searchSpaceS = searchSpaceS;
    }

    public void setPreconfigurationMapping(PreConfigMapping preconfigurationMapping) {
        this.preconfigurationMapping = preconfigurationMapping;
    }

    public void setMaxRecursionLevelsConstruction(int maxRecursionLevelsConstruction) {
        this.maxRecursionLevelsConstruction = maxRecursionLevelsConstruction;
    }

    public void setEnableMapping(boolean enableMapping) {
        this.enableMapping = enableMapping;
    }

    public void setDissectionRecLimit(int dissectionRecLimit) {
        this.dissectionRecLimit = dissectionRecLimit;
    }

    public void setDisableReductions(boolean disableReductions) {
        this.disableReductions = disableReductions;
    }

    public void setReductionOrder(List<NestedDissectionReductionType> reductionOrder) {
        this.reductionOrder = reductionOrder;
    }

    public void setConvergenceFactor(double convergenceFactor) {
        this.convergenceFactor = convergenceFactor;
    }

    public void setMaxSimplicialDegree(int maxSimplicialDegree) {
        this.maxSimplicialDegree = maxSimplicialDegree;
    }

    public void setEnableOmp(boolean enableOmp) {
        this.enableOmp = enableOmp;
    }
    public boolean isUseMmapIo() {
        return useMmapIo;
    }

    public boolean isEdgeRatingTiebreaking() {
        return edgeRatingTiebreaking;
    }

    public EdgeRating getEdgeRating() {
        return edgeRating;
    }

    public PermutationQuality getPermutationQuality() {
        return permutationQuality;
    }

    public MatchingType getMatchingType() {
        return matchingType;
    }

    public boolean isMatchIslands() {
        return matchIslands;
    }

    public boolean isFirstLevelRandomMatching() {
        return firstLevelRandomMatching;
    }

    public boolean isRateFirstLevelInnerOuter() {
        return rateFirstLevelInnerOuter;
    }

    public int getMaxVertexWeight() {
        return maxVertexWeight;
    }

    public int getLargestGraphWeight() {
        return largestGraphWeight;
    }

    public int getWorkLoad() {
        return workLoad;
    }

    public int getAggressiveRandomLevels() {
        return aggressiveRandomLevels;
    }

    public boolean isDisableMaxVertexWeightConstraint() {
        return disableMaxVertexWeightConstraint;
    }

    public int getInitialPartitioningRepetitions() {
        return initialPartitioningRepetitions;
    }

    public int getMinipreps() {
        return minipreps;
    }

    public boolean isRefinedBubbling() {
        return refinedBubbling;
    }

    public InitialPartitioningType getInitialPartitioningType() {
        return initialPartitioningType;
    }

    public boolean isInitialPartitionOptimize() {
        return initialPartitionOptimize;
    }

    public BipartitionAlgorithm getBipartitionAlgorithm() {
        return bipartitionAlgorithm;
    }

    public boolean isInitialPartitioning() {
        return initialPartitioning;
    }

    public int getBipartitionTries() {
        return bipartitionTries;
    }

    public int getBipartitionPostFmLimits() {
        return bipartitionPostFmLimits;
    }

    public int getBipartitionPostMlLimits() {
        return bipartitionPostMlLimits;
    }

    public boolean isCornerRefinementEnabled() {
        return cornerRefinementEnabled;
    }

    public boolean isUseBucketQueues() {
        return useBucketQueues;
    }

    public RefinementType getRefinementType() {
        return refinementType;
    }

    public PermutationQuality getPermutationDuringRefinement() {
        return permutationDuringRefinement;
    }

    public double getImbalance() {
        return imbalance;
    }

    public int getBubblingIterations() {
        return bubblingIterations;
    }

    public int getKwayRounds() {
        return kwayRounds;
    }

    public boolean isQuotientGraphRefinementDisabled() {
        return quotientGraphRefinementDisabled;
    }

    public KWayStopRule getKwayStopRule() {
        return kwayStopRule;
    }

    public double getKwayAdaptiveLimitsAlpha() {
        return kwayAdaptiveLimitsAlpha;
    }

    public double getKwayAdaptiveLimitsBeta() {
        return kwayAdaptiveLimitsBeta;
    }

    public int getMaxFlowIterations() {
        return maxFlowIterations;
    }

    public int getLocalMultitryRounds() {
        return localMultitryRounds;
    }

    public int getLocalMultitryFmAlpha() {
        return localMultitryFmAlpha;
    }

    public boolean isGraphAlreadyPartitioned() {
        return graphAlreadyPartitioned;
    }

    public int getFmSearchLimit() {
        return fmSearchLimit;
    }

    public int getKwayFmSearchLimit() {
        return kwayFmSearchLimit;
    }

    public int getUpperBoundPartition() {
        return upperBoundPartition;
    }

    public double getBankAccountFactor() {
        return bankAccountFactor;
    }

    public RefinementSchedulingAlgorithm getRefinementSchedulingAlgorithm() {
        return refinementSchedulingAlgorithm;
    }

    public boolean isMostBalancedMinimumCuts() {
        return mostBalancedMinimumCuts;
    }

    public boolean isMostBalancedMinimumCutsNodeSep() {
        return mostBalancedMinimumCutsNodeSep;
    }

    public int getToposortIterations() {
        return toposortIterations;
    }

    public boolean isSoftRebalance() {
        return softRebalance;
    }

    public boolean isRebalance() {
        return rebalance;
    }

    public double getFlowRegionFactor() {
        return flowRegionFactor;
    }

    public boolean isGpaGrowPathsBetweenBlocks() {
        return gpaGrowPathsBetweenBlocks;
    }

    public int getGlobalCycleIterations() {
        return globalCycleIterations;
    }

    public boolean isUseWcycles() {
        return useWcycles;
    }

    public boolean isUseFullMultigrid() {
        return useFullMultigrid;
    }

    public int getLevelSplit() {
        return levelSplit;
    }

    public boolean isNoNewInitialPartitioning() {
        return noNewInitialPartitioning;
    }

    public boolean isOmitGivenPartitioning() {
        return omitGivenPartitioning;
    }

    public StopRule getStopRule() {
        return stopRule;
    }

    public int getNumVertStopFactor() {
        return numVertStopFactor;
    }

    public boolean isNoChangeConvergence() {
        return noChangeConvergence;
    }

    public boolean isRemoveNegativeCycles() {
        return removeNegativeCycles;
    }

    public boolean isKabaIncludeRemovalOfPaths() {
        return kabaIncludeRemovalOfPaths;
    }

    public boolean isKabaEnableZeroWeightCycles() {
        return kabaEnableZeroWeightCycles;
    }

    public double getKabaEInternalBal() {
        return kabaEInternalBal;
    }

    public CycleRefinementAlgorithm getCycleRefinementAlgorithm() {
        return cycleRefinementAlgorithm;
    }

    public int getKabaInternalNoAugStepsAug() {
        return kabaInternalNoAugStepsAug;
    }

    public int getKabaPackingIterations() {
        return kabaPackingIterations;
    }

    public boolean isKabaFlipPackings() {
        return kabaFlipPackings;
    }

    public MLSRule getKabaLsearchP() {
        return kabaLsearchP;
    }

    public boolean isKaffpaPerfectlyBalancedRefinement() {
        return kaffpaPerfectlyBalancedRefinement;
    }

    public int getKabaUnsuccIterations() {
        return kabaUnsuccIterations;
    }

    public double getTimeLimit() {
        return timeLimit;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public int getNoUnsucReps() {
        return noUnsucReps;
    }

    public int getLocalPartitioningRepetitions() {
        return localPartitioningRepetitions;
    }

    public boolean isMhPlainRepetitions() {
        return mhPlainRepetitions;
    }

    public boolean isMhEasyConstruction() {
        return mhEasyConstruction;
    }

    public boolean isMhEnableGalCombine() {
        return mhEnableGalCombine;
    }

    public boolean isMhNoMh() {
        return mhNoMh;
    }

    public boolean isMhPrintLog() {
        return mhPrintLog;
    }

    public int getMhFlipCoin() {
        return mhFlipCoin;
    }

    public int getMhInitialPopulationFraction() {
        return mhInitialPopulationFraction;
    }

    public boolean isMhDisableCrossCombine() {
        return mhDisableCrossCombine;
    }

    public boolean isMhCrossCombineOriginalK() {
        return mhCrossCombineOriginalK;
    }

    public boolean isMhDisableNcCombine() {
        return mhDisableNcCombine;
    }

    public boolean isMhDisableCombine() {
        return mhDisableCombine;
    }

    public boolean isMhEnableQuickstart() {
        return mhEnableQuickstart;
    }

    public boolean isMhDisableDiversifyIslands() {
        return mhDisableDiversifyIslands;
    }

    public boolean isMhDiversify() {
        return mhDiversify;
    }

    public boolean isMhDiversifyBest() {
        return mhDiversifyBest;
    }

    public boolean isMhEnableTournamentSelection() {
        return mhEnableTournamentSelection;
    }

    public boolean isMhOptimizeCommunicationVolume() {
        return mhOptimizeCommunicationVolume;
    }

    public int getMhNumNcsToCompute() {
        return mhNumNcsToCompute;
    }

    public int getMhPoolSize() {
        return mhPoolSize;
    }

    public boolean isCombine() {
        return combine;
    }

    public int getInitialPartitionOptimizeFmLimits() {
        return initialPartitionOptimizeFmLimits;
    }

    public int getInitialPartitionOptimizeMultitryFmAlpha() {
        return initialPartitionOptimizeMultitryFmAlpha;
    }

    public int getInitialPartitionOptimizeMultitryRounds() {
        return initialPartitionOptimizeMultitryRounds;
    }

    public int getWalshawMhRepetitions() {
        return walshawMhRepetitions;
    }

    public int getScalingFactor() {
        return scalingFactor;
    }

    public boolean isScaleBack() {
        return scaleBack;
    }

    public boolean isSuppressPartitionerOutput() {
        return suppressPartitionerOutput;
    }

    public int getMaxT() {
        return maxT;
    }

    public int getMaxIter() {
        return maxIter;
    }

    public boolean isDisableHardRebalance() {
        return disableHardRebalance;
    }

    public boolean isBuffoon() {
        return buffoon;
    }

    public boolean isKabapE() {
        return kabapE;
    }

    public boolean isMhPenaltyForUnconnected() {
        return mhPenaltyForUnconnected;
    }

    public String getInputPartition() {
        return inputPartition;
    }

    public int getSeed() {
        return seed;
    }

    public boolean isFast() {
        return fast;
    }

    public boolean isEco() {
        return eco;
    }

    public boolean isStrong() {
        return strong;
    }

    public boolean isKaffpaE() {
        return kaffpaE;
    }

    public boolean isBalanceEdges() {
        return balanceEdges;
    }

    public int getK() {
        return k;
    }

    public boolean isComputeVertexSeparator() {
        return computeVertexSeparator;
    }

    public boolean isOnlyFirstLevel() {
        return onlyFirstLevel;
    }

    public boolean isUseBalanceSingletons() {
        return useBalanceSingletons;
    }

    public int getAmgIterations() {
        return amgIterations;
    }

    public String getGraphFilename() {
        return graphFilename;
    }

    public String getFilenameOutput() {
        return filenameOutput;
    }

    public boolean isKaffpaPerfectlyBalance() {
        return kaffpaPerfectlyBalance;
    }

    public boolean isModeNodeSeparators() {
        return modeNodeSeparators;
    }

    public NodeOrderingType getNodeOrdering() {
        return nodeOrdering;
    }

    public int getClusterCoarseningFactor() {
        return clusterCoarseningFactor;
    }

    public boolean isEnsembleClusterings() {
        return ensembleClusterings;
    }

    public int getLabelIterations() {
        return labelIterations;
    }

    public int getLabelIterationsRefinement() {
        return labelIterationsRefinement;
    }

    public int getNumberOfClusterings() {
        return numberOfClusterings;
    }

    public boolean isLabelPropagationRefinement() {
        return labelPropagationRefinement;
    }

    public double getBalanceFactor() {
        return balanceFactor;
    }

    public boolean isClusterCoarseningDuringIp() {
        return clusterCoarseningDuringIp;
    }

    public boolean isSetUpperbound() {
        return setUpperbound;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public int getMaxFlowImprovSteps() {
        return maxFlowImprovSteps;
    }

    public int getMaxInitialNsTries() {
        return maxInitialNsTries;
    }

    public double getRegionFactorNodeSeparators() {
        return regionFactorNodeSeparators;
    }

    public boolean isSepFlowsDisabled() {
        return sepFlowsDisabled;
    }

    public boolean isSepFmDisabled() {
        return sepFmDisabled;
    }

    public boolean isSepLocFmDisabled() {
        return sepLocFmDisabled;
    }

    public int getSepLocFmNoSnodes() {
        return sepLocFmNoSnodes;
    }

    public boolean isSepGreedyDisabled() {
        return sepGreedyDisabled;
    }

    public int getSepFmUnsuccSteps() {
        return sepFmUnsuccSteps;
    }

    public int getSepLocFmUnsuccSteps() {
        return sepLocFmUnsuccSteps;
    }

    public int getSepNumFmReps() {
        return sepNumFmReps;
    }

    public int getSepNumLocFmReps() {
        return sepNumLocFmReps;
    }

    public int getSepNumVertStop() {
        return sepNumVertStop;
    }

    public boolean isSepFullBoundaryIp() {
        return sepFullBoundaryIp;
    }

    public boolean isFasterNs() {
        return fasterNs;
    }

    public EdgeRating getSepEdgeRatingDuringIp() {
        return sepEdgeRatingDuringIp;
    }

    public int getClusterUpperbound() {
        return clusterUpperbound;
    }

    public List<Integer> getTargetWeights() {
        return targetWeights;
    }

    public boolean isInitialBipartitioning() {
        return initialBipartitioning;
    }

    public int getGrowTarget() {
        return growTarget;
    }

    public OptimizationMode getIlpMode() {
        return ilpMode;
    }

    public int getIlpMinGain() {
        return ilpMinGain;
    }

    public int getIlpBfsDepth() {
        return ilpBfsDepth;
    }

    public int getIlpBfsMinGain() {
        return ilpBfsMinGain;
    }

    public OverlapPresets getIlpOverlapPresets() {
        return ilpOverlapPresets;
    }

    public int getIlpLimitNonzeroes() {
        return ilpLimitNonzeroes;
    }

    public int getIlpOverlapRuns() {
        return ilpOverlapRuns;
    }

    public int getIlpTimeout() {
        return ilpTimeout;
    }

    public int getCommunicationNeighborhoodDist() {
        return communicationNeighborhoodDist;
    }

    public LsNeighborhoodType getLsNeighborhood() {
        return lsNeighborhood;
    }

    public ConstructionAlgorithm getConstructionAlgorithm() {
        return constructionAlgorithm;
    }

    public DistanceConstructionAlgorithm getDistanceConstructionAlgorithm() {
        return distanceConstructionAlgorithm;
    }

    public List<Integer> getGroupSizes() {
        return groupSizes;
    }

    public List<Integer> getDistances() {
        return distances;
    }

    public int getSearchSpaceS() {
        return searchSpaceS;
    }

    public int getMaxRecursionLevelsConstruction() {
        return maxRecursionLevelsConstruction;
    }

    public boolean isEnableMapping() {
        return enableMapping;
    }

    public int getDissectionRecLimit() {
        return dissectionRecLimit;
    }

    public boolean isDisableReductions() {
        return disableReductions;
    }

    public List<NestedDissectionReductionType> getReductionOrder() {
        return reductionOrder;
    }

    public double getConvergenceFactor() {
        return convergenceFactor;
    }

    public int getMaxSimplicialDegree() {
        return maxSimplicialDegree;
    }

    public boolean isEnableOmp() {
        return enableOmp;
    }

    public PreConfigMapping getPreconfigurationMapping() {
        return preconfigurationMapping;
    }

}
