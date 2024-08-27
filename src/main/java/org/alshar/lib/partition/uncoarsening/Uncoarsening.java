package org.alshar.lib.partition.uncoarsening;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.GraphHierarchy;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.MixedRefinement;
import org.alshar.lib.partition.uncoarsening.refinement.Refinement;
import org.alshar.lib.partition.uncoarsening.refinement.label_propagation_refinement.LabelPropagationRefinement;
import org.alshar.lib.partition.uncoarsening.refinement.node_separators.FmNsLocalSearch;
import org.alshar.lib.partition.uncoarsening.refinement.node_separators.LocalizedFmNsLocalSearch;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.PartialBoundary;
import org.alshar.lib.partition.uncoarsening.separator.VertexSeparatorAlgorithm;
import org.alshar.lib.tools.GraphPartitionAssertions;
import org.alshar.lib.tools.QualityMetrics;
import org.alshar.lib.tools.RandomFunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Uncoarsening {

    public Uncoarsening() {
    }

    public int performUncoarsening(PartitionConfig config, GraphHierarchy hierarchy) {
        if (config.isModeNodeSeparators()) {
            if (config.isFasterNs()) {
                return performUncoarseningNodeSeparatorFast(config, hierarchy);
            } else {
                return performUncoarseningNodeSeparator(config, hierarchy);
            }
        } else {
            return performUncoarseningCut(config, hierarchy);
        }
    }

    public int performUncoarseningCut(PartitionConfig config, GraphHierarchy hierarchy) {
        int improvement = 0;

        PartitionConfig cfg = new PartitionConfig(config);
        Refinement refine = null;

        if (config.isLabelPropagationRefinement()) {
            refine = new LabelPropagationRefinement();
        } else {
            refine = new MixedRefinement();
        }

        GraphAccess coarsest = hierarchy.getCoarsest();
        System.out.println("log> unrolling graph with " + coarsest.numberOfNodes());

        CompleteBoundary finerBoundary = null;
        CompleteBoundary coarserBoundary = null;
        if (!config.isLabelPropagationRefinement()) {
            coarserBoundary = new CompleteBoundary(coarsest);
            coarserBoundary.build();
        }

        double factor = config.getBalanceFactor();
        cfg.setUpperBoundPartition((int) ((!hierarchy.isEmpty() ? factor : 0) + 1.0 * config.getUpperBoundPartition()));
        improvement += refine.performRefinement(cfg, coarsest, coarserBoundary);

        int coarserNoNodes = coarsest.numberOfNodes();
        GraphAccess finest = null;
        GraphAccess toDelete = null;
        int hierarchyDepth = hierarchy.size();

        while (!hierarchy.isEmpty()) {
            GraphAccess G = hierarchy.popFinerAndProject();
            System.out.println("log> unrolling graph with " + G.numberOfNodes());

            if (!config.isLabelPropagationRefinement()) {
                finerBoundary = new CompleteBoundary(G);
                finerBoundary.buildFromCoarser(coarserBoundary, coarserNoNodes, hierarchy.getMappingOfCurrentFiner());
            }

            double curFactor = factor / (hierarchyDepth - hierarchy.size());
            cfg.setUpperBoundPartition((int)((!hierarchy.isEmpty() ? curFactor : 0) + 1.0 * config.getUpperBoundPartition()));
            System.out.println("cfg upperbound " + cfg.getUpperBoundPartition());
            improvement += refine.performRefinement(cfg, G, finerBoundary);
            GraphPartitionAssertions.assertGraphHasKWayPartition(config, G);

            if (config.isUseBalanceSingletons() && !config.isLabelPropagationRefinement()) {
                finerBoundary.balanceSingletons(config, G);
            }

            if (!config.isLabelPropagationRefinement()) {
                if (coarserBoundary != null) {
                    coarserBoundary = null;
                }
                coarserBoundary = finerBoundary;
            }

            coarserNoNodes = G.numberOfNodes();

            if (toDelete != null) {
                toDelete = null;
            }
            if (!hierarchy.isEmpty()) {
                toDelete = G;
            }

            finest = G;
        }

        if (config.isComputeVertexSeparator()) {
            System.out.println("now computing a vertex separator from the given edge separator");
            VertexSeparatorAlgorithm vsa = new VertexSeparatorAlgorithm();
            List<Integer> overallSeparator = new ArrayList<>();
            vsa.computeVertexSeparator(config, finest, finerBoundary, overallSeparator);
        }

        if (refine != null) {
            refine = null;
        }
        if (finerBoundary != null) {
            finerBoundary = null;
        }
        coarsest = null;

        return improvement;
    }

    public int performUncoarseningNodeSeparator(PartitionConfig config, GraphHierarchy hierarchy) {
        System.out.println("log> starting uncoarsening ---------------");
        PartitionConfig cfg = new PartitionConfig(config);
        GraphAccess coarsest = hierarchy.getCoarsest();
        QualityMetrics qm = new QualityMetrics();
        System.out.println("log> unrolling graph with " + coarsest.numberOfNodes());

        if (!config.isSepFmDisabled()) {
            for (int i = 0; i < config.getSepNumFmReps(); i++) {
                FmNsLocalSearch fmnsls = new FmNsLocalSearch();
                fmnsls.performRefinement(config, coarsest, false, 0);  // Adjusted call

                int rndBlock = RandomFunctions.nextInt(0, 1);
                fmnsls.performRefinement(config, coarsest, true, rndBlock);  // Adjusted call
                fmnsls.performRefinement(config, coarsest, true, rndBlock == 0 ? 1 : 0);  // Adjusted call
            }
        }

        if (!config.isSepFlowsDisabled()) {
            for (int i = 0; i < config.getMaxFlowImprovSteps(); i++) {
                VertexSeparatorAlgorithm vsa = new VertexSeparatorAlgorithm();
                List<Integer> separator = new ArrayList<>();
                for (int node = 0; node < coarsest.numberOfNodes(); node++) {
                    if (coarsest.getPartitionIndex(node) == 2) {
                        separator.add(node);
                    }
                }

                List<Integer> outputSeparator = new ArrayList<>();
                int improvement = vsa.improveVertexSeparator(config, coarsest, separator, outputSeparator);
                if (improvement == 0) break;
            }
        }

        GraphAccess toDelete = null;
        while (!hierarchy.isEmpty()) {
            GraphAccess G = hierarchy.popFinerAndProject();
            System.out.println("log> unrolling graph with " + G.numberOfNodes());

            if (!config.isSepFmDisabled()) {
                for (int i = 0; i < config.getSepNumFmReps(); i++) {
                    FmNsLocalSearch fmnsls = new FmNsLocalSearch();
                    fmnsls.performRefinement(config, G, false, 0);  // Adjusted call

                    int rndBlock = RandomFunctions.nextInt(0, 1);
                    fmnsls.performRefinement(config, G, true, rndBlock);  // Adjusted call
                    fmnsls.performRefinement(config, G, true, rndBlock == 0 ? 1 : 0);  // Adjusted call
                }
            }

            if (!config.isSepLocFmDisabled()) {
                for (int i = 0; i < config.getSepNumLocFmReps(); i++) {
                    LocalizedFmNsLocalSearch fmnsls = new LocalizedFmNsLocalSearch();
                    fmnsls.performRefinement(config, G, false, 0);  // Adjusted call

                    int rndBlock = RandomFunctions.nextInt(0, 1);
                    fmnsls.performRefinement(config, G, true, rndBlock);  // Adjusted call
                    fmnsls.performRefinement(config, G, true, rndBlock == 0 ? 1 : 0);  // Adjusted call
                }
            }

            if (!config.isSepFlowsDisabled()) {
                for (int i = 0; i < config.getMaxFlowImprovSteps(); i++) {
                    VertexSeparatorAlgorithm vsa = new VertexSeparatorAlgorithm();
                    List<Integer> separator = new ArrayList<>();
                    for (int node = 0; node < G.numberOfNodes(); node++) {
                        if (G.getPartitionIndex(node) == 2) {
                            separator.add(node);
                        }
                    }

                    List<Integer> outputSeparator = new ArrayList<>();
                    int improvement = vsa.improveVertexSeparator(config, G, separator, outputSeparator);
                    if (improvement == 0) break;
                }
            }

            if (toDelete != null) {
                toDelete = null;
            }
            if (!hierarchy.isEmpty()) {
                toDelete = G;
            }
        }
        coarsest = null;

        return 0;
    }


    public int performUncoarseningNodeSeparatorFast(PartitionConfig config, GraphHierarchy hierarchy) {
        System.out.println("log> starting uncoarsening ---------------");
        PartitionConfig cfg = new PartitionConfig(config);
        GraphAccess coarsest = hierarchy.getCoarsest();
        System.out.println("log> unrolling graph with " + coarsest.numberOfNodes());

        List<Integer> blockWeights = new ArrayList<>(Collections.nCopies(3, 0));
        PartialBoundary currentSeparator = new PartialBoundary();

        // Compute coarsest block weights and separator
        for (int node = 0; node < coarsest.numberOfNodes(); node++) {
            blockWeights.set(coarsest.getPartitionIndex(node), blockWeights.get(coarsest.getPartitionIndex(node)) + coarsest.getNodeWeight(node));
            if (coarsest.getPartitionIndex(node) == 2) {
                currentSeparator.insert(node);
            }
        }

        List<Boolean> movedOutOfS = new ArrayList<>(Collections.nCopies(coarsest.numberOfNodes(), false));
        if (!config.isSepFmDisabled()) {
            for (int i = 0; i < config.getSepNumFmReps(); i++) {
                FmNsLocalSearch fmnsls = new FmNsLocalSearch();
                fmnsls.performRefinement(config, coarsest, blockWeights, movedOutOfS, currentSeparator, false, 0);


                int rndBlock = RandomFunctions.nextInt(0, 1);
                fmnsls.performRefinement(config, coarsest, blockWeights, movedOutOfS, currentSeparator, true, rndBlock);
                fmnsls.performRefinement(config, coarsest, blockWeights, movedOutOfS, currentSeparator, true, rndBlock == 0 ? 1 : 0);
            }
        }

        if (!config.isSepFlowsDisabled()) {
            for (int i = 0; i < config.getMaxFlowImprovSteps(); i++) {
                VertexSeparatorAlgorithm vsa = new VertexSeparatorAlgorithm();
                int improvement = vsa.improveVertexSeparator(config, coarsest, blockWeights, currentSeparator);
                if (improvement == 0) break;
            }
        }

        GraphAccess toDelete = null;
        while (!hierarchy.isEmpty()) {
            GraphAccess G = hierarchy.popFinerAndProjectNs(currentSeparator);
            System.out.println("log> unrolling graph with " + G.numberOfNodes());

            movedOutOfS = new ArrayList<>(Collections.nCopies(G.numberOfNodes(), false));
            if (!config.isSepFmDisabled()) {
                for (int i = 0; i < config.getSepNumFmReps(); i++) {
                    FmNsLocalSearch fmnsls = new FmNsLocalSearch();
                    int improvement = 0;
                    improvement += fmnsls.performRefinement(config, G, blockWeights, movedOutOfS, currentSeparator, false, 4);

                    int rndBlock = RandomFunctions.nextInt(0, 1);
                    improvement += fmnsls.performRefinement(config, G, blockWeights, movedOutOfS, currentSeparator, true, rndBlock);
                    improvement += fmnsls.performRefinement(config, G, blockWeights, movedOutOfS, currentSeparator, true, rndBlock == 0 ? 1 : 0);
                    if (improvement == 0) break;
                }
            }

            if (!config.isSepLocFmDisabled()) {
                for (int i = 0; i < config.getSepNumLocFmReps(); i++) {
                    LocalizedFmNsLocalSearch fmnsls = new LocalizedFmNsLocalSearch();
                    int improvement = 0;
                    improvement += fmnsls.performRefinement(config, G, blockWeights, movedOutOfS, currentSeparator, false, 4);

                    int rndBlock = RandomFunctions.nextInt(0, 1);
                    improvement += fmnsls.performRefinement(config, G, blockWeights, movedOutOfS, currentSeparator, true, rndBlock);
                    improvement += fmnsls.performRefinement(config, G, blockWeights, movedOutOfS, currentSeparator, true, rndBlock == 0 ? 1 : 0);
                }
            }

            if (!config.isSepFlowsDisabled()) {
                for (int i = 0; i < config.getMaxFlowImprovSteps(); i++) {
                    VertexSeparatorAlgorithm vsa = new VertexSeparatorAlgorithm();
                    int improvement = vsa.improveVertexSeparator(config, G, blockWeights, currentSeparator);
                    if (improvement == 0) break;
                }
            }

            if (toDelete != null) {
                toDelete = null;
            }
            if (!hierarchy.isEmpty()) {
                toDelete = G;
            }
        }
        coarsest = null;

        return 0;
    }
}
