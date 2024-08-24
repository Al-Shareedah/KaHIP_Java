package org.alshar.app;
import org.alshar.lib.enums.EdgeRating;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.enums.*;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.cli.*;
public class ParseParameters {

    public static int parseParameters(String[] argv,
                                      PartitionConfig partitionConfig,
                                      boolean isGraphWeighted,
                                      boolean suppressProgramOutput,
                                      boolean recursive,
                                      String[] filename) {
        String progname = argv[0];

        // Define argument variables
        boolean useMmapIo = false;
        boolean edgeRatingTiebreaking = false;
        boolean matchIslands = false;
        boolean onlyFirstLevel = false;
        boolean graphWeighted = false;
        boolean enableCornerRefinement = false;
        boolean disableQgraphRefinement = false;
        boolean useFullMultigrid = false;
        boolean useVcycle = false;
        boolean computeVertexSeparator = false;
        boolean firstLevelRandomMatching = false;
        boolean rateFirstLevelInnerOuter = false;
        boolean useBucketQueues = false;
        boolean useWcycles = false;
        boolean disableRefinedBubbling = false;
        boolean enableConvergence = false;
        boolean enableOmp = false;
        boolean wcycleNoNewInitialPartitioning = false;
        String filenameOutput = null;
        int userSeed = 0;
        boolean versionFlag = false;
        int k = 0;
        String edgeRating = null;
        String refinementType = null;
        String matchingType = null;
        int mhPoolSize = 0;
        boolean mhPlainRepetitions = false;
        boolean mhPenaltyForUnconnected = false;
        boolean mhDisableNcCombine = false;
        boolean mhDisableCrossCombine = false;
        boolean mhDisableCombine = false;
        boolean mhEnableQuickstart = false;
        boolean mhDisableDiversifyIslands = false;
        boolean mhDisableDiversify = false;
        boolean mhDiversifyBest = false;
        boolean mhEnableTournamentSelection = false;
        boolean mhCrossCombineOriginalK = false;
        boolean mhOptimizeCommunicationVolume = false;
        boolean disableBalanceSingletons = false;
        boolean gpaGrowInternal = false;
        int initialPartitioningRepetitions = 0;
        int minipreps = 0;
        int aggressiveRandomLevels = 0;
        double imbalance = 0.0;
        String initialPartition = null;
        boolean initialPartitionOptimize = false;
        String bipartitionAlgorithm = null;
        String permutationQuality = null;
        String permutationDuringRefinement = null;
        int fmSearchLimit = 0;
        int bipartitionPostFmLimit = 0;
        int bipartitionPostMlLimit = 0;
        int bipartitionTries = 0;
        String refinementSchedulingAlgorithm = null;
        double bankAccountFactor = 0.0;
        double flowRegionFactor = 0.0;
        double kwayAdaptiveLimitsAlpha = 0.0;
        String stopRule = null;
        int numVertStopFactor = 0;
        String kwaySearchStopRule = null;
        int bubblingIterations = 0;
        int kwayRounds = 0;
        int kwayFmLimits = 0;
        int globalCycleIterations = 0;
        int levelSplit = 0;
        int toposortIterations = 0;
        boolean mostBalancedFlows = false;
        String inputPartition = null;
        boolean recursiveBipartitioning = false;
        boolean suppressOutput = false;
        boolean disableMaxVertexWeightConstraint = false;
        int localMultitryFmAlpha = 0;
        int localMultitryRounds = 0;
        int initialPartitionOptimizeFmLimits = 0;
        int initialPartitionOptimizeMultitryFmAlpha = 0;
        int initialPartitionOptimizeMultitryRounds = 0;
        String preconfiguration = null;
        double timeLimit = 0.0;
        int unsuccessfulReps = 0;
        int localPartitioningRepetitions = 0;
        int amgIterations = 0;
        int mhFlipCoin = 0;
        int mhInitialPopulationFraction = 0;
        boolean mhPrintLog = false;
        boolean mhSequentialMode = false;
        String kabaNegCycleAlgorithm = null;
        double kabaEInternalBal = 0.0;
        int kabaInternalNoAugStepsAug = 0;
        int kabaPackingIterations = 0;
        int kabaUnsuccIterations = 0;
        boolean kabaFlipPackings = false;
        String kabaLsearchP = null;
        boolean kaffpaPerfectlyBalancedRefinement = false;
        boolean kabaDisableZeroWeightCycles = false;
        boolean enforceBalance = false;
        boolean mhEnableTabuSearch = false;
        boolean mhEnableKabapE = false;
        int maxT = 0;
        int maxIter = 0;
        boolean balanceEdges = false;
        int clusterUpperbound = 0;
        int labelPropagationIterations = 0;
        int maxInitialNsTries = 0;
        int maxFlowImprovSteps = 0;
        boolean mostBalancedFlowsNodeSep = false;
        double regionFactorNodeSeparators = 0.0;
        boolean sepFlowsDisabled = false;
        boolean sepFmDisabled = false;
        boolean sepLocFmDisabled = false;
        boolean sepGreedyDisabled = false;
        boolean sepFullBoundaryIp = false;
        boolean sepFasterNs = false;
        int sepFmUnsuccSteps = 0;
        int sepNumFmReps = 0;
        int sepLocFmUnsuccSteps = 0;
        int sepNumLocFmReps = 0;
        int sepLocFmNoSnodes = 0;
        int sepNumVertStop = 0;
        String sepEdgeRatingDuringIp = null;
        boolean enableMapping = false;
        String hierarchyParameterString = null;
        String distanceParameterString = null;
        boolean onlineDistances = false;
        int dissectionRecLimit = 0;
        boolean disableReductions = false;
        String reductionOrder = null;
        double convergenceFactor = 0.0;
        int maxSimplicialDegree = 0;
        String ilpMode = null;
        int ilpMinGain = 0;
        int ilpBfsDepth = 0;
        String ilpOverlapPresets = null;
        int ilpLimitNonzeroes = 0;
        int ilpOverlapRuns = 0;
        int ilpTimeout = 0;

        // Parse the arguments here, assign values to the variables above
        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];

