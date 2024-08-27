package org.alshar.lib.partition.uncoarsening.refinement.node_separators;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.priority_queues.MaxNodeHeap;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.PartialBoundary;
import org.alshar.lib.tools.RandomFunctions;

import java.util.ArrayList;
import java.util.List;


public class LocalizedFmNsLocalSearch {

    private List<Integer> movedNodes = new ArrayList<>();

    public LocalizedFmNsLocalSearch() {
    }

    public int performRefinement(PartitionConfig config, GraphAccess G, List<Integer> blockWeights,
                                  List<Boolean> movedOutOfSeparator, PartialBoundary separator, boolean balance, int to) {

        List<Integer> startNodes = new ArrayList<>();
        separator.forAllBoundaryNodes(node -> {
            startNodes.add(node);
        });

        RandomFunctions.permutateVectorGood(startNodes, false);

        long improvement = 0;
        while (!startNodes.isEmpty()) {
            List<Integer> realStartNodes = new ArrayList<>();
            int noRndNodes = Math.min(config.getSepLocFmNoSnodes(), startNodes.size());
            for (int i = 0; i < noRndNodes; i++) {
                int idx = RandomFunctions.nextInt(0, startNodes.size() - 1);
                int curNode = startNodes.get(idx);
                swap(startNodes, idx, startNodes.size() - 1);
                startNodes.remove(startNodes.size() - 1);
                if (G.getPartitionIndex(curNode) == 2 && !movedOutOfSeparator.get(curNode)) {
                    realStartNodes.add(curNode);
                }
            }

            if (!realStartNodes.isEmpty()) {
                improvement += performRefinementInternal(config, G, realStartNodes, blockWeights, movedOutOfSeparator, separator, balance, to);
            }
        }

        for (int node : movedNodes) {
            movedOutOfSeparator.set(node, false);
        }
        movedNodes.clear();

        return (int) improvement;
    }

    public long performRefinement(PartitionConfig config, GraphAccess G, boolean balance, int to) {

        List<Boolean> movedOutOfSeparator = new ArrayList<>(G.numberOfNodes());
        for (int i = 0; i < G.numberOfNodes(); i++) {
            movedOutOfSeparator.add(false);
        }

        List<Integer> startNodes = new ArrayList<>();
        for (int node = 0; node < G.numberOfNodes(); node++) {
            if (G.getPartitionIndex(node) == 2) {
                startNodes.add(node);
            }
        }
        RandomFunctions.permutateVectorGood(startNodes, false);

        long improvement = 0;
        while (!startNodes.isEmpty()) {
            List<Integer> realStartNodes = new ArrayList<>();
            int noRndNodes = Math.min(config.getSepLocFmNoSnodes(), startNodes.size());
            for (int i = 0; i < noRndNodes; i++) {
                int idx = RandomFunctions.nextInt(0, startNodes.size() - 1);
                int curNode = startNodes.get(idx);
                swap(startNodes, idx, startNodes.size() - 1);
                startNodes.remove(startNodes.size() - 1);
                if (G.getPartitionIndex(curNode) == 2 && !movedOutOfSeparator.get(curNode)) {
                    realStartNodes.add(curNode);
                }
            }

            if (!realStartNodes.isEmpty()) {
                improvement += performRefinementInternal(config, G, realStartNodes, movedOutOfSeparator, balance, to);
            }
        }

        return improvement;
    }

