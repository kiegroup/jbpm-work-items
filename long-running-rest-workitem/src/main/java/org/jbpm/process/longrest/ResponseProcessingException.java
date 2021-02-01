package org.jbpm.process.longrest;

public class ResponseProcessingException extends Exception {

    public ResponseProcessingException(String message, Exception e) {
        super(message, e);
    }
}
