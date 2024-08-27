package org.alshar.lib.partition.uncoarsening.separator;
import org.alshar.lib.data_structure.FlowGraph;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.algorithms.PushRelabel;
import java.util.*;
import java.util.stream.Collectors;

public class VertexSeparatorFlowSolver {

    public VertexSeparatorFlowSolver() {
    }

    public boolean buildFlowProblem(PartitionConfig config,
                                    GraphAccess G,
                                    int lhs,
                                    int rhs,
                                    List<Integer> lhsNodes,
                                    List<Integer> rhsNodes,
                                    List<Integer> newToOldIds,
                                    FlowGraph fG) {

        int noEdges = 0;
        for (int node : lhsNodes) {
            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                int target = G.getEdgeTarget(e);
                if (rhs == G.getPartitionIndex(target)) {
                    ++noEdges;
                }
            }
        }

        // Build mappings from old to new node ids and reverse
        int idx = 0;
        newToOldIds.clear();
        newToOldIds.addAll(new ArrayList<>(lhsNodes.size() + rhsNodes.size()));
        Map<Integer, Integer> oldToNew = new HashMap<>();

        for (int node : lhsNodes) {
            newToOldIds.set(idx, node);
            oldToNew.put(node, idx++);
        }

        for (int node : rhsNodes) {
            newToOldIds.set(idx, node);
            oldToNew.put(node, idx++);
        }

        int n = lhsNodes.size() + rhsNodes.size() + 2; // +source and target
        if (n == 2) return false;

        int source = n - 2;
        int sink = n - 1;

        long maxCapacity = Integer.MAX_VALUE;
        fG.startConstruction(n, noEdges);  // Call with both nodes and edges

        // Insert directed edges from L to R
        idx = 0;
        for (int node : lhsNodes) {
            int sourceID = idx++;
            for (int e = G.getFirstEdge(node); e < G.getFirstInvalidEdge(node); e++) {
                if (G.getPartitionIndex(G.getEdgeTarget(e)) == rhs) {
                    int targetID = oldToNew.get(G.getEdgeTarget(e));
                    fG.newEdge(sourceID, targetID, maxCapacity);
                }
            }
        }

        // Connect source and sink with outer boundary nodes
        for (int node : lhsNodes) {
            int targetID = oldToNew.get(node);
            fG.newEdge(source, targetID, G.getNodeWeight(node));
        }

        for (int node : rhsNodes) {
            int sourceID = oldToNew.get(node);
            fG.newEdge(sourceID, sink, G.getNodeWeight(node));
        }

        return true;
    }


    public void findSeparator(PartitionConfig config,
                              GraphAccess G,
                              int lhs,
                              int rhs,
                              List<Integer> lhsNodes,
                              List<Integer> rhsNodes,
                              List<Integer> separator) {

        if (lhsNodes.isEmpty() || rhsNodes.isEmpty()) return;

        List<Integer> newToOldIds = new ArrayList<>();
        FlowGraph fG = new FlowGraph();
        buildFlowProblem(config, G, lhs, rhs, lhsNodes, rhsNodes, newToOldIds, fG);

        PushRelabel pr = new PushRelabel();
        int source = fG.numberOfNodes() - 2;
        int sink = fG.numberOfNodes() - 1;

        List<Integer> sTmp = new ArrayList<>();
        pr.solveMaxFlowMinCut(fG, source, sink, true, sTmp);

        List<Integer> s = sTmp.stream()
                .filter(v -> v != source && v != sink)
                .map(newToOldIds::get)
                .collect(Collectors.toList());

        Collections.sort(lhsNodes);
        Collections.sort(rhsNodes);
        Collections.sort(s);

        List<Integer> separatorTmp = new ArrayList<>(Collections.nCopies(lhsNodes.size() + rhsNodes.size(), -1));
        List<Integer> separatorTmp2 = new ArrayList<>(Collections.nCopies(lhsNodes.size() + rhsNodes.size(), -1));

        List<Integer> intersection = lhsNodes.stream()
                .filter(s::contains)
                .collect(Collectors.toList());

        List<Integer> difference = lhsNodes.stream()
                .filter(node -> !s.contains(node))
                .collect(Collectors.toList());

        separator.addAll(intersection);
        separator.addAll(difference);
    }
}