    public long performRefinementInternal(PartitionConfig config, GraphAccess G, List<Integer> startNodes,
                                          List<Integer> blockWeights, List<Boolean> movedOutOfSeparator,
                                          PartialBoundary separator, boolean balance, int to) {

        List<MaxNodeHeap> queues = new ArrayList<>(2);
        queues.add(new MaxNodeHeap());
        queues.add(new MaxNodeHeap());

        List<ChangeSet> rollbackInfo = new ArrayList<>();

        for (int node : startNodes) {
            int toLHS = 0;
            int toRHS = 0;
            computeGain(G, node, toLHS, toRHS);

            queues.get(0).insert(node, toLHS);
            queues.get(1).insert(node, toRHS);
        }

        long bestSeparator = blockWeights.get(2);
        long inputSeparator = blockWeights.get(2);
        int bestDiff = Math.abs((int) (blockWeights.get(1) - blockWeights.get(0)));
        int undoIdx = 0;

        List<Integer> bestBlockWeights = new ArrayList<>(blockWeights);

        int stepsTillLastImprovement = 0;
        while (stepsTillLastImprovement < config.getSepLocFmUnsuccSteps()) {
            long gainToA = queues.get(0).maxValue();
            long gainToB = queues.get(1).maxValue();

            long topGain;
            int toBlock;

            if (balance) {
                topGain = queues.get(to).maxValue();
                toBlock = to;
            } else {
                if (gainToA == gainToB) {
                    topGain = gainToA;
                    toBlock = RandomFunctions.nextInt(0, 1);
                } else {
                    topGain = Math.max(gainToA, gainToB);
                    toBlock = topGain == gainToA ? 0 : 1;
                }
            }

            long otherGain = Math.min(gainToA, gainToB);
            int otherBlock = toBlock == 0 ? 1 : 0;

            int nodeToBlock = queues.get(toBlock).maxElement();
            if (blockWeights.get(toBlock) + G.getNodeWeight(nodeToBlock) < config.getUpperBoundPartition()) {
                queues.get(toBlock).deleteMax();
                queues.get(otherBlock).deleteNode(nodeToBlock);
                moveNode(G, nodeToBlock, toBlock, otherBlock, blockWeights, movedOutOfSeparator, queues, rollbackInfo, separator);
            } else {
                int nodeOtherBlock = queues.get(otherBlock).maxElement();
                if (otherGain >= 0 && blockWeights.get(otherBlock) + G.getNodeWeight(nodeOtherBlock) < config.getUpperBoundPartition()) {
                    queues.get(otherBlock).deleteMax();
                    queues.get(toBlock).deleteNode(nodeOtherBlock);
                    moveNode(G, nodeOtherBlock, otherBlock, toBlock, blockWeights, movedOutOfSeparator, queues, rollbackInfo, separator);
                } else {
                    if (nodeOtherBlock == nodeToBlock) {
                        queues.get(0).deleteMax();
                        queues.get(1).deleteMax();
                    } else {
                        int block = RandomFunctions.nextInt(0, 1);
                        queues.get(block).deleteMax();
                    }
                }
            }

            int curDiff = Math.abs((int) (blockWeights.get(1) - blockWeights.get(0)));
            if (blockWeights.get(2) < bestSeparator || (blockWeights.get(2) == bestSeparator && curDiff < bestDiff)) {
                bestSeparator = blockWeights.get(2);
                undoIdx = rollbackInfo.size();
                stepsTillLastImprovement = 0;
                bestBlockWeights = new ArrayList<>(blockWeights);
            } else {
                stepsTillLastImprovement++;
            }

            if (queues.get(0).isEmpty() || queues.get(1).isEmpty()) {
                break;
            }
        }

        for (int i = rollbackInfo.size() - 1; i >= undoIdx; i--) {
            if (G.getPartitionIndex(rollbackInfo.get(i).node) == 2) separator.deleteNode(rollbackInfo.get(i).node);
            G.setPartitionIndex(rollbackInfo.get(i).node, rollbackInfo.get(i).block);
            if (G.getPartitionIndex(rollbackInfo.get(i).node) == 2) separator.insert(rollbackInfo.get(i).node);
        }
        blockWeights.clear();
        blockWeights.addAll(bestBlockWeights);

        return inputSeparator - bestSeparator;
    }

