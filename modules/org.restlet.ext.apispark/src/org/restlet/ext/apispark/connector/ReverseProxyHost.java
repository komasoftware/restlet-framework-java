package org.restlet.ext.apispark.connector;

import java.io.File;

import org.restlet.Component;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.ext.apispark.connector.configuration.ApisparkAgentConfiguration;
import org.restlet.ext.apispark.connector.configuration.ApisparkAgentProperties;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.FileRepresentation;

public class ReverseProxyHost {

    public static void main(String[] args) throws Exception {
        String configPath = null;
        for (int i = 0; i < args.length; i++) {
            if ("-c".equals(args[i]) || "--config".equals(args[i])) {
                configPath = args[++i];
            } else if ("-h".equals(args[i]) || "--help".equals(args[i])) {
                displayHelp();
                System.exit(0);
            }
        }
        if (configPath == null) {
            throw new Exception(
                    "Please provide the path of your configuration file");
        }

        // Retrieve the properties (endpoint and credentials to the APISpark
        // service)
        ApisparkAgentProperties properties = new JacksonRepresentation<ApisparkAgentProperties>(
                new FileRepresentation(new File(configPath),
                        MediaType.APPLICATION_YAML),
                ApisparkAgentProperties.class).getObject();

        ApisparkAgentService service = new ApisparkAgentService(
                properties.getApisparkEndpoint(), properties.getUsername(),
                properties.getPassword());

        // Retrieve the configuration from the APISpark service
        ApisparkAgentConfiguration configuration = service.getFilter()
                .getConfiguration();

        // Launch the reverse proxy component
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

    private static void displayHelp() {
        // TODO display help
        System.out.println("HELP");
    }
}
