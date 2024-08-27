package org.alshar.lib.partition.uncoarsening.refinement.label_propagation_refinement;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.coarsening.clustering.NodeOrdering;
import org.alshar.lib.partition.uncoarsening.refinement.Refinement;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import org.alshar.lib.tools.RandomFunctions;

import java.util.*;

public class LabelPropagationRefinement extends Refinement {

    public LabelPropagationRefinement() {
    }

    @Override
    public int performRefinement(PartitionConfig partitionConfig, GraphAccess G, CompleteBoundary boundary) {
        int blockUpperBound = partitionConfig.getUpperBoundPartition();

        // HashMap for storing the cluster sizes
        List<Integer> clusterSizes = new ArrayList<>(Collections.nCopies(partitionConfig.getK(), 0));

        // Ordering the nodes
        List<Integer> permutation = new ArrayList<>(G.numberOfNodes());
        for (int i = 0; i < G.numberOfNodes(); i++) {
            permutation.add(i);
        }
        NodeOrdering nodeOrdering = new NodeOrdering();
        nodeOrdering.orderNodes(partitionConfig, G, permutation);

        // Initialize queues
        Queue<Integer> Q = new LinkedList<>();
        Queue<Integer> nextQ = new LinkedList<>();
        List<Boolean> QContained = new ArrayList<>(Collections.nCopies(G.numberOfNodes(), false));
        List<Boolean> nextQContained = new ArrayList<>(Collections.nCopies(G.numberOfNodes(), false));

        for (int node = 0; node < G.numberOfNodes(); node++) {
            clusterSizes.set(G.getPartitionIndex(node), clusterSizes.get(G.getPartitionIndex(node)) + G.getNodeWeight(node));
            Q.add(permutation.get(node));
        }

        for (int j = 0; j < partitionConfig.getLabelIterationsRefinement(); j++) {
            int changeCounter = 0;

            while (!Q.isEmpty()) {
                int node = Q.poll();
                QContained.set(node, false);

                List<Integer> hashMap = new ArrayList<>(Collections.nCopies(partitionConfig.getK(), 0));

                // Move the node to the cluster that is most common in the neighborhood
                for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                    int target = G.getEdgeTarget(e);
                    hashMap.set(G.getPartitionIndex(target), hashMap.get(G.getPartitionIndex(target)) + G.getEdgeWeight(e));
                }

                // Second sweep for finding max and resetting array
                int maxBlock = G.getPartitionIndex(node);
                int myBlock = G.getPartitionIndex(node);
                int maxValue = 0;

                for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                    int target = G.getEdgeTarget(e);
                    int curBlock = G.getPartitionIndex(target);
                    int curValue = hashMap.get(curBlock);

                    if ((curValue > maxValue || (curValue == maxValue && RandomFunctions.nextBool())) &&
                            (clusterSizes.get(curBlock) + G.getNodeWeight(node) < blockUpperBound || (curBlock == myBlock && clusterSizes.get(myBlock) <= partitionConfig.getUpperBoundPartition()))) {
                        maxValue = curValue;
                        maxBlock = curBlock;
                    }

                    hashMap.set(curBlock, 0);
                }

                clusterSizes.set(G.getPartitionIndex(node), clusterSizes.get(G.getPartitionIndex(node)) - G.getNodeWeight(node));
                clusterSizes.set(maxBlock, clusterSizes.get(maxBlock) + G.getNodeWeight(node));
                boolean changedLabel = G.getPartitionIndex(node) != maxBlock;
                changeCounter += changedLabel ? 1 : 0;
                G.setPartitionIndex(node, maxBlock);

                if (changedLabel) {
                    for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                        int target = G.getEdgeTarget(e);
                        if (!nextQContained.get(target)) {
                            nextQ.add(target);
                            nextQContained.set(target, true);
                        }
                    }
                }
            }

            // Swap Q and nextQ
            Queue<Integer> tempQ = Q;
            Q = nextQ;
            nextQ = tempQ;

            List<Boolean> tempQContained = QContained;
            QContained = nextQContained;
            nextQContained = tempQContained;
        }

        return 0;
    }
}
