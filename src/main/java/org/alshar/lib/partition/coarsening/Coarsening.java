package org.alshar.lib.partition.coarsening;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.GraphHierarchy;
import org.alshar.lib.enums.MatchingType;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.coarsening.matching.Matching;
import org.alshar.lib.partition.coarsening.stop_rules.*;
import org.alshar.lib.partition.coarsening.Contraction;
import org.alshar.lib.partition.coarsening.CoarseningConfigurator;
import org.alshar.lib.partition.coarsening.EdgeRatings;

import java.util.ArrayList;
import java.util.List;

public class Coarsening {
    public Coarsening() {}

    public void performCoarsening(PartitionConfig partitionConfig, GraphAccess G, GraphHierarchy hierarchy) {
        int noOfCoarserVertices = G.numberOfNodes();
        int noOfFinerVertices = G.numberOfNodes();

        EdgeRatings rating = new EdgeRatings(partitionConfig);
        List<Integer> coarseMapping = null;

        GraphAccess finer = G;
        Matching[] edgeMatcherArray = new Matching[1]; // Change to an array of size 1
        Contraction contracter = new Contraction();
        PartitionConfig copyOfPartitionConfig = new PartitionConfig(partitionConfig);

        StopRule coarseningStopRule = null;
        if (partitionConfig.isModeNodeSeparators()) {
            coarseningStopRule = new SeparatorSimpleStopRule(copyOfPartitionConfig, G.numberOfNodes());
        } else {
            switch (partitionConfig.getStopRule()) {
                case STOP_RULE_SIMPLE:
                    coarseningStopRule = new SimpleStopRule(copyOfPartitionConfig, G.numberOfNodes());
                    break;
                case STOP_RULE_MULTIPLE_K:
                    coarseningStopRule = new MultipleKStopRule(copyOfPartitionConfig, G.numberOfNodes());
                    break;
                default:
                    coarseningStopRule = new StrongStopRule(copyOfPartitionConfig, G.numberOfNodes());
                    break;
            }
        }

        CoarseningConfigurator coarseningConfig = new CoarseningConfigurator();

        int level = 0;
        boolean contractionStop;
        do {
            GraphAccess coarser = new GraphAccess();
            coarseMapping = new ArrayList<>();
            List<Integer> edgeMatching = new ArrayList<>();
            List<Integer> permutation = new ArrayList<>();

            coarseningConfig.configureCoarsening(copyOfPartitionConfig, edgeMatcherArray, level);
            Matching edgeMatcher = edgeMatcherArray[0];
            if (partitionConfig.getMatchingType() != MatchingType.CLUSTER_COARSENING) {
                rating.rate(finer, level);
            }

            edgeMatcher.match(copyOfPartitionConfig, finer, edgeMatching, coarseMapping, noOfCoarserVertices, permutation);

            edgeMatcher = null; // Clean up

            if (partitionConfig.isGraphAlreadyPartitioned()) {
                contracter.contractPartitioned(copyOfPartitionConfig, finer, coarser, edgeMatching, coarseMapping, noOfCoarserVertices, permutation);
            } else {
                contracter.contract(copyOfPartitionConfig, finer, coarser, edgeMatching, coarseMapping, noOfCoarserVertices, permutation);
            }

            hierarchy.pushBack(finer, coarseMapping);
            contractionStop = coarseningStopRule.stop(noOfFinerVertices, noOfCoarserVertices);

            noOfFinerVertices = noOfCoarserVertices;
            finer = coarser;

            level++;
        } while (!contractionStop);

        hierarchy.pushBack(finer, null); // append the last created level

        // Clean up
        contracter = null;
        coarseningStopRule = null;
    }
}