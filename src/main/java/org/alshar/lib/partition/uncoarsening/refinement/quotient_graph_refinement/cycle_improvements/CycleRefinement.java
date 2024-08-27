package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.cycle_improvements;

import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.uncoarsening.refinement.Refinement;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.kway_graph_refinement.KWayGraphRefinementCommons;

public class CycleRefinement extends Refinement {

    private AdvancedModels advancedModelling;

    public CycleRefinement() {
        this.advancedModelling = new AdvancedModels();
    }

    public int performRefinement(PartitionConfig config, GraphAccess G, CompleteBoundary boundary) {
        int overallGain = 0;
        PartitionConfig copy = new PartitionConfig(config);

        switch (config.getCycleRefinementAlgorithm()) {
            case CYCLE_REFINEMENT_ALGORITHM_ULTRA_MODEL:
                overallGain = greedyUltraModel(copy, G, boundary);
                break;
            case CYCLE_REFINEMENT_ALGORITHM_ULTRA_MODEL_PLUS:
                overallGain = greedyUltraModelPlus(copy, G, boundary);
                break;
            case CYCLE_REFINEMENT_ALGORITHM_PLAYFIELD:
                // Dropbox for new algorithms
                overallGain = playfieldAlgorithm(copy, G, boundary);
                break;
        }

        return overallGain;
    }

    private int playfieldAlgorithm(PartitionConfig config, GraphAccess G, CompleteBoundary boundary) {
        greedyUltraModel(config, G, boundary);
        greedyUltraModelPlus(config, G, boundary);
        return 0;
    }

    private int greedyUltraModel(PartitionConfig config, GraphAccess G, CompleteBoundary boundary) {
        AugmentedQGraphFabric augmentedFabric = new AugmentedQGraphFabric();
        int s = config.getKabaInternalNoAugStepsAug();
        boolean somethingChanged = false;
        boolean overloaded = false;
        int unsuccCount = 0;

        do {
            AugmentedQGraph aqg = new AugmentedQGraph();
            boolean plus = false; // Set 'plus' to an appropriate value based on your logic

            // Pass the missing 'plus' argument
            augmentedFabric.buildAugmentedQuotientGraph(config, G, boundary, aqg, s, false, plus);
            somethingChanged = advancedModelling.computeVertexMovementsUltraModel(config, G, boundary, aqg, s, false);

            if (somethingChanged) {
                unsuccCount = 0;
            } else {
                unsuccCount++;
            }

            if (unsuccCount > 2 && unsuccCount <= config.getKabaUnsuccIterations() && config.isKabaEnableZeroWeightCycles()) {
                somethingChanged = advancedModelling.computeVertexMovementsUltraModel(config, G, boundary, aqg, s, true);
            }

            if (unsuccCount >= config.getKabaUnsuccIterations()) {
                GraphAccess GBar = new GraphAccess();
                boundary.getUnderlyingQuotientGraph(GBar);
                overloaded = false;

                // Iterate over the nodes in GBar
                for (int block = 0; block < GBar.numberOfNodes(); block++) {
                    if (boundary.getBlockWeight(block) > config.getUpperBoundPartition()) {
                        overloaded = true;
                        break;
                    }
                }

                if (overloaded) {
                    AugmentedQGraph aqgRebal = new AugmentedQGraph();
                    boolean movesAlreadyPerformed = augmentedFabric.buildAugmentedQuotientGraph(config, G, boundary, aqgRebal, s, true, plus);

                    if (!movesAlreadyPerformed) {
                        advancedModelling.computeVertexMovementsRebalance(config, G, boundary, aqgRebal, s);
                    }
                }
            }
        } while (unsuccCount < config.getKabaUnsuccIterations() || overloaded);

        return 0;
    }

    private int greedyUltraModelPlus(PartitionConfig config, GraphAccess G, CompleteBoundary boundary) {
        int s = config.getKabaInternalNoAugStepsAug();
        boolean somethingChanged = false;
        boolean overloaded = false;

        AugmentedQGraphFabric augmentedFabric = new AugmentedQGraphFabric();
        boolean firstLevel = true;

        for (int node = 0; node < G.numberOfNodes(); node++) {
            if (G.getNodeWeight(node) != 1) {
                firstLevel = false;
                break;
            }
        }

        int unsuccCount = 0;

        do {
            AugmentedQGraph aqg = new AugmentedQGraph();
            augmentedFabric.buildAugmentedQuotientGraph(config, G, boundary, aqg, s, false, true);
            somethingChanged = advancedModelling.computeVertexMovementsUltraModel(config, G, boundary, aqg, s, false);

            if (somethingChanged) {
                unsuccCount = 0;
            } else {
                unsuccCount++;
            }

            if (unsuccCount > 2 && unsuccCount < 19) {
                somethingChanged = advancedModelling.computeVertexMovementsUltraModel(config, G, boundary, aqg, s, true);
            }

            if (unsuccCount > 19 && firstLevel) {
                GraphAccess GBar = new GraphAccess();
                boundary.getUnderlyingQuotientGraph(GBar);
                overloaded = false;

                for (int block = 0; block < GBar.numberOfNodes(); block++) {
                    if (boundary.getBlockWeight(block) > config.getUpperBoundPartition()) {
                        overloaded = true;
                        break;
                    }
                }


                if (overloaded) {
                    AugmentedQGraph aqgRebal = new AugmentedQGraph();
                    boolean movesPerformed = augmentedFabric.buildAugmentedQuotientGraph(config, G, boundary, aqgRebal, s, true, true);

                    if (!movesPerformed) {
                        advancedModelling.computeVertexMovementsRebalance(config, G, boundary, aqgRebal, s);
                    }
                }
            }
        } while (unsuccCount < 20 || overloaded);

        return 0;
    }
}
