package com.salesforce.androidsyncengine.syncmanifest.processors;

/**
 * Created by Jakub Stefanowski on 28.03.2017.
 */

public class ManifestProcessingException extends Exception {

    public ManifestProcessingException() {
    }

    public ManifestProcessingException(String detailMessage) {
        super(detailMessage);
    }

    public ManifestProcessingException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ManifestProcessingException(Throwable throwable) {
        super(throwable);
    }
}
