package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement;

import org.alshar.lib.data_structure.BasicGraph;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.data_structure.CoarseMapping;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.BoundaryLookup.BoundaryPair;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.BoundaryLookup.HashBoundaryPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.AbstractMap;
import java.util.concurrent.atomic.AtomicInteger;


public class CompleteBoundary {

    private GraphAccess graphRef;
    private BoundaryLookup.BlockPairs mPairs;
    private List<BlockInformation> blockInfos;
    private List<Integer> singletons;

    private PartialBoundary pbLhsLazy;
    private PartialBoundary pbRhsLazy;
    private int lazyLhs;
    private int lazyRhs;
    private BoundaryLookup.BoundaryPair lastPair;
    private int lastKey;
    private BoundaryLookup.HashBoundaryPair hbp;

    public CompleteBoundary(GraphAccess G) {
        this.graphRef = G;
        this.pbLhsLazy = null;
        this.pbRhsLazy = null;
        this.lastPair = null;
        this.lastKey = -1;
        this.blockInfos = new ArrayList<>(Collections.nCopies((int) G.getPartitionCount(), new BlockInformation()));
        this.singletons = new ArrayList<>();
        this.mPairs = new BoundaryLookup.BlockPairs();  // Use the custom BlockPairs class which extends HashMap
    }
    public void postMovedBoundaryNodeUpdates(int node, BoundaryLookup.BoundaryPair pair, boolean updateEdgeCuts, boolean updateAllBoundaries) {
        GraphAccess G = graphRef;

        // Convert long values to int where necessary
        int to = graphRef.getPartitionIndex(node);
        int from = (to == (int) pair.lhs) ? (int) pair.rhs : (int) pair.lhs;

        assert from != to;

        // Loop through all out-edges of the node
        for (int e = G.getFirstEdge(node), end = G.getFirstInvalidEdge(node); e < end; e++) {
            int target = G.getEdgeTarget(e);
            int targetPartition = G.getPartitionIndex(target);

            if (updateAllBoundaries || targetPartition != to) {
                // Delete from old boundary
                BoundaryLookup.BoundaryPair deleteBp = new BoundaryLookup.BoundaryPair(graphRef.getPartitionCount(), from, targetPartition);

                int edgeWeight = G.getEdgeWeight(e);
                if (targetPartition != from) {
                    deleteNode(node, from, deleteBp);

                    boolean targetIsStillIncident = false;
                    for (int te = G.getFirstEdge(target), tEnd = G.getFirstInvalidEdge(target); te < tEnd; te++) {
                        int targetsTarget = G.getEdgeTarget(te);
                        int targetsTargetPartition = G.getPartitionIndex(targetsTarget);
                        if (targetsTargetPartition == from) {
                            targetIsStillIncident = true;
                            break;
                        }
                    }

                    if (!targetIsStillIncident) {
                        deleteNode(target, targetPartition, deleteBp);
                    }

                    if (updateEdgeCuts) {
                        BoundaryLookup.DataBoundaryPair dataPair = mPairs.get(deleteBp);
                        if (dataPair != null) {
                            dataPair.edgeCut -= edgeWeight;
                        }
                    }
                }

                if (targetPartition != to) {
                    // Insert into new boundary
                    BoundaryLookup.BoundaryPair insertBp = new BoundaryLookup.BoundaryPair(graphRef.getPartitionCount(), to, targetPartition);

                    insert(node, to, insertBp);
                    insert(target, targetPartition, insertBp);

                    if (updateEdgeCuts) {
                        BoundaryLookup.DataBoundaryPair dataPair = mPairs.get(insertBp);
                        if (dataPair != null) {
                            dataPair.edgeCut += edgeWeight;
                        }
                    }
                }
            }
        }
    }