    public long performRefinementInternal(PartitionConfig config, GraphAccess G, List<Integer> startNodes,
                                          List<Boolean> movedOutOfSeparator, boolean balance, int to) {

        List<MaxNodeHeap> queues = new ArrayList<>(2);
        queues.add(new MaxNodeHeap());
        queues.add(new MaxNodeHeap());

        List<ChangeSet> rollbackInfo = new ArrayList<>();

        for (int node : startNodes) {
            int toLHS = 0;
            int toRHS = 0;
            computeGain(G, node, toLHS, toRHS);

            queues.get(0).insert(node, toLHS);
            queues.get(1).insert(node, toRHS);
        }

        List<Long> blockWeights = new ArrayList<>(3);
        blockWeights.add(0L);
        blockWeights.add(0L);
        blockWeights.add(0L);

        for (int node = 0; node < G.numberOfNodes(); node++) {
            if (G.getPartitionIndex(node) == 0) {
                blockWeights.set(0, blockWeights.get(0) + G.getNodeWeight(node));
            } else if (G.getPartitionIndex(node) == 1) {
                blockWeights.set(1, blockWeights.get(1) + G.getNodeWeight(node));
            } else {
                blockWeights.set(2, blockWeights.get(2) + G.getNodeWeight(node));
            }
        }

        long bestSeparator = blockWeights.get(2);
        long inputSeparator = blockWeights.get(2);
        int bestDiff = Math.abs((int) (blockWeights.get(1) - blockWeights.get(0)));
        int undoIdx = 0;

        int stepsTillLastImprovement = 0;
        while (stepsTillLastImprovement < config.getSepLocFmUnsuccSteps()) {
            long gainToA = queues.get(0).maxValue();
            long gainToB = queues.get(1).maxValue();

            long topGain;
            int toBlock;

            if (balance) {
                topGain = queues.get(to).maxValue();
                toBlock = to;
            } else {
                if (gainToA == gainToB) {
                    topGain = gainToA;
                    toBlock = RandomFunctions.nextInt(0, 1);
                } else {
                    topGain = Math.max(gainToA, gainToB);
                    toBlock = topGain == gainToA ? 0 : 1;
                }
            }

            long otherGain = Math.min(gainToA, gainToB);
            int otherBlock = toBlock == 0 ? 1 : 0;

            int nodeToBlock = queues.get(toBlock).maxElement();
            if (blockWeights.get(toBlock) + G.getNodeWeight(nodeToBlock) < config.getUpperBoundPartition()) {
                queues.get(toBlock).deleteMax();
                queues.get(otherBlock).deleteNode(nodeToBlock);
                moveNode(G, nodeToBlock, toBlock, otherBlock, blockWeights, movedOutOfSeparator, queues, rollbackInfo);
            } else {
                int nodeOtherBlock = queues.get(otherBlock).maxElement();
                if (otherGain >= 0 && blockWeights.get(otherBlock) + G.getNodeWeight(nodeOtherBlock) < config.getUpperBoundPartition()) {
                    queues.get(otherBlock).deleteMax();
                    queues.get(toBlock).deleteNode(nodeOtherBlock);
                    moveNode(G, nodeOtherBlock, otherBlock, toBlock, blockWeights, movedOutOfSeparator, queues, rollbackInfo);
                } else {
                    if (nodeOtherBlock == nodeToBlock) {
                        queues.get(0).deleteMax();
                        queues.get(1).deleteMax();
                    } else {
                        int block = RandomFunctions.nextInt(0, 1);
                        queues.get(block).deleteMax();
                    }
                }
            }

            int curDiff = Math.abs((int) (blockWeights.get(1) - blockWeights.get(0)));
            if (blockWeights.get(2) < bestSeparator || (blockWeights.get(2) == bestSeparator && curDiff < bestDiff)) {
                bestSeparator = blockWeights.get(2);
                undoIdx = rollbackInfo.size();
                stepsTillLastImprovement = 0;
            } else {
                stepsTillLastImprovement++;
            }

            if (queues.get(0).isEmpty() || queues.get(1).isEmpty()) {
                break;
            }
        }

        for (int i = rollbackInfo.size() - 1; i >= undoIdx; i--) {
            G.setPartitionIndex(rollbackInfo.get(i).node, rollbackInfo.get(i).block);
        }

        return inputSeparator - bestSeparator;
    }

