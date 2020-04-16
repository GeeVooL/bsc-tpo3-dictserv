package com.mdevv.tpo3.client.net;

import com.mdevv.tpo3.common.Configuration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DictionaryService {
    final static String LISTENING_PORT = "LISTENING_PORT";
    final static String CORE_ADDRESS = "CORE_ADDRESS";
    final static String CORE_PORT = "CORE_PORT";

    private final DictionaryRequestMonitor dictionaryRequestMonitor = new DictionaryRequestMonitor();
    private final InetAddress coreServerAddress;
    private final int coreServerPort;
    private final ServerSocket serverSocket;

    public DictionaryService(String configurationPath) throws IOException {
        Configuration configuration = new Configuration(
                configurationPath,
                Arrays.asList(
                        LISTENING_PORT,
                        CORE_ADDRESS,
                        CORE_PORT
                )
        );

        Integer listeningPort = configuration.getAsInt(LISTENING_PORT);
        serverSocket = new ServerSocket(listeningPort);
        System.out.println("Translation service listening port is set to " + listeningPort);

        // Get the core server's requests socket data
        coreServerAddress = InetAddress.getByName(configuration.get(CORE_ADDRESS));
        coreServerPort = configuration.getAsInt(CORE_PORT);
        System.out.println("Core server address is set to " + coreServerAddress.toString() + ":" + coreServerPort);
    }

    public DictionaryRequest createDictionaryRequest() {
        return new DictionaryRequest(this);
    }

    public void startListening() {
        Thread thread = new Thread(() -> {
            try {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                while (true) {
                    executor.submit(new DictionaryResponseHandler(serverSocket.accept(), this));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    // Internal getters
    DictionaryRequestMonitor getDictionaryRequestMonitor() {
        return dictionaryRequestMonitor;
    }

    InetAddress getCoreServerAddress() {
        return coreServerAddress;
    }

    int getCoreServerPort() {
        return coreServerPort;
    }

    int getServicePort() {
        return serverSocket.getLocalPort();
    }
}
