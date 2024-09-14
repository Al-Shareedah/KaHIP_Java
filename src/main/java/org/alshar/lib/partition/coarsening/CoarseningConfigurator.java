package org.alshar.lib.partition.coarsening;

import org.alshar.lib.enums.MatchingType;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.coarsening.clustering.SizeConstraintLabelPropagation;
import org.alshar.lib.partition.coarsening.matching.Matching;
import org.alshar.lib.partition.coarsening.matching.RandomMatching;
import org.alshar.lib.partition.coarsening.matching.gpa.GPAMatching;

public class CoarseningConfigurator {

    public CoarseningConfigurator() {}

    public void configureCoarsening(PartitionConfig partitionConfig, Matching[] edgeMatcher, int level) {
        switch (partitionConfig.getMatchingType()) {
            case MATCHING_RANDOM:
                edgeMatcher[0] = new RandomMatching();
                break;
            case MATCHING_GPA:
                edgeMatcher[0] = new GPAMatching();
                //System.out.println("GPA matching");
                break;
            case MATCHING_RANDOM_GPA:
                //System.out.println("Random GPA matching");
                edgeMatcher[0] = new GPAMatching();
                break;
            case CLUSTER_COARSENING:
                //System.out.println("Cluster coarsening");
                edgeMatcher[0] = new SizeConstraintLabelPropagation();
                break;
        }

        if (partitionConfig.getMatchingType() == MatchingType.MATCHING_RANDOM_GPA
                && level < partitionConfig.getAggressiveRandomLevels()) {
            edgeMatcher[0] = null;  // Clean up the previous matcher
            //System.out.println("Random matching");
            edgeMatcher[0] = new RandomMatching();
        }
    }
}
