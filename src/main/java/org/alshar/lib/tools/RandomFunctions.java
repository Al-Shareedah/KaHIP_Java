package org.alshar.lib.tools;
import org.alshar.lib.data_structure.Pair;
import org.alshar.lib.partition.PartitionConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomFunctions {

    private static int seed;
    private static Random random = new Random(seed);

    public RandomFunctions() {
    }

    public static <Sometype> void circularPermutation(List<Sometype> vec) {
        if (vec.size() < 2) return;

        for (int i = 0; i < vec.size(); i++) {
            vec.set(i, (Sometype) Integer.valueOf(i));
        }

        int size = vec.size();
        for (int i = 0; i < size; i++) {
            int posA = nextInt(0, size - 1);
            int posB = nextInt(0, size - 1);

            while (posB == posA) {
                posB = nextInt(0, size - 1);
            }

            if (posA != (int) vec.get(posB) && posB != (int) vec.get(posA)) {
                Collections.swap(vec, posA, posB);
            }
        }
    }

    public static <Sometype> void permutateVectorFast(List<Sometype> vec, boolean init) {
        if (init) {
            for (int i = 0; i < vec.size(); i++) {
                vec.set(i, (Sometype) Integer.valueOf(i));
            }
        }

        if (vec.size() < 10) return;

        int distance = 20;
        int size = vec.size() - 4;
        for (int i = 0; i < size; i++) {
            int posA = i;
            int posB = (posA + nextInt(0, distance)) % size;
            Collections.swap(vec, posA, posB);
            Collections.swap(vec, posA + 1, posB + 1);
            Collections.swap(vec, posA + 2, posB + 2);
            Collections.swap(vec, posA + 3, posB + 3);
        }
    }

    public static void permutateVectorGood(List<Pair<Integer, Integer>> vec) {
        int size = vec.size();
        if (size < 4) return;

        for (int i = 0; i < size; i++) {
            int posA = nextInt(0, size - 4);
            int posB = nextInt(0, size - 4);
            Collections.swap(vec, posA, posB);
            Collections.swap(vec, posA + 1, posB + 1);
            Collections.swap(vec, posA + 2, posB + 2);
            Collections.swap(vec, posA + 3, posB + 3);
        }
    }

    public static <Sometype> void permutateVectorGood(List<Sometype> vec, boolean init) {
        if (init) {
            for (int i = 0; i < vec.size(); i++) {
                vec.set(i, (Sometype) Integer.valueOf(i));
            }
        }

        if (vec.size() < 10) {
            permutateVectorGoodSmall(vec);
            return;
        }

        int size = vec.size();
        ThreadLocalRandom random = ThreadLocalRandom.current();  // Ensure proper random number generation

        for (int i = 0; i < size; i++) {
            int posA = random.nextInt(0, size - 3);  // Generate random int in range [0, size-4]
            int posB = random.nextInt(0, size - 3);  // Generate random int in range [0, size-4]

            Collections.swap(vec, posA, posB);
            Collections.swap(vec, posA + 1, posB + 1);
            Collections.swap(vec, posA + 2, posB + 2);
            Collections.swap(vec, posA + 3, posB + 3);
        }
    }

    public static <Sometype> void permutateVectorGoodSmall(List<Sometype> vec) {
        if (vec.size() < 2) return;  // Return immediately if the vector has fewer than 2 elements

        int size = vec.size();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < size; i++) {
            int posA = random.nextInt(0, size);  // Generate random int in range [0, size-1]
            int posB = random.nextInt(0, size);  // Generate random int in range [0, size-1]
            Collections.swap(vec, posA, posB);   // Swap the elements at posA and posB
        }
    }

    public static <Sometype> void permutateEntries(PartitionConfig partitionConfig, List<Sometype> vec, boolean init) {
        if (init) {
            for (int i = 0; i < vec.size(); i++) {
                vec.set(i, (Sometype) Integer.valueOf(i));
            }
        }

        switch (partitionConfig.getPermutationQuality()) {
            case PERMUTATION_QUALITY_NONE:
                break;
            case PERMUTATION_QUALITY_FAST:
                permutateVectorFast(vec, false);
                break;
            case PERMUTATION_QUALITY_GOOD:
                permutateVectorGood(vec, false);
                break;
        }
    }

    public static boolean nextBool() {
        return random.nextBoolean();
    }

    public static int nextInt(int lb, int rb) {
        return lb + random.nextInt(rb - lb + 1);
    }

    public static double nextDouble(double lb, double rb) {
        return lb + (rb - lb) * random.nextDouble();
    }

    public static void setSeed(int seed) {
        RandomFunctions.seed = seed;
        random = new Random(seed);
    }
}

