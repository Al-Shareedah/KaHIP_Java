package org.alshar.lib.mapping;

import org.alshar.lib.data_structure.Matrix;
import org.alshar.lib.partition.PartitionConfig;

import java.util.*;

public class ConstructDistanceMatrix {

    public ConstructDistanceMatrix() {
    }

    public void constructMatrix(PartitionConfig config, Matrix D) {
        // Check whether distance matrix is a square matrix
        if (D.getXDim() != D.getYDim()) {
            System.out.println("Distance matrix is not symmetric.");
            System.exit(0);
        }

        switch (config.getDistanceConstructionAlgorithm()) {
            case DIST_CONST_RANDOM:
                constructMatrixRandom(config, D);
                break;
            case DIST_CONST_IDENTITY:
                constructMatrixIdentity(config, D);
                break;
            case DIST_CONST_HIERARCHY:
                constructMatrixHierarchy(config, D);
                break;
            case DIST_CONST_HIERARCHY_ONLINE:
                // Handle hierarchy online case here if needed
                break;
            default:
                constructMatrixRandom(config, D);
        }
    }

    private void constructMatrixRandom(PartitionConfig config, Matrix D) {
        Random random = new Random();
        for (int i = 0; i < D.getXDim(); i++) {
            for (int j = 0; j <= i; j++) {
                int value = random.nextInt(100) + 1; // Random integer between 1 and 100
                D.setXY(i, j, value);
                D.setXY(j, i, value);
            }
        }
    }

    private void constructMatrixIdentity(PartitionConfig config, Matrix D) {
        for (int i = 0; i < D.getXDim(); i++) {
            for (int j = 0; j <= i; j++) {
                D.setXY(i, j, 1);
                D.setXY(j, i, 1);
            }
        }
    }

    private void constructMatrixHierarchy(PartitionConfig config, Matrix D) {
        List<Integer> intervalSizes = new ArrayList<>(Collections.nCopies(config.getGroupSizes().size(), 0));
        intervalSizes.set(0, config.getGroupSizes().get(0));

        for (int i = 1; i < intervalSizes.size(); i++) {
            intervalSizes.set(i, config.getGroupSizes().get(i) * intervalSizes.get(i - 1));
        }

        System.out.println("Total num cores " + intervalSizes.get(intervalSizes.size() - 1));

        for (int i = 0; i < D.getXDim(); i++) {
            for (int j = 0; j <= i; j++) {
                int k = config.getGroupSizes().size() - 1;
                for (; k >= 0; k--) {
                    int intervalA = i / intervalSizes.get(k);
                    int intervalB = j / intervalSizes.get(k);
                    if (intervalA != intervalB) {
                        break;
                    }
                }
                k++;

                int distance = config.getDistances().get(k);
                D.setXY(i, j, distance);
                D.setXY(j, i, distance);
            }
        }
    }
}
