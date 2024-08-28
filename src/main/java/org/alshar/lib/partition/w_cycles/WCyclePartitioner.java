package org.alshar.lib.partition.w_cycles;
import org.alshar.lib.data_structure.CoarseMapping;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.coarsening.CoarseningConfigurator;
import org.alshar.lib.partition.coarsening.Contraction;
import org.alshar.lib.partition.coarsening.EdgeRatings;
import org.alshar.lib.partition.coarsening.matching.Matching;
import org.alshar.lib.partition.coarsening.stop_rules.MultipleKStopRule;
import org.alshar.lib.partition.coarsening.stop_rules.SimpleStopRule;
import org.alshar.lib.partition.initial_partitioning.InitialPartitioning;
import org.alshar.lib.partition.uncoarsening.refinement.MixedRefinement;
import org.alshar.lib.partition.uncoarsening.refinement.Refinement;
import org.alshar.lib.partition.uncoarsening.refinement.label_propagation_refinement.LabelPropagationRefinement;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import org.alshar.lib.partition.coarsening.stop_rules.StopRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class WCyclePartitioner {

    private int level;
    private int deepestLevel;
    private StopRule coarseningStopRule;
    private Map<Integer, Boolean> haveBeenLevelDown;

    public WCyclePartitioner() {
        this.level = 0;
        this.haveBeenLevelDown = new HashMap<>();
    }

    public int performPartitioning(PartitionConfig config, GraphAccess G) {
        PartitionConfig cfg = new PartitionConfig(config);

        if (config.getStopRule() == org.alshar.lib.enums.StopRule.STOP_RULE_SIMPLE) {
            coarseningStopRule = new SimpleStopRule(cfg, G.numberOfNodes());
        } else {
            coarseningStopRule = new MultipleKStopRule(cfg, G.numberOfNodes());
        }

        int improvement = performPartitioningRecursive(cfg, G, null);
        coarseningStopRule = null; // Clean up
        return improvement;
    }

    public int performPartitioningRecursive(PartitionConfig partitionConfig, GraphAccess G, CompleteBoundary[] cBoundary) {
        AtomicInteger noOfCoarserVertices = new AtomicInteger(G.numberOfNodes());
        int noOfFinerVertices = G.numberOfNodes();
        int improvement = 0;

        EdgeRatings rating = new EdgeRatings(partitionConfig);
        ArrayList<Integer> coarseMapping = new ArrayList<>(Collections.nCopies(G.numberOfNodes(), -1));
        GraphAccess finer = G;
        Matching edgeMatcher = null;
        Contraction contracter = new Contraction();
        PartitionConfig copyOfPartitionConfig = new PartitionConfig(partitionConfig);
        GraphAccess coarser = new GraphAccess();

        ArrayList<Integer> edgeMatching = new ArrayList<>(Collections.nCopies(G.numberOfNodes(), -1));
        ArrayList<Integer> permutation = new ArrayList<>(G.numberOfNodes());

        CoarseningConfigurator coarseningConfig = new CoarseningConfigurator();

        // Use an array to pass to the configureCoarsening method
        Matching[] edgeMatcherArray = new Matching[1];
        coarseningConfig.configureCoarsening(partitionConfig, edgeMatcherArray, level);
        edgeMatcher = edgeMatcherArray[0]; // Retrieve the configured Matching object

        rating.rate(finer, level);

        edgeMatcher.match(partitionConfig, finer, edgeMatching, coarseMapping, noOfCoarserVertices, permutation);
        edgeMatcher = null; // Clean up

        if (partitionConfig.isGraphAlreadyPartitioned()) {
            contracter.contractPartitioned(partitionConfig, finer, coarser, edgeMatching, coarseMapping, noOfCoarserVertices, permutation);
        } else {
            contracter.contract(partitionConfig, finer, coarser, edgeMatching, coarseMapping, noOfCoarserVertices, permutation);
        }

        coarser.setPartitionCount(partitionConfig.getK());
        CompleteBoundary coarserBoundary = null;
        Refinement refine;

        if (!partitionConfig.isLabelPropagationRefinement()) {
            coarserBoundary = new CompleteBoundary(coarser);
            refine = new MixedRefinement();
        } else {
            refine = new LabelPropagationRefinement();
        }

        if (!coarseningStopRule.stop(noOfFinerVertices, noOfCoarserVertices)) {
            PartitionConfig cfg = new PartitionConfig(partitionConfig);
            double factor = partitionConfig.getBalanceFactor();
            cfg.setUpperBoundPartition((int) Math.ceil((factor + 1.0) * partitionConfig.getUpperBoundPartition()));

            InitialPartitioning initPart = new InitialPartitioning();
            initPart.performInitialPartitioning(cfg, coarser);

            if (!partitionConfig.isLabelPropagationRefinement()) {
                coarserBoundary.build();
            }

            improvement += refine.performRefinement(cfg, coarser, coarserBoundary);
            deepestLevel = level + 1;
        } else {
            level++;

            improvement += performPartitioningRecursive(partitionConfig, coarser, new CompleteBoundary[]{coarserBoundary});
            partitionConfig.setGraphAlreadyPartitioned(true);

            if (level % partitionConfig.getLevelSplit() == 0) {
                if (!partitionConfig.isUseFullMultigrid() || !haveBeenLevelDown.containsKey(level)) {
                    if (!partitionConfig.isLabelPropagationRefinement()) {
                        coarserBoundary = null;
                        coarserBoundary = new CompleteBoundary(coarser);
                    }
                    haveBeenLevelDown.put(level, true);

                    PartitionConfig cfg = new PartitionConfig(partitionConfig);
                    cfg.setSetUpperbound(false);

                    double curFactor = partitionConfig.getBalanceFactor() / (deepestLevel - level);
                    cfg.setUpperBoundPartition((int) (((level != 0 ? 1 : 0) * curFactor + 1.0) * partitionConfig.getUpperBoundPartition()));

                    improvement += performPartitioningRecursive(cfg, coarser, new CompleteBoundary[]{coarserBoundary});
                }
            }

            level--;
        }

        if (partitionConfig.isUseBalanceSingletons() && !partitionConfig.isLabelPropagationRefinement()) {
            coarserBoundary.balanceSingletons(partitionConfig, coarser);
        }

        // Project back to the finer graph
        for (int n = 0; n < finer.numberOfNodes(); n++) {
            int coarserNode = coarseMapping.get(n);
            int coarserPartitionId = coarser.getPartitionIndex(coarserNode);
            finer.setPartitionIndex(n, coarserPartitionId);
        }

        finer.setPartitionCount(coarser.getPartitionCount());
        CompleteBoundary currentBoundary = null;
        if (!partitionConfig.isLabelPropagationRefinement()) {
            currentBoundary = new CompleteBoundary(finer);
            currentBoundary.buildFromCoarser(coarserBoundary, noOfCoarserVertices.get(), coarseMapping);
        }

        PartitionConfig cfg = new PartitionConfig(partitionConfig);
        double curFactor = partitionConfig.getBalanceFactor() / (deepestLevel - level);

        if (partitionConfig.isSetUpperbound()) {
            cfg.setUpperBoundPartition((int) Math.round(((level != 0 ? 1 : 0) * curFactor + 1.0) * partitionConfig.getUpperBoundPartition()));
        } else {
            cfg.setUpperBoundPartition(partitionConfig.getUpperBoundPartition());
        }

        improvement += refine.performRefinement(cfg, finer, currentBoundary);

        if (cBoundary != null) {
            cBoundary[0] = currentBoundary;
        } else {
            currentBoundary = null;
        }

        // Clean up
        contracter = null;
        coarseMapping = null;
        coarserBoundary = null;
        coarser = null;
        refine = null;

        return improvement;
    }

}
