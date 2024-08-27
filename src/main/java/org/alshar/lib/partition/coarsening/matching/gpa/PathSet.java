package org.alshar.lib.partition.coarsening.matching.gpa;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;

import java.util.ArrayList;
import java.util.List;

public class PathSet {

    private GraphAccess pG;
    private PartitionConfig config;

    private int noOfPaths;
    private List<Integer> vertexToPath;
    private List<Path> paths;
    private List<Integer> next;
    private List<Integer> prev;
    private List<Integer> nextEdge;
    private List<Integer> prevEdge;

    public PathSet(GraphAccess G, PartitionConfig config) {
        this.pG = G;
        this.config = config;

        noOfPaths = G.numberOfNodes();
        vertexToPath = new ArrayList<>(noOfPaths);
        paths = new ArrayList<>(noOfPaths);
        next = new ArrayList<>(noOfPaths);
        prev = new ArrayList<>(noOfPaths);
        nextEdge = new ArrayList<>(noOfPaths);
        prevEdge = new ArrayList<>(noOfPaths);

        for (int node = 0; node < noOfPaths; node++) {
            paths.add(new Path(node));
            vertexToPath.add(node);
            next.add(node);
            prev.add(node);
            nextEdge.add(Integer.MAX_VALUE);
            prevEdge.add(Integer.MAX_VALUE);
        }
    }

    public Path getPath(int v) {
        int pathId = vertexToPath.get(v);
        return paths.get(pathId);
    }

    public int pathCount() {
        return noOfPaths;
    }

    public int nextVertex(int v) {
        return next.get(v);
    }

    public int prevVertex(int v) {
        return prev.get(v);
    }

    public int edgeToNext(int v) {
        return nextEdge.get(v);
    }

    public int edgeToPrev(int v) {
        return prevEdge.get(v);
    }

    public boolean addIfApplicable(int source, int e) {
        GraphAccess G = pG;
        int target = G.getEdgeTarget(e);

        if (config.isGraphAlreadyPartitioned() && !config.isGpaGrowPathsBetweenBlocks()) {
            if (G.getPartitionIndex(source) != G.getPartitionIndex(target)) return false;

            if (config.isCombine()) {
                if (G.getSecondPartitionIndex(source) != G.getSecondPartitionIndex(target)) return false;
            }
        }

        int sourcePathId = vertexToPath.get(source);
        int targetPathId = vertexToPath.get(target);

        Path sourcePath = paths.get(sourcePathId);
        Path targetPath = paths.get(targetPathId);

        if (!isEndpoint(source) || !isEndpoint(target)) return false;

        if (sourcePath.isCycle() || targetPath.isCycle()) return false;

        if (sourcePathId != targetPathId) {
            noOfPaths--;
            sourcePath.setLength(sourcePath.getLength() + targetPath.getLength() + 1);

            if (sourcePath.getHead() == source && targetPath.getHead() == target) {
                vertexToPath.set(targetPath.getTail(), sourcePathId);
                sourcePath.setHead(targetPath.getTail());
            } else if (sourcePath.getHead() == source && targetPath.getTail() == target) {
                vertexToPath.set(targetPath.getHead(), sourcePathId);
                sourcePath.setHead(targetPath.getHead());
            } else if (sourcePath.getTail() == source && targetPath.getHead() == target) {
                vertexToPath.set(targetPath.getTail(), sourcePathId);
                sourcePath.setTail(targetPath.getTail());
            } else if (sourcePath.getTail() == source && targetPath.getTail() == target) {
                vertexToPath.set(targetPath.getHead(), sourcePathId);
                sourcePath.setTail(targetPath.getHead());
            }

            if (next.get(source) == source) {
                next.set(source, target);
                nextEdge.set(source, e);
            } else {
                prev.set(source, target);
                prevEdge.set(source, e);
            }

            if (next.get(target) == target) {
                next.set(target, source);
                nextEdge.set(target, e);
            } else {
                prev.set(target, source);
                prevEdge.set(target, e);
            }

            targetPath.setActive(false);
            return true;

        } else if (sourcePathId == targetPathId && sourcePath.getLength() % 2 == 1) {
            sourcePath.setLength(sourcePath.getLength() + 1);

            if (next.get(sourcePath.getHead()) == sourcePath.getHead()) {
                next.set(sourcePath.getHead(), sourcePath.getTail());
                nextEdge.set(sourcePath.getHead(), e);
            } else {
                prev.set(sourcePath.getHead(), sourcePath.getTail());
                prevEdge.set(sourcePath.getHead(), e);
            }

            if (next.get(sourcePath.getTail()) == sourcePath.getTail()) {
                next.set(sourcePath.getTail(), sourcePath.getHead());
                nextEdge.set(sourcePath.getTail(), e);
            } else {
                prev.set(sourcePath.getTail(), sourcePath.getHead());
                prevEdge.set(sourcePath.getTail(), e);
            }

            sourcePath.setTail(sourcePath.getHead());
            return true;
        }

        return false;
    }

    private boolean isEndpoint(int v) {
        return next.get(v) == v || prev.get(v) == v;
    }
}
