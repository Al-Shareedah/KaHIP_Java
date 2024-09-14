package org.alshar.lib.partition.initial_partitioning;
import java.util.Arrays;
import java.util.Random;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.GraphHierarchy;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.initial_partitioning.initial_refinement.InitialRefinement;
import org.alshar.lib.tools.GraphPartitionAssertions;
import org.alshar.lib.tools.QualityMetrics;
import org.alshar.lib.tools.Timer;

public class InitialPartitioning {

    public InitialPartitioning() {
    }

    public void performInitialPartitioning(PartitionConfig config, GraphHierarchy hierarchy) {
        GraphAccess G = hierarchy.getCoarsest();
        if (config.isModeNodeSeparators()) {
            performInitialPartitioningSeparator(config, G);
        } else {
            performInitialPartitioning(config, G);
        }
    }

    public void performInitialPartitioning(PartitionConfig config, GraphAccess G) {
        InitialPartitioner partitioner = null;
        switch (config.getInitialPartitioningType()) {
            case INITIAL_PARTITIONING_RECPARTITION:
                partitioner = new InitialPartitionBipartition();
                break;
            case INITIAL_PARTITIONING_BIPARTITION:
                partitioner = new Bipartition();
                break;
        }

        QualityMetrics qm = new QualityMetrics();
        int bestCut;
        int[] bestMap = new int[G.numberOfNodes()];
        if (config.isGraphAlreadyPartitioned() && !config.isOmitGivenPartitioning()) {
            bestCut = qm.edgeCut(G);
            for (int n = 0; n < G.numberOfNodes(); n++) {
                bestMap[n] = G.getPartitionIndex(n);
            }
        } else {
            bestCut = Integer.MAX_VALUE;
        }

        Timer t = new Timer();
        t.restart();
        int[] partitionMap = new int[G.numberOfNodes()];

        int repsToDo = 2;
        if (config.getK() > 1) {
            repsToDo = (int) Math.max(Math.ceil(config.getInitialPartitioningRepetitions() / (Math.log(config.getK()) / Math.log(2))), 2);
        }

        if (config.getInitialPartitioningRepetitions() == 0) {
            repsToDo = 1;
        }
        if (config.isEco()) {
            // Bound the number of initial partitioning repetitions
            repsToDo = Math.min(config.getMinipreps(), repsToDo);
        }

        //System.out.println("Number of initial partitioning repetitions = " + repsToDo);
        //System.out.println("Number of nodes for partition = " + G.numberOfNodes());

        if (!((config.isGraphAlreadyPartitioned() && config.isNoNewInitialPartitioning()) || config.isOmitGivenPartitioning())) {
            for (int rep = 0; rep < repsToDo; rep++) {
                int seed = new Random().nextInt(Integer.MAX_VALUE);
                PartitionConfig workingConfig = new PartitionConfig(config);
                workingConfig.setCombine(false);
                partitioner.initialPartition(workingConfig, seed, G, partitionMap);

                int curCut = qm.edgeCut(G, partitionMap);
                if (curCut < bestCut) {
                    //System.out.println("Improved the current initial partitioning from " + bestCut + " to " + curCut);

                    for (int n = 0; n < G.numberOfNodes(); n++) {
                        bestMap[n] = partitionMap[n];
                    }

                    bestCut = curCut;
                    if (bestCut == 0) break;
                }
            }

            for (int n = 0; n < G.numberOfNodes(); n++) {
                G.setPartitionIndex(n, bestMap[n]);
            }
        }

        G.setPartitionCount(config.getK());

        //System.out.println("Initial partitioning took " + t.elapsed() + " ms");
        //System.out.println("Current initial balance " + qm.balance(G));

        if (config.isInitialPartitionOptimize() || config.isCombine()) {
            InitialRefinement iniref = new InitialRefinement();
            iniref.optimize(config, G, bestCut);
        }

        //System.out.println("Final current initial partitioning from " + bestCut + " to " + bestCut);

        if (!(config.isGraphAlreadyPartitioned() && config.isNoNewInitialPartitioning())) {
            //System.out.println("Final initial cut " + bestCut);
            //System.out.println("Final current initial balance " + qm.balance(G));
        }

        GraphPartitionAssertions.assertGraphHasKWayPartition(config, G);

        partitionMap = null;
        bestMap = null;
        partitioner = null;
    }

    public void performInitialPartitioningSeparator(PartitionConfig config, GraphAccess G) {
        InitialNodeSeparator ipns = new InitialNodeSeparator();
        ipns.computeNodeSeparator(config, G);
    }
}

