package org.alshar.lib.data_structure.matrix;
public abstract class Matrix {

    protected int dimX;
    protected int dimY;

    // Constructors
    public Matrix(int dimX, int dimY) {
        this.dimX = dimX;
        this.dimY = dimY;
    }

    public Matrix() {
        this.dimX = 0;
        this.dimY = 0;
    }

    // Destructor (not needed in Java, managed by garbage collector)
    // public void finalize() {
    //     // Cleanup code if necessary
    // }

    // Abstract methods equivalent to pure virtual methods in C++
    public abstract int getXY(int x, int y);

    public abstract void setXY(int x, int y, int value);

    public abstract int getXDim();

    public abstract int getYDim();
}

