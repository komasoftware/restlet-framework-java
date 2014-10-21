package org.restlet.ext.apispark.connector;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.apispark.connector.client.ConfigurationClientResource;
import org.restlet.ext.apispark.connector.configuration.ApisparkAgentConfiguration;
import org.restlet.routing.Filter;
import org.restlet.routing.Redirector;

public class ApisparkAgentFilter extends Filter {

    private String apisparkEndpoint;

    private String username;

    private char[] password;

    private ApisparkAgentConfiguration configuration;

    private ApisparkAgentAuthenticator guard;

    private ApisparkAgentFirewall firewall;

    public ApisparkAgentFilter(Context context, String apisparkEndpoint,
            String username, char[] password) {
        super(context);
        this.apisparkEndpoint = apisparkEndpoint;
        this.username = username;
        this.password = password;
        configure();
    }

    public void configure() {
        configuration = new ConfigurationClientResource(apisparkEndpoint,
                username, password).represent();

        if (configuration != null) {
            guard = null;
            firewall = null;
            if (configuration.getAuthenticationConfiguration().isEnabled()) {
                guard = new ApisparkAgentAuthenticator(getContext(),
                        configuration.getAuthenticationConfiguration(), this);
            }
            if (configuration.getFirewallConfiguration().isEnabled()) {
                firewall = new ApisparkAgentFirewall(
                        configuration.getFirewallConfiguration());
            }
        }
        if (getNext() instanceof Redirector) {
            ((Redirector) getNext()).setTargetTemplate(configuration
                    .getApiEndpoint() + "{rr}");
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

    public String getApisparkEndpoint() {
        return apisparkEndpoint;
    }

    public void setApisparkEndpoint(String apisparkEndpoint) {
        this.apisparkEndpoint = apisparkEndpoint;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public ApisparkAgentConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ApisparkAgentConfiguration configuration) {
        this.configuration = configuration;
    }
}