    private void computeGain(GraphAccess G, int node, long toLHS, long toRHS) {
        toLHS = G.getNodeWeight(node);
        toRHS = G.getNodeWeight(node);

        for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
            int target = G.getEdgeTarget(e);
            if (G.getPartitionIndex(target) == 0) {
                toRHS -= G.getNodeWeight(target);
            } else if (G.getPartitionIndex(target) == 1) {
                toLHS -= G.getNodeWeight(target);
            }
        }
    }

    private void moveNode(GraphAccess G, int node, int toBlock, int otherBlock, List<Long> blockWeights,
                          List<Boolean> movedOutOfS, List<MaxNodeHeap> queues, List<ChangeSet> rollbackInfo) {

        ChangeSet curMove = new ChangeSet();
        curMove.node = node;
        curMove.block = G.getPartitionIndex(node);
        rollbackInfo.add(curMove);

        G.setPartitionIndex(node, toBlock);
        blockWeights.set(toBlock, blockWeights.get(toBlock) + G.getNodeWeight(node));
        blockWeights.set(2, blockWeights.get(2) - G.getNodeWeight(node));
        movedOutOfS.set(node, true);

        List<Integer> toBeAdded = new ArrayList<>();
        List<Integer> toBeUpdated = new ArrayList<>();
        long gainAchieved = G.getNodeWeight(node);

        for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
            int target = G.getEdgeTarget(e);

            if (G.getPartitionIndex(target) == otherBlock) {
                ChangeSet targetMove = new ChangeSet();
                targetMove.node = target;
                targetMove.block = G.getPartitionIndex(target);
                rollbackInfo.add(targetMove);

                G.setPartitionIndex(target, 2);
                blockWeights.set(otherBlock, blockWeights.get(otherBlock) - G.getNodeWeight(target));
                blockWeights.set(2, blockWeights.get(2) + G.getNodeWeight(target));
                gainAchieved -= G.getNodeWeight(target);

                if (!movedOutOfS.get(target)) {
                    toBeAdded.add(target);
                }

                for (int eBar = G.getFirstEdge(target); eBar < G.getFirstInvalidEdge(target); eBar++) {
                    int v = G.getEdgeTarget(eBar);
                    if (queues.get(0).contains(v)) {
                        toBeUpdated.add(v);
                    }
                }
            } else if (G.getPartitionIndex(target) == 2) {
                toBeUpdated.add(target);
            }
        }

        int toLHS = 0;
        int toRHS = 0;

        for (int n : toBeAdded) {
            computeGain(G, n, toLHS, toRHS);
            queues.get(0).insert(n, toLHS);
            queues.get(1).insert(n, toRHS);
        }

        for (int n : toBeUpdated) {
            computeGain(G, n, toLHS, toRHS);
            queues.get(0).changeKey(n, toLHS);
            queues.get(1).changeKey(n, toRHS);
        }
    }

    private void moveNode(GraphAccess G, int node, int toBlock, int otherBlock, List<Integer> blockWeights,
                          List<Boolean> movedOutOfS, List<MaxNodeHeap> queues, List<ChangeSet> rollbackInfo,
                          PartialBoundary separator) {

        ChangeSet curMove = new ChangeSet();
        curMove.node = node;
        curMove.block = G.getPartitionIndex(node);
        rollbackInfo.add(curMove);
        separator.deleteNode(node);

        G.setPartitionIndex(node, toBlock);
        blockWeights.set(toBlock, blockWeights.get(toBlock) + G.getNodeWeight(node));
        blockWeights.set(2, blockWeights.get(2) - G.getNodeWeight(node));
        movedOutOfS.set(node, true);
        movedNodes.add(node);

        List<Integer> toBeAdded = new ArrayList<>();
        List<Integer> toBeUpdated = new ArrayList<>();
        long gainAchieved = G.getNodeWeight(node);

        for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
            int target = G.getEdgeTarget(e);

            if (G.getPartitionIndex(target) == otherBlock) {
                ChangeSet targetMove = new ChangeSet();
                targetMove.node = target;
                targetMove.block = G.getPartitionIndex(target);
                rollbackInfo.add(targetMove);

                G.setPartitionIndex(target, 2);
                separator.insert(target);

                blockWeights.set(otherBlock, blockWeights.get(otherBlock) - G.getNodeWeight(target));
                blockWeights.set(2, blockWeights.get(2) + G.getNodeWeight(target));
                gainAchieved -= G.getNodeWeight(target);

                if (!movedOutOfS.get(target)) {
                    toBeAdded.add(target);
                }

                for (int eBar = G.getFirstEdge(target); eBar < G.getFirstInvalidEdge(target); eBar++) {
                    int v = G.getEdgeTarget(eBar);
                    if (queues.get(0).contains(v)) {
                        toBeUpdated.add(v);
                    }
                }
            } else if (G.getPartitionIndex(target) == 2) {
                toBeUpdated.add(target);
            }
        }

        int toLHS = 0;
        int toRHS = 0;

        for (int n : toBeAdded) {
            computeGain(G, n, toLHS, toRHS);
            queues.get(0).insert(n, toLHS);
            queues.get(1).insert(n, toRHS);
        }

        for (int n : toBeUpdated) {
            computeGain(G, n, toLHS, toRHS);
            queues.get(0).changeKey(n, toLHS);
            queues.get(1).changeKey(n, toRHS);
        }
    }

    private void swap(List<Integer> list, int i, int j) {
        int temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}

