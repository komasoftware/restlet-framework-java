package org.restlet.ext.apispark.connector;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.ext.apispark.connector.client.ConfigurationClientResource;
import org.restlet.ext.apispark.connector.configuration.Configuration;
import org.restlet.routing.Filter;
import org.restlet.routing.Redirector;

public class ConnectorHostFilter extends Filter {

    private String path;

    private String username;

    private char[] password;

    private Configuration configuration;

    private ConnectorHostAuthenticator guard;

    private ConnectorHostFirewall firewall;

    public ConnectorHostFilter(Context context, String path, String username,
            char[] password, Restlet next) {
        super(context, next);
        this.path = path;
        this.username = username;
        this.password = password;
        configure();
    }

    private void configure() {
        configuration = new ConfigurationClientResource(path, username,
                password).represent();

        if (configuration != null) {
            guard = null;
            firewall = null;
            if (configuration.getAuthenticationConfiguration().isEnabled()) {
                guard = new ConnectorHostAuthenticator(getContext(),
                        configuration.getAuthenticationConfiguration());
            }
            if (configuration.getFirewallConfiguration().isEnabled()) {
                firewall = new ConnectorHostFirewall(
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
