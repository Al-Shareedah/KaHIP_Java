package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.cycle_improvements;

import java.util.ArrayList;
import java.util.List;

public class PairwiseLocalSearch {
    List<Integer> gains = new ArrayList<>();
    List<Integer> vertexMovements = new ArrayList<>();
    List<Integer> blockMovements = new ArrayList<>();
    int[] loadDifference;
    public int getVertexMovement(int index) {
        return vertexMovements.get(index);
    }
    public List<Integer> getGains() {
        return gains;
    }

    public List<Integer> getVertexMovements() {
        return vertexMovements;
    }

    public List<Integer> getBlockMovements() {
        return blockMovements;
    }

    public int[] getLoadDifference() {
        return loadDifference;
    }
    public void clearMovements() {
        gains.clear();
        vertexMovements.clear();
        blockMovements.clear();
    }

    // Method to add a movement
    public void addMovement(int node, int toPartition, int overallGain) {
        vertexMovements.add(node);
        blockMovements.add(toPartition);
        gains.add(overallGain);
    }
    public int getMovementsSize() {
        return vertexMovements.size();
    }
}
