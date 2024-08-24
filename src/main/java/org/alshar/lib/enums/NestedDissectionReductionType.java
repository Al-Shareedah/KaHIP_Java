package org.alshar.lib.enums;

public enum NestedDissectionReductionType {
    SIMPLICIAL_NODES(0),
    INDISTINGUISHABLE_NODES(1),
    TWINS(2),
    PATH_COMPRESSION(3),
    DEGREE_2_NODES(4),
    TRIANGLE_CONTRACTION(5),
    NUM_TYPES(6);

    private final int value;

    NestedDissectionReductionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
