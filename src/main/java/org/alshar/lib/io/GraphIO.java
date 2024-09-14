package org.alshar.lib.io;
import org.alshar.lib.data_structure.GraphAccess;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
public class GraphIO {
    public GraphIO() {
    }
    public int readGraphWeighted(GraphAccess G, String filename) {
        try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
            String line;

            // Skip comments
            while ((line = in.readLine()) != null && line.startsWith("%")) {
                // Do nothing, just skip the comment lines
            }

            if (line == null) {
                System.err.println("Error reading file: " + filename);
                return 1;
            }

            String[] parts = line.split("\\s+");
            long nmbNodes = Long.parseLong(parts[0]);
            long nmbEdges = Long.parseLong(parts[1]);
            int ew = (parts.length > 2) ? Integer.parseInt(parts[2]) : 0;

            if (2 * nmbEdges > Integer.MAX_VALUE || nmbNodes > Integer.MAX_VALUE) {
                System.err.println("The graph is too large. Currently only 32bit supported!");
                System.exit(0);
            }

            boolean readEw = false;
            boolean readNw = false;

            if (ew == 1) {
                readEw = true;
            } else if (ew == 11) {
                readEw = true;
                readNw = true;
            } else if (ew == 10) {
                readNw = true;
            }

            nmbEdges *= 2; // since we have forward and backward edges

            long nodeCounter = 0;
            long edgeCounter = 0;
            long totalNodeWeight = 0;

            G.startConstruction((int) nmbNodes, (int) nmbEdges);

            while ((line = in.readLine()) != null) {
                if (line.startsWith("%")) { // a comment in the file
                    continue;
                }

                int node = G.newNode();
                nodeCounter++;
                G.setPartitionIndex(node, 0);

                String[] tokens = line.split("\\s+");
                int index = 0;

                int weight = 1;
                if (readNw) {
                    weight = Integer.parseInt(tokens[index++]);
                    totalNodeWeight += weight;
                    if (totalNodeWeight > Integer.MAX_VALUE) {
                        System.err.println("The sum of the node weights is too large (it exceeds the node weight type).");
                        System.err.println("Currently not supported. Please scale your node weights.");
                        System.exit(0);
                    }
                }
                G.setNodeWeight(node, weight);

                while (index < tokens.length) {
                    // Skip empty tokens
                    if (tokens[index].isEmpty()) {
                        index++;
                        continue;
                    }
                    int target = Integer.parseInt(tokens[index++]);

                    // Check for self-loops
                    if (target - 1 == node) {
                        System.err.println("The graph file contains self-loops. This is not supported. Please remove them from the file.");
                    }

                    int edgeWeight = 1;
                    if (readEw && index < tokens.length) {
                        edgeWeight = Integer.parseInt(tokens[index++]);
                    }
                    edgeCounter++;
                    int e = G.newEdge(node, target - 1);

                    G.setEdgeWeight(e, edgeWeight);
                }
            }

            if (edgeCounter != nmbEdges) {
                System.err.println("number of specified edges mismatch");
                System.err.println(edgeCounter + " " + nmbEdges);
                System.exit(0);
            }

            if (nodeCounter != nmbNodes) {
                System.err.println("number of specified nodes mismatch");
                System.err.println(nodeCounter + " " + nmbNodes);
                System.exit(0);
            }

            G.finishConstruction();
            return 0;

        } catch (IOException e) {
            System.err.println("Error opening " + filename);
            return 1;
        }
    }

    public int writeGraphWeighted(GraphAccess G, String filename) {
        try (PrintWriter f = new PrintWriter(new FileWriter(filename))) {
            f.println(G.numberOfNodes() + " " + G.numberOfEdges() / 2 + " 11");

            for (int node = 0; node < G.numberOfNodes(); node++) {
                f.print(G.getNodeWeight(node));
                for (int e : G.getOutEdges(node)) {
                    f.print(" " + (G.getEdgeTarget(e) + 1) + " " + G.getEdgeWeight(e));
                }
                f.println();
            }

            return 0;
        } catch (IOException e) {
            System.err.println("Error writing file: " + filename);
            return 1;
        }
    }

    public int writeGraph(GraphAccess G, String filename) {
        try (PrintWriter f = new PrintWriter(new FileWriter(filename))) {
            f.println(G.numberOfNodes() + " " + G.numberOfEdges() / 2);

            for (int node = 0; node < G.numberOfNodes(); node++) {
                for (int e : G.getOutEdges(node)) {
                    f.print((G.getEdgeTarget(e) + 1) + " ");
                }
                f.println();
            }

            return 0;
        } catch (IOException e) {
            System.err.println("Error writing file: " + filename);
            return 1;
        }
    }

    public int readPartition(GraphAccess G, String filename) {
        try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
            String line;
            int max = 0;

            for (int node = 0; node < G.numberOfNodes(); node++) {
                line = in.readLine();
                if (line == null || line.startsWith("%")) {
                    node--;
                    continue;
                }

                int partitionIndex = Integer.parseInt(line.trim());
                G.setPartitionIndex(node, partitionIndex);

                if (partitionIndex > max) {
                    max = partitionIndex;
                }
            }

            G.setPartitionCount(max + 1);
            return 0;

        } catch (IOException e) {
            System.err.println("Error opening file: " + filename);
            return 1;
        }
    }

    public void writePartition(GraphAccess G, String filename) {
        try (PrintWriter f = new PrintWriter(new FileWriter(filename))) {
            System.out.println("writing partition to " + filename + " ... ");

            for (int node = 0; node < G.numberOfNodes(); node++) {
                f.println(G.getPartitionIndex(node));
            }

        } catch (IOException e) {
            System.err.println("Error writing file: " + filename);
        }
    }

    public <T> void writeVector(List<T> vec, String filename) {
        try (PrintWriter f = new PrintWriter(new FileWriter(filename))) {
            for (T value : vec) {
                f.println(value);
            }
        } catch (IOException e) {
            System.err.println("Error writing vector to file: " + filename);
        }
    }

    public <T> void readVector(List<T> vec, String filename, Class<T> clazz) {
        try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("%")) {
                    continue;
                }

                T value = clazz.getConstructor(String.class).newInstance(line.trim());
                vec.add(value);
            }
        } catch (Exception e) {
            System.err.println("Error reading vector from file: " + filename);
        }
    }
}

