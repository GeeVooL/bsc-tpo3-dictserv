package com.mdevv.tpo3.core.net;

import com.mdevv.tpo3.common.net.Handler;
import com.mdevv.tpo3.common.protobuf.DictionaryServerRequest;
import com.mdevv.tpo3.core.components.DictionaryServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class DelegationHandler extends Handler {
    private final DictionaryServer dictionaryServer;
    private final String word;
    private final InetAddress clientAddress;
    private final int clientPort;

    public DelegationHandler(DictionaryServer dictionaryServer, String word, InetAddress clientAddress, int clientPort) {
        this.dictionaryServer = dictionaryServer;
        this.word = word;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
    }

    @Override
    public void handle() {
        try (Socket dictSocket = new Socket(dictionaryServer.getAddress(), dictionaryServer.getPort())) {
            System.out.println("Delegating translation to dictionary server " +
                    dictSocket.toString() +
                    " (" +
                    dictionaryServer.getLanguage() +
                    "):"
            );
            DictionaryServerRequest request = DictionaryServerRequest.newBuilder()
                    .setClientAddress(clientAddress.getHostAddress())
                    .setClientPort(clientPort)
                    .setSourceWord(word)
                    .build();
            System.out.println(request.toString());
            try (OutputStream dictOutputString = dictSocket.getOutputStream()) {
                request.writeDelimitedTo(dictOutputString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
