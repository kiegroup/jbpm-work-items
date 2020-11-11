package org.jbpm.contrib.restservice;

public class ResponseProcessingException extends Exception {

    public ResponseProcessingException(String message, Exception e) {
        super(message, e);
    }
}
