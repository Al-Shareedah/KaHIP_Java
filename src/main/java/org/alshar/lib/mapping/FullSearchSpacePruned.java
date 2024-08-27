package org.alshar.lib.mapping;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.Pair;
import org.alshar.lib.partition.PartitionConfig;

import java.util.ArrayList;
import java.util.List;

public class FullSearchSpacePruned implements SearchSpace {

    private int ub;
    private int internalK;
    private int unsuccTries;
    private int numberOfNodes;
    private PartitionConfig config;
    private List<Pair<Integer, Integer>> searchSpacePointers;

    public FullSearchSpacePruned(PartitionConfig config, int numberOfNodes) {
        this.config = config;
        this.numberOfNodes = numberOfNodes;
        this.ub = config.getSearchSpaceS() * (config.getSearchSpaceS() - 1) / 2;
        this.internalK = 0;
        this.unsuccTries = 0;
        this.searchSpacePointers = new ArrayList<>();

        int s = config.getSearchSpaceS();
        for (int k = 0; k < Math.ceil((double) numberOfNodes / s); k++) {
            int lb = k * s;
            searchSpacePointers.add(new Pair<>(lb, lb + 1));
        }
    }

    @Override
    public boolean done() {
        int s = config.getSearchSpaceS();
        return unsuccTries > ub && internalK + 1 == Math.ceil((double) numberOfNodes / s);
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
        // Placeholder method as in the C++ code
    }

    @Override
    public Pair<Integer, Integer> nextPair() {
        int s = config.getSearchSpaceS();
        if (unsuccTries > ub && internalK + 1 != Math.ceil((double) numberOfNodes / s)) {
            internalK++;
            unsuccTries = 0;
        }
        return nextPair(internalK);
    }

    private Pair<Integer, Integer> nextPair(int k) {
        int s = config.getSearchSpaceS();
        int lb = k * s;
        int ub = Math.min((k + 1) * s, numberOfNodes);
        Pair<Integer, Integer> retValue = searchSpacePointers.get(k);

        if (searchSpacePointers.get(k).getSecond() + 1 < ub) {
            searchSpacePointers.set(k, new Pair<>(searchSpacePointers.get(k).getFirst(), searchSpacePointers.get(k).getSecond() + 1));
        } else {
            if (searchSpacePointers.get(k).getFirst() + 2 < ub) {
                searchSpacePointers.set(k, new Pair<>(searchSpacePointers.get(k).getFirst() + 1, searchSpacePointers.get(k).getFirst() + 2));
            } else {
                searchSpacePointers.set(k, new Pair<>(lb, lb + 1));
            }
        }

        return retValue;
    }
}