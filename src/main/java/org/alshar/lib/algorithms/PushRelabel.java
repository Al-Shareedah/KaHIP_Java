package org.alshar.lib.algorithms;
import org.alshar.lib.data_structure.FlowGraph;

import java.util.*;
public class PushRelabel {
    private static final int WORK_OP_RELABEL = 9;
    private static final double GLOBAL_UPDATE_FRQ = 0.51;
    private static final int WORK_NODE_TO_EDGES = 4;

    private List<Long> excess;
    private List<Integer> distance;
    private List<Boolean> active;
    private List<Integer> count;
    private Queue<Integer> queue;
    private List<Boolean> bfsTouched;

    private int numRelabels;
    private int gaps;
    private int globalUpdates;
    private int pushes;
    private int work;
    private FlowGraph graph;

    public PushRelabel() {
    }

    public void init(FlowGraph G, int source, int sink) {
        int numNodes = G.numberOfNodes();
        excess = new ArrayList<>(Collections.nCopies(numNodes, 0L));
        distance = new ArrayList<>(Collections.nCopies(numNodes, 0));
        active = new ArrayList<>(Collections.nCopies(numNodes, false));
        count = new ArrayList<>(Collections.nCopies(2 * numNodes, 0));
        bfsTouched = new ArrayList<>(Collections.nCopies(numNodes, false));

        count.set(0, numNodes - 1);
        count.set(numNodes, 1);

        distance.set(source, numNodes);
        active.set(source, true);
        active.set(sink, true);

        for (int e = G.getFirstEdge(source); e < G.getFirstInvalidEdge(source); e++) {
            excess.set(source, excess.get(source) + G.getEdgeCapacity(source, e));
            push(source, e);
        }
    }

    public void globalRelabeling(int source, int sink) {
        Queue<Integer> Q = new LinkedList<>();
        int numNodes = graph.numberOfNodes();

        for (int node = 0; node < numNodes; node++) {
            distance.set(node, Math.max(distance.get(node), numNodes));
            bfsTouched.set(node, false);
        }

        Q.add(sink);
        bfsTouched.set(sink, true);
        bfsTouched.set(source, true);
        distance.set(sink, 0);

        while (!Q.isEmpty()) {
            int node = Q.poll();

            for (int e = graph.getFirstEdge(node); e < graph.getFirstInvalidEdge(node); e++) {
                int target = graph.getEdgeTarget(node, e);
                if (bfsTouched.get(target)) continue;

                int revE = graph.getReverseEdge(node, e);
                if (graph.getEdgeCapacity(target, revE) - graph.getEdgeFlow(target, revE) > 0) {
                    count.set(distance.get(target), count.get(distance.get(target)) - 1);
                    distance.set(target, distance.get(node) + 1);
                    count.set(distance.get(target), count.get(distance.get(target)) + 1);
                    Q.add(target);
                    bfsTouched.set(target, true);
                }
            }
        }
    }

    public void push(int source, int e) {
        pushes++;
        long capacity = graph.getEdgeCapacity(source, e);
        long flow = graph.getEdgeFlow(source, e);
        long amount = Math.min(capacity - flow, excess.get(source));
        int target = graph.getEdgeTarget(source, e);

        if (distance.get(source) <= distance.get(target) || amount == 0) return;

        graph.setEdgeFlow(source, e, flow + amount);

        int revE = graph.getReverseEdge(source, e);
        long revFlow = graph.getEdgeFlow(target, revE);
        graph.setEdgeFlow(target, revE, revFlow - amount);

        excess.set(source, excess.get(source) - amount);
        excess.set(target, excess.get(target) + amount);

        enqueue(target);
    }

    public void enqueue(int target) {
        if (active.get(target)) return;
        if (excess.get(target) > 0) {
            active.set(target, true);
            queue.add(target);
        }
    }

    public void discharge(int node) {
        int end = graph.getFirstInvalidEdge(node);
        for (int e = graph.getFirstEdge(node); e < end && excess.get(node) > 0; e++) {
            push(node, e);
        }

        if (excess.get(node) > 0) {
            if (count.get(distance.get(node)) == 1 && distance.get(node) < graph.numberOfNodes()) {
                gapHeuristic(distance.get(node));
            } else {
                relabel(node);
            }
        }
    }

    public void gapHeuristic(int level) {
        gaps++;
        int numNodes = graph.numberOfNodes();

        for (int node = 0; node < numNodes; node++) {
            if (distance.get(node) < level) continue;
            count.set(distance.get(node), count.get(distance.get(node)) - 1);
            distance.set(node, Math.max(distance.get(node), numNodes));
            count.set(distance.get(node), count.get(distance.get(node)) + 1);
            enqueue(node);
        }
    }

    public void relabel(int node) {
        work += WORK_OP_RELABEL;
        numRelabels++;

        count.set(distance.get(node), count.get(distance.get(node)) - 1);
        distance.set(node, 2 * graph.numberOfNodes());

        for (int e = graph.getFirstEdge(node); e < graph.getFirstInvalidEdge(node); e++) {
            if (graph.getEdgeCapacity(node, e) - graph.getEdgeFlow(node, e) > 0) {
                int target = graph.getEdgeTarget(node, e);
                distance.set(node, Math.min(distance.get(node), distance.get(target) + 1));
            }
            work++;
        }

        count.set(distance.get(node), count.get(distance.get(node)) + 1);
        enqueue(node);
    }

    public long solveMaxFlowMinCut(FlowGraph G, int source, int sink, boolean computeSourceSet, List<Integer> sourceSet) {
        graph = G;
        work = 0;
        numRelabels = 0;
        gaps = 0;
        pushes = 0;
        globalUpdates = 1;

        init(G, source, sink);
        globalRelabeling(source, sink);

        int workTodo = WORK_NODE_TO_EDGES * G.numberOfNodes() + G.numberOfEdges();

        while (!queue.isEmpty()) {
            int v = queue.poll();
            active.set(v, false);
            discharge(v);

            if (work > GLOBAL_UPDATE_FRQ * workTodo) {
                globalRelabeling(source, sink);
                work = 0;
                globalUpdates++;
            }
        }

        if (computeSourceSet) {
            sourceSet.clear();

            for (int node = 0; node < G.numberOfNodes(); node++) {
                bfsTouched.set(node, false);
            }

            Queue<Integer> Q = new LinkedList<>();
            Q.add(source);
            bfsTouched.set(source, true);

            while (!Q.isEmpty()) {
                int node = Q.poll();
                sourceSet.add(node);

                for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                    int target = G.getEdgeTarget(node, e);
                    long resCap = G.getEdgeCapacity(node, e) - G.getEdgeFlow(node, e);
                    if (resCap > 0 && !bfsTouched.get(target)) {
                        Q.add(target);
                        bfsTouched.set(target, true);
                    }
                }
            }
        }

        return excess.get(sink);
    }
}
