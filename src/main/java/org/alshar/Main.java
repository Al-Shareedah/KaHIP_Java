package org.alshar;
import org.alshar.app.BalanceConfiguration;
import org.alshar.lib.data_structure.matrix.NormalMatrix;
import org.alshar.lib.data_structure.matrix.OnlineDistanceMatrix;
import org.alshar.lib.io.GraphIO;
import org.alshar.app.ParseParameters;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.mapping.MappingAlgorithms;
import org.alshar.lib.partition.GraphPartitioner;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.cycle_improvements.CycleRefinement;
import org.alshar.lib.tools.QualityMetrics;
import org.alshar.lib.tools.RandomFunctions;
import org.alshar.lib.tools.Timer;

import java.util.*;

import static org.alshar.lib.enums.DistanceConstructionAlgorithm.DIST_CONST_HIERARCHY_ONLINE;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        args = new String[]{"4elt.graph", "--k", "4", "--preconfiguration=eco"};
        PartitionConfig partitionConfig = new PartitionConfig();
        String[] graphFilename = new String[1];
        boolean isGraphWeighted = false;
        boolean suppressOutput = false;
        boolean recursive = false;
        Timer totalTimer = new Timer();
        int retCode = ParseParameters.parseParameters(args, partitionConfig, isGraphWeighted, suppressOutput, recursive, graphFilename);

        if (retCode != 0) {
            System.exit(0);
        }


        // Timer start (you would need a Timer class similar to C++)
        Timer t = new Timer();

        // Load graph based on partitionConfig settings
        GraphAccess G = new GraphAccess();
        if (partitionConfig.isUseMmapIo()) {
            // Implement the mmap_io logic here, if available in Java
            //MmapIo.graphFromMetisFile(G, graphFilename);
        } else {
            // Assuming GraphIo is a class that handles reading graphs
            GraphIO graphIO = new GraphIO();
            graphIO.readGraphWeighted(G, graphFilename[0]);

        }

        // Output the elapsed time
        System.out.println("IO time: " + t.elapsed() + " ms");
        G.setPartitionCount(partitionConfig.getK());

        // Instantiate and use BalanceConfiguration to configure balance
        BalanceConfiguration bc = new BalanceConfiguration();
        bc.configurateBalance(partitionConfig, G);

        // Set random seed
        RandomFunctions.setSeed(partitionConfig.getSeed());

        // Output graph details
        System.out.println("Graph has " + G.numberOfNodes() + " nodes and " + G.numberOfEdges() + " edges");

        // Restart the timer
        t.restart();

        // Perform partitioning
        GraphPartitioner partitioner = new GraphPartitioner();
        QualityMetrics qm = new QualityMetrics();
        System.out.println("Performing partitioning!");
        if (partitionConfig.getTimeLimit() == 0) {
            partitioner.performPartitioning(partitionConfig, G);
        } else {
            int[] map = new int[G.numberOfNodes()];
            int bestCut = Integer.MAX_VALUE;

            while (t.elapsed() < partitionConfig.getTimeLimit()) {
                partitionConfig.setGraphAlreadyPartitioned(false);
                partitioner.performPartitioning(partitionConfig, G);
                int cut = qm.edgeCut(G);
                if (cut < bestCut) {
                    bestCut = cut;
                    for (int node = 0; node < G.numberOfNodes(); node++) {
                        map[node] = G.getPartitionIndex(node);
                    }
                }
            }

            for (int node = 0; node < G.numberOfNodes(); node++) {
                G.setPartitionIndex(node, map[node]);
            }
        }

        if (partitionConfig.isKaffpaPerfectlyBalance()) {
            double epsilon = partitionConfig.getImbalance() / 100.0;
            int upperBoundPartition = (int) ((1 + epsilon) * Math.ceil(partitionConfig.getLargestGraphWeight() / (double) partitionConfig.getK()));
            partitionConfig.setUpperBoundPartition(upperBoundPartition);
            // Complete boundary construction
            CompleteBoundary boundary = new CompleteBoundary(G);
            boundary.build();

            // Perform cycle refinement
            CycleRefinement cr = new CycleRefinement();
            cr.performRefinement(partitionConfig, G, boundary);
        }

        // Output the time spent on partitioning
        t.printElapsed("Time spent for partitioning");


        int qap = 0;
        if (partitionConfig.isEnableMapping()) {
            System.out.println("Performing mapping!");

            // Check if k is a power of 2
            boolean powerOfTwo = (partitionConfig.getK() & (partitionConfig.getK() - 1)) == 0;
            int[] permRankArray = new int[partitionConfig.getK()];
            List<Integer> permRank = new ArrayList<>();
            for (int i : permRankArray) {
                permRank.add(i);
            }

            GraphAccess C = new GraphAccess();
            CompleteBoundary boundary = new CompleteBoundary(G);
            boundary.build();
            boundary.getUnderlyingQuotientGraph(C);

            for (int node = 0; node < C.numberOfNodes(); node++) {
                C.setNodeWeight(node, 1);
            }

            if (!powerOfTwo) {
                t.restart();
                MappingAlgorithms ma = new MappingAlgorithms();
                if (partitionConfig.getDistanceConstructionAlgorithm() != DIST_CONST_HIERARCHY_ONLINE) {
                    NormalMatrix D = new NormalMatrix(partitionConfig.getK(), partitionConfig.getK());
                    ma.constructAMapping(partitionConfig, C, D, permRank);
                    System.out.println("Time spent for mapping: " + t.elapsed() + " ms");
                    qap = qm.totalQap(C, D, permRank);
                } else {
                    OnlineDistanceMatrix D = new OnlineDistanceMatrix(partitionConfig.getK(), partitionConfig.getK());
                    D.setPartitionConfig(partitionConfig);
                    ma.constructAMapping(partitionConfig, C, D, permRank);
                    System.out.println("Time spent for mapping: " + t.elapsed() + " ms");
                    qap = qm.totalQap(C, D, permRank);
                }
            } else {
                System.out.println("Number of processors is a power of two, so no mapping algorithm is performed (identity is best)");
                System.out.println("Time spent for mapping: 0 ms");
                for (int i = 0; i < permRank.size(); i++) {
                    permRank.set(i, i);
                }

                OnlineDistanceMatrix D = new OnlineDistanceMatrix(partitionConfig.getK(), partitionConfig.getK());
                D.setPartitionConfig(partitionConfig);
                qap = qm.totalQap(C, D, permRank);
            }

            // Solution check
            List<Integer> tbsorted = new ArrayList<>(permRank); // Clone the list
            Collections.sort(tbsorted); // Sort the list
            for (int i = 0; i < tbsorted.size(); i++) {
                if (!tbsorted.get(i).equals(i)) {
                    System.out.println("Solution is NOT a permutation. Please report this.");
                    System.out.println(tbsorted.get(i) + " " + i);
                    System.exit(0);
                }
            }

            for (int node = 0; node < G.numberOfNodes(); node++) {
                G.setPartitionIndex(node, permRank.get(G.getPartitionIndex(node)));
            }
        }
        // ******************************* done partitioning *****************************************
        // Output some information about the partition that we have computed
        System.out.println("cut \t\t" + qm.edgeCut(G));
        System.out.println("final objective \t" + qm.edgeCut(G));
        System.out.println("bnd \t\t" + qm.boundaryNodes(G));
        System.out.println("balance \t" + qm.balance(G));
        System.out.println("max_comm_vol \t" + qm.maxCommunicationVolume(G));

        calculateConnectedComponentsAndCCM(G);

        if (partitionConfig.isEnableMapping()) {
            System.out.println("quadratic assignment objective J(C,D,Pi') = " + qap);
        }

        // Write the partition to the disk
        String filename;
        if (partitionConfig.getFilenameOutput().isEmpty()) {
            filename = "tmppartition" + partitionConfig.getK();
        } else {
            filename = partitionConfig.getFilenameOutput();
        }

        // Create an instance of GraphIO
        GraphIO graphIO = new GraphIO();
        graphIO.writePartition(G, filename);

        totalTimer.printElapsed("Total execution time");

    }

    private static void calculateConnectedComponentsAndCCM(GraphAccess G) {
        Map<Integer, Set<Integer>> blockNodes = new HashMap<>();

        // Group nodes by their partition/block
        for (int node = 0; node < G.numberOfNodes(); node++) {
            int blockId = G.getPartitionIndex(node);
            blockNodes.computeIfAbsent(blockId, k -> new HashSet<>()).add(node);
        }

        List<Integer> actualSizes = new ArrayList<>();
        int totalComponents = 0;  // To accumulate total number of connected components

        // Iterate through each block and calculate its connected components
        for (Map.Entry<Integer, Set<Integer>> entry : blockNodes.entrySet()) {
            int blockId = entry.getKey();
            Set<Integer> nodes = entry.getValue();

            Map<Integer, Boolean> visitedNodes = new HashMap<>();
            for (Integer node : nodes) {
                visitedNodes.put(node, false);  // Mark all nodes as unvisited
            }

            Map<Integer, List<Integer>> connectedComponents = new HashMap<>();

            // Depth-first search to find connected components
            for (Integer node : nodes) {
                if (!visitedNodes.get(node)) {
                    List<Integer> component = new ArrayList<>();
                    Stack<Integer> stack = new Stack<>();
                    stack.push(node);

                    // Explore the component using DFS
                    while (!stack.isEmpty()) {
                        int u = stack.pop();

                        if (!visitedNodes.get(u)) {
                            visitedNodes.put(u, true);
                            component.add(u);

                            for (Integer edge : G.getOutEdges(u)) {
                                int v = G.getEdgeTarget(edge);
                                if (G.getPartitionIndex(v) == blockId && !visitedNodes.get(v)) {
                                    stack.push(v);
                                }
                            }
                        }
                    }

                    // Store this connected component
                    connectedComponents.put(node, component);
                }
            }

            // Number of connected components for the current block
            int numComponents = connectedComponents.size();
            totalComponents += numComponents;  // Accumulate the total number of components

            // Print information for the current block
            System.out.printf("Block %d has %d nodes and %d components:\n", blockId, nodes.size(), numComponents);

            actualSizes.add(nodes.size());  // Store the size of the block
        }

        // Print the total number of connected components across all blocks
        System.out.printf("Total number of connected components across all blocks: %d\n", totalComponents);

        // Calculate CCM based on the desired sizes and actual block sizes
        calculateCCM(actualSizes, G.numberOfNodes());
    }


    private static void calculateCCM(List<Integer> actualSizes, int totalNodes) {
        List<Integer> desiredSizes = Arrays.asList(11014, 3304, 991, 297);
        int totalDifference = 0;
        int partitionsNotMeetingSize = 0;
        double totalPercentageOff = 0.0;

        for (int i = 0; i < desiredSizes.size(); i++) {
            int actualSize = (i < actualSizes.size()) ? actualSizes.get(i) : 0;
            int difference = Math.abs(desiredSizes.get(i) - actualSize);

            totalDifference += difference;

            if (difference > 0) {
                partitionsNotMeetingSize++;
                totalPercentageOff += (double) difference / desiredSizes.get(i) * 100.0;
            }
        }

        double ncdm = (double) totalDifference / totalNodes;
        double percentageNotMeetingSize = (double) partitionsNotMeetingSize / desiredSizes.size() * 100.0;
        double averagePercentageOff = partitionsNotMeetingSize > 0 ? totalPercentageOff / partitionsNotMeetingSize : 0.0;

        System.out.println("Cardinality Compliance Metric (CCM): " + ncdm);
        System.out.println("Percentage of partitions not meeting desired size: " + percentageNotMeetingSize + "%");
        System.out.println("Average percentage by which partitions missed the desired size: " + averagePercentageOff + "%");
    }

}