    public void balanceSingletons(PartitionConfig config, GraphAccess G) {
        // Iterate over all singleton nodes
        for (int i = 0; i < singletons.size(); i++) {
            // Initialize with the first block's weight
            int minWeight = blockInfos.get(0).getBlockWeight();
            int partition = 0;

            // Find the partition with the minimum block weight
            for (int j = 0; j < blockInfos.size(); j++) {
                if (blockInfos.get(j).getBlockWeight() < minWeight) {
                    minWeight = blockInfos.get(j).getBlockWeight();
                    partition = j;
                }
            }

            // Get the current node and its weight
            int node = singletons.get(i);
            int nodeWeight = G.getNodeWeight(node);

            // Check if the node can be added to the partition without exceeding the upper bound
            if (blockInfos.get(partition).getBlockWeight() + nodeWeight <= config.getUpperBoundPartition()) {
                // Adjust the block weights for the current partition and the target partition
                int currentPartition = G.getPartitionIndex(node);
                blockInfos.get(currentPartition).setBlockWeight(
                        blockInfos.get(currentPartition).getBlockWeight() - nodeWeight
                );
                blockInfos.get(partition).setBlockWeight(
                        blockInfos.get(partition).getBlockWeight() + nodeWeight
                );

                // Update the node's partition in the graph
                G.setPartitionIndex(node, partition);
            }
        }
    }



    public void build() {
        GraphAccess G = graphRef;

        for (int block = 0; block < G.getPartitionCount(); block++) {
            blockInfos.get(block).setBlockWeight(0);
            blockInfos.get(block).setBlockNoNodes(0);
        }

        for (int n = 0; n < G.numberOfNodes(); n++) {
            int sourcePartition = G.getPartitionIndex(n);

            int currentWeight = blockInfos.get(sourcePartition).getBlockWeight();
            blockInfos.get(sourcePartition).setBlockWeight(currentWeight + G.getNodeWeight(n));

            int currentNoNodes = blockInfos.get(sourcePartition).getBlockNoNodes();
            blockInfos.get(sourcePartition).setBlockNoNodes(currentNoNodes + 1);

            if (G.getNodeDegree(n) == 0) {
                singletons.add(n);
            }

            for (int e = G.getFirstEdge(n), end = G.getFirstInvalidEdge(n); e < end; e++) {
                int targetID = G.getEdgeTarget(e);
                int targetPartition = G.getPartitionIndex(targetID);
                boolean isCutEdge = (sourcePartition != targetPartition);

                if (isCutEdge) {
                    BoundaryPair bp = new BoundaryPair();
                    bp.k = graphRef.getPartitionCount();
                    bp.lhs = sourcePartition;
                    bp.rhs = targetPartition;
                    updateLazyValues(bp);
                    mPairs.get(bp).edgeCut += G.getEdgeWeight(e);
                    insert(n, sourcePartition, bp);
                }
            }
        }

        for (BoundaryLookup.DataBoundaryPair value : mPairs.values()) {
            value.edgeCut /= 2;
        }
    }

    public void buildFromCoarser(CompleteBoundary coarserBoundary, int coarserNoNodes, CoarseMapping cmapping) {
        GraphAccess G = graphRef;

        List<Boolean> coarseIsBorderNode = new ArrayList<>(Collections.nCopies(coarserNoNodes, false));
        List<BoundaryPair> coarserQGraphEdges = new ArrayList<>();
        coarserBoundary.getQuotientGraphEdges(coarserQGraphEdges);

        for (BoundaryPair bp : coarserQGraphEdges) {
            int lhs = bp.lhs;
            int rhs = bp.rhs;
            PartialBoundary lhsB = coarserBoundary.getDirectedBoundary(lhs, lhs, rhs);
            PartialBoundary rhsB = coarserBoundary.getDirectedBoundary(rhs, lhs, rhs);

            lhsB.forAllBoundaryNodes(n -> coarseIsBorderNode.set((int) n, true));
            rhsB.forAllBoundaryNodes(n -> coarseIsBorderNode.set((int) n, true));
        }

        for (int block = 0; block < G.getPartitionCount(); block++) {
            BlockInformation blockInfo = blockInfos.get(block);
            blockInfo.setBlockWeight(0);
            blockInfo.setBlockNoNodes(0);
        }

        for (int n = 0; n < G.numberOfNodes(); n++) {
            int sourcePartition = G.getPartitionIndex(n);
            BlockInformation blockInfo = blockInfos.get(sourcePartition);
            blockInfo.setBlockNoNodes(blockInfo.getBlockNoNodes() + 1);

            if (G.getNodeDegree(n) == 0) {
                singletons.add(n);
            }

            int coarseNode = cmapping.get(n);
            if (!coarseIsBorderNode.get(coarseNode)) continue;

            for (int e = G.getFirstEdge(n), end = G.getFirstInvalidEdge(n); e < end; e++) {
                int targetID = G.getEdgeTarget(e);
                int targetPartition = G.getPartitionIndex(targetID);
                boolean isCutEdge = (sourcePartition != targetPartition);

                if (isCutEdge) {
                    BoundaryPair bp = new BoundaryPair(G.getPartitionCount(), sourcePartition, targetPartition);
                    updateLazyValues(bp);
                    mPairs.get(bp).edgeCut += G.getEdgeWeight(e);
                    insert(n, sourcePartition, bp);
                }
            }
        }

        for (int p = 0; p < G.getPartitionCount(); p++) {
            setBlockWeight(p, coarserBoundary.getBlockWeight(p));
        }

        for (BoundaryLookup.DataBoundaryPair value : mPairs.values()) {
            value.edgeCut /= 2;
        }
    }



