package org.alshar.lib.partition.coarsening.matching;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;

import java.io.PrintWriter;
import java.util.List;

public abstract class Matching {

    public Matching() {}

    public abstract void match(PartitionConfig partitionConfig,
                               GraphAccess G,
                               List<Integer> matching,
                               List<Integer> mapping,
                               int noOfCoarseVertices,
                               List<Integer> permutation);

    public void printMatching(PrintWriter out, List<Integer> edgeMatching) {
        for (int n = 0; n < edgeMatching.size(); n++) {
            out.printf("%d:%d%n", n, edgeMatching.get(n));
        }
    }
}
