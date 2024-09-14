package org.alshar.lib.partition.coarsening.matching.gpa;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.enums.EdgeRating;
import org.alshar.lib.enums.PermutationQuality;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.partition.coarsening.matching.Matching;
import org.alshar.lib.tools.RandomFunctions;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class GPAMatching extends Matching {

    public GPAMatching() {
        super();
    }

    @Override
    public void match(PartitionConfig partitionConfig,
                      GraphAccess G,
                      List<Integer> edgeMatching,
                      List<Integer> coarseMapping,
                      AtomicInteger noOfCoarseVertices,
                      List<Integer> permutation) {

        //System.out.println("matching using gpa");

        permutation.clear();
        edgeMatching.clear();
        coarseMapping.clear();

        for (int i = 0; i < G.numberOfNodes(); i++) {
            permutation.add(i);
            edgeMatching.add(i, i);
            coarseMapping.add(-1);
        }

        noOfCoarseVertices.set(0);

        List<Integer> edgePermutation = new ArrayList<>(G.numberOfEdges());
        List<Integer> sources = new ArrayList<>(Collections.nCopies(G.numberOfEdges(), -1));

        init(G, partitionConfig, permutation, edgeMatching, edgePermutation, sources);

        if (partitionConfig.isEdgeRatingTiebreaking()) {
            PartitionConfig gpaPermConfig = new PartitionConfig(partitionConfig);
            gpaPermConfig.setPermutationQuality(PermutationQuality.PERMUTATION_QUALITY_GOOD);
            RandomFunctions.permutateEntries(gpaPermConfig, edgePermutation, false);
        }

        edgePermutation.sort(new CompareRating(G));

        PathSet pathSet = new PathSet(G, partitionConfig);

        // Grow the paths
        for (int e = 0; e < G.numberOfEdges(); e++) {
            int curEdge = edgePermutation.get(e);
            int source = sources.get(curEdge);
            int target = G.getEdgeTarget(curEdge);
            if (target < source) continue; // Skip double edges

            if (G.getEdgeRating(curEdge) == 0.0) {
                continue;
            }

            // Max vertex weight constraint
            if (G.getNodeWeight(source) + G.getNodeWeight(target) > partitionConfig.getMaxVertexWeight()) {
                continue;
            }

            if (partitionConfig.isCombine() &&
                    G.getSecondPartitionIndex(source) != G.getSecondPartitionIndex(target)) {
                continue;
            }

            pathSet.addIfApplicable(source, curEdge);
        }

        extractPathsApplyMatching(G, sources, edgeMatching, pathSet);

        // Construct the coarse mapping
        noOfCoarseVertices.set(0);
        if (!partitionConfig.isGraphAlreadyPartitioned()) {
            for (int n = 0; n < G.numberOfNodes(); n++) {
                if (partitionConfig.isCombine() &&
                        G.getSecondPartitionIndex(n) != G.getSecondPartitionIndex(edgeMatching.get(n))) {
                    edgeMatching.set(n, n);
                }

                if (n < edgeMatching.get(n)) {
                    coarseMapping.set(n, noOfCoarseVertices.get());
                    coarseMapping.set(edgeMatching.get(n), noOfCoarseVertices.get());
                    noOfCoarseVertices.incrementAndGet();
                } else if (n == edgeMatching.get(n)) {
                    coarseMapping.set(n, noOfCoarseVertices.get());
                    noOfCoarseVertices.incrementAndGet();
                }
            }
        } else {
            for (int n = 0; n < G.numberOfNodes(); n++) {
                if (G.getPartitionIndex(n) != G.getPartitionIndex(edgeMatching.get(n))) {
                    edgeMatching.set(n, n);
                }

                if (partitionConfig.isCombine() &&
                        G.getSecondPartitionIndex(n) != G.getSecondPartitionIndex(edgeMatching.get(n))) {
                    edgeMatching.set(n, n);
                }

                if (n < edgeMatching.get(n)) {
                    coarseMapping.set(n, noOfCoarseVertices.get());
                    coarseMapping.set(edgeMatching.get(n), noOfCoarseVertices.get());
                    noOfCoarseVertices.incrementAndGet();
                } else if (n == edgeMatching.get(n)) {
                    coarseMapping.set(n, noOfCoarseVertices.get());
                    noOfCoarseVertices.incrementAndGet();
                }
            }
        }
    }


    private void init(GraphAccess G,
                      PartitionConfig partitionConfig,
                      List<Integer> permutation,
                      List<Integer> edgeMatching,
                      List<Integer> edgePermutation,
                      List<Integer> sources) {

        for (int n = 0; n < G.numberOfNodes(); n++) {
            permutation.set(n, n);
            edgeMatching.set(n, n);

            for (int e = G.getFirstEdge(n); e < G.getFirstInvalidEdge(n); e++) {
                sources.set(e, n);
                edgePermutation.add(e);

                if (partitionConfig.getEdgeRating() == EdgeRating.WEIGHT) {
                    G.setEdgeRating(e, G.getEdgeWeight(e));
                }
            }
        }
    }

    private void extractPathsApplyMatching(GraphAccess G,
                                           List<Integer> sources,
                                           List<Integer> edgeMatching,
                                           PathSet pathSet) {
        for (int n = 0; n < G.numberOfNodes(); n++) {
            Path p = pathSet.getPath(n);

            if (!p.isActive()) {
                continue;
            }
            if (p.getTail() != n) {
                continue;
            }
            if (p.getLength() == 0) {
                continue;
            }

            if (p.getHead() == p.getTail()) {
                // Handling cycles
                List<Integer> aMatching = new ArrayList<>();
                List<Integer> aSecondMatching = new ArrayList<>();
                Deque<Integer> unpackedCycle = new ArrayDeque<>();
                unpackPath(p, pathSet, unpackedCycle);

                int first = unpackedCycle.pollFirst();  // Equivalent to pop_front() in C++

                AtomicReference<Double> matchingRating = new AtomicReference<>(0.0);
                maximumWeightMatching(G, unpackedCycle, aMatching, matchingRating);

                unpackedCycle.addFirst(first); // Equivalent to push_front() in C++
                int last = unpackedCycle.pollLast(); // Equivalent to pop_back() in C++

                AtomicReference<Double> secondMatchingRating = new AtomicReference<>(0.0);
                maximumWeightMatching(G, unpackedCycle, aSecondMatching, secondMatchingRating);

                unpackedCycle.addLast(last); // Equivalent to push_back() in C++

                if (matchingRating.get() > secondMatchingRating.get()) {
                    applyMatching(G, aMatching, sources, edgeMatching);
                } else {
                    applyMatching(G, aSecondMatching, sources, edgeMatching);
                }
            } else {
                // Handling paths
                List<Integer> aMatching = new ArrayList<>();
                List<Integer> unpackedPath = new ArrayList<>();

                if (p.getLength() == 1) {
                    int e;
                    if (pathSet.nextVertex(p.getTail()) == p.getHead()) {
                        e = pathSet.edgeToNext(p.getTail());
                    } else {
                        e = pathSet.edgeToPrev(p.getTail());
                    }

                    int source = sources.get(e);
                    int target = G.getEdgeTarget(e);

                    edgeMatching.set(source, target);
                    edgeMatching.set(target, source);

                    continue;
                }
                unpackPath(p, pathSet, unpackedPath);

                AtomicReference<Double> finalRating = new AtomicReference<>(0.0);
                maximumWeightMatching(G, unpackedPath, aMatching, finalRating);

                applyMatching(G, aMatching, sources, edgeMatching);
            }
        }
    }



    private void applyMatching(GraphAccess G,
                               List<Integer> matchedEdges,
                               List<Integer> sources,
                               List<Integer> edgeMatching) {

        for (int e : matchedEdges) {
            int source = sources.get(e);
            int target = G.getEdgeTarget(e);

            edgeMatching.set(source, target);
            edgeMatching.set(target, source);
        }
    }

    private <T extends Collection<Integer>> void unpackPath(Path p,
                                                            PathSet pathSet,
                                                            T unpackedPath) {

        int head = p.getHead();
        int prev = p.getTail();
        int next;
        int current = prev;

        if (prev == head) {
            // Special case: the given path is a cycle
            current = pathSet.nextVertex(prev);
            unpackedPath.add(pathSet.edgeToNext(prev));
        }

        while (current != head) {
            if (pathSet.nextVertex(current) == prev) {
                next = pathSet.prevVertex(current);
                unpackedPath.add(pathSet.edgeToPrev(current));
            } else {
                next = pathSet.nextVertex(current);
                unpackedPath.add(pathSet.edgeToNext(current));
            }
            prev = current;
            current = next;
        }
    }

    private <T extends Collection<Integer>> void maximumWeightMatching(GraphAccess G,
                                                                       T unpackedPath,
                                                                       List<Integer> matchedEdges,
                                                                       AtomicReference<Double> finalRating) {
        List<Integer> unpackedPathList = new ArrayList<>(unpackedPath);
        int k = unpackedPath.size();
        if (k == 1) {
            matchedEdges.add(unpackedPathList.get(0));
            return;
        }

        List<Double> ratings = new ArrayList<>(Collections.nCopies(k, 0.0));
        List<Boolean> decision = new ArrayList<>(Collections.nCopies(k, false));

        ratings.set(0, G.getEdgeRating(unpackedPathList.get(0)));
        ratings.set(1, G.getEdgeRating(unpackedPathList.get(1)));

        decision.set(0, true);
        if (ratings.get(0) < ratings.get(1)) {
            decision.set(1, true);
        }

        for (int i = 2; i < k; i++) {
            double curRating = G.getEdgeRating(unpackedPathList.get(i));
            if (curRating + ratings.get(i - 2) > ratings.get(i - 1)) {
                decision.set(i, true);
                ratings.set(i, curRating + ratings.get(i - 2));
            } else {
                decision.set(i, false);
                ratings.set(i, ratings.get(i - 1));
            }
        }

        if (decision.get(k - 1)) {
            finalRating.set(ratings.get(k - 1));
        } else {
            finalRating.set(ratings.get(k - 2));
        }

        for (int i = k - 1; i >= 0;) {
            if (decision.get(i)) {
                matchedEdges.add(unpackedPathList.get(i));
                i -= 2;
            } else {
                i -= 1;
            }
        }
    }

}