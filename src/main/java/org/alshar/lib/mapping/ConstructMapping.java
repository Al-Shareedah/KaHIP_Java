package org.alshar.lib.mapping;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.Matrix;
import org.alshar.lib.data_structure.priority_queues.MaxNodeHeap;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.tools.QualityMetrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.*;
import java.util.stream.IntStream;

public class ConstructMapping {

    private static final int UNASSIGNED = Integer.MAX_VALUE;
    private static final int ASSIGNED = 1;

    private QualityMetrics qm;

    public ConstructMapping() {
        this.qm = new QualityMetrics();
    }

    public void constructInitialMapping(PartitionConfig config, GraphAccess C, Matrix D, List<Integer> permRank) {
        switch (config.getConstructionAlgorithm()) {
            case MAP_CONST_IDENTITY:
                System.out.println("Running identity mapping");
                constructIdentity(config, C, D, permRank);
                break;
            case MAP_CONST_RANDOM:
                System.out.println("Running random initial mapping");
                constructRandom(config, C, D, permRank);
                break;
            case MAP_CONST_OLDGROWING:
                System.out.println("Running old growing");
                constructOldGrowing(config, C, D, permRank);
                break;
            case MAP_CONST_OLDGROWING_FASTER:
                System.out.println("Running faster growing");
                constructOldGrowingFaster(config, C, D, permRank);
                break;
            case MAP_CONST_FASTHIERARCHY_BOTTOMUP:
                System.out.println("Running fast hierarchy bottom up");
                constructFastHierarchyBottomUp(config, C, D, permRank);
                break;
            case MAP_CONST_FASTHIERARCHY_TOPDOWN:
                System.out.println("Running fast hierarchy top down");
                constructFastHierarchyTopDown(config, C, D, permRank);
                break;
            default:
                System.out.println("Running identity mapping");
                constructIdentity(config, C, D, permRank);
        }
    }

    private void constructOldGrowing(PartitionConfig config, GraphAccess C, Matrix D, List<Integer> permRank) {
        System.out.println("Constructing initial mapping");

        int numNodes = C.numberOfNodes();
        int[] totalDist = new int[numNodes];
        int[] totalVol = new int[numNodes];
        int[] coreAssigned = new int[numNodes];
        for (int i = 0; i < permRank.size(); i++) {
            permRank.set(i, UNASSIGNED);
        }


        // Initialize core assignments
        int maxVol = 0;
        int maxVolElem = 0;
        for (int node = 0; node < numNodes; node++) {
            for (int e = C.getFirstEdge(node); e < C.getFirstInvalidEdge(node); e++) {
                totalVol[node] += C.getEdgeWeight(e);
            }
            if (totalVol[node] > maxVol) {
                maxVol = totalVol[node];
                maxVolElem = node;
            }
        }

        int minDist = Integer.MAX_VALUE;
        int minDistElem = 0;
        for (int cpu = 0; cpu < numNodes; cpu++) {
            for (int cpuBar = 0; cpuBar < numNodes; cpuBar++) {
                totalDist[cpu] += D.getXY(cpu, cpuBar);
            }
            if (totalDist[cpu] < minDist) {
                minDist = totalDist[cpu];
                minDistElem = cpu;
            }
        }

        permRank.set(maxVolElem, minDistElem);
        coreAssigned[minDistElem] = ASSIGNED;

        for (int i = 0; i < numNodes - 1; i++) {
            maxVol = 0;
            maxVolElem = Integer.MAX_VALUE;

            for (int node = 0; node < numNodes; node++) {
                if (permRank.get(node) != UNASSIGNED) continue;

                totalVol[node] = 0;
                for (int e = C.getFirstEdge(node); e < C.getFirstInvalidEdge(node); e++) {
                    int target = C.getEdgeTarget(e);
                    if (permRank.get(target) != UNASSIGNED) {
                        totalVol[node] += C.getEdgeWeight(e);
                    }
                }
                if (totalVol[node] >= maxVol) {
                    maxVol = totalVol[node];
                    maxVolElem = node;
                }
            }

            minDist = Integer.MAX_VALUE;
            minDistElem = Integer.MAX_VALUE;

            for (int cpu = 0; cpu < numNodes; cpu++) {
                totalDist[cpu] = 0;
                if (coreAssigned[cpu] == ASSIGNED) continue;
                for (int cpuBar = 0; cpuBar < numNodes; cpuBar++) {
                    if (coreAssigned[cpuBar] == ASSIGNED) {
                        totalDist[cpu] += D.getXY(cpu, cpuBar);
                    }
                }
                if (totalDist[cpu] <= minDist) {
                    minDist = totalDist[cpu];
                    minDistElem = cpu;
                }
            }

            permRank.set(maxVolElem, minDistElem);
            coreAssigned[minDistElem] = ASSIGNED;
        }
    }

