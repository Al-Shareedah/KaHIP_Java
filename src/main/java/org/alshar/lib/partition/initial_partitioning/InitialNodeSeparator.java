package org.alshar.lib.partition.initial_partitioning;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.GraphPartitioner;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.separator.VertexSeparatorAlgorithm;
import org.alshar.lib.tools.QualityMetrics;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.CompleteBoundary;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class InitialNodeSeparator {

    public InitialNodeSeparator() {
    }

    public int singleRun(PartitionConfig config, GraphAccess G) {
        // Redirect stdout to suppress output
        PrintStream originalOut = System.out;
        PrintStream dummyStream = new PrintStream(new ByteArrayOutputStream());
        System.setOut(dummyStream);

        GraphPartitioner partitioner = new GraphPartitioner();
        PartitionConfig partitionConfig = new PartitionConfig(config);
        partitionConfig.setModeNodeSeparators(false);
        partitionConfig.setGlobalCycleIterations(1);
        partitionConfig.setRepetitions(1);

        // Computing a partition
        partitioner.performPartitioning(partitionConfig, G);

        CompleteBoundary boundary = new CompleteBoundary(G);
        boundary.build();

        // Restore stdout
        System.setOut(originalOut);

        VertexSeparatorAlgorithm vsa = new VertexSeparatorAlgorithm();
        List<Integer> separator = new ArrayList<>();

        // Create a very simple separator from that partition
        if (partitionConfig.isSepFullBoundaryIp()) {
            vsa.computeVertexSeparatorSimpler(partitionConfig, G, boundary, separator);
        } else {
            vsa.computeVertexSeparatorSimple(partitionConfig, G, boundary, separator);
        }

        List<Integer> outputSeparator = new ArrayList<>();
        // Improve the separator using flow-based techniques
        vsa.improveVertexSeparator(partitionConfig, G, separator, outputSeparator);

        QualityMetrics qm = new QualityMetrics();
        return qm.separatorWeight(G);
    }

    public void computeNodeSeparator(PartitionConfig config, GraphAccess G) {
        if (config.isGraphAlreadyPartitioned()) {
            return;
        }

        List<Integer> bestSeparator = new ArrayList<>(G.numberOfNodes());
        for (int i = 0; i < G.numberOfNodes(); i++) {
            bestSeparator.add(0);
        }

        long bestSeparatorSize = Long.MAX_VALUE;
        int unsuccCounter = 0;

        for (int i = 0; i < config.getMaxInitialNsTries(); i++) {
            long curSeparatorSize = singleRun(config, G);
            if (curSeparatorSize < bestSeparatorSize) {
                for (int node = 0; node < G.numberOfNodes(); node++) {
                    bestSeparator.set(node, G.getPartitionIndex(node));
                }
                bestSeparatorSize = curSeparatorSize;

                System.out.println("improved initial separator size " + curSeparatorSize);
                unsuccCounter = 0;
            } else {
                unsuccCounter++;
            }

            if (config.isFasterNs() && unsuccCounter >= 5) {
                break;
            }
        }

        for (int node = 0; node < G.numberOfNodes(); node++) {
            G.setPartitionIndex(node, bestSeparator.get(node));
        }
    }
}