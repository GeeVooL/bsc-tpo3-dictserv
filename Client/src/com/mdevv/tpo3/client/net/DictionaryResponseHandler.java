package com.mdevv.tpo3.client.net;

import com.mdevv.tpo3.client.utils.TranslationStatus;
import com.mdevv.tpo3.client.utils.TranslationStatusUtils;
import com.mdevv.tpo3.common.net.Handler;
import com.mdevv.tpo3.common.protobuf.TranslationResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Queue;
import java.util.function.BiConsumer;

public class DictionaryResponseHandler extends Handler {
    private final Socket socket;
    private final DictionaryService dictionaryService;

    public DictionaryResponseHandler(Socket socket, DictionaryService dictionaryService) {
        this.socket = socket;
        this.dictionaryService = dictionaryService;
    }

    @Override
    public void handle() {
        try {
            // Get response from the server
            TranslationResponse translationResponse;
            try (InputStream inputStream = socket.getInputStream()) {
                System.out.println("Incoming request from the dictionary server:");
                translationResponse = TranslationResponse.parseDelimitedFrom(inputStream);
                System.out.println(translationResponse.toString());
            }

            DictionaryRequestMonitor dictionaryRequestMonitor = dictionaryService.getDictionaryRequestMonitor();
            synchronized (dictionaryRequestMonitor.monitor) {
                DictionaryRequest awaitingDictionaryRequest = dictionaryRequestMonitor.getAwaitingDictionaryRequests();
                if (awaitingDictionaryRequest == null
                        || !awaitingDictionaryRequest.getLanguage().equals(translationResponse.getTranslationLanguage())
                        || !awaitingDictionaryRequest.getWord().equals(translationResponse.getSourceWord())) {
                    System.err.println("Missing request for response: " + translationResponse.toString());
                    return;
                }

                // Send result to the caller
                BiConsumer<TranslationStatus, String> callback = awaitingDictionaryRequest.getCallback();
                if (callback != null) {
                    callback.accept(
                            TranslationStatusUtils.fromProtobuf(translationResponse.getStatus()),
                            translationResponse.getTranslatedWord()
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
