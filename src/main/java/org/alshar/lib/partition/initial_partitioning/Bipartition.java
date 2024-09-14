package org.alshar.lib.partition.initial_partitioning;
import org.alshar.lib.data_structure.Edge;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.priority_queues.MaxNodeHeap;
import org.alshar.lib.enums.BipartitionAlgorithm;
import org.alshar.lib.enums.RefinementSchedulingAlgorithm;
import org.alshar.lib.enums.RefinementType;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.Refinement;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.QuotientGraphRefinement;
import org.alshar.lib.tools.QualityMetrics;
import org.alshar.lib.tools.Timer;
import org.alshar.lib.tools.RandomFunctions;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import java.util.LinkedList;
import java.util.Queue;

public class Bipartition extends InitialPartitioner {

    public Bipartition() {
        // Constructor logic, if any
    }

    @Override
    public void initialPartition(PartitionConfig config, int seed, GraphAccess G, int[] partitionMap) {
        Timer t = new Timer();
        t.restart();
        int iterations = config.getBipartitionTries();
        long bestCut = Long.MAX_VALUE;
        int bestLoad = Integer.MAX_VALUE;

        for (int i = 0; i < iterations; i++) {
            if (config.getBipartitionAlgorithm() == BipartitionAlgorithm.BIPARTITION_BFS) {
                growRegionsBfs(config, G);
            } else if (config.getBipartitionAlgorithm() == BipartitionAlgorithm.BIPARTITION_FM) {
                growRegionsFm(config, G);
            }

            G.setPartitionCount(2);
            postFm(config, G);

            QualityMetrics qm = new QualityMetrics();
            long curCut = qm.edgeCut(G);

            int lhsBlockWeight = 0;
            int rhsBlockWeight = 0;

            for (int node = 0; node < G.numberOfNodes(); node++) {
                if (G.getPartitionIndex(node) == 0) {
                    lhsBlockWeight += G.getNodeWeight(node);
                } else {
                    rhsBlockWeight += G.getNodeWeight(node);
                }
            }

            int lhsOverload = Math.max(lhsBlockWeight - config.getTargetWeights().get(0), 0);
            int rhsOverload = Math.max(rhsBlockWeight - config.getTargetWeights().get(1), 0);

            if (curCut < bestCut || (curCut == bestCut && lhsOverload + rhsOverload < bestLoad)) {
                bestCut = curCut;
                bestLoad = lhsOverload + rhsOverload;

                for (int n = 0; n < G.numberOfNodes(); n++) {
                    partitionMap[n] = G.getPartitionIndex(n);
                }
            }
        }
        //System.out.println("Bipartition took " + t.elapsed() + " ms");
    }

    @Override
    public void initialPartition(PartitionConfig config, int seed, GraphAccess G, int[] xadj, int[] adjncy, int[] vwgt, int[] adjwgt, int[] partitionMap) {
        System.out.println("Not implemented yet");
    }

    private void postFm(PartitionConfig config, GraphAccess G) {
        Refinement refine = new QuotientGraphRefinement();
        CompleteBoundary boundary = new CompleteBoundary(G);
        boundary.build();

        PartitionConfig initialCfg = new PartitionConfig(config);
        initialCfg.setFmSearchLimit(config.getBipartitionPostFmLimits());
        initialCfg.setRefinementType(RefinementType.REFINEMENT_TYPE_FM);
        initialCfg.setRefinementSchedulingAlgorithm(RefinementSchedulingAlgorithm.REFINEMENT_SCHEDULING_ACTIVE_BLOCKS);
        initialCfg.setBankAccountFactor(5);
        initialCfg.setRebalance(true);
        initialCfg.setSoftRebalance(true);
        initialCfg.setUpperBoundPartition(100000000);
        initialCfg.setInitialBipartitioning(true);

        refine.performRefinement(initialCfg, G, boundary);

        refine = null;
        boundary = null;
    }

    private int findStartNode(PartitionConfig config, GraphAccess G) {
        int startNode = RandomFunctions.nextInt(0, G.numberOfNodes() - 1);
        int lastNode = startNode;

        int counter = G.numberOfNodes();
        while (G.getNodeDegree(startNode) == 0 && --counter > 0) {
            startNode = RandomFunctions.nextInt(0, G.numberOfNodes() - 1);
        }

        // Perform a BFS to get a partition
        for (int i = 0; i < 3; i++) {
            boolean[] touched = new boolean[G.numberOfNodes()];
            startNode = lastNode;
            touched[startNode] = true;

            Queue<Integer> bfsQueue = new LinkedList<>();
            bfsQueue.add(startNode);

            while (!bfsQueue.isEmpty()) {
                int source = bfsQueue.poll();
                lastNode = source;

                // Iterate over all out-edges of the source node
                for (int e = G.getFirstEdge(source); e < G.getFirstInvalidEdge(source); e++) {
                    int target = G.getEdgeTarget(e);
                    if (!touched[target]) {
                        touched[target] = true;
                        bfsQueue.add(target);
                    }
                }
            }
        }
        return lastNode;
    }


