package org.alshar.lib.partition.coarsening;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.enums.EdgeRating;
import org.alshar.lib.enums.MatchingType;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.tools.RandomFunctions;

import java.util.List;
import java.util.ArrayList;

public class EdgeRatings {

    private final PartitionConfig partitionConfig;

    public EdgeRatings(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }

    public void rate(GraphAccess G, int level) {
        if (level == 0 && partitionConfig.isFirstLevelRandomMatching()) {
            return;
        } else if (partitionConfig.getMatchingType() == MatchingType.MATCHING_RANDOM_GPA && level < partitionConfig.getAggressiveRandomLevels()) {
            return;
        }

        if (level == 0 && partitionConfig.isRateFirstLevelInnerOuter() && partitionConfig.getEdgeRating() != EdgeRating.EXPANSIONSTAR2ALGDIST) {
            rateInnerOuter(G);
        } else if (partitionConfig.getMatchingType() != MatchingType.MATCHING_RANDOM) {
            switch (partitionConfig.getEdgeRating()) {
                case EXPANSIONSTAR:
                    rateExpansionStar(G);
                    break;
                case PSEUDOGEOM:
                    ratePseudogeom(G);
                    break;
                case EXPANSIONSTAR2:
                    rateExpansionStar2(G);
                    break;
                case EXPANSIONSTAR2ALGDIST:
                    rateExpansionStar2Algdist(G);
                    break;
                case WEIGHT:
                case REALWEIGHT:
                    break;
                case SEPARATOR_MULTX:
                    rateSeparatorMultx(G);
                    break;
                case SEPARATOR_ADDX:
                    rateSeparatorAddx(G);
                    break;
                case SEPARATOR_MAX:
                    rateSeparatorMax(G);
                    break;
                case SEPARATOR_LOG:
                    rateSeparatorLog(G);
                    break;
                case SEPARATOR_R1:
                    rateSeparatorR1(G);
                    break;
                case SEPARATOR_R2:
                    rateSeparatorR2(G);
                    break;
                case SEPARATOR_R3:
                    rateSeparatorR3(G);
                    break;
                case SEPARATOR_R4:
                    rateSeparatorR4(G);
                    break;
                case SEPARATOR_R5:
                    rateSeparatorR5(G);
                    break;
                case SEPARATOR_R6:
                    rateSeparatorR6(G);
                    break;
                case SEPARATOR_R7:
                    rateSeparatorR7(G);
                    break;
                case SEPARATOR_R8:
                    rateSeparatorR8(G);
                    break;
            }
        }
    }

