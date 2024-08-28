package org.alshar.lib.data_structure;

import org.alshar.lib.data_structure.matrix.Matrix;

public class ConcreteMatrix extends Matrix {

    private int[][] data;

    public ConcreteMatrix(int dimX, int dimY) {
        super(dimX, dimY);
        data = new int[dimX][dimY];
    }

    @Override
    public int getXY(int x, int y) {
        return data[x][y];
    }

    @Override
    public void setXY(int x, int y, int value) {
        data[x][y] = value;
    }

    @Override
    public int getXDim() {
        return dimX;
    }

    @Override
    public int getYDim() {
        return dimY;
    }
}

