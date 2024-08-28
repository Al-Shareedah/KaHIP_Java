package org.alshar.lib.data_structure.matrix;
import org.alshar.lib.partition.PartitionConfig;

import java.util.ArrayList;
import java.util.List;

public class OnlineDistanceMatrix extends Matrix {

    private PartitionConfig config;
    private int dimX, dimY;
    private List<Integer> intervalSizes;

    public OnlineDistanceMatrix(int dimX, int dimY) {
        this.dimX = dimX;
        this.dimY = dimY;
        this.intervalSizes = new ArrayList<>();
    }

    public void setPartitionConfig(PartitionConfig config) {
        this.config = config;
        this.intervalSizes = new ArrayList<>(config.getGroupSizes().size());
        this.intervalSizes.add(config.getGroupSizes().get(0));
        for (int i = 1; i < config.getGroupSizes().size(); i++) {
            this.intervalSizes.add(config.getGroupSizes().get(i) * this.intervalSizes.get(i - 1));
        }
    }

    @Override
    public int getXY(int x, int y) {
        // Generate distance based on x and y
        int k = config.getGroupSizes().size() - 1;
        for (; k >= 0; k--) {
            int intervalA = x / intervalSizes.get(k);
            int intervalB = y / intervalSizes.get(k);
            if (intervalA != intervalB) {
                break;
            }
        }
        k++;
        return config.getDistances().get(k);
    }

    @Override
    public void setXY(int x, int y, int value) {
        // Do nothing -- matrix cannot be modified
    }

    @Override
    public int getXDim() {
        return dimX;
    }

    @Override
    public int getYDim() {
        return dimY;
    }

    public void print() {
        for (int i = 0; i < getXDim(); i++) {
            for (int j = 0; j < getYDim(); j++) {
                System.out.print(getXY(i, j) + " ");
            }
            System.out.println();
        }
    }
}

