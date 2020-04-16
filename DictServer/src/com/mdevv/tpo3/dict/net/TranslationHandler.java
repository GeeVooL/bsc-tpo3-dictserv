package com.mdevv.tpo3.dict.net;

import com.mdevv.tpo3.common.net.Handler;
import com.mdevv.tpo3.common.protobuf.DictionaryServerRequest;
import com.mdevv.tpo3.common.protobuf.Status;
import com.mdevv.tpo3.common.protobuf.TranslationResponse;
import com.mdevv.tpo3.dict.components.Translator;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

public class TranslationHandler extends Handler {
    private final Socket requestingSocket;
    private final Translator translator;

    public TranslationHandler(Socket requestingSocket, Translator translator) {
        this.requestingSocket = requestingSocket;
        this.translator = translator;
    }

    @Override
    public void handle() {
        try {
            // Get message from the server
            DictionaryServerRequest dictionaryServerRequest;
            try (InputStream requestingInputStream = requestingSocket.getInputStream()) {
                System.out.println("Incoming request from the core server:");
                dictionaryServerRequest = DictionaryServerRequest.parseDelimitedFrom(requestingInputStream);
                System.out.println(dictionaryServerRequest.toString());
            }

            // Translate
            Status status = Status.OK;
            String sourceWord = dictionaryServerRequest.getSourceWord();
            String translatedWord = translator.translate(sourceWord);

            if (translatedWord == null) {
                status = Status.NOT_FOUND;
                translatedWord = "";
            }

            System.out.println("Translated word: " + translatedWord);

            // Send response to the client
            try (Socket clientSocket = new Socket(
                    InetAddress.getByName(dictionaryServerRequest.getClientAddress()),
                    dictionaryServerRequest.getClientPort())
            ) {
                System.out.println("Sending a response to the client " + clientSocket.toString() + ":");
                TranslationResponse translationResponse = TranslationResponse.newBuilder()
                        .setStatus(status)
                        .setSourceWord(sourceWord)
                        .setTranslatedWord(translatedWord)
                        .setTranslationLanguage(translator.getLanguage())
                        .build();
                translationResponse.writeDelimitedTo(clientSocket.getOutputStream());
                System.out.println(translationResponse.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
