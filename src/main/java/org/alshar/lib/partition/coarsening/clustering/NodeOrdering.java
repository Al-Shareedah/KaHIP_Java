package org.alshar.lib.partition.coarsening.clustering;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.tools.RandomFunctions;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NodeOrdering {

    public NodeOrdering() {}

    public void orderNodes(PartitionConfig config, GraphAccess G, List<Integer> orderedNodes) {
        for (int node = 0; node < G.numberOfNodes(); node++) {
            orderedNodes.set(node, node);
        }

        switch (config.getNodeOrdering()) {
            case RANDOM_NODEORDERING:
                orderNodesRandom(config, G, orderedNodes);
                break;
            case DEGREE_NODEORDERING:
                orderNodesDegree(config, G, orderedNodes);
                break;
        }
    }

    private void orderNodesRandom(PartitionConfig config, GraphAccess G, List<Integer> orderedNodes) {
        RandomFunctions.permutateVectorFast(orderedNodes, false);
    }

    private void orderNodesDegree(PartitionConfig config, GraphAccess G, List<Integer> orderedNodes) {
        Collections.sort(orderedNodes, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return Integer.compare(G.getNodeDegree(lhs), G.getNodeDegree(rhs));
            }
        });
    }
}