    public void computeAlgdist(GraphAccess G, List<Float> dist) {
        for (int R = 0; R < 3; R++) {
            List<Float> prev = new ArrayList<>(G.numberOfNodes());
            for (int i = 0; i < G.numberOfNodes(); i++) {
                prev.add((float) RandomFunctions.nextDouble(-0.5, 0.5));
            }

            List<Float> next = new ArrayList<>(G.numberOfNodes());
            float w = 0.5f;

            for (int k = 0; k < 7; k++) {
                for (int node = 0; node < G.numberOfNodes(); node++) {
                    next.set(node, 0f);

                    for (int e : G.getOutEdges(node)) {
                        int target = G.getEdgeTarget(e);
                        next.set(node, next.get(node) + prev.get(target) * G.getEdgeWeight(e));
                    }

                    float wdegree = G.getWeightedNodeDegree(node);
                    if (wdegree > 0) {
                        next.set(node, next.get(node) / wdegree);
                    }
                }

                for (int node = 0; node < G.numberOfNodes(); node++) {
                    prev.set(node, (1 - w) * prev.get(node) + w * next.get(node));
                }
            }

            for (int node = 0; node < G.numberOfNodes(); node++) {
                for (int e : G.getOutEdges(node)) {
                    int target = G.getEdgeTarget(e);
                    dist.set(e, dist.get(e) + Math.abs(prev.get(node) - prev.get(target)) / 7.0f);
                }
            }
        }

        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e : G.getOutEdges(node)) {
                dist.set(e, dist.get(e) + 0.0001f);
            }
        }
    }

    public void rateExpansionStar2Algdist(GraphAccess G) {
        List<Float> dist = new ArrayList<>(G.numberOfEdges());
        for (int i = 0; i < G.numberOfEdges(); i++) {
            dist.add(0f);
        }
        computeAlgdist(G, dist);

        for (int n = 0; n < G.numberOfNodes(); n++) {
            int sourceWeight = G.getNodeWeight(n);
            for (int e : G.getOutEdges(n)) {
                int targetNode = G.getEdgeTarget(e);
                int targetWeight = G.getNodeWeight(targetNode);
                int edgeWeight = G.getEdgeWeight(e);

                float rating = 1.0f * edgeWeight * edgeWeight / (targetWeight * sourceWeight * dist.get(e));
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void rateExpansionStar2(GraphAccess G) {
        for (int n = 0; n < G.numberOfNodes(); n++) {
            int sourceWeight = G.getNodeWeight(n);
            for (int e : G.getOutEdges(n)) {
                int targetNode = G.getEdgeTarget(e);
                int targetWeight = G.getNodeWeight(targetNode);
                int edgeWeight = G.getEdgeWeight(e);

                float rating = 1.0f * edgeWeight * edgeWeight / (targetWeight * sourceWeight);
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void rateInnerOuter(GraphAccess G) {
        for (int n = 0; n < G.numberOfNodes(); n++) {
            int sourceDegree = G.getWeightedNodeDegree(n);
            if (sourceDegree == 0) continue;

            for (int e : G.getOutEdges(n)) {
                int targetNode = G.getEdgeTarget(e);
                int targetDegree = G.getWeightedNodeDegree(targetNode);
                int edgeWeight = G.getEdgeWeight(e);
                float rating = 1.0f * edgeWeight / (sourceDegree + targetDegree - edgeWeight);
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void rateExpansionStar(GraphAccess G) {
        for (int n = 0; n < G.numberOfNodes(); n++) {
            int sourceWeight = G.getNodeWeight(n);
            for (int e : G.getOutEdges(n)) {
                int targetNode = G.getEdgeTarget(e);
                int targetWeight = G.getNodeWeight(targetNode);
                int edgeWeight = G.getEdgeWeight(e);

                float rating = 1.0f * edgeWeight / (targetWeight * sourceWeight);
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void ratePseudogeom(GraphAccess G) {
        for (int n = 0; n < G.numberOfNodes(); n++) {
            int sourceWeight = G.getNodeWeight(n);
            for (int e : G.getOutEdges(n)) {
                int targetNode = G.getEdgeTarget(e);
                int targetWeight = G.getNodeWeight(targetNode);
                int edgeWeight = G.getEdgeWeight(e);
                double randomTerm = RandomFunctions.nextDouble(0.6, 1.0);
                float rating = (float) (randomTerm * edgeWeight * (1.0 / Math.sqrt(targetWeight) + 1.0 / Math.sqrt(sourceWeight)));
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void rateSeparatorAddx(GraphAccess G) {
        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e : G.getOutEdges(node)) {
                int target = G.getEdgeTarget(e);
                float rating = 1.0f / (G.getNodeDegree(node) + G.getNodeDegree(target));
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void rateSeparatorMultx(GraphAccess G) {
        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e : G.getOutEdges(node)) {
                int target = G.getEdgeTarget(e);
                float rating = (float) Math.pow(G.getNodeDegree(node) * G.getNodeDegree(target), -0.5);
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void rateSeparatorMax(GraphAccess G) {
        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e : G.getOutEdges(node)) {
                int target = G.getEdgeTarget(e);
                float rating = 1.0f / Math.max(G.getNodeDegree(node), G.getNodeDegree(target));
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void rateSeparatorLog(GraphAccess G) {
        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e : G.getOutEdges(node)) {
                int target = G.getEdgeTarget(e);
                float rating = 1.0f / (float) Math.log(G.getNodeDegree(node) * G.getNodeDegree(target));
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void rateSeparatorR1(GraphAccess G) {
        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e : G.getOutEdges(node)) {
                int target = G.getEdgeTarget(e);
                float rating = 1.0f / (G.getNodeDegree(node) * G.getNodeDegree(target));
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void rateSeparatorR2(GraphAccess G) {
        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e : G.getOutEdges(node)) {
                int target = G.getEdgeTarget(e);
                float rating = 1.0f / (G.getNodeDegree(node) * G.getNodeDegree(target) * G.getNodeWeight(node) * G.getNodeWeight(target));
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void rateSeparatorR3(GraphAccess G) {
        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e : G.getOutEdges(node)) {
                int target = G.getEdgeTarget(e);
                float rating = 1.0f / (G.getNodeDegree(node) + G.getNodeDegree(target) + G.getNodeWeight(node) + G.getNodeWeight(target));
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void rateSeparatorR4(GraphAccess G) {
        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e : G.getOutEdges(node)) {
                int target = G.getEdgeTarget(e);
                float rating = (float) ((double) G.getNodeDegree(node) * G.getNodeDegree(target) / (G.getNodeWeight(node) * G.getNodeWeight(target)));
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void rateSeparatorR5(GraphAccess G) {
        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e : G.getOutEdges(node)) {
                int target = G.getEdgeTarget(e);
                float rating = (float) ((double) G.getNodeDegree(node) + G.getNodeDegree(target) / (G.getNodeWeight(node) + G.getNodeWeight(target)));
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void rateSeparatorR6(GraphAccess G) {
        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e : G.getOutEdges(node)) {
                int target = G.getEdgeTarget(e);
                float rating = 1.0f / ((G.getNodeDegree(node) + G.getNodeDegree(target)) * (G.getNodeWeight(node) + G.getNodeWeight(target)));
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void rateSeparatorR7(GraphAccess G) {
        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e : G.getOutEdges(node)) {
                int target = G.getEdgeTarget(e);
                float rating = G.getEdgeWeight(e) * 1.0f / (G.getNodeDegree(node) * G.getNodeDegree(target) * G.getNodeWeight(node) * G.getNodeWeight(target));
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void rateRealweight(GraphAccess G) {
        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e : G.getOutEdges(node)) {
                float rating = G.getEdgeWeight(e);
                G.setEdgeRating(e, rating);
            }
        }
    }

    public void rateSeparatorR8(GraphAccess G) {
        for (int node = 0; node < G.numberOfNodes(); node++) {
            for (int e : G.getOutEdges(node)) {
                int target = G.getEdgeTarget(e);
                float rating = G.getEdgeWeight(e) * 1.0f * (G.getNodeDegree(node) * G.getNodeDegree(target)) / (G.getNodeWeight(node) * G.getNodeWeight(target));
                G.setEdgeRating(e, rating);
            }
        }
    }
}