    public void insert(int node, int insertNodeInto, BoundaryPair pair) {
        updateLazyValues(pair);
        assert (lazyLhs == pair.lhs && lazyRhs == pair.rhs) || (lazyLhs == pair.rhs && lazyRhs == pair.lhs);

        if (insertNodeInto == lazyLhs) {
            assert graphRef.getPartitionIndex(node) == lazyLhs;
            pbLhsLazy.insert(node);
        } else {
            assert graphRef.getPartitionIndex(node) == lazyRhs;
            pbRhsLazy.insert(node);
        }
    }

    public boolean contains(int node, int partition, BoundaryPair pair) {
        updateLazyValues(pair);
        if (partition == lazyLhs) {
            assert graphRef.getPartitionIndex(node) == lazyLhs;
            return pbLhsLazy.contains(node);
        } else {
            assert graphRef.getPartitionIndex(node) == lazyRhs;
            return pbRhsLazy.contains(node);
        }
    }

    public void deleteNode(int node, int partition, BoundaryPair pair) {
        updateLazyValues(pair);
        if (partition == lazyLhs) {
            pbLhsLazy.deleteNode(node);
        } else {
            pbRhsLazy.deleteNode(node);
        }
    }

    public int size(int partition, BoundaryPair pair) {
        updateLazyValues(pair);
        if (partition == lazyLhs) {
            return pbLhsLazy.size();
        } else {
            return pbRhsLazy.size();
        }
    }

    public int getBlockWeight(int partition) {
        return blockInfos.get(partition).getBlockWeight();
    }

    public long getBlockNoNodes(int partition) {
        return blockInfos.get(partition).getBlockNoNodes();
    }

    public int getEdgeCut(BoundaryPair pair) {
        updateLazyValues(pair);
        return mPairs.get(pair).edgeCut;
    }

    public int getEdgeCut(int lhs, int rhs) {
        BoundaryPair bp = new BoundaryPair();
        bp.k = graphRef.getPartitionCount();
        bp.lhs = lhs;
        bp.rhs = rhs;

        return getEdgeCut(bp);
    }

    public void setBlockWeight(int partition, int weight) {
        blockInfos.get(partition).setBlockWeight(weight);
    }

    public void setBlockNoNodes(int partition, int noNodes) {
        blockInfos.get(partition).setBlockNoNodes(noNodes);
    }


    public void setEdgeCut(BoundaryPair pair, int edgeCut) {
        updateLazyValues(pair);
        mPairs.get(pair).edgeCut = edgeCut;
    }

    public void getQuotientGraphEdges(List<BoundaryPair> qgraphEdges) {
        qgraphEdges.addAll(mPairs.keySet());
    }

    public PartialBoundary getDirectedBoundary(int partition, int lhs, int rhs) {
        BoundaryPair bp = new BoundaryPair();
        bp.k = graphRef.getPartitionCount();
        bp.lhs = lhs;
        bp.rhs = rhs;

        updateLazyValues(bp);
        if (partition == lazyLhs) {
            return pbLhsLazy;
        } else {
            return pbRhsLazy;
        }
    }

