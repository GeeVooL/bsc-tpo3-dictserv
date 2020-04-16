package com.mdevv.tpo3.core.net;

import com.mdevv.tpo3.common.net.Handler;
import com.mdevv.tpo3.common.protobuf.RegistrationRequest;
import com.mdevv.tpo3.common.protobuf.Response;
import com.mdevv.tpo3.common.protobuf.Status;
import com.mdevv.tpo3.core.components.DictionaryServer;
import com.mdevv.tpo3.core.components.DictionaryServersListing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RegistrationHandler extends Handler {
    private final Socket dictSocket;
    private final DictionaryServersListing dictionaryServersListing;

    public RegistrationHandler(Socket dictSocket, DictionaryServersListing dictionaryServersListing) {
        this.dictSocket = dictSocket;
        this.dictionaryServersListing = dictionaryServersListing;
    }

    @Override
    public void handle() {
        DictionaryServer dictionaryServer = null;

        try {
            InputStream dictInputStream = dictSocket.getInputStream();
            System.out.println("Incoming registration request from " + dictSocket.toString() + ":");
            RegistrationRequest registrationRequest = RegistrationRequest.parseDelimitedFrom(dictInputStream);
            System.out.println(registrationRequest.toString());

            dictionaryServer = new DictionaryServer(
                    registrationRequest.getLanguage(),
                    dictSocket.getInetAddress(),
                    registrationRequest.getPort()
            );

            switch (registrationRequest.getType()) {
                case REGISTER:
                    dictionaryServersListing.add(dictionaryServer);
                    break;
                case UNREGISTER:
                    dictionaryServersListing.remove(dictionaryServer);
                    break;
            }

            try (OutputStream dictOutputStream = dictSocket.getOutputStream()) {
                Response.newBuilder().setStatus(Status.OK).build().writeDelimitedTo(dictOutputStream);
            }
            System.out.println("Successful registration call.");
        } catch (IOException e) {
            if (dictionaryServer != null) {
                dictionaryServersListing.remove(dictionaryServer);
            }

            try (OutputStream dictOutputStream = dictSocket.getOutputStream()) {
                Response.newBuilder().setStatus(Status.ERROR).build().writeDelimitedTo(dictOutputStream);
                System.out.println("Registration call failed.");
            } catch (IOException ne) {
                ne.printStackTrace();
            }

            e.printStackTrace();
        } finally {
            try {
                dictSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
