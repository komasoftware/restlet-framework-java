package org.restlet.test.ext.apispark;

import org.restlet.ext.apispark.connector.configuration.ApisparkAgentAuthenticationConfiguration;
import org.restlet.ext.apispark.connector.configuration.ApisparkAgentConfiguration;
import org.restlet.ext.apispark.connector.configuration.ApisparkAgentFirewallConfiguration;
import org.restlet.resource.ServerResource;

public class ConfigurationServerResource extends ServerResource implements
        ConfigurationResource {

    @Override
    public ApisparkAgentConfiguration represent() {
        ApisparkAgentConfiguration result = new ApisparkAgentConfiguration();
        result.setApiEndpoint("");
        result.setConnectorHostLogin("");
        result.setConnectorHostSecret("".toCharArray());

        ApisparkAgentAuthenticationConfiguration authConf = new ApisparkAgentAuthenticationConfiguration();
        authConf.setEnabled(true);
        authConf.setEndpoint("http://localhost:8182/authentication");
        authConf.setUsername("owner");
        authConf.setPassword("owner");
        authConf.setRefreshRate(5);
        result.setAuthenticationConfiguration(authConf);

        ApisparkAgentFirewallConfiguration firewallConf = new ApisparkAgentFirewallConfiguration();
        firewallConf.setEnabled(false);
        result.setFirewallConfiguration(firewallConf);

        return result;
    }

}