    public void updateLazyValues(BoundaryPair pair) {
        assert pair.lhs != pair.rhs;

        int key = hbp.hash(pair);
        if (key != lastKey) {
            BoundaryLookup.DataBoundaryPair dbp = mPairs.get(pair);
            if (!dbp.initialized) {
                mPairs.put(pair, new BoundaryLookup.DataBoundaryPair(pair.lhs, pair.rhs));
                dbp.initialized = true;
            }

            pbLhsLazy = dbp.pbLhs;
            pbRhsLazy = dbp.pbRhs;
            lazyLhs = dbp.lhs;
            lazyRhs = dbp.rhs;
            lastPair = pair;
            lastKey = key;
        }
    }

    public void setupStartNodes(GraphAccess G, int partition, BoundaryPair bp, List<Integer> startNodes) {
        startNodes.clear();
        startNodes.addAll(new ArrayList<>(Collections.nCopies(size(partition, bp), 0)));
        AtomicInteger curIdx = new AtomicInteger(0);  // Use AtomicInteger to allow modification inside lambda

        int lhs = bp.lhs;
        int rhs = bp.rhs;
        PartialBoundary lhsB = getDirectedBoundary(partition, lhs, rhs);

        lhsB.forAllBoundaryNodes(curBndNode -> {
            assert G.getPartitionIndex(curBndNode) == partition;
            startNodes.set(curIdx.getAndIncrement(), curBndNode);
        });
    }

    public void getMaxNorm() {
        List<BoundaryPair> qgraphEdges = new ArrayList<>();
        getQuotientGraphEdges(qgraphEdges);
        double max = 0;
        for (BoundaryPair pair : qgraphEdges) {
            if (mPairs.get(pair).edgeCut > max) {
                max = mPairs.get(pair).edgeCut;
            }
        }

        System.out.println("max norm is " + max);
    }

    public void getUnderlyingQuotientGraph(GraphAccess QBar) {
        BasicGraph graphref = new BasicGraph();

        if (QBar.getGraphRef() != null) {
            QBar.getGraphRef().delete();
        }
        QBar.setGraphRef(graphref);

        List<List<Map.Entry<Integer, Integer>>> buildingTool = new ArrayList<>();
        buildingTool.addAll(new ArrayList<>(Collections.nCopies(blockInfos.size(), new ArrayList<>())));

        for (Map.Entry<BoundaryPair, BoundaryLookup.DataBoundaryPair> entry : mPairs.entrySet()) {
            BoundaryPair curPair = entry.getKey();

            buildingTool.get(curPair.lhs).add(new AbstractMap.SimpleEntry<>(curPair.rhs, entry.getValue().edgeCut));
            buildingTool.get(curPair.rhs).add(new AbstractMap.SimpleEntry<>(curPair.lhs, entry.getValue().edgeCut));
        }

        QBar.startConstruction(buildingTool.size(), 2 * mPairs.size());

        for (int p = 0; p < buildingTool.size(); p++) {
            int node = QBar.newNode();
            QBar.setNodeWeight(node, blockInfos.get(p).blockWeight);

            for (Map.Entry<Integer, Integer> qedge : buildingTool.get(p)) {
                int e = QBar.newEdge(node, qedge.getKey());
                QBar.setEdgeWeight(e, qedge.getValue());
            }
        }

        QBar.finishConstruction();
    }

    public void getNeighbors(int block, List<Integer> neighbors) {
        if (QBar.getGraphRef() == null) {
            getUnderlyingQuotientGraph(QBar);
        }

        for (int e = QBar.getFirstEdge(block), end = QBar.getFirstInvalidEdge(block); e < end; e++) {
            assert QBar.getEdgeTarget(e) != block;
            neighbors.add(QBar.getEdgeTarget(e));
        }
    }

