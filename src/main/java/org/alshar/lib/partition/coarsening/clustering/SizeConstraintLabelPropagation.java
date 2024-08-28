package org.alshar.lib.partition.coarsening.clustering;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.coarsening.matching.Matching;
import org.alshar.lib.tools.RandomFunctions;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SizeConstraintLabelPropagation extends Matching {

    public SizeConstraintLabelPropagation() {}

    @Override
    public void match(PartitionConfig partitionConfig, GraphAccess G, List<Integer> matching,
                      List<Integer> coarseMapping, AtomicInteger noOfCoarseVertices, List<Integer> permutation) {
        permutation.clear();
        for (int i = 0; i < G.numberOfNodes(); i++) {
            permutation.add(i);
        }

        coarseMapping.clear();
        for (int i = 0; i < G.numberOfNodes(); i++) {
            coarseMapping.add(i);
        }

        // Set the initial value of noOfCoarseVertices to 0
        noOfCoarseVertices.set(0);

        if (partitionConfig.isEnsembleClusterings()) {
            ensembleClusterings(partitionConfig, G, matching, coarseMapping, noOfCoarseVertices, permutation);
        } else {
            matchInternal(partitionConfig, G, matching, coarseMapping, noOfCoarseVertices, permutation);
        }
    }


    private void matchInternal(PartitionConfig partitionConfig, GraphAccess G, List<Integer> matching,
                               List<Integer> coarseMapping, AtomicInteger noOfCoarseVertices, List<Integer> permutation) {
        int numNodes = G.numberOfNodes();
        List<Integer> clusterId = new ArrayList<>(numNodes);
        for (int i = 0; i < numNodes; i++) {
            clusterId.add(0);
        }

        double blockUpperbound = Math.ceil(partitionConfig.getUpperBoundPartition() /
                (double) partitionConfig.getClusterCoarseningFactor());

        labelPropagation(partitionConfig, G, blockUpperbound, clusterId, noOfCoarseVertices);
        createCoarseMapping(partitionConfig, G, clusterId, coarseMapping);
    }

    private void ensembleClusterings(PartitionConfig partitionConfig, GraphAccess G, List<Integer> matching,
                                     List<Integer> coarseMapping, AtomicInteger noOfCoarseVertices, List<Integer> permutation) {
        int runs = partitionConfig.getNumberOfClusterings();
        int numNodes = G.numberOfNodes();
        List<Integer> curCluster = new ArrayList<>(Collections.nCopies(numNodes, 0));
        List<Integer> ensembleCluster = new ArrayList<>(Collections.nCopies(numNodes, 0));

        int newCf = partitionConfig.getClusterCoarseningFactor();
        for (int i = 0; i < runs; i++) {
            PartitionConfig config = new PartitionConfig(partitionConfig);
            config.setClusterCoarseningFactor(newCf);

            AtomicInteger curNoBlocks = new AtomicInteger(0);

            // Calculate the block upper bound
            double blockUpperbound = Math.ceil(partitionConfig.getUpperBoundPartition() / (double) partitionConfig.getClusterCoarseningFactor());

            // Call labelPropagation with the blockUpperbound
            labelPropagation(config, G, blockUpperbound, curCluster, curNoBlocks);

            if (i != 0) {
                ensembleTwoClusterings(G, curCluster, ensembleCluster, ensembleCluster, noOfCoarseVertices);
            } else {
                for (int node = 0; node < numNodes; node++) {
                    ensembleCluster.set(node, curCluster.get(node));
                }
                noOfCoarseVertices.set(curNoBlocks.get());
            }
            newCf = RandomFunctions.nextInt(10, 30);
        }

        createCoarseMapping(partitionConfig, G, ensembleCluster, coarseMapping);
    }


    private void ensembleTwoClusterings(GraphAccess G, List<Integer> lhs, List<Integer> rhs,
                                        List<Integer> output, AtomicInteger noOfCoarseVertices) {
        Map<EnsemblePair, DataEnsemblePair> newMapping = new HashMap<>();
        noOfCoarseVertices.set(0); // Initialize to 0
        for (int node = 0; node < lhs.size(); node++) {
            EnsemblePair curPair = new EnsemblePair(lhs.get(node), rhs.get(node), G.numberOfNodes());

            if (!newMapping.containsKey(curPair)) {
                newMapping.put(curPair, new DataEnsemblePair(noOfCoarseVertices.getAndIncrement()));
            }

            output.set(node, newMapping.get(curPair).getMapping());
        }

        noOfCoarseVertices.set(newMapping.size()); // Update with the final size
    }


    private void labelPropagation(PartitionConfig partitionConfig, GraphAccess G,
                                  double blockUpperbound, List<Integer> clusterId, AtomicInteger noOfBlocks) {
        // Initialization
        int numNodes = G.numberOfNodes();
        List<Integer> hashMap = new ArrayList<>(Collections.nCopies(numNodes, 0));
        List<Integer> permutation = new ArrayList<>(numNodes);
        List<Integer> clusterSizes = new ArrayList<>(numNodes);

        for (int i = 0; i < numNodes; i++) {
            clusterSizes.add(G.getNodeWeight(i));
            clusterId.set(i, i);
        }

        NodeOrdering nOrdering = new NodeOrdering();
        nOrdering.orderNodes(partitionConfig, G, permutation);

        for (int j = 0; j < partitionConfig.getLabelIterations(); j++) {
            int changeCounter = 0;

            for (int i = 0; i < numNodes; i++) {
                int node = permutation.get(i);

                // Move the node to the cluster that is most common in the neighborhood
                for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                    int target = G.getEdgeTarget(e);
                    hashMap.set(clusterId.get(target), hashMap.get(clusterId.get(target)) + G.getEdgeWeight(e));
                }

                // Second sweep for finding max and resetting array
                int maxBlock = clusterId.get(node);
                int myBlock = clusterId.get(node);
                int maxValue = 0;

                for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                    int target = G.getEdgeTarget(e);
                    int curBlock = clusterId.get(target);
                    int curValue = hashMap.get(curBlock);

                    if ((curValue > maxValue || (curValue == maxValue && RandomFunctions.nextBool())) &&
                            (clusterSizes.get(curBlock) + G.getNodeWeight(node) < blockUpperbound || curBlock == myBlock) &&
                            (!partitionConfig.isGraphAlreadyPartitioned() || G.getPartitionIndex(node) == G.getPartitionIndex(target)) &&
                            (!partitionConfig.isCombine() || G.getSecondPartitionIndex(node) == G.getSecondPartitionIndex(target))) {
                        maxValue = curValue;
                        maxBlock = curBlock;
                    }

                    hashMap.set(curBlock, 0);
                }

                clusterSizes.set(clusterId.get(node), clusterSizes.get(clusterId.get(node)) - G.getNodeWeight(node));
                clusterSizes.set(maxBlock, clusterSizes.get(maxBlock) + G.getNodeWeight(node));
                changeCounter += (clusterId.get(node) != maxBlock) ? 1 : 0;
                clusterId.set(node, maxBlock);
            }
        }

        remapClusterIds(partitionConfig, G, clusterId, noOfBlocks);
    }

    private void createCoarseMapping(PartitionConfig partitionConfig, GraphAccess G,
                                     List<Integer> clusterId, List<Integer> coarseMapping) {
        for (int node = 0; node < G.numberOfNodes(); node++) {
            coarseMapping.set(node, clusterId.get(node));
        }
    }

    private void remapClusterIds(PartitionConfig partitionConfig, GraphAccess G,
                                 List<Integer> clusterId, AtomicInteger noOfCoarseVertices) {
        remapClusterIds(partitionConfig, G, clusterId, noOfCoarseVertices, false);
    }

    private void remapClusterIds(PartitionConfig partitionConfig, GraphAccess G,
                                 List<Integer> clusterId, AtomicInteger noOfCoarseVertices, boolean applyToGraph) {
        Map<Integer, Integer> remap = new HashMap<>();
        int curNoClusters = 0;

        for (int node = 0; node < G.numberOfNodes(); node++) {
            int curCluster = clusterId.get(node);

            if (!remap.containsKey(curCluster)) {
                remap.put(curCluster, curNoClusters++);
            }

            clusterId.set(node, remap.get(curCluster));
        }

        if (applyToGraph) {
            for (int node = 0; node < G.numberOfNodes(); node++) {
                G.setPartitionIndex(node, clusterId.get(node));
            }
            G.setPartitionCount(curNoClusters);
        }

        noOfCoarseVertices.set(curNoClusters);
    }

    private static class EnsemblePair {
        int n;
        int lhs;
        int rhs;

        EnsemblePair(int lhs, int rhs, int n) {
            this.lhs = lhs;
            this.rhs = rhs;
            this.n = n;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EnsemblePair that = (EnsemblePair) o;
            return lhs == that.lhs && rhs == that.rhs;
        }

        @Override
        public int hashCode() {
            return lhs * n + rhs;
        }
    }

    private static class DataEnsemblePair {
        int mapping;

        DataEnsemblePair(int mapping) {
            this.mapping = mapping;
        }

        public int getMapping() {
            return mapping;
        }
    }
}
