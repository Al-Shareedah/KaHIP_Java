package org.alshar.lib.data_structure.matrix;
import java.util.ArrayList;
import java.util.List;

public class NormalMatrix extends Matrix {

    private List<List<Integer>> internalMatrix;
    private int dimX, dimY;
    private int lazyInitVal;

    public NormalMatrix(int dimX, int dimY, int lazyInitVal) {
        this.dimX = dimX;
        this.dimY = dimY;
        this.lazyInitVal = lazyInitVal;
        this.internalMatrix = new ArrayList<>(dimX);

        // Initialize the internal matrix with empty lists for lazy initialization
        for (int i = 0; i < dimX; i++) {
            internalMatrix.add(new ArrayList<>());
        }
    }

    public NormalMatrix(int dimX, int dimY) {
        this(dimX, dimY, 0); // Default lazy initialization value to 0
    }

    public int getXY(int x, int y) {
        if (internalMatrix.get(x).isEmpty()) {
            return lazyInitVal;
        }
        return internalMatrix.get(x).get(y);
    }

    public void setXY(int x, int y, int value) {
        // Lazy initialization of the internal matrix row
        if (internalMatrix.get(x).isEmpty()) {
            List<Integer> row = new ArrayList<>(dimY);
            for (int i = 0; i < dimY; i++) {
                row.add(lazyInitVal);
            }
            internalMatrix.set(x, row);
        }
        internalMatrix.get(x).set(y, value);
    }

    public int getXDim() {
        return dimX;
    }

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
