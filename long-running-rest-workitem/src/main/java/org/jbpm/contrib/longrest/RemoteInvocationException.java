package org.jbpm.contrib.longrest;

public class RemoteInvocationException extends Exception {

    public RemoteInvocationException(String message) {
        super(message);
    }

    public RemoteInvocationException(String message, Exception e) {
        super(message, e);
    }
}
