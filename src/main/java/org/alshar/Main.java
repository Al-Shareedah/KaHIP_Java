package org.alshar;
import org.alshar.app.BalanceConfiguration;
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

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        args = new String[]{"email.graph", "--k", "4", "--preconfiguration=eco"};
        PartitionConfig partitionConfig = new PartitionConfig();
        String[] graphFilename = new String[1];
        boolean isGraphWeighted = false;
        boolean suppressOutput = false;
        boolean recursive = false;

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
        System.out.println("Time spent for partitioning: " + t.elapsed() + " ms");


        int qap = 0;
        if (partitionConfig.isEnableMapping()) {
            System.out.println("Performing mapping!");

            // Check if k is a power of 2
            boolean powerOfTwo = (partitionConfig.getK() & (partitionConfig.getK() - 1)) == 0;
            int[] permRank = new int[partitionConfig.getK()];

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
                if (partitionConfig.getDistanceConstructionAlgorithm() != PartitionConfig.DIST_CONST_HIERARCHY_ONLINE) {
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
                for (int i = 0; i < permRank.length; i++) {
                    permRank[i] = i;
                }

                OnlineDistanceMatrix D = new OnlineDistanceMatrix(partitionConfig.getK(), partitionConfig.getK());
                D.setPartitionConfig(partitionConfig);
                qap = qm.totalQap(C, D, permRank);
            }

            // Solution check
            int[] tbsorted = permRank.clone();
            Arrays.sort(tbsorted);
            for (int i = 0; i < tbsorted.length; i++) {
                if (tbsorted[i] != i) {
                    System.out.println("Solution is NOT a permutation. Please report this.");
                    System.out.println(tbsorted[i] + " " + i);
                    System.exit(0);
                }
            }

            for (int node = 0; node < G.numberOfNodes(); node++) {
                G.setPartitionIndex(node, permRank[G.getPartitionIndex(node)]);
            }
        }

    }
}