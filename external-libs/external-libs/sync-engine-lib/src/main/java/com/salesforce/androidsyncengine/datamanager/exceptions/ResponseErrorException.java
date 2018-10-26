package com.salesforce.androidsyncengine.datamanager.exceptions;


public  class ResponseErrorException extends Exception {


    public ResponseErrorException(String detailMessage) {
        super(detailMessage);
    }

    public ResponseErrorException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ResponseErrorException(Throwable throwable) {
        super(throwable);
    }
}
