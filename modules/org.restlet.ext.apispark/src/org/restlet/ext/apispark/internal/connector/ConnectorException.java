package org.restlet.ext.apispark.internal.connector;

/**
 * @author Manuel Boillod
 */
public class ConnectorException extends RuntimeException {

    public ConnectorException() {
    }

    public ConnectorException(String message) {
        super(message);
    }

    public ConnectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectorException(Throwable cause) {
        super(cause);
    }
}
