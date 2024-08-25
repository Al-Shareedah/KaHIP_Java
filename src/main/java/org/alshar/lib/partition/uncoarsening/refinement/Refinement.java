package org.alshar.lib.partition.uncoarsening.refinement;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;

public abstract class Refinement {

    // Constructor
    public Refinement() {
    }

    // Destructor
    public void finalize() {
        // No explicit cleanup needed, relying on garbage collection
    }

    // Abstract method to perform refinement
    public abstract int performRefinement(PartitionConfig config,
                                          GraphAccess G,
                                          CompleteBoundary boundary);
}

