package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;

import java.util.List;

public abstract class TwoWayRefinement {

    public TwoWayRefinement() {}

    // In TwoWayRefinement (abstract class)
    public abstract int performRefinement(PartitionConfig config,
                                          GraphAccess G,
                                          CompleteBoundary boundary,
                                          List<Integer> lhsStartNodes,
                                          List<Integer> rhsStartNodes,
                                          BoundaryLookup.BoundaryPair pair,
                                          int lhsPartWeight,
                                          int rhsPartWeight,
                                          int cut,
                                          boolean[] somethingChanged);

}

