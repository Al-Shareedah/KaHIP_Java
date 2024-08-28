package org.alshar.lib.data_structure.priority_queues;
import java.util.*;

public class MaxNodeHeap implements PriorityQueueInterface {

    public static class Data {
        int node;

        public Data(int node) {
            this.node = node;
        }
    }

    public static class PQElement {
        private Data data;
        private int key;
        private int index;

        public PQElement(Data data, int key, int index) {
            this.data = data;
            this.key = key;
            this.index = index;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        public int getKey() {
            return key;
        }

        public void setKey(int key) {
            this.key = key;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    private final List<PQElement> elements = new ArrayList<>();
    private final Map<Integer, Integer> elementIndex = new HashMap<>();
    public final List<Map.Entry<Integer, Integer>> heap = new ArrayList<>();

    @Override
    public int size() {
        return heap.size();
    }

    @Override
    public boolean isEmpty() {
        return heap.isEmpty();
    }
    public void clear() {
        elements.clear();
        elementIndex.clear();
        heap.clear();
    }

    @Override
    public void insert(int node, int gain) {
        if (!elementIndex.containsKey(node)) {
            int elementIndexSize = elements.size();
            int heapSize = heap.size();

            PQElement element = new PQElement(new Data(node), gain, heapSize);
            elements.add(element);
            heap.add(new AbstractMap.SimpleEntry<>(gain, elementIndexSize));
            elementIndex.put(node, elementIndexSize);

            siftUp(heapSize);
        }
    }

    @Override
    public int deleteMax() {
        if (heap.size() > 0) {
            // Retrieve the index of the element in the elements array
            int elementIdx = heap.get(0).getValue();
            int node = elements.get(elementIdx).getData().node;

            // Remove the node from the elementIndex map
            elementIndex.remove(node);

            // Move the last element in the heap to the root and adjust the heap
            heap.set(0, heap.get(heap.size() - 1));
            elements.get(heap.get(0).getValue()).setIndex(0);

            // If the deleted element is not the last one in elements, we need to adjust the elements array
            if (elementIdx != elements.size() - 1) {
                elements.set(elementIdx, elements.get(elements.size() - 1));
                heap.get(elements.get(elementIdx).getIndex()).setValue(elementIdx);
                int cNode = elements.get(elementIdx).getData().node;
                elementIndex.put(cNode, elementIdx);
            }

            // Remove the last element from the lists
            elements.remove(elements.size() - 1);
            heap.remove(heap.size() - 1);

            // Restore the heap property
            if (heap.size() > 1) {
                siftDown(0);
            }

            return node;
        }
        return -1;
    }


    @Override
    public void deleteNode(int node) {
        // Retrieve the index of the element in the elements list
        int elementIdx = elementIndex.get(node);
        int heapIdx = elements.get(elementIdx).getIndex();

        // Remove the node from the elementIndex map
        elementIndex.remove(node);

        // Replace the removed element with the last element in the heap
        heap.set(heapIdx, heap.get(heap.size() - 1));
        elements.get(heap.get(heapIdx).getValue()).setIndex(heapIdx);

        // If the deleted element is not the last one in elements, update the elements and heap
        if (elementIdx != elements.size() - 1) {
            elements.set(elementIdx, elements.get(elements.size() - 1));
            heap.get(elements.get(elementIdx).getIndex()).setValue(elementIdx);
            int cNode = elements.get(elementIdx).getData().node;
            elementIndex.put(cNode, elementIdx);
        }

        // Remove the last element from the elements list and the heap
        elements.remove(elements.size() - 1);
        heap.remove(heap.size() - 1);

        // Restore the heap property if necessary
        if (heap.size() > 1 && heapIdx < heap.size()) {
            siftDown(heapIdx);
            siftUp(heapIdx);
        }
    }


    @Override
    public int maxValue() {
        return heap.get(0).getKey();
    }

    @Override
    public int maxElement() {
        return elements.get(heap.get(0).getValue()).getData().node;
    }

    @Override
    public void decreaseKey(int node, int newGain) {
        int queueIdx = elementIndex.get(node);
        int heapIdx = elements.get(queueIdx).getIndex();
        elements.get(queueIdx).setKey(newGain);
        heap.get(heapIdx).setValue(newGain);
        siftDown(heapIdx);
    }

    @Override
    public void increaseKey(int node, int newKey) {
        int queueIdx = elementIndex.get(node);
        int heapIdx = elements.get(queueIdx).getIndex();
        elements.get(queueIdx).setKey(newKey);
        heap.get(heapIdx).setValue(newKey);
        siftUp(heapIdx);
    }

    @Override
    public void changeKey(int node, int newKey) {
        int oldGain = heap.get(elements.get(elementIndex.get(node)).getIndex()).getKey();
        if (oldGain > newKey) {
            decreaseKey(node, newKey);
        } else if (oldGain < newKey) {
            increaseKey(node, newKey);
        }
    }

    @Override
    public int getKey(int node) {
        return heap.get(elements.get(elementIndex.get(node)).getIndex()).getKey();
    }

    @Override
    public boolean contains(int node) {
        return elementIndex.containsKey(node);
    }

    private void siftUp(int pos) {
        if (pos > 0) {
            int parentPos = (pos - 1) / 2;
            if (heap.get(parentPos).getKey() < heap.get(pos).getKey()) {
                swapElements(parentPos, pos);
                siftUp(parentPos);
            }
        }
    }

    private void siftDown(int pos) {
        int curKey = heap.get(pos).getKey();
        int lhsChild = 2 * pos + 1;
        int rhsChild = 2 * pos + 2;
        if (rhsChild < heap.size()) {
            int lhsKey = heap.get(lhsChild).getKey();
            int rhsKey = heap.get(rhsChild).getKey();

            if (lhsKey < curKey && rhsKey < curKey) {
                return;
            } else {
                int swapPos = lhsKey > rhsKey ? lhsChild : rhsChild;
                swapElements(pos, swapPos);
                siftDown(swapPos);
            }
        } else if (lhsChild < heap.size()) {
            if (heap.get(pos).getKey() < heap.get(lhsChild).getKey()) {
                swapElements(pos, lhsChild);
                siftDown(lhsChild);
            }
        }
    }

    private void swapElements(int pos1, int pos2) {
        Collections.swap(heap, pos1, pos2);
        int elementPos1 = heap.get(pos1).getValue();
        int elementPos2 = heap.get(pos2).getValue();
        elements.get(elementPos1).setIndex(pos1);
        elements.get(elementPos2).setIndex(pos2);
    }
}

