package org.alshar.lib.mapping;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.Pair;
import org.alshar.lib.partition.PartitionConfig;

import java.util.*;

public class CommunicationGraphSearchSpace implements SearchSpace {

    private List<Pair<Integer, Integer>> listOfPairs;
    private Map<Pair<Integer, Integer>, Boolean> pairActive;
    private List<Integer> deepth;
    private int limit;
    private int pointer;
    private int lastPointer;
    private int searchDeepth;  // Depth of the neighborhood
    private boolean haveToBreak;
    private int unsuccTries;

    private PartitionConfig config;
    private GraphAccess C;

    public static int globalNumNodes = 0;

    public CommunicationGraphSearchSpace(PartitionConfig config, int numberOfNodes) {
        this.pointer = 0;
        this.lastPointer = 0;
        this.unsuccTries = 0;
        this.searchDeepth = config.getCommunicationNeighborhoodDist();
        this.haveToBreak = false;
        this.deepth = new ArrayList<>(Collections.nCopies(numberOfNodes, -1));
        globalNumNodes = numberOfNodes;
        this.config = config;
        this.listOfPairs = new ArrayList<>();
        this.pairActive = new HashMap<>();
    }

    @Override
    public boolean done() {
        return unsuccTries >= limit || haveToBreak;
    }

    @Override
    public void commitStatus(boolean success) {
        if (success) {
            unsuccTries = 0;
        } else {
            unsuccTries++;
        }

        Pair<Integer, Integer> retValue = listOfPairs.get(lastPointer);
        if (!success) {
            pairActive.remove(retValue);
        } else {
            Queue<Integer> bfsQueue = new LinkedList<>();
            List<Integer> touchedNodes = new ArrayList<>();

            bfsQueue.add(retValue.getFirst());
            bfsQueue.add(retValue.getSecond());

            deepth.set(retValue.getFirst(), 0);
            deepth.set(retValue.getSecond(), 0);
            int curDeepth = 0;

            touchedNodes.add(retValue.getFirst());
            touchedNodes.add(retValue.getSecond());

            while (!bfsQueue.isEmpty()) {
                int node = bfsQueue.poll();

                if (deepth.get(node) == curDeepth) {
                    curDeepth++;
                }

                if (curDeepth > searchDeepth) {
                    break;
                }

                for (int e = C.getFirstEdge(node); e < C.getFirstInvalidEdge(node); e++) {
                    int target = C.getEdgeTarget(e);
                    if (deepth.get(target) == -1) {
                        deepth.set(target, curDeepth);
                        bfsQueue.add(target);
                        touchedNodes.add(target);

                        Pair<Integer, Integer> p1 = new Pair<>(retValue.getFirst(), target);
                        Pair<Integer, Integer> p2 = new Pair<>(retValue.getSecond(), target);

                        pairActive.put(p1, true);
                        pairActive.put(p2, true);
                    }
                }
            }

            for (int node : touchedNodes) {
                deepth.set(node, -1);
            }
        }
    }

    @Override
    public Pair<Integer, Integer> nextPair() {
        int startingPos = pointer;
        lastPointer = pointer;

        Pair<Integer, Integer> retValue = listOfPairs.get(pointer++);
        pointer = pointer == limit ? 0 : pointer;

        while (!pairActive.containsKey(retValue) && pointer != startingPos) {
            lastPointer = pointer;
            retValue = listOfPairs.get(pointer++);
            pointer = pointer == limit ? 0 : pointer;
        }

        if (pointer == startingPos) {
            haveToBreak = true;
        }

        return retValue;
    }

    @Override
    public void setGraphRef(GraphAccess C) {
        this.C = C;
        if (config.getCommunicationNeighborhoodDist() == 1) {
            for (int node = 0; node < C.numberOfNodes(); node++) {
                for (int e = C.getFirstEdge(node); e < C.getFirstInvalidEdge(node); e++) {
                    int target = C.getEdgeTarget(e);
                    if (node < target) {
                        listOfPairs.add(new Pair<>(node, target));
                    }
                }
            }
        } else {
            List<Integer> touchedNodes = new ArrayList<>();
            List<Integer> deepth = new ArrayList<>(Collections.nCopies(C.numberOfNodes(), -1));

            for (int node = 0; node < C.numberOfNodes(); node++) {
                touchedNodes.clear();
                Queue<Integer> bfsQueue = new LinkedList<>();
                bfsQueue.add(node);
                touchedNodes.add(node);

                deepth.set(node, 0);
                int curDeepth = 0;

                while (!bfsQueue.isEmpty()) {
                    int v = bfsQueue.poll();

                    if (deepth.get(v) == curDeepth) {
                        curDeepth++;
                    }

                    if (curDeepth > config.getCommunicationNeighborhoodDist()) {
                        break;
                    }

                    for (int e = C.getFirstEdge(v); e < C.getFirstInvalidEdge(v); e++) {
                        int target = C.getEdgeTarget(e);
                        if (deepth.get(target) == -1) {
                            deepth.set(target, curDeepth);
                            bfsQueue.add(target);
                            touchedNodes.add(target);
                            listOfPairs.add(new Pair<>(node, target));
                        }
                    }
                }

                for (int v : touchedNodes) {
                    deepth.set(v, -1);
                }
            }
        }

        Collections.shuffle(listOfPairs, new Random());

        limit = listOfPairs.size();

        for (Pair<Integer, Integer> pair : listOfPairs) {
            pairActive.put(pair, true);
        }

        for (int i = 0; i < deepth.size(); i++) {
            deepth.set(i, -1);
        }
    }
}
