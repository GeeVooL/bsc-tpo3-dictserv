package com.mdevv.tpo3.core.net;

import com.mdevv.tpo3.common.net.Handler;
import com.mdevv.tpo3.common.protobuf.Response;
import com.mdevv.tpo3.common.protobuf.Status;
import com.mdevv.tpo3.common.protobuf.TranslationRequest;
import com.mdevv.tpo3.core.components.DictionaryServer;
import com.mdevv.tpo3.core.components.DictionaryServersListing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientRequestHandler extends Handler {
    private final Socket clientSocket;
    private final DictionaryServersListing dictionaryServersListing;

    public ClientRequestHandler(Socket clientSocket, DictionaryServersListing dictionaryServersListing) {
        this.clientSocket = clientSocket;
        this.dictionaryServersListing = dictionaryServersListing;
    }

    @Override
    public void handle() {
        try {
            InputStream clientInputStream = clientSocket.getInputStream();
            System.out.println("Incoming client request.");
            TranslationRequest translationRequest = TranslationRequest.parseDelimitedFrom(clientInputStream);
            System.out.println(translationRequest.toString());

            String language = translationRequest.getTranslationLanguage();
            DictionaryServer dictionaryServer = dictionaryServersListing.get(language);

            Response.Builder responseBuilder = Response.newBuilder();

            // If the dictionary server entry exists, delegate the request to the appropriate server.
            if (dictionaryServer != null) {
                responseBuilder.setStatus(Status.OK);
                Thread delegationThread = new Thread(
                        new DelegationHandler(
                                dictionaryServer,
                                translationRequest.getSourceWord(),
                                clientSocket.getInetAddress(),
                                translationRequest.getClientPort()
                        ));
                delegationThread.start();
            } else {
                System.out.println("Dictionary server for " + language + " not found.");
                responseBuilder.setStatus(Status.NOT_SUPPORTED);
            }

            try (OutputStream clientOutputStream = clientSocket.getOutputStream()) {
                responseBuilder.build().writeDelimitedTo(clientOutputStream);
            }
        } catch (IOException | RuntimeException e) {
            try (OutputStream clientOutputStream = clientSocket.getOutputStream()) {
                Response.newBuilder().setStatus(Status.ERROR).build().writeDelimitedTo(clientOutputStream);
            } catch (IOException ne) {
                ne.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
