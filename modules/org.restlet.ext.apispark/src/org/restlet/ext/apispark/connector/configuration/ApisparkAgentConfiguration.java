package org.restlet.ext.apispark.connector.configuration;

public class ApisparkAgentConfiguration {

    private ApisparkAgentAuthenticationConfiguration authenticationConfiguration;

    private ApisparkAgentAuthorizationConfiguration authorizationConfiguration;

    private ApisparkAgentFirewallConfiguration firewallConfiguration;

    private String apiEndpoint;

    private String proxyBasePath;

    private String proxyProtocol;

    private int proxyPort;

    private String apisparkEndpoint;

    private String username;

    private char[] password;

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

    public String getProxyBasePath() {
        return proxyBasePath;
    }

    public void setProxyBasePath(String proxyBasePath) {
        this.proxyBasePath = proxyBasePath;
    }

    public String getProxyProtocol() {
        return proxyProtocol;
    }

    public void setProxyProtocol(String proxyProtocol) {
        this.proxyProtocol = proxyProtocol;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
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
}
