package org.alshar.lib.data_structure.priority_queues;

public interface PriorityQueueInterface {
    /* returns the size of the priority queue */
    int size();
    boolean isEmpty();

    void insert(int id, int gain);

    int maxValue();
    int maxElement();
    int deleteMax();

    void decreaseKey(int node, int newGain);
    void increaseKey(int node, int newKey);

    void changeKey(int element, int newKey);
    int getKey(int element);
    void deleteNode(int node);
    boolean contains(int node);
    void clear();
}

