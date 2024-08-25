package org.alshar.lib.partition.initial_partitioning;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.enums.InitialPartitioningType;
import org.alshar.lib.enums.MatchingType;
import org.alshar.lib.enums.PermutationQuality;
import org.alshar.lib.partition.GraphPartitioner;
import org.alshar.lib.partition.PartitionConfig;

import java.io.IOException;
import java.util.Arrays;

public class InitialPartitionBipartition extends InitialPartitioner {

    public InitialPartitionBipartition() {
        // Constructor logic, if any
    }

    @Override
    public void initialPartition(PartitionConfig config, int seed, GraphAccess G, int[] partitionMap) {
        GraphPartitioner gp = new GraphPartitioner();
        PartitionConfig recConfig = new PartitionConfig(config);
        recConfig.setInitialPartitioningType(InitialPartitioningType.INITIAL_PARTITIONING_BIPARTITION);
        recConfig.setInitialPartitioningRepetitions(0);
        recConfig.setGlobalCycleIterations(1);
        recConfig.setUseWcycles(false);
        recConfig.setUseFullMultigrid(false);
        recConfig.setFmSearchLimit(config.getBipartitionPostMlLimits());
        recConfig.setMatchingType(MatchingType.MATCHING_GPA);
        recConfig.setPermutationQuality(PermutationQuality.PERMUTATION_QUALITY_GOOD);
        recConfig.setInitialPartitioning(true);
        recConfig.setGraphAlreadyPartitioned(false);
        recConfig.setLabelPropagationRefinement(false);

        if (config.isClusterCoarseningDuringIp()) {
            recConfig.setMatchingType(MatchingType.CLUSTER_COARSENING);
            recConfig.setClusterCoarseningFactor(12);
            recConfig.setEnsembleClusterings(false);
        }

        // Perform recursive partitioning
        gp.performRecursivePartitioning(recConfig, G);

        // Store the partition indices in partitionMap
        for (int n = 0; n < G.numberOfNodes(); n++) {
            partitionMap[n] = G.getPartitionIndex(n);
        }
    }

    @Override
    public void initialPartition(PartitionConfig config, int seed, GraphAccess G, int[] xadj, int[] adjncy, int[] vwgt, int[] adjwgt, int[] partitionMap) {
        // Not implemented yet
        System.out.println("Not implemented yet");
    }
}

