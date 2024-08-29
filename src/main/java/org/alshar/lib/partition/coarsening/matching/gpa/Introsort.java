package org.alshar.lib.partition.coarsening.matching.gpa;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Introsort {

    private static final int INSERTION_SORT_THRESHOLD = 16;

    public static <T> void introsort(List<T> list, Comparator<? super T> comp) {
        introsort(list, 0, list.size(), 2 * floorLog(list.size()), comp);
    }

    private static <T> void introsort(List<T> list, int start, int end, int maxDepth, Comparator<? super T> comp) {
        if (end - start <= INSERTION_SORT_THRESHOLD) {
            insertionSort(list, start, end, comp);
            return;
        }
        if (maxDepth == 0) {
            heapsort(list, start, end, comp);
            return;
        }

        int pivot = partition(list, start, end, comp);
        introsort(list, start, pivot, maxDepth - 1, comp);
        introsort(list, pivot + 1, end, maxDepth - 1, comp);
    }

    private static <T> void insertionSort(List<T> list, int start, int end, Comparator<? super T> comp) {
        for (int i = start + 1; i < end; i++) {
            T key = list.get(i);
            int j = i - 1;
            while (j >= start && comp.compare(list.get(j), key) > 0) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }

    private static <T> void heapsort(List<T> list, int start, int end, Comparator<? super T> comp) {
        int n = end - start;
        for (int i = n / 2 - 1; i >= start; i--) {
            heapify(list, n, i, comp, start);
        }
        for (int i = end - 1; i > start; i--) {
            Collections.swap(list, start, i);
            heapify(list, i - start, start, comp, start);
        }
    }

    private static <T> void heapify(List<T> list, int n, int i, Comparator<? super T> comp, int offset) {
        int largest = i;
        int left = 2 * (i - offset) + 1 + offset;
        int right = 2 * (i - offset) + 2 + offset;

        if (left < n + offset && comp.compare(list.get(left), list.get(largest)) > 0) {
            largest = left;
        }
        if (right < n + offset && comp.compare(list.get(right), list.get(largest)) > 0) {
            largest = right;
        }

        if (largest != i) {
            Collections.swap(list, i, largest);
            heapify(list, n, largest, comp, offset);
        }
    }

    private static <T> int partition(List<T> list, int start, int end, Comparator<? super T> comp) {
        T pivot = list.get(start);
        int i = start - 1;
        int j = end;

        while (true) {
            do {
                i++;
            } while (comp.compare(list.get(i), pivot) < 0);
            do {
                j--;
            } while (comp.compare(list.get(j), pivot) > 0);
            if (i >= j) {
                return j;
            }
            Collections.swap(list, i, j);
        }
    }

    private static int floorLog(int a) {
        return (int) (Math.log(a) / Math.log(2));
    }
}