            // Check for the graph filename (first argument without a flag)
            if (i == 0 && !arg.startsWith("--")) {
                filename[0] = arg;
                continue;
            }

            // Parse the --k argument
            if (arg.equals("--k") && i + 1 < argv.length) {
                try {
                    k = Integer.parseInt(argv[++i]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid value for --k: " + argv[i]);
                    return 1;
                }
                continue;
            }

            // Parse the --preconfiguration argument
            if (arg.startsWith("--preconfiguration=")) {
                preconfiguration = arg.substring("--preconfiguration=".length());
                continue;
            }

            // Other arguments parsing can go here
        }

        if (versionFlag) {
            System.out.println("v3.14");
            return 1;
        }

        // Handle help request
        // Display help message if needed and return 1

        // Handle errors and invalid input
        // Print errors if any and return 1

        if (k > 0) {
            partitionConfig.k = k;
        }


        recursive = false;

        Configuration cfg = new Configuration();
        cfg.standard(partitionConfig);

        if ("MODE_KAFFPA".equals(System.getProperty("mode"))) {
            cfg.eco(partitionConfig);
        } else {
            cfg.strong(partitionConfig);
        }


        if (preconfiguration != null) {
            switch (preconfiguration) {
                case "strong":
                    cfg.strong(partitionConfig);
                    break;
                case "eco":
                    cfg.eco(partitionConfig);
                    break;
                case "fast":
                    cfg.fast(partitionConfig);
                    break;
                default:
                    System.err.println("Invalid preconfiguration variant: \"" + preconfiguration + "\"");
                    return 1;
            }
        }

        if (useMmapIo) {
            partitionConfig.useMmapIo = true;
        }

        if (enableMapping) {
            partitionConfig.enableMapping = true;
            if (hierarchyParameterString == null) {
                System.out.println("Please specify the hierarchy using the --hierarchy_parameter_string option.");
                return 1;
            }

            if (distanceParameterString == null) {
                System.out.println("Please specify the distances using the --distance_parameter_string option.");
                return 1;
            }
        }

