package org.alshar.lib.mapping;

import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.data_structure.Pair;

public interface SearchSpace {
    Pair<Integer, Integer> nextPair();
    boolean done();
    void commitStatus(boolean success);
    void setGraphRef(GraphAccess C);
}
