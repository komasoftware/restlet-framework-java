/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.connector;

import java.io.File;

import org.restlet.Component;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.ext.apispark.connector.configuration.ApisparkAgentConfiguration;
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
        ApisparkAgentConfiguration configuration = new JacksonRepresentation<ApisparkAgentConfiguration>(
                new FileRepresentation(new File(configPath),
                        MediaType.APPLICATION_YAML),
                ApisparkAgentConfiguration.class).getObject();

        ApisparkAgentService service = new ApisparkAgentService(configuration);

        // Launch the reverse proxy component
        Component reverseProxyHost = new Component();
        reverseProxyHost.getServers().add(
                new Protocol(configuration.getProxyProtocol()),
                configuration.getProxyPort());
        reverseProxyHost.getClients().add(
                new Protocol(configuration.getProxyProtocol()));
        ReverseProxyApplication app = new ReverseProxyApplication(configuration);
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
