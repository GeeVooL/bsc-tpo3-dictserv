package com.mdevv.tpo3.client.utils;

import com.mdevv.tpo3.common.protobuf.Status;

public class TranslationStatusUtils {
    public static TranslationStatus fromProtobuf(Status protobufStatus) {
        switch (protobufStatus) {
            case OK:
                return TranslationStatus.OK;
            case ERROR:
                return TranslationStatus.ERROR;
            case NOT_SUPPORTED:
                return TranslationStatus.NOT_SUPPORTED;
            case NOT_FOUND:
                return TranslationStatus.NOT_FOUND;
            default:
                return TranslationStatus.ERROR;
        }
    }
}
