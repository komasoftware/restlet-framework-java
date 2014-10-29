package org.restlet.ext.apispark.internal.connector;

/**
 * @author Manuel Boillod
 */
public class ConnectorConfigurationException extends ConnectorException {

    public ConnectorConfigurationException() {
    }

    public ConnectorConfigurationException(String message) {
        super(message);
    }

    public ConnectorConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectorConfigurationException(Throwable cause) {
        super(cause);
    }
}
