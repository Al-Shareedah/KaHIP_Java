package org.alshar.lib.mapping;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.Pair;
import org.alshar.lib.partition.PartitionConfig;

public class FullSearchSpace implements SearchSpace {

    private int ub;
    private int swapLhs;
    private int swapRhs;
    private int unsuccTries;
    private int numberOfNodes;
    private GraphAccess graph; // Reference to the graph

    public FullSearchSpace(PartitionConfig config, int numberOfNodes) {
        this.unsuccTries = 0;
        this.swapLhs = 0;
        this.swapRhs = 1;
        this.ub = (numberOfNodes * (numberOfNodes - 1)) / 2;
        this.numberOfNodes = numberOfNodes;
        this.graph = null; // Initialize graph reference as null
    }
    @Override
    public boolean done() {
        return !(unsuccTries < ub);
    }

    @Override
    public void commitStatus(boolean success) {
        if (success) {
            unsuccTries = 0;
        } else {
            unsuccTries++;
        }
    }
    @Override
    public void setGraphRef(GraphAccess C) {
        this.graph = C;
    }
    @Override
    public Pair<Integer, Integer> nextPair() {
        Pair<Integer, Integer> retValue = new Pair<>(swapLhs, swapRhs);

        if (swapRhs + 1 < numberOfNodes) {
            swapRhs++;
        } else {
            if (swapLhs + 2 < numberOfNodes) {
                swapLhs++;
                swapRhs = swapLhs + 1;
            } else {
                swapLhs = 0;
                swapRhs = 1;
            }
        }

        return retValue;
    }
}