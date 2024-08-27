package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.quotient_graph_scheduling;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.BoundaryLookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleQuotientGraphScheduler extends QuotientGraphScheduling {

    private List<BoundaryLookup.BoundaryPair> quotientGraphEdgesPool;

    // Constructor
    public SimpleQuotientGraphScheduler(PartitionConfig config, List<BoundaryLookup.BoundaryPair> qgraphEdges, int account) {
        quotientGraphEdgesPool = new ArrayList<>();
        int addedEdges = 0;

        for (int i = 0; i < Math.ceil(config.bankAccountFactor) && addedEdges <= account; i++) {
            Collections.shuffle(qgraphEdges);  // Replacing random_functions::permutate_vector_good_small with Java's shuffle
            for (BoundaryLookup.BoundaryPair edge : qgraphEdges) {
                if (addedEdges > account) break;
                quotientGraphEdgesPool.add(edge);
                addedEdges++;
            }
        }
    }

    // Destructor equivalent (no explicit cleanup required in Java)
    @Override
    public void finalize() {
        super.finalize();
    }

    @Override
    public boolean hasFinished() {
        return quotientGraphEdgesPool.isEmpty();
    }

    @Override
    public BoundaryLookup.BoundaryPair getNext() {
        return quotientGraphEdgesPool.remove(quotientGraphEdgesPool.size() - 1);  // Equivalent to pop_back() in C++
    }

    @Override
    public void pushStatistics(QGraphEdgeStatistics statistic) {
        // No implementation as in the original C++ code
    }
}