    public void growRegionsBfs(PartitionConfig config, GraphAccess G) {
        if (G.numberOfNodes() == 0) return;

        RandomFunctions randomFunctions = new RandomFunctions();
        int startNode = randomFunctions.nextInt(0, G.numberOfNodes() - 1);
        if (config.isBuffoon()) {
            startNode = findStartNode(config, G);  // more likely to produce connected partitions
        }

        boolean[] touched = new boolean[G.numberOfNodes()];
        touched[startNode] = true;
        int curPartitionWeight = 0;

        // Set initial partition index for all nodes to 1
        for (int node = 0; node < G.numberOfNodes(); node++) {
            G.setPartitionIndex(node, 1);
        }

        int nodesLeft = G.numberOfNodes() - 1;

        // Perform BFS to get a partition
        Queue<Integer> bfsQueue = new LinkedList<>();
        bfsQueue.add(startNode);

        while (true) {
            if (nodesLeft == 1) {
                // Only one node left -> we have to break
                break;
            }

            if (bfsQueue.isEmpty() && nodesLeft > 0) {
                // Disconnected graph -> find a new start node among those that haven't been touched
                int k = randomFunctions.nextInt(0, nodesLeft - 1);
                int startNodeNew = 0;

                for (int node = 0; node < G.numberOfNodes(); node++) {
                    if (!touched[node]) {
                        if (k == 0) {
                            if (G.getNodeDegree(node) != 0) {
                                startNodeNew = node;
                                break;
                            } else {
                                G.setPartitionIndex(node, 0);
                                nodesLeft--;
                                curPartitionWeight += G.getNodeWeight(node);
                                touched[node] = true;

                                if (curPartitionWeight >= config.getGrowTarget()) break;
                            }
                        } else {
                            k--;
                        }
                    }
                }

                if (curPartitionWeight >= config.getGrowTarget()) break;

                bfsQueue.add(startNodeNew);
                touched[startNodeNew] = true;
            } else if (bfsQueue.isEmpty() && nodesLeft == 0) {
                break;
            }

            int source = bfsQueue.poll();
            G.setPartitionIndex(source, 0);

            nodesLeft--;
            curPartitionWeight += G.getNodeWeight(source);

            if (curPartitionWeight >= config.getGrowTarget()) break;

            // Iterate over all out-edges of the source node
            for (int e = G.getFirstEdge(source), end = G.getFirstInvalidEdge(source); e < end; e++) {
                int target = G.getEdgeTarget(e);
                if (!touched[target]) {
                    touched[target] = true;
                    bfsQueue.add(target);
                }
            }
        }
    }

    private void growRegionsFm(PartitionConfig config, GraphAccess G) {
        if (G.numberOfNodes() == 0) return;

        int startNode = findStartNode(config, G);

        boolean[] touched = new boolean[G.numberOfNodes()];
        touched[startNode] = true;
        long curPartitionWeight = 0;

        // Initialize all nodes to partition 1
        for (int node = 0; node < G.numberOfNodes(); node++) {
            G.setPartitionIndex(node, 1);
        }

        int nodesLeft = G.numberOfNodes() - 1;

        // Perform a pseudo-Dijkstra to get a partition
        MaxNodeHeap queue = new MaxNodeHeap();
        queue.insert(startNode, 0); // In this case, the gain doesn't really matter

        while (true) {
            if (nodesLeft == 1) {
                break; // Only one node left --> we have to break
            }

            if (queue.isEmpty() && nodesLeft > 0) {
                // Disconnected graph -> find a new start node among those that haven't been touched
                int k = RandomFunctions.nextInt(0, nodesLeft - 1);
                int start_node = 0;

                for (int node = 0; node < G.numberOfNodes(); node++) {
                    if (!touched[node]) {
                        if (k == 0) {
                            start_node = node;
                            break;
                        } else {
                            k--;
                        }
                    }
                }

                queue.insert(start_node, 0);
                touched[start_node] = true;
            } else if (queue.isEmpty() && nodesLeft == 0) {
                break; // No more nodes left to process
            }

            int source = queue.deleteMax();
            G.setPartitionIndex(source, 0);

            nodesLeft--;
            curPartitionWeight += G.getNodeWeight(source);

            if (curPartitionWeight >= config.getGrowTarget()) break;

            // Iterate over all out-edges of the source node
            for (int e = G.getFirstEdge(source), end = G.getFirstInvalidEdge(source); e < end; e++) {
                int target = G.getEdgeTarget(e);
                if (G.getPartitionIndex(target) == 1) { // Then we might need to update the gain!
                    int gain = computeGain(G, target, 0);
                    touched[target] = true;

                    if (queue.contains(target)) {
                        // Change the gain
                        queue.changeKey(target, gain);
                    } else {
                        // Insert the node with the new gain
                        queue.insert(target, gain);
                    }
                }
            }
        }

        queue.clear(); // Cleanup, equivalent to `delete queue` in C++
    }


    private int computeGain(GraphAccess G, int node, int targetingPartition) {
        int gain = 0;

        // Iterating over all out-edges of the node
        for (int e = G.getFirstEdge(node), end = G.getFirstInvalidEdge(node); e < end; e++) {
            int target = G.getEdgeTarget(e);
            if (G.getPartitionIndex(target) == targetingPartition) {
                gain += G.getEdgeWeight(e);
            } else {
                gain -= G.getEdgeWeight(e);
            }
        }

        return gain;
    }

}