    private void constructOldGrowingFaster(PartitionConfig config, GraphAccess C, Matrix D, List<Integer> permRank) {
        System.out.println("Constructing initial mapping with faster growing");

        int numNodes = C.numberOfNodes();
        for (int i = 0; i < permRank.size(); i++) {
            permRank.set(i, UNASSIGNED);
        }

        MaxNodeHeap unassignedTasks = new MaxNodeHeap();
        int[] totalVol = new int[numNodes];
        int[] totalDist = new int[numNodes];
        int[] coreAssigned = new int[numNodes];
        List<Integer> unassignedPEs = new ArrayList<>();

        int maxVol = 0;
        int maxVolElem = 0;
        for (int node = 0; node < numNodes; node++) {
            int curVol = 0;
            for (int e = C.getFirstEdge(node); e < C.getFirstInvalidEdge(node); e++) {
                curVol += C.getEdgeWeight(e);
            }
            unassignedTasks.insert(node, 0);
            if (curVol > maxVol) {
                maxVol = curVol;
                maxVolElem = node;
            }
        }

        int minDist = Integer.MAX_VALUE;
        int minDistElem = 0;
        for (int cpu = 0; cpu < numNodes; cpu++) {
            int curDist = 0;
            for (int cpuBar = 0; cpuBar < numNodes; cpuBar++) {
                curDist += D.getXY(cpu, cpuBar);
            }
            if (curDist < minDist) {
                minDist = curDist;
                minDistElem = cpu;
            }
        }

        permRank.set(maxVolElem, minDistElem);
        coreAssigned[minDistElem] = ASSIGNED;
        unassignedTasks.deleteNode(maxVolElem);
        IntStream.range(0, numNodes).forEach(unassignedPEs::add);
        unassignedPEs.remove(Integer.valueOf(minDistElem));

        for (int i = 0; i < unassignedPEs.size(); i++) {
            int cpu = unassignedPEs.get(i);
            totalDist[cpu] += D.getXY(minDistElem, cpu);
        }

        for (int e = C.getFirstEdge(maxVolElem); e < C.getFirstInvalidEdge(maxVolElem); e++) {
            int targetTask = C.getEdgeTarget(e);
            if (unassignedTasks.contains(targetTask)) {
                totalVol[targetTask] += C.getEdgeWeight(e);
                unassignedTasks.changeKey(targetTask, totalVol[targetTask]);
            }
        }

        while (unassignedTasks.size() > 0) {
            int curTask = unassignedTasks.deleteMax();

            minDist = Integer.MAX_VALUE;
            minDistElem = Integer.MAX_VALUE;

            int idx = 0;
            for (int i = 0; i < unassignedPEs.size(); i++) {
                int cpu = unassignedPEs.get(i);
                if (coreAssigned[cpu] == ASSIGNED) continue;
                if (totalDist[cpu] <= minDist) {
                    minDist = totalDist[cpu];
                    minDistElem = cpu;
                    idx = i;
                }
            }

            permRank.set(curTask, minDistElem);
            coreAssigned[minDistElem] = ASSIGNED;

            for (int e = C.getFirstEdge(curTask); e < C.getFirstInvalidEdge(curTask); e++) {
                int targetTask = C.getEdgeTarget(e);
                if (unassignedTasks.contains(targetTask)) {
                    totalVol[targetTask] += C.getEdgeWeight(e);
                    unassignedTasks.changeKey(targetTask, totalVol[targetTask]);
                }
            }

            unassignedPEs.remove(idx);
            for (int cpu : unassignedPEs) {
                totalDist[cpu] += D.getXY(minDistElem, cpu);
            }
        }
    }

    private void constructIdentity(PartitionConfig config, GraphAccess C, Matrix D, List<Integer> permRank) {
        for (int i = 0; i < permRank.size(); i++) {
            permRank.set(i, i);
        }
    }

    private void constructRandom(PartitionConfig config, GraphAccess C, Matrix D, List<Integer> permRank) {
        constructIdentity(config, C, D, permRank);
        Collections.shuffle(permRank);
    }

    private void constructFastHierarchyBottomUp(PartitionConfig config, GraphAccess C, Matrix D, List<Integer> permRank) {
        FastConstructMapping fcm = new FastConstructMapping();
        fcm.constructInitialMappingBottomUp(config, C, D, permRank);
    }

    private void constructFastHierarchyTopDown(PartitionConfig config, GraphAccess C, Matrix D, List<Integer> permRank) {
        FastConstructMapping fcm = new FastConstructMapping();
        fcm.constructInitialMappingTopDown(config, C, D, permRank);
    }

    private int minimumNode(List<Integer> nodeAttribs) {
        int minNode = -1;
        int minValue = Integer.MAX_VALUE;
        for (int i = 0; i < nodeAttribs.size(); i++) {
            if (nodeAttribs.get(i) < minValue) {
                minNode = i;
                minValue = nodeAttribs.get(i);
            }
        }
        return minNode;
    }

    private int maximumNode(List<Integer> nodeAttribs) {
        int maxNode = -1;
        int maxValue = -1;
        for (int i = 0; i < nodeAttribs.size(); i++) {
            if (nodeAttribs.get(i) > maxValue) {
                maxNode = i;
                maxValue = nodeAttribs.get(i);
            }
        }
        return maxNode;
    }
}

