package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.quotient_graph_scheduling;

import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.BoundaryLookup;

public abstract class QuotientGraphScheduling {

    public QuotientGraphScheduling() {
    }

    public void finalize() {
        // No explicit cleanup needed, relying on garbage collection
    }

    // Abstract methods that must be implemented by subclasses
    public abstract boolean hasFinished();
    public abstract BoundaryLookup.BoundaryPair getNext();
    public abstract void pushStatistics(QGraphEdgeStatistics statistic);

    // Nested class for qgraph_edge_statistics
    public static class QGraphEdgeStatistics {
        public int improvement;  // EdgeWeight is equivalent to int in Java
        public boolean somethingChanged;
        public BoundaryLookup.BoundaryPair pair;

        public boolean isSomethingChanged() {
            return somethingChanged;
        }

        public BoundaryLookup.BoundaryPair getPair() {
            return pair;
        }

        public QGraphEdgeStatistics(int improvement, BoundaryLookup.BoundaryPair pair, boolean somethingChanged) {
            this.improvement = improvement;
            this.pair = pair;
            this.somethingChanged = somethingChanged;
        }
    }
}
