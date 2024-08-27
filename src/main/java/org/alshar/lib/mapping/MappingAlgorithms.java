package org.alshar.lib.mapping;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.Matrix;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.tools.QualityMetrics;
import org.alshar.lib.tools.Timer;

import java.util.List;

public class MappingAlgorithms {
    private QualityMetrics qm;
    private Timer t;

    public MappingAlgorithms() {
        this.qm = new QualityMetrics();
        this.t = new Timer();
    }

    public void constructAMapping(PartitionConfig config, GraphAccess C, Matrix D, List<Integer> permRank) {
        System.out.println("Computing distance matrix");

        // Construct the distance matrix
        ConstructDistanceMatrix cdm = new ConstructDistanceMatrix();
        cdm.constructMatrix(config, D);

        // Construct the initial mapping
        t.restart();
        ConstructMapping cm = new ConstructMapping();
        cm.constructInitialMapping(config, C, D, permRank);
        System.out.println("Construction took " + t.elapsed() + " ms");

        // Perform local search
        t.restart();
        LocalSearchMapping lsm = new LocalSearchMapping();
        switch (config.getLsNeighborhood()) {
            case NSQUARE:
                lsm.performLocalSearch(FullSearchSpace.class, config, C, D, permRank);
                break;
            case NSQUAREPRUNED:
                lsm.performLocalSearch(FullSearchSpacePruned.class, config, C, D, permRank);
                break;
            case COMMUNICATIONGRAPH:
                lsm.performLocalSearch(CommunicationGraphSearchSpace.class, config, C, D, permRank);
                break;
        }

        System.out.println("Local search took " + t.elapsed() + " ms");
    }

    public void graphToMatrix(GraphAccess C, Matrix C_bar) {
        int numNodes = C.numberOfNodes();

        // Initialize the matrix
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                C_bar.setXY(i, j, 0);
            }
        }

        // Populate the matrix based on the graph edges
        for (int node = 0; node < numNodes; node++) {
            for (int e = C.getFirstEdge(node); e < C.getFirstInvalidEdge(node); e++) {
                int target = C.getEdgeTarget(e);
                C_bar.setXY(node, target, C.getEdgeWeight(e));
            }
        }
    }
}

