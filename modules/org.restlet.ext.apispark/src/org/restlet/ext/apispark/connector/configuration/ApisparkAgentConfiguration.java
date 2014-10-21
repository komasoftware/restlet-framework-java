package org.restlet.ext.apispark.connector.configuration;

public class ApisparkAgentConfiguration {

    private ApisparkAgentAuthenticationConfiguration authenticationConfiguration;

    private ApisparkAgentAuthorizationConfiguration authorizationConfiguration;

    private ApisparkAgentFirewallConfiguration firewallConfiguration;

    private String apiEndpoint;

    private String connectorHostLogin;

    private char[] connectorHostSecret;

    private String proxyBasePath;

    private String proxyProtocol;

    public String getProxyProtocol() {
        return proxyProtocol;
    }

    public void setProxyProtocol(String proxyProtocol) {
        this.proxyProtocol = proxyProtocol;
    }

    private int proxyPort;

    public ApisparkAgentAuthenticationConfiguration getAuthenticationConfiguration() {
        return authenticationConfiguration;
    }

    public void setAuthenticationConfiguration(
            ApisparkAgentAuthenticationConfiguration authenticationConfiguration) {
        this.authenticationConfiguration = authenticationConfiguration;
    }

    public ApisparkAgentAuthorizationConfiguration getAuthorizationConfiguration() {
        return authorizationConfiguration;
    }

    public void setAuthorizationConfiguration(
            ApisparkAgentAuthorizationConfiguration authorizationConfiguration) {
        this.authorizationConfiguration = authorizationConfiguration;
    }

    public ApisparkAgentFirewallConfiguration getFirewallConfiguration() {
        return firewallConfiguration;
    }

    public void setFirewallConfiguration(
            ApisparkAgentFirewallConfiguration firewallConfiguration) {
        this.firewallConfiguration = firewallConfiguration;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public String getConnectorHostLogin() {
        return connectorHostLogin;
    }

    public void setConnectorHostLogin(String connectorHostLogin) {
        this.connectorHostLogin = connectorHostLogin;
    }

    public char[] getConnectorHostSecret() {
        return connectorHostSecret;
    }

    public void setConnectorHostSecret(char[] connectorHostSecret) {
        this.connectorHostSecret = connectorHostSecret;
    }

    public String getProxyBasePath() {
        return proxyBasePath;
    }

    public void setProxyBasePath(String proxyBasePath) {
        this.proxyBasePath = proxyBasePath;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }
}