    public void setupStartNodesAroundBlocks(GraphAccess G, int lhs, int rhs, List<Integer> startNodes) {
        List<Integer> lhsNeighbors = new ArrayList<>();
        getNeighbors(lhs, lhsNeighbors);

        List<Integer> rhsNeighbors = new ArrayList<>();
        getNeighbors(rhs, rhsNeighbors);

        Map<Integer, Boolean> alreadyContained = new HashMap<>();
        for (int neighbor : lhsNeighbors) {
            PartialBoundary partialBoundaryLhs = getDirectedBoundary(lhs, lhs, neighbor);
            for (int curBndNode : partialBoundaryLhs.getBoundaryNodes()) {
                assert G.getPartitionIndex(curBndNode) == lhs;
                if (!alreadyContained.containsKey(curBndNode)) {
                    startNodes.add(curBndNode);
                    alreadyContained.put(curBndNode, true);
                }
            }

            PartialBoundary partialBoundaryNeighbor = getDirectedBoundary(neighbor, lhs, neighbor);
            for (int curBndNode : partialBoundaryNeighbor.getBoundaryNodes()) {
                assert G.getPartitionIndex(curBndNode) == neighbor;
                if (!alreadyContained.containsKey(curBndNode)) {
                    startNodes.add(curBndNode);
                    alreadyContained.put(curBndNode, true);
                }
            }
        }

        for (int neighbor : rhsNeighbors) {
            PartialBoundary partialBoundaryRhs = getDirectedBoundary(rhs, rhs, neighbor);
            for (int curBndNode : partialBoundaryRhs.getBoundaryNodes()) {
                assert G.getPartitionIndex(curBndNode) == rhs;
                if (!alreadyContained.containsKey(curBndNode)) {
                    startNodes.add(curBndNode);
                    alreadyContained.put(curBndNode, true);
                }
            }

            PartialBoundary partialBoundaryNeighbor = getDirectedBoundary(neighbor, rhs, neighbor);
            for (int curBndNode : partialBoundaryNeighbor.getBoundaryNodes()) {
                assert G.getPartitionIndex(curBndNode) == neighbor;
                if (!alreadyContained.containsKey(curBndNode)) {
                    startNodes.add(curBndNode);
                    alreadyContained.put(curBndNode, true);
                }
            }
        }
    }

    public void setupStartNodesAll(GraphAccess G, List<Integer> startNodes) {
        List<BoundaryPair> quotientGraphEdges = new ArrayList<>();
        getQuotientGraphEdges(quotientGraphEdges);

        Map<Integer, Boolean> alreadyContained = new HashMap<>();

        for (BoundaryPair retValue : quotientGraphEdges) {
            int lhs = retValue.lhs;
            int rhs = retValue.rhs;

            PartialBoundary partialBoundaryLhs = getDirectedBoundary(lhs, lhs, rhs);
            for (int curBndNode : partialBoundaryLhs.getBoundaryNodes()) {
                assert G.getPartitionIndex(curBndNode) == lhs;
                if (!alreadyContained.containsKey(curBndNode)) {
                    startNodes.add(curBndNode);
                    alreadyContained.put(curBndNode, true);
                }
            }

            PartialBoundary partialBoundaryRhs = getDirectedBoundary(rhs, lhs, rhs);
            for (int curBndNode : partialBoundaryRhs.getBoundaryNodes()) {
                assert G.getPartitionIndex(curBndNode) == rhs;
                if (!alreadyContained.containsKey(curBndNode)) {
                    startNodes.add(curBndNode);
                    alreadyContained.put(curBndNode, true);
                }
            }
        }
    }

    public void fastComputeQuotientGraph(GraphAccess QBar, int noOfBlocks) {
        BasicGraph graphref = new BasicGraph();

        if (QBar.getGraphRef() != null) {
            QBar.getGraphRef().delete();
        }
        QBar.setGraphRef(graphref);

        List<List<Integer>> blockListNodes = new ArrayList<>(Collections.nCopies(noOfBlocks, new ArrayList<>()));
        List<Integer> blockWeight = new ArrayList<>(Collections.nCopies(noOfBlocks, 0));
        List<Integer> targetEdgeWeight = new ArrayList<>(Collections.nCopies(noOfBlocks, 0));
        List<Integer> listTargets = new ArrayList<>();

        for (int node = 0; node < graphRef.numberOfNodes(); node++) {
            int sourcePartition = graphRef.getPartitionIndex(node);
            blockListNodes.get(sourcePartition).add(node);
            blockWeight.set(sourcePartition, blockWeight.get(sourcePartition) + graphRef.getNodeWeight(node));
        }

        QBar.startConstruction(noOfBlocks, graphRef.numberOfEdges());

        for (int p = 0; p < noOfBlocks; p++) {
            int sourcePartition = QBar.newNode();
            QBar.setNodeWeight(sourcePartition, blockWeight.get(p));

            for (int node : blockListNodes.get(sourcePartition)) {
                for (int e = graphRef.getFirstEdge(node), end = graphRef.getFirstInvalidEdge(node); e < end; e++) {
                    int targetID = graphRef.getEdgeTarget(e);
                    int targetPartition = graphRef.getPartitionIndex(targetID);
                    boolean isCutEdge = (sourcePartition != targetPartition);
                    if (isCutEdge) {
                        if (targetEdgeWeight.get(targetPartition) == 0) {
                            listTargets.add(targetPartition);
                        }
                        targetEdgeWeight.set(targetPartition, targetEdgeWeight.get(targetPartition) + graphRef.getEdgeWeight(e));
                    }
                }
            }

            for (int targetPartition : listTargets) {
                int e = QBar.newEdge(sourcePartition, targetPartition);
                QBar.setEdgeWeight(e, targetEdgeWeight.get(targetPartition));
                targetEdgeWeight.set(targetPartition, 0);
            }
            listTargets.clear();
        }

        QBar.finishConstruction();
    }