        if (hierarchyParameterString != null) {
            partitionConfig.groupSizes = Arrays.stream(hierarchyParameterString.split(":"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            int oldK = partitionConfig.k;
            partitionConfig.k = partitionConfig.groupSizes.stream().reduce(1, (a, b) -> a * b);

            if (oldK != partitionConfig.k) {
                System.out.println("number of processors defined through specified hierarchy does not match k!");
                System.out.println("please specify k as " + partitionConfig.k);
                return 1;
            }
        }

        if (distanceParameterString != null) {
            partitionConfig.distances = Arrays.stream(distanceParameterString.split(":"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }

        if (onlineDistances) {
            partitionConfig.distanceConstructionAlgorithm = DistanceConstructionAlgorithm.DIST_CONST_HIERARCHY_ONLINE;
        }

        if (filenameOutput != null) {
            partitionConfig.filenameOutput = filenameOutput;
        }

        if (initialPartitionOptimize) {
            partitionConfig.initialPartitionOptimize = true;
        }

        if (disableBalanceSingletons) {
            partitionConfig.useBalanceSingletons = false;
        }


        if (mhDisableNcCombine) {
            partitionConfig.mhDisableNcCombine = true;
        }

        if (mhDisableCrossCombine) {
            partitionConfig.mhDisableCrossCombine = true;
        }

        if (imbalance > 0.0) {
            partitionConfig.epsilon = imbalance;
        }

        if (mhDisableCombine) {
            partitionConfig.mhDisableCombine = true;
        }

        if (balanceEdges) {
            partitionConfig.balanceEdges = true;
        }

        if (mhOptimizeCommunicationVolume) {
            partitionConfig.mhOptimizeCommunicationVolume = true;
        }

        if (mhEnableTournamentSelection) {
            partitionConfig.mhEnableTournamentSelection = true;
        }

        if (amgIterations > 0) {
            partitionConfig.amgIterations = amgIterations;
        }

        if (maxInitialNsTries > 0) {
            partitionConfig.maxInitialNsTries = maxInitialNsTries;
        }

        if (maxFlowImprovSteps > 0) {
            partitionConfig.maxFlowImprovSteps = maxFlowImprovSteps;
        }

        if (regionFactorNodeSeparators > 0.0) {
            partitionConfig.regionFactorNodeSeparators = regionFactorNodeSeparators;
        }

        if (kabaEInternalBal > 0.0) {
            partitionConfig.kabaEInternalBal = kabaEInternalBal;
        }

        if (kabaInternalNoAugStepsAug > 0) {
            partitionConfig.kabaInternalNoAugStepsAug = kabaInternalNoAugStepsAug;
        }

        if (kaffpaPerfectlyBalancedRefinement) {
            partitionConfig.kaffpaPerfectlyBalancedRefinement = true;
        }

        if (!kabaDisableZeroWeightCycles) {
            partitionConfig.kabaEnableZeroWeightCycles = false;
        }

        if (kabaUnsuccIterations > 0) {
            partitionConfig.kabaUnsuccIterations = kabaUnsuccIterations;
        }

        if (kabaFlipPackings) {
            partitionConfig.kabaFlipPackings = true;
        }

        if (sepFlowsDisabled) {
            partitionConfig.sepFlowsDisabled = true;
        }

        if (sepFasterNs) {
            partitionConfig.fasterNs = true;
        }

        if (sepLocFmNoSnodes > 0) {
            partitionConfig.sepLocFmNoSnodes = sepLocFmNoSnodes;
        }

        if (sepNumVertStop > 0) {
            partitionConfig.sepNumVertStop = sepNumVertStop;
        }

        if (sepFmUnsuccSteps > 0) {
            partitionConfig.sepFmUnsuccSteps = sepFmUnsuccSteps;
        }

        if (sepNumFmReps > 0) {
            partitionConfig.sepNumFmReps = sepNumFmReps;
        }

        if (sepLocFmUnsuccSteps > 0) {
            partitionConfig.sepLocFmUnsuccSteps = sepLocFmUnsuccSteps;
        }

        if (sepNumLocFmReps > 0) {
            partitionConfig.sepNumLocFmReps = sepNumLocFmReps;
        }

        if (sepFmDisabled) {
            partitionConfig.sepFmDisabled = true;
        }

        if (sepLocFmDisabled) {
            partitionConfig.sepLocFmDisabled = true;
        }

        if (sepGreedyDisabled) {
            partitionConfig.sepGreedyDisabled = true;
        }

        if (sepFullBoundaryIp) {
            partitionConfig.sepFullBoundaryIp = true;
        }

        if (kabaLsearchP != null) {
            switch (kabaLsearchP) {
                case "coindiff":
                    partitionConfig.kabaLsearchP = MLSRule.COIN_DIFFTIE;
                    break;
                case "nocoindiff":
                    partitionConfig.kabaLsearchP = MLSRule.NOCOIN_DIFFTIE;
                    break;
                case "coinrnd":
                    partitionConfig.kabaLsearchP = MLSRule.COIN_RNDTIE;
                    break;
                case "nocoinrnd":
                    partitionConfig.kabaLsearchP = MLSRule.NOCOIN_RNDTIE;
                    break;
                default:
                    System.err.println("Invalid combine variant: \"" + kabaLsearchP + "\"");
                    return 1;
            }
        }

        if (maxT > 0) {
            partitionConfig.maxT = maxT;
        }

        if (maxIter > 0) {
            partitionConfig.maxIter = maxIter;
        }

        if (mhEnableTabuSearch) {
            partitionConfig.mhEnableGalCombine = true;
        }

        if (kabaPackingIterations > 0) {
            partitionConfig.kabaPackingIterations = kabaPackingIterations;
        }

        if (mhFlipCoin > 0) {
            partitionConfig.mhFlipCoin = mhFlipCoin;
        }

        if (mhInitialPopulationFraction > 0) {
            partitionConfig.mhInitialPopulationFraction = mhInitialPopulationFraction;
        }

        if (minipreps > 0) {
            partitionConfig.minipreps = minipreps;
        }

        if (mhEnableQuickstart) {
            partitionConfig.mhEnableQuickstart = true;
        }

        if (mhDisableDiversifyIslands) {
            partitionConfig.mhDisableDiversifyIslands = true;
        }

        if (gpaGrowInternal) {
            partitionConfig.gpaGrowPathsBetweenBlocks = false;
        }

        if (suppressOutput) {
            suppressProgramOutput = true;
        }

        if (mhPrintLog) {
            partitionConfig.mhPrintLog = true;
        }

        if (useBucketQueues) {
            partitionConfig.useBucketQueues = true;
        }

        if (recursiveBipartitioning) {
            recursive = true;
        }

        if (timeLimit > 0.0) {
            partitionConfig.timeLimit = timeLimit;
        }

        if (unsuccessfulReps > 0) {
            partitionConfig.noUnsucReps = unsuccessfulReps;
        }

        if (mhPoolSize > 0) {
            partitionConfig.mhPoolSize = mhPoolSize;
        }

        if (mhPenaltyForUnconnected) {
            partitionConfig.mhPenaltyForUnconnected = true;
        }

        if (mhEnableKabapE) {
            partitionConfig.kabapE = true;
        }

        if (initialPartitionOptimizeMultitryFmAlpha > 0) {
            partitionConfig.initialPartitionOptimizeMultitryFmAlpha = initialPartitionOptimizeMultitryFmAlpha;
        }

        if (initialPartitionOptimizeMultitryRounds > 0) {
            partitionConfig.initialPartitionOptimizeMultitryRounds = initialPartitionOptimizeMultitryRounds;
        }

        if (initialPartitionOptimizeFmLimits > 0) {
            partitionConfig.initialPartitionOptimizeFmLimits = initialPartitionOptimizeFmLimits;
        }

        if (mhDisableDiversify) {
            partitionConfig.mhDiversify = false;
        }

        if (mhDiversifyBest) {
            partitionConfig.mhDiversifyBest = true;
        }

        if (enforceBalance) {
            partitionConfig.kaffpaPerfectlyBalance = true;
        }

        if (mhPlainRepetitions) {
            partitionConfig.mhPlainRepetitions = true;
        }

        if (localPartitioningRepetitions > 0) {
            partitionConfig.localPartitioningRepetitions = localPartitioningRepetitions;
        }

        if (onlyFirstLevel) {
            partitionConfig.onlyFirstLevel = true;
        }

        if (mhCrossCombineOriginalK) {
            partitionConfig.mhCrossCombineOriginalK = true;
        }

        if (mhSequentialMode) {
            partitionConfig.mhNoMh = true;
        }

        if (enableOmp) {
            partitionConfig.enableOmp = true;
        }

        if (computeVertexSeparator) {
            partitionConfig.computeVertexSeparator = true;
        }

        if (mostBalancedFlows) {
            partitionConfig.mostBalancedMinimumCuts = true;
        }

        if (mostBalancedFlowsNodeSep) {
            partitionConfig.mostBalancedMinimumCutsNodeSep = true;
        }

        if (useWcycles) {
            partitionConfig.useWcycles = true;
        }

        if (enableConvergence) {
            partitionConfig.noChangeConvergence = true;
        }

        if (useFullMultigrid) {
            partitionConfig.useFullMultigrid = true;
        }

        if (useVcycle) {
            partitionConfig.useFullMultigrid = false;
            partitionConfig.useWcycles = false;
        }

        if (toposortIterations > 0) {
            partitionConfig.toposortIterations = toposortIterations;
        }

        if (bipartitionTries > 0) {
            partitionConfig.bipartitionTries = bipartitionTries;
        }

        if (bipartitionPostFmLimit > 0) {
            partitionConfig.bipartitionPostFmLimits = bipartitionPostFmLimit;
        }

        if (bipartitionPostMlLimit > 0) {
            partitionConfig.bipartitionPostMlLimits = bipartitionPostMlLimit;
        }

        if (disableMaxVertexWeightConstraint) {
            partitionConfig.disableMaxVertexWeightConstraint = true;
        }

        if (numVertStopFactor > 0) {
            partitionConfig.numVertStopFactor = numVertStopFactor;
        }

        if (localMultitryRounds > 0) {
            partitionConfig.localMultitryRounds = localMultitryRounds;
        }

        if (localMultitryFmAlpha > 0) {
            partitionConfig.localMultitryFmAlpha = localMultitryFmAlpha;
        }

        if (wcycleNoNewInitialPartitioning) {
            partitionConfig.noNewInitialPartitioning = true;
        }

        if (graphWeighted) {
            isGraphWeighted = true;
        }

        if (disableRefinedBubbling) {
            partitionConfig.refinedBubbling = false;
        }

        if (inputPartition != null) {
            partitionConfig.inputPartition = inputPartition;
        }

        if (globalCycleIterations > 0) {
            partitionConfig.globalCycleIterations = globalCycleIterations;
        }

        if (levelSplit > 0) {
            partitionConfig.levelSplit = levelSplit;
        }

        if (disableQgraphRefinement) {
            partitionConfig.quotientGraphRefinementDisabled = true;
        }

        if (bubblingIterations > 0) {
            partitionConfig.bubblingIterations = bubblingIterations;
        }

        if (kwayFmLimits > 0) {
            partitionConfig.kwayFmSearchLimit = kwayFmLimits;
        }

        if (kwayRounds > 0) {
            partitionConfig.kwayRounds = kwayRounds;
        }

        if (enableCornerRefinement) {
            partitionConfig.cornerRefinementEnabled = true;
        }

        if (matchIslands) {
            partitionConfig.matchIslands = true;
        }

        if (aggressiveRandomLevels > 0) {
            partitionConfig.aggressiveRandomLevels = aggressiveRandomLevels;
        }

        if (rateFirstLevelInnerOuter) {
            partitionConfig.rateFirstLevelInnerOuter = true;
        }

        if (userSeed > 0) {
            partitionConfig.seed = userSeed;
        }

        if (fmSearchLimit > 0) {
            partitionConfig.fmSearchLimit = fmSearchLimit;
        }

        if (bankAccountFactor > 0.0) {
            partitionConfig.bankAccountFactor = bankAccountFactor;
        }

        if (flowRegionFactor > 0.0) {
            partitionConfig.flowRegionFactor = flowRegionFactor;
        }

        if (kwayAdaptiveLimitsAlpha > 0.0) {
            partitionConfig.kwayAdaptiveLimitsAlpha = kwayAdaptiveLimitsAlpha;
        }

        if (imbalance > 0.0) {
            partitionConfig.imbalance = imbalance;
        }

        if (initialPartitioningRepetitions > 0) {
            partitionConfig.initialPartitioningRepetitions = initialPartitioningRepetitions;
        }

        if (edgeRatingTiebreaking) {
            partitionConfig.edgeRatingTiebreaking = true;
        }

        if (firstLevelRandomMatching) {
            partitionConfig.firstLevelRandomMatching = true;
        } else {
            partitionConfig.firstLevelRandomMatching = false;
        }

        if (kabaNegCycleAlgorithm != null) {
            switch (kabaNegCycleAlgorithm) {
                case "playfield":
                    partitionConfig.cycleRefinementAlgorithm = CycleRefinementAlgorithm.CYCLE_REFINEMENT_ALGORITHM_PLAYFIELD;
                    break;
                case "ultramodel":
                    partitionConfig.cycleRefinementAlgorithm = CycleRefinementAlgorithm.CYCLE_REFINEMENT_ALGORITHM_ULTRA_MODEL;
                    break;
                case "ultramodelplus":
                    partitionConfig.cycleRefinementAlgorithm = CycleRefinementAlgorithm.CYCLE_REFINEMENT_ALGORITHM_ULTRA_MODEL_PLUS;
                    break;
                default:
                    System.err.println("Invalid balanced refinement operator: \"" + kabaNegCycleAlgorithm + "\"");
                    return 1;
            }
        }

        if (sepEdgeRatingDuringIp != null) {
            switch (sepEdgeRatingDuringIp) {
                case "expansionstar":
                    partitionConfig.sepEdgeRatingDuringIp = EdgeRating.EXPANSIONSTAR;
                    break;
                case "expansionstar2":
                    partitionConfig.sepEdgeRatingDuringIp = EdgeRating.EXPANSIONSTAR2;
                    break;
                case "expansionstar2algdist":
                    partitionConfig.sepEdgeRatingDuringIp = EdgeRating.EXPANSIONSTAR2ALGDIST;
                    break;
                case "geom":
                    partitionConfig.sepEdgeRatingDuringIp = EdgeRating.PSEUDOGEOM;
                    break;
                case "sepaddx":
                    partitionConfig.sepEdgeRatingDuringIp = EdgeRating.SEPARATOR_ADDX;
                    break;
                case "sepmultx":
                    partitionConfig.sepEdgeRatingDuringIp = EdgeRating.SEPARATOR_MULTX;
                    break;
                case "sepmax":
                    partitionConfig.sepEdgeRatingDuringIp = EdgeRating.SEPARATOR_MAX;
                    break;
                case "seplog":
                    partitionConfig.sepEdgeRatingDuringIp = EdgeRating.SEPARATOR_LOG;
                    break;
                case "r1":
                    partitionConfig.sepEdgeRatingDuringIp = EdgeRating.SEPARATOR_R1;
                    break;
                case "r2":
                    partitionConfig.sepEdgeRatingDuringIp = EdgeRating.SEPARATOR_R2;
                    break;
                case "r3":
                    partitionConfig.sepEdgeRatingDuringIp = EdgeRating.SEPARATOR_R3;
                    break;
                case "r4":
                    partitionConfig.sepEdgeRatingDuringIp = EdgeRating.SEPARATOR_R4;
                    break;
                case "r5":
                    partitionConfig.sepEdgeRatingDuringIp = EdgeRating.SEPARATOR_R5;
                    break;
                case "r6":
                    partitionConfig.sepEdgeRatingDuringIp = EdgeRating.SEPARATOR_R6;
                    break;
                case "r7":
                    partitionConfig.sepEdgeRatingDuringIp = EdgeRating.SEPARATOR_R7;
                    break;
                case "r8":
                    partitionConfig.sepEdgeRatingDuringIp = EdgeRating.SEPARATOR_R8;
                    break;
                default:
                    System.err.println("Invalid edge rating variant: \"" + sepEdgeRatingDuringIp + "\"");
                    return 1;
            }
        }

        if (edgeRating != null) {
            switch (edgeRating) {
                case "expansionstar":
                    partitionConfig.edgeRating = EdgeRating.EXPANSIONSTAR;
                    break;
                case "expansionstar2":
                    partitionConfig.edgeRating = EdgeRating.EXPANSIONSTAR2;
                    break;
                case "weight":
                    partitionConfig.edgeRating = EdgeRating.WEIGHT;
                    break;
                case "realweight":
                    partitionConfig.edgeRating = EdgeRating.REALWEIGHT;
                    break;
                case "expansionstar2algdist":
                    partitionConfig.edgeRating = EdgeRating.EXPANSIONSTAR2ALGDIST;
                    break;
                case "geom":
                    partitionConfig.edgeRating = EdgeRating.PSEUDOGEOM;
                    break;
                case "sepaddx":
                    partitionConfig.edgeRating = EdgeRating.SEPARATOR_ADDX;
                    break;
                case "sepmultx":
                    partitionConfig.edgeRating = EdgeRating.SEPARATOR_MULTX;
                    break;
                case "sepmax":
                    partitionConfig.edgeRating = EdgeRating.SEPARATOR_MAX;
                    break;
                case "seplog":
                    partitionConfig.edgeRating = EdgeRating.SEPARATOR_LOG;
                    break;
                case "r1":
                    partitionConfig.edgeRating = EdgeRating.SEPARATOR_R1;
                    break;
                case "r2":
                    partitionConfig.edgeRating = EdgeRating.SEPARATOR_R2;
                    break;
                case "r3":
                    partitionConfig.edgeRating = EdgeRating.SEPARATOR_R3;
                    break;
                case "r4":
                    partitionConfig.edgeRating = EdgeRating.SEPARATOR_R4;
                    break;
                case "r5":
                    partitionConfig.edgeRating = EdgeRating.SEPARATOR_R5;
                    break;
                case "r6":
                    partitionConfig.edgeRating = EdgeRating.SEPARATOR_R6;
                    break;
                case "r7":
                    partitionConfig.edgeRating = EdgeRating.SEPARATOR_R7;
                    break;
                case "r8":
                    partitionConfig.edgeRating = EdgeRating.SEPARATOR_R8;
                    break;
                default:
                    System.err.println("Invalid edge rating variant: \"" + edgeRating + "\"");
                    return 1;
            }
        }

        if (bipartitionAlgorithm != null) {
            switch (bipartitionAlgorithm) {
                case "bfs":
                    partitionConfig.bipartitionAlgorithm = BipartitionAlgorithm.BIPARTITION_BFS;
                    break;
                case "fm":
                    partitionConfig.bipartitionAlgorithm = BipartitionAlgorithm.BIPARTITION_FM;
                    break;
                default:
                    System.err.println("Invalid bipartition algorithm: \"" + bipartitionAlgorithm + "\"");
                    return 1;
            }
        }

        if (refinementSchedulingAlgorithm != null) {
            switch (refinementSchedulingAlgorithm) {
                case "fast":
                    partitionConfig.refinementSchedulingAlgorithm = RefinementSchedulingAlgorithm.REFINEMENT_SCHEDULING_FAST;
                    break;
                case "active_blocks":
                    partitionConfig.refinementSchedulingAlgorithm = RefinementSchedulingAlgorithm.REFINEMENT_SCHEDULING_ACTIVE_BLOCKS;
                    break;
                case "active_blocks_kway":
                    partitionConfig.refinementSchedulingAlgorithm = RefinementSchedulingAlgorithm.REFINEMENT_SCHEDULING_ACTIVE_BLOCKS_REF_KWAY;
                    break;
                default:
                    System.err.println("Invalid refinement scheduling variant: \"" + refinementSchedulingAlgorithm + "\"");
                    return 1;
            }
        }

        if (stopRule != null) {
            switch (stopRule) {
                case "simple":
                    partitionConfig.stopRule = StopRule.STOP_RULE_SIMPLE;
                    break;
                case "multiplek":
                    partitionConfig.stopRule = StopRule.STOP_RULE_MULTIPLE_K;
                    break;
                case "strong":
                    partitionConfig.stopRule = StopRule.STOP_RULE_STRONG;
                    break;
                default:
                    System.err.println("Invalid stop rule: \"" + stopRule + "\"");
                    return 1;
            }
        }

        if (kwaySearchStopRule != null) {
            switch (kwaySearchStopRule) {
                case "simple":
                    partitionConfig.kwayStopRule = KWayStopRule.KWAY_SIMPLE_STOP_RULE;
                    break;
                case "adaptive":
                    partitionConfig.kwayStopRule = KWayStopRule.KWAY_ADAPTIVE_STOP_RULE;
                    break;
                default:
                    System.err.println("Invalid kway stop rule: \"" + kwaySearchStopRule + "\"");
                    return 1;
            }
        }

        if (permutationQuality != null) {
            switch (permutationQuality) {
                case "none":
                    partitionConfig.permutationQuality = PermutationQuality.PERMUTATION_QUALITY_NONE;
                    break;
                case "fast":
                    partitionConfig.permutationQuality = PermutationQuality.PERMUTATION_QUALITY_FAST;
                    break;
                case "good":
                    partitionConfig.permutationQuality = PermutationQuality.PERMUTATION_QUALITY_GOOD;
                    break;
                default:
                    System.err.println("Invalid permutation quality variant: \"" + permutationQuality + "\"");
                    return 1;
            }
        }

        if (permutationDuringRefinement != null) {
            switch (permutationDuringRefinement) {
                case "none":
                    partitionConfig.permutationDuringRefinement = PermutationQuality.PERMUTATION_QUALITY_NONE;
                    break;
                case "fast":
                    partitionConfig.permutationDuringRefinement = PermutationQuality.PERMUTATION_QUALITY_FAST;
                    break;
                case "good":
                    partitionConfig.permutationDuringRefinement = PermutationQuality.PERMUTATION_QUALITY_GOOD;
                    break;
                default:
                    System.err.println("Invalid permutation quality during refinement variant: \"" + permutationDuringRefinement + "\"");
                    return 1;
            }
        }

        if (matchingType != null) {
            switch (matchingType) {
                case "random":
                    partitionConfig.matchingType = MatchingType.MATCHING_RANDOM;
                    break;
                case "gpa":
                    partitionConfig.matchingType = MatchingType.MATCHING_GPA;
                    break;
                case "randomgpa":
                    partitionConfig.matchingType = MatchingType.MATCHING_RANDOM_GPA;
                    break;
                default:
                    System.err.println("Invalid matching variant: \"" + matchingType + "\"");
                    return 1;
            }
        }

        if (refinementType != null) {
            switch (refinementType) {
                case "fm":
                    partitionConfig.refinementType = RefinementType.REFINEMENT_TYPE_FM;
                    break;
                case "fm_flow":
                    partitionConfig.refinementType = RefinementType.REFINEMENT_TYPE_FM_FLOW;
                    break;
                case "flow":
                    partitionConfig.refinementType = RefinementType.REFINEMENT_TYPE_FM;
                    break;
                default:
                    System.err.println("Invalid refinement type variant: \"" + refinementType + "\"");
                    return 1;
            }
        }

        if (initialPartition != null) {
            if ("recursive".equals(initialPartition)) {
                partitionConfig.initialPartitioningType = InitialPartitioningType.INITIAL_PARTITIONING_RECPARTITION;
            } else {
                System.err.println("Invalid initial partition variant: \"" + initialPartition + "\"");
                return 1;
            }
        }

        if (labelPropagationIterations > 0) {
            partitionConfig.labelIterations = labelPropagationIterations;
        }

        if (clusterUpperbound > 0) {
            partitionConfig.clusterUpperbound = clusterUpperbound;
        } else {
            partitionConfig.clusterUpperbound = Integer.MAX_VALUE / 2;
        }

        if (dissectionRecLimit > 0) {
            partitionConfig.dissectionRecLimit = dissectionRecLimit;
        } else {
            partitionConfig.dissectionRecLimit = 120;
        }

        if (disableReductions) {
            partitionConfig.disableReductions = true;
        } else {
            partitionConfig.disableReductions = false;
        }

        if (reductionOrder != null) {
            List<Integer> reductions = Arrays.stream(reductionOrder.split("\\s+"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            partitionConfig.reductionOrder = reductions.stream()
                    .map(value -> NestedDissectionReductionType.values()[value])
                    .collect(Collectors.toList());
        } else {
            partitionConfig.reductionOrder = Arrays.asList(NestedDissectionReductionType.SIMPLICIAL_NODES,
                    NestedDissectionReductionType.DEGREE_2_NODES);
        }

        if (convergenceFactor > 0.0) {
            partitionConfig.convergenceFactor = convergenceFactor;
        } else {
            partitionConfig.convergenceFactor = 1.0;
        }

        if (maxSimplicialDegree > 0) {
            partitionConfig.maxSimplicialDegree = maxSimplicialDegree;
        } else {
            partitionConfig.maxSimplicialDegree = Integer.MAX_VALUE;
        }

        if (ilpMode != null) {
            switch (ilpMode) {
                case "gain":
                    partitionConfig.ilpMode = OptimizationMode.GAIN;
                    break;
                case "overlap":
                    partitionConfig.ilpMode = OptimizationMode.OVERLAP;
                    break;
                case "boundary":
                    partitionConfig.ilpMode = OptimizationMode.BOUNDARY;
                    break;
                case "trees":
                    partitionConfig.ilpMode = OptimizationMode.TREES;
                    break;
                default:
                    System.err.println("Invalid ilp mode \"" + ilpMode + "\"");
            }
        } else {
            partitionConfig.ilpMode = OptimizationMode.GAIN;
        }

        if (ilpMinGain > 0) {
            partitionConfig.ilpMinGain = ilpMinGain;
        } else {
            partitionConfig.ilpMinGain = -1;
        }

        if (ilpBfsDepth > 0) {
            partitionConfig.ilpBfsDepth = ilpBfsDepth;
        } else {
            partitionConfig.ilpBfsDepth = 2;
        }

        if (ilpOverlapPresets != null) {
            switch (ilpOverlapPresets) {
                case "none":
                    partitionConfig.ilpOverlapPresets = OverlapPresets.NONE;
                    break;
                case "random":
                    partitionConfig.ilpOverlapPresets = OverlapPresets.RANDOM;
                    break;
                case "noequal":
                    partitionConfig.ilpOverlapPresets = OverlapPresets.NOEQUAL;
                    break;
                case "center":
                    partitionConfig.ilpOverlapPresets = OverlapPresets.CENTER;
                    break;
                case "heaviest":
                    partitionConfig.ilpOverlapPresets = OverlapPresets.HEAVIEST;
                    break;
                default:
                    System.err.println("Invalid ilp overlap preset \"" + ilpOverlapPresets + "\"");
            }
        } else {
            partitionConfig.ilpOverlapPresets = OverlapPresets.NOEQUAL;
        }

        if (ilpLimitNonzeroes > 0) {
            partitionConfig.ilpLimitNonzeroes = ilpLimitNonzeroes;
        } else {
            partitionConfig.ilpLimitNonzeroes = 5000000;
        }

        if (ilpOverlapRuns > 0) {
            partitionConfig.ilpOverlapRuns = ilpOverlapRuns;
        } else {
            partitionConfig.ilpOverlapRuns = 3;
        }

        if (ilpTimeout > 0) {
            partitionConfig.ilpTimeout = ilpTimeout;
        } else {
            partitionConfig.ilpTimeout = 7200;
        }

        return 0;
    }
}
