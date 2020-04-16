package com.mdevv.tpo3.core;

import com.mdevv.tpo3.common.Configuration;
import com.mdevv.tpo3.core.components.DictionaryServersListing;
import com.mdevv.tpo3.core.net.ClientRequestHandler;
import com.mdevv.tpo3.core.net.RegistrationHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoreServer {
    final static String LISTENING_PORT = "LISTENING_PORT";
    final static String CONFIGURATION_PORT = "CONFIGURATION_PORT";

    private DictionaryServersListing dictionaryServersListing = new DictionaryServersListing();
    private ServerSocket requestsSocket;
    private ServerSocket configurationSocket;

    public static void main(String[] args) throws IOException {
        String configurationPath = "";

        if (args.length > 0) {
            configurationPath = args[0];
        } else {
            System.err.println("This program requires one parameter - a path to configuration file.");
            System.exit(1);
        }

        Configuration configuration = new Configuration(
                configurationPath,
                Arrays.asList(
                        LISTENING_PORT,
                        CONFIGURATION_PORT
                )
        );

        CoreServer coreServer = new CoreServer(configuration);
        coreServer.listen();
    }

    public CoreServer(Configuration configuration) {
        try {
            Integer listeningPort = configuration.getAsInt(LISTENING_PORT);
            requestsSocket = new ServerSocket(listeningPort);
            System.out.println("Core server listening port is set to " + listeningPort);

            Integer configurationPort = configuration.getAsInt(CONFIGURATION_PORT);
            configurationSocket = new ServerSocket(configurationPort);
            System.out.println("Core server configuration port is set to " + configurationPort);
        } catch (RuntimeException | IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    public void listen() {
        Thread requestsThread = new Thread(() -> {
            try {
                ExecutorService executor = Executors.newFixedThreadPool(5);
                while (true) {
                    executor.submit(new ClientRequestHandler(requestsSocket.accept(), dictionaryServersListing));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Thread configurationThread = new Thread(() -> {
            try {
                ExecutorService executor = Executors.newFixedThreadPool(2);
                while (true) {
                    executor.submit(new RegistrationHandler(configurationSocket.accept(), dictionaryServersListing));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        requestsThread.start();
        configurationThread.start();
    }
}