    public boolean assertBNodesInBoundaries() {
        int k = graphRef.getPartitionCount();

        for (int lhs = 0; lhs < k; lhs++) {
            for (int rhs = 0; rhs < k; rhs++) {
                if (rhs == lhs || lhs > rhs) continue;

                BoundaryPair bp = new BoundaryPair();
                bp.k = graphRef.getPartitionCount();
                bp.lhs = lhs;
                bp.rhs = rhs;
                GraphAccess G = graphRef;

                int lhsPartWeight = 0;
                int rhsPartWeight = 0;

                int lhsNoNodes = 0;
                int rhsNoNodes = 0;

                int edgeCut = 0;
                for (int n = 0; n < G.numberOfNodes(); n++) {
                    int sourcePartition = G.getPartitionIndex(n);
                    if (sourcePartition == lhs) {
                        lhsPartWeight += G.getNodeWeight(n);
                        lhsNoNodes++;
                    } else if (sourcePartition == rhs) {
                        rhsPartWeight += G.getNodeWeight(n);
                        rhsNoNodes++;
                    }

                    for (int e = G.getFirstEdge(n), end = G.getFirstInvalidEdge(n); e < end; e++) {
                        int targetID = G.getEdgeTarget(e);
                        int targetPartition = G.getPartitionIndex(targetID);
                        boolean isCutEdge = (sourcePartition == lhs && targetPartition == rhs)
                                || (sourcePartition == rhs && targetPartition == lhs);

                        if (isCutEdge) {
                            edgeCut += G.getEdgeWeight(e);
                            assert contains(n, sourcePartition, bp);
                        }
                    }
                }

                assert blockInfos.get(lhs).blockWeight == lhsPartWeight;
                assert blockInfos.get(rhs).blockWeight == rhsPartWeight;
                assert blockInfos.get(lhs).blockNoNodes == lhsNoNodes;
                assert blockInfos.get(rhs).blockNoNodes == rhsNoNodes;
                assert Mpairs.get(bp).edgeCut == edgeCut / 2;
            }
        }

        return true;
    }

    public boolean assertBoundariesAreBNodes() {
        GraphAccess G = graphRef;
        for (int n = 0; n < G.numberOfNodes(); n++) {
            int partition = G.getPartitionIndex(n);
            for (int e = G.getFirstEdge(n), end = G.getFirstInvalidEdge(n); e < end; e++) {
                int target = G.getEdgeTarget(e);
                int targetsPartition = G.getPartitionIndex(target);

                if (partition != targetsPartition) {
                    BoundaryPair bp = new BoundaryPair();
                    bp.k = G.getPartitionCount();
                    bp.lhs = partition;
                    bp.rhs = targetsPartition;

                    assert contains(n, partition, bp);
                    assert contains(target, targetsPartition, bp);
                }
            }
        }

        List<BoundaryPair> qgraphEdges = new ArrayList<>();
        getQuotientGraphEdges(qgraphEdges);
        for (BoundaryPair pair : qgraphEdges) {
            assert pair.lhs != pair.rhs;
        }

        return true;
    }
}

