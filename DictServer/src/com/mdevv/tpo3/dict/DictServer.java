package com.mdevv.tpo3.dict;

import com.mdevv.tpo3.common.Configuration;
import com.mdevv.tpo3.common.protobuf.RegistrationRequest;
import com.mdevv.tpo3.common.protobuf.Response;
import com.mdevv.tpo3.common.protobuf.Status;
import com.mdevv.tpo3.dict.components.Translator;
import com.mdevv.tpo3.dict.net.TranslationHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DictServer {
    final static String LISTENING_PORT = "LISTENING_PORT";
    final static String LANGUAGE = "LANGUAGE";
    final static String DICT_PATH = "DICT_PATH";
    final static String CORE_ADDRESS = "CORE_ADDRESS";
    final static String CORE_PORT = "CORE_PORT";

    private ServerSocket serverSocket;
    private String language;
    private Translator translator;
    private InetAddress coreAddress;
    private int corePort;
    private Boolean isRegistered = false;

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
                        LANGUAGE,
                        DICT_PATH,
                        CORE_ADDRESS,
                        CORE_PORT
                )
        );

        DictServer dictServer = new DictServer(configuration);
        dictServer.sendRegistrationRequest(RegistrationRequest.RequestType.REGISTER);
        dictServer.listen();
    }

    public DictServer(Configuration configuration) {
        try {
            // Create server socket
            Integer listeningPort = configuration.getAsInt(LISTENING_PORT);
            serverSocket = new ServerSocket(listeningPort);
            System.out.println("Dictionary server listening port is set to " + listeningPort);
            registerShutdownHook();

            // Create translator
            language = configuration.get(LANGUAGE);
            System.out.println("Dictionary server language is set to " + language);
            Path dictPath = Paths.get(configuration.get(DICT_PATH));
            System.out.println("Dictionary location is set to " + dictPath.toString());
            translator = new Translator(dictPath, language);

            // Get core server's configuration socket data
            coreAddress = InetAddress.getByName(configuration.get(CORE_ADDRESS));
            corePort = configuration.getAsInt(CORE_PORT);
            System.out.println("Core address is set to " + coreAddress.toString() + ":" + corePort);
        } catch (RuntimeException | IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down the server...");
            try {
                if (isRegistered) {
                    sendRegistrationRequest(RegistrationRequest.RequestType.UNREGISTER);
                }
                serverSocket.close();
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
        }));
    }

    public void sendRegistrationRequest(RegistrationRequest.RequestType type) throws IOException {
        try (Socket socket = new Socket(coreAddress, corePort);
             OutputStream outputStream = socket.getOutputStream()) {
            System.out.println("Sending a registration request to " + socket.toString() + ":");
            RegistrationRequest registrationRequest = RegistrationRequest.newBuilder()
                    .setType(type)
                    .setLanguage(language)
                    .setPort(serverSocket.getLocalPort())
                    .build();
            registrationRequest.writeDelimitedTo(outputStream);
            System.out.println(registrationRequest.toString());

            try (InputStream inputStream = socket.getInputStream()) {
                Response response = Response.parseDelimitedFrom(inputStream);
                if (response.getStatus() != Status.OK) {
                    throw new IllegalStateException("Registration call failed.");
                }
            }

            System.out.println("Successful registration call.");
        }

        if (type == RegistrationRequest.RequestType.REGISTER) isRegistered = true;
    }

    public void listen() {
        try {
            ExecutorService executor = Executors.newFixedThreadPool(5);
            while (true) {
                executor.submit(new TranslationHandler(serverSocket.accept(), translator));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
