package com.mdevv.tpo3.client.net;

import com.mdevv.tpo3.client.utils.TranslationStatus;
import com.mdevv.tpo3.common.protobuf.Response;
import com.mdevv.tpo3.common.protobuf.Status;
import com.mdevv.tpo3.common.protobuf.TranslationRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.function.BiConsumer;

public class DictionaryRequest {
    private final DictionaryService dictionaryService;
    private Boolean isConstructed = false;
    private String word = null;
    private String language = null;
    private BiConsumer<TranslationStatus, String> callback = null;

    public DictionaryRequest(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public DictionaryRequest setCallback(BiConsumer<TranslationStatus, String> callback) {
        if (!isConstructed) this.callback = callback;
        return this;
    }

    public DictionaryRequest setWord(String word) {
        if (!isConstructed) this.word = word;
        return this;
    }

    public DictionaryRequest setLanguage(String language) {
        if (!isConstructed) this.language = language;
        return this;
    }

    public TranslationStatus send() {
        if (word == null) {
            throw new IllegalStateException("Word is not set");
        }

        if (language == null) {
            throw new IllegalStateException("Language is not set");
        }

        isConstructed = true;

        DictionaryRequestMonitor dictionaryRequestMonitor = dictionaryService.getDictionaryRequestMonitor();

        try (Socket socket = new Socket(dictionaryService.getCoreServerAddress(), dictionaryService.getCoreServerPort())) {
            OutputStream outputStream = socket.getOutputStream();
            System.out.println("Sending a translation request to " + socket.toString() + ":");
            TranslationRequest translationRequest = TranslationRequest.newBuilder()
                    .setClientPort(dictionaryService.getServicePort())
                    .setTranslationLanguage(language)
                    .setSourceWord(word)
                    .build();

            synchronized (dictionaryRequestMonitor.monitor) {
                dictionaryRequestMonitor.setAwaitingDictionaryRequest(this);
                Status status;
                try {
                    translationRequest.writeDelimitedTo(outputStream);
                    System.out.println(translationRequest.toString());

                    InputStream inputStream = socket.getInputStream();
                    Response response = Response.parseDelimitedFrom(inputStream);
                    status = response.getStatus();
                } catch (IOException e) {
                    dictionaryRequestMonitor.setAwaitingDictionaryRequest(null);
                    throw e;
                }

                if (status == Status.ERROR) {
                    dictionaryRequestMonitor.setAwaitingDictionaryRequest(null);
                    return TranslationStatus.ERROR;
                }

                if (status == Status.NOT_SUPPORTED) {
                    dictionaryRequestMonitor.setAwaitingDictionaryRequest(null);
                    return TranslationStatus.NOT_SUPPORTED;
                }
            }

            System.out.println("Translation request successfully sent.");
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return TranslationStatus.ERROR;
        }

        return TranslationStatus.OK;
    }

    // Internal getters
    String getWord() {
        return word;
    }

    String getLanguage() {
        return language;
    }

    BiConsumer<TranslationStatus, String> getCallback() {
        return callback;
    }
}
