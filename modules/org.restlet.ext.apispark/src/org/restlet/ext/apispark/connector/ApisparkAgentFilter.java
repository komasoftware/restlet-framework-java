package org.restlet.ext.apispark.connector;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.apispark.connector.configuration.ApisparkAgentConfiguration;
import org.restlet.routing.Filter;

public class ApisparkAgentFilter extends Filter {

    private ApisparkAgentConfiguration configuration;

    private ApisparkAgentAuthenticator guard;

    private ApisparkAgentFirewall firewall;

    public ApisparkAgentFilter(ApisparkAgentConfiguration configuration) {
        this.configuration = configuration;
        configure();
    }

    public void configure() {
        if (configuration != null) {
            guard = null;
            firewall = null;
            if (configuration.getAuthenticationConfiguration().isEnabled()) {
                guard = new ApisparkAgentAuthenticator(getContext(),
                        configuration);
            }
            if (configuration.getFirewallConfiguration().isEnabled()) {
                firewall = new ApisparkAgentFirewall(
                        configuration.getFirewallConfiguration());
            }
        }
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        if (guard != null) {
            if (!guard.authenticate(request, response)) {
                return STOP;
            }
        }
        if (firewall != null) {
            int firewallTest = firewall.beforeHandle(request, response);
            if (firewallTest != CONTINUE) {
                return firewallTest;
            }
        }
        return CONTINUE;
    }

    public ApisparkAgentConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ApisparkAgentConfiguration configuration) {
        this.configuration = configuration;
    }
}
