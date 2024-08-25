package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement;
public class BlockInformation {
    private int blockWeight;
    private int blockNoNodes;

    // Constructor
    public BlockInformation() {
        this.blockWeight = 0;
        this.blockNoNodes = 0;
    }

    // Getter and Setter for blockWeight
    public int getBlockWeight() {
        return blockWeight;
    }

    public void setBlockWeight(int blockWeight) {
        this.blockWeight = blockWeight;
    }

    // Getter and Setter for blockNoNodes
    public int getBlockNoNodes() {
        return blockNoNodes;
    }

    public void setBlockNoNodes(int blockNoNodes) {
        this.blockNoNodes = blockNoNodes;
    }
}
