package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BoundaryLookup {

    public static class BoundaryPair {
        public int k;  // PartitionID corresponds to int in Java
        public int lhs;
        public int rhs;

        public BoundaryPair(int k, int lhs, int rhs) {
            this.k = k;
            this.lhs = lhs;
            this.rhs = rhs;
        }

        public BoundaryPair() {
            this.k = 0;
            this.lhs = 0;
            this.rhs = 0;
        }

        public int getK() {
            return k;
        }

        public int getLhs() {
            return lhs;
        }

        public int getRhs() {
            return rhs;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BoundaryPair that = (BoundaryPair) o;
            return (lhs == that.lhs && rhs == that.rhs) || (lhs == that.rhs && rhs == that.lhs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(Math.min(lhs, rhs) * k + Math.max(lhs, rhs));
        }
    }

    public static class CompareBoundaryPair {
        public boolean compare(BoundaryPair pairA, BoundaryPair pairB) {
            return (pairA.lhs == pairB.lhs && pairA.rhs == pairB.rhs) ||
                    (pairA.lhs == pairB.rhs && pairA.rhs == pairB.lhs);
        }
    }

    public static class CompareBoundaryPairDirected {
        public boolean compare(BoundaryPair pairA, BoundaryPair pairB) {
            return (pairA.lhs == pairB.lhs && pairA.rhs == pairB.rhs);
        }
    }

    public static class DataBoundaryPair {
        public PartialBoundary pbLhs;
        public PartialBoundary pbRhs;
        public int lhs;
        public int rhs;
        public int edgeCut;  // EdgeWeight corresponds to int in Java
        public boolean initialized;

        // No-argument constructor
        public DataBoundaryPair() {
            this.edgeCut = 0;
            this.lhs = Integer.MAX_VALUE; // Equivalent to std::numeric_limits<PartitionID>::max() in C++
            this.rhs = Integer.MAX_VALUE;
            this.initialized = false;
            this.pbLhs = new PartialBoundary();
            this.pbRhs = new PartialBoundary();
        }

        // Constructor that accepts lhs and rhs
        public DataBoundaryPair(int lhs, int rhs) {
            this.edgeCut = 0;
            this.lhs = lhs;
            this.rhs = rhs;
            this.initialized = false;
            this.pbLhs = new PartialBoundary();
            this.pbRhs = new PartialBoundary();
        }
    }

    public static class HashBoundaryPairDirected {
        public int hash(BoundaryPair pair) {
            return pair.lhs * pair.k + pair.rhs;
        }
    }

    public static class HashBoundaryPair {
        public int hash(BoundaryPair pair) {
            if (pair.lhs < pair.rhs) {
                return pair.lhs * pair.k + pair.rhs;
            } else {
                return pair.rhs * pair.k + pair.lhs;
            }
        }
    }

    // Type aliasing the C++ block_pairs as a Map in Java
    public static class BlockPairs extends HashMap<BoundaryPair, DataBoundaryPair> {
        public BlockPairs() {
            super();
        }
    }
}
