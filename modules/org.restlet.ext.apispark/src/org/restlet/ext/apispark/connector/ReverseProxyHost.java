package org.restlet.ext.apispark.connector;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.ext.apispark.connector.configuration.ApisparkAgentConfiguration;

public class ReverseProxyHost {

    public static void main(String[] args) throws Exception {
        ApisparkAgentService service = new ApisparkAgentService(
                args.length > 0 ? args[0] : "reverseProxy.properties");

        ApisparkAgentConfiguration configuration = service.getFilter()
                .getConfiguration();

        Component reverseProxyHost = new Component();
        reverseProxyHost.getServers().add(
                new Protocol(configuration.getProxyProtocol()),
                configuration.getProxyPort());
        reverseProxyHost.getClients().add(
                new Protocol(configuration.getProxyProtocol()));
        ReverseProxyApplication app = new ReverseProxyApplication(service
                .getFilter().getConfiguration());
        reverseProxyHost.getDefaultHost().attach(
                configuration.getProxyBasePath(), app);
        reverseProxyHost.getServices().add(service);
        reverseProxyHost.start();
    }
}
