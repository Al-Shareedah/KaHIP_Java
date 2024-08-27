package org.alshar.lib.partition.coarsening.matching.gpa;
public class Path {

    private int head;   // Last vertex of the path. Cycles have head == tail
    private int tail;   // First vertex of the path. Cycles have head == tail
    private int length; // Number of edges in the graph
    private boolean active; // True if the path is still in use. False if it has been removed.

    // Constructor
    public Path() {
        this.head = Integer.MAX_VALUE;
        this.tail = Integer.MAX_VALUE;
        this.length = 0;
        this.active = false;
    }

    // Constructor with initialization
    public Path(int v) {
        this.head = v;
        this.tail = v;
        this.length = 0;
        this.active = true;
    }

    // Initialize path with a node
    public void init(int v) {
        this.head = v;
        this.tail = v;
        this.length = 0;
        this.active = true;
    }

    // Getters and Setters
    public int getTail() {
        return tail;
    }

    public void setTail(int v) {
        this.tail = v;
    }

    public int getHead() {
        return head;
    }

    public void setHead(int v) {
        this.head = v;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int len) {
        this.length = len;
    }

    // Check if a node is an endpoint of the path
    public boolean isEndpoint(int v) {
        return v == tail || v == head;
    }

    // Check if the path is a cycle
    public boolean isCycle() {
        return head == tail && length > 0;
    }

    // Check if the path is active
    public boolean isActive() {
        return active;
    }

    // Set the active status of the path
    public void setActive(boolean act) {
        this.active = act;
    }
}