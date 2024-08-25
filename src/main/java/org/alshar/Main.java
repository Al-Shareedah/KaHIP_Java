package org.alshar;
import org.alshar.app.BalanceConfiguration;
import org.alshar.lib.io.GraphIO;
import org.alshar.app.ParseParameters;
import org.alshar.lib.data_structure.GraphAccess;
import org.alshar.lib.partition.PartitionConfig;
import org.alshar.lib.tools.RandomFunctions;
import org.alshar.lib.tools.Timer;

import java.io.FileOutputStream;
import java.io.PrintStream;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        args = new String[]{"email.graph", "--k", "4", "--preconfiguration=eco"};
        PartitionConfig partitionConfig = new PartitionConfig();
        String[] graphFilename = new String[1];
        boolean isGraphWeighted = false;
        boolean suppressOutput = false;
        boolean recursive = false;

        int retCode = ParseParameters.parseParameters(args, partitionConfig, isGraphWeighted, suppressOutput, recursive, graphFilename);

        if (retCode != 0) {
            System.exit(0);
        }


        // Timer start (you would need a Timer class similar to C++)
        Timer t = new Timer();

        // Load graph based on partitionConfig settings
        GraphAccess G = new GraphAccess();
        if (partitionConfig.isUseMmapIo()) {
            // Implement the mmap_io logic here, if available in Java
            //MmapIo.graphFromMetisFile(G, graphFilename);
        } else {
            // Assuming GraphIo is a class that handles reading graphs
            GraphIO graphIO = new GraphIO();
            graphIO.readGraphWeighted(G, graphFilename[0]);

        }

        // Output the elapsed time
        System.out.println("IO time: " + t.elapsed() + " ms");
        G.setPartitionCount(partitionConfig.getK());

        // Instantiate and use BalanceConfiguration to configure balance
        BalanceConfiguration bc = new BalanceConfiguration();
        bc.configurateBalance(partitionConfig, G);

        // Set random seed
        RandomFunctions.setSeed(partitionConfig.getSeed());

        // Output graph details
        System.out.println("Graph has " + G.numberOfNodes() + " nodes and " + G.numberOfEdges() + " edges");

        // Restart the timer
        t.restart();
    }
}