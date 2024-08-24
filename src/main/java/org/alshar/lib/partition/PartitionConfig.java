package org.alshar.lib.partition;
import java.util.ArrayList;
import java.util.List;

import org.alshar.lib.enums.*;

public class PartitionConfig {

    public EdgeRating getEdgeRating() {
        return edgeRating;
    }

    public List<Integer> getGroupSizes() {
        return groupSizes;
    }

    public boolean isUseMmapIo() {
        return useMmapIo;
    }

    public List<Integer> getDistances() {
        return distances;
    }

    public PermutationQuality getPermutationQuality() {
        return permutationQuality;
    }

    public MatchingType getMatchingType() {
        return matchingType;
    }

    public boolean isFirstLevelRandomMatching() {
        return firstLevelRandomMatching;
    }

    public int getAggressiveRandomLevels() {
        return aggressiveRandomLevels;
    }

    public boolean isRateFirstLevelInnerOuter() {
        return rateFirstLevelInnerOuter;
    }

    public int getK() {
        return k;
    }

    public boolean isModeNodeSeparators() {
        return this.modeNodeSeparators;
    }

    public PreConfigMapping getPreconfigurationMapping() {
        return preconfigurationMapping;
    }

    public PartitionConfig() {
        this.groupSizes = new ArrayList<>();
        this.distances = new ArrayList<>();
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


}
