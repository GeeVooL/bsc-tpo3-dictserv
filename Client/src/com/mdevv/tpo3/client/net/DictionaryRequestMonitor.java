package com.mdevv.tpo3.client.net;

public class DictionaryRequestMonitor {
    private DictionaryRequest dictionaryRequest = null;

    public final Object monitor = new Object();

    void setAwaitingDictionaryRequest(DictionaryRequest awaitingDictionaryRequest) {
        this.dictionaryRequest = awaitingDictionaryRequest;
    }

    DictionaryRequest getAwaitingDictionaryRequests() {
        return dictionaryRequest;
    }
}
