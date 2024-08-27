package org.alshar.lib.tools;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;

public class GraphPartitionAssertions {

    public GraphPartitionAssertions() {
    }

    public static boolean assertGraphHasKWayPartition(PartitionConfig config, GraphAccess G) {
        boolean[] allPartsThere = new boolean[config.getK()];
        for (int i = 0; i < config.getK(); i++) {
            allPartsThere[i] = false;
        }

        for (int n = 0; n < G.numberOfNodes(); n++) {
            allPartsThere[G.getPartitionIndex(n)] = true;
        }

        for (int i = 0; i < config.getK(); i++) {
            if (!allPartsThere[i]) {
                throw new AssertionError("Partition " + i + " is missing in the graph.");
            }
        }

        return true;
    }
}

