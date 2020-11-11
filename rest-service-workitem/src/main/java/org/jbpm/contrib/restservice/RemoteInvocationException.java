package org.jbpm.contrib.restservice;

public class RemoteInvocationException extends Exception {

    public RemoteInvocationException(String message) {
        super(message);
    }

    public RemoteInvocationException(String message, Exception e) {
        super(message, e);
    }
}
