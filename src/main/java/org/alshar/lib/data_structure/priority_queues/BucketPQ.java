package org.alshar.lib.data_structure.priority_queues;
import java.util.*;

public class BucketPQ implements PriorityQueueInterface {

    private int elements;
    private int gainSpan;
    private int maxIdx;
    private Map<Integer, Pair<Integer, Integer>> queueIndex; // Pair<position in bucket, gain>
    private List<List<Integer>> buckets;

    public BucketPQ(int gainSpanInput) {
        this.elements = 0;
        this.gainSpan = gainSpanInput;
        this.maxIdx = 0;
        this.queueIndex = new HashMap<>();
        this.buckets = new ArrayList<>(2 * gainSpan + 1);

        for (int i = 0; i < 2 * gainSpan + 1; i++) {
            buckets.add(new ArrayList<>());
        }
    }

    @Override
    public int size() {
        return elements;
    }

    @Override
    public boolean isEmpty() {
        return elements == 0;
    }

    @Override
    public void insert(int node, int gain) {
        int address = gain + gainSpan;
        if (address > maxIdx) {
            maxIdx = address;
        }

        buckets.get(address).add(node);
        queueIndex.put(node, new Pair<>(buckets.get(address).size() - 1, gain));

        elements++;
    }

    @Override
    public int maxValue() {
        return maxIdx - gainSpan;
    }

    @Override
    public int maxElement() {
        return buckets.get(maxIdx).get(buckets.get(maxIdx).size() - 1);
    }

    @Override
    public int deleteMax() {
        int node = buckets.get(maxIdx).remove(buckets.get(maxIdx).size() - 1);
        queueIndex.remove(node);

        if (buckets.get(maxIdx).isEmpty()) {
            while (maxIdx != 0) {
                maxIdx--;
                if (!buckets.get(maxIdx).isEmpty()) {
                    break;
                }
            }
        }

        elements--;
        return node;
    }

    @Override
    public void decreaseKey(int node, int newGain) {
        changeKey(node, newGain);
    }

    @Override
    public void increaseKey(int node, int newGain) {
        changeKey(node, newGain);
    }

    @Override
    public void changeKey(int node, int newKey) {
        deleteNode(node);
        insert(node, newKey);
    }

    @Override
    public void clear() {
        elements = 0;
        maxIdx = 0;
        queueIndex.clear();
        for (List<Integer> bucket : buckets) {
            bucket.clear();
        }
    }

    @Override
    public int getKey(int node) {
        return queueIndex.get(node).getSecond();
    }

    @Override
    public void deleteNode(int node) {
        Pair<Integer, Integer> nodeInfo = queueIndex.get(node);
        int inBucketIdx = nodeInfo.getFirst();
        int oldGain = nodeInfo.getSecond();
        int address = oldGain + gainSpan;

        List<Integer> bucket = buckets.get(address);

        if (bucket.size() > 1) {
            int lastNode = bucket.get(bucket.size() - 1);
            queueIndex.put(lastNode, new Pair<>(inBucketIdx, queueIndex.get(lastNode).getSecond()));
            Collections.swap(bucket, inBucketIdx, bucket.size() - 1);
            bucket.remove(bucket.size() - 1);
        } else {
            bucket.remove(0);
            if (address == maxIdx) {
                while (maxIdx != 0) {
                    maxIdx--;
                    if (!buckets.get(maxIdx).isEmpty()) {
                        break;
                    }
                }
            }
        }

        elements--;
        queueIndex.remove(node);
    }

    @Override
    public boolean contains(int node) {
        return queueIndex.containsKey(node);
    }

    // Simple Pair class to hold two values
    private static class Pair<A, B> {
        private A first;
        private B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }
    }
}
