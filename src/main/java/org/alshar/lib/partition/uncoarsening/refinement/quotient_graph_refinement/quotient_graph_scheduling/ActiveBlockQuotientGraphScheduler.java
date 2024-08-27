package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.quotient_graph_scheduling;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.BoundaryLookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
public class ActiveBlockQuotientGraphScheduler extends QuotientGraphScheduling {

    private List<BoundaryLookup.BoundaryPair> mQuotientGraphEdges;
    private List<BoundaryLookup.BoundaryPair> mActiveQuotientGraphEdges;
    private int mNoOfActiveBlocks;
    private List<Boolean> mIsBlockActive;

    public ActiveBlockQuotientGraphScheduler(PartitionConfig config, List<BoundaryLookup.BoundaryPair> qgraphEdges, int bankAccount) {
        this.mQuotientGraphEdges = qgraphEdges;
        this.mIsBlockActive = new ArrayList<>(config.k);
        for (int i = 0; i < config.k; i++) {
            this.mIsBlockActive.add(true);
        }
        this.mNoOfActiveBlocks = config.k;
        this.mActiveQuotientGraphEdges = new ArrayList<>();
        init();
    }

    @Override
    public boolean hasFinished() {
        if (mActiveQuotientGraphEdges.isEmpty()) {
            init();
        }
        return mNoOfActiveBlocks == 0;
    }

    @Override
    public BoundaryLookup.BoundaryPair getNext() {
        BoundaryLookup.BoundaryPair retValue = mActiveQuotientGraphEdges.remove(mActiveQuotientGraphEdges.size() - 1);
        return retValue;
    }

    @Override
    public void pushStatistics(QGraphEdgeStatistics statistic) {
        if (statistic.isSomethingChanged()) {
            mIsBlockActive.set(statistic.getPair().lhs, true);
            mIsBlockActive.set(statistic.getPair().rhs, true);
        }
    }

    public void activateBlocks(Map<Integer, Integer> blocks) {
        for (Map.Entry<Integer, Integer> entry : blocks.entrySet()) {
            mIsBlockActive.set(entry.getKey(), true);
        }
    }

    private void init() {
        mNoOfActiveBlocks = 0;
        mActiveQuotientGraphEdges.clear();

        for (BoundaryLookup.BoundaryPair bp : mQuotientGraphEdges) {
            int lhs = bp.lhs;
            int rhs = bp.rhs;

            if (mIsBlockActive.get(lhs)) mNoOfActiveBlocks++;
            if (mIsBlockActive.get(rhs)) mNoOfActiveBlocks++;

            if (mIsBlockActive.get(lhs) || mIsBlockActive.get(rhs)) {
                mActiveQuotientGraphEdges.add(bp);
            }
        }

        // Shuffle mActiveQuotientGraphEdges to mimic C++ random_functions::permutate_vector_good_small
        java.util.Collections.shuffle(mActiveQuotientGraphEdges, new Random());

        // Reset the block activity status
        for (int i = 0; i < mIsBlockActive.size(); i++) {
            mIsBlockActive.set(i, false);
        }
    }

}

