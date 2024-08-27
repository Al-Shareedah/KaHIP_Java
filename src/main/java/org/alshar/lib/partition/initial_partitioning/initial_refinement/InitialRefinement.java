package org.alshar.lib.partition.initial_partitioning.initial_refinement;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.GraphHierarchy;
import org.alshar.lib.enums.MatchingType;
import org.alshar.lib.enums.StopRule;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.coarsening.Coarsening;
import org.alshar.lib.partition.uncoarsening.Uncoarsening;


public class InitialRefinement {

    public InitialRefinement() {
        // Constructor logic (if any) can be added here
    }

    public int optimize(PartitionConfig config, GraphAccess G, int initialCut) {
        // Create a copy of the configuration to modify for optimization
        PartitionConfig partitionConfig = new PartitionConfig(config);
        partitionConfig.setGraphAlreadyPartitioned(true);
        partitionConfig.setStopRule(StopRule.STOP_RULE_STRONG);
        partitionConfig.setFmSearchLimit(partitionConfig.getInitialPartitionOptimizeFmLimits());
        partitionConfig.setKwayFmSearchLimit(partitionConfig.getInitialPartitionOptimizeFmLimits());
        partitionConfig.setLocalMultitryFmAlpha(partitionConfig.getInitialPartitionOptimizeMultitryFmAlpha());
        partitionConfig.setLocalMultitryRounds(partitionConfig.getInitialPartitionOptimizeMultitryRounds());
        partitionConfig.setMatchingType(MatchingType.MATCHING_GPA);
        partitionConfig.setGpaGrowPathsBetweenBlocks(false);
        partitionConfig.setKaffpaPerfectlyBalancedRefinement(false);  // For runtime reasons

        // Create a graph hierarchy
        GraphHierarchy hierarchy = new GraphHierarchy();

        // Perform coarsening
        Coarsening coarsen = new Coarsening();
        coarsen.performCoarsening(partitionConfig, G, hierarchy);

        // Perform uncoarsening
        Uncoarsening uncoarsen = new Uncoarsening();
        int improvement = uncoarsen.performUncoarsening(partitionConfig, hierarchy);

        // Update the initial cut
        initialCut -= improvement;

        return improvement;
    }
}
