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

package org.restlet.ext.apispark.connector.configuration;

public class ApisparkAgentConfiguration {

    private ApisparkAgentAuthenticationConfiguration authenticationConfiguration;

    private ApisparkAgentAuthorizationConfiguration authorizationConfiguration;

    private ApisparkAgentFirewallConfiguration firewallConfiguration;

    private ApisparkAgentAnalyticsConfiguration analyticsConfiguration;

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

    public ApisparkAgentAnalyticsConfiguration getAnalyticsConfiguration() {
        return analyticsConfiguration;
    }

    public void setAnalyticsConfiguration(
            ApisparkAgentAnalyticsConfiguration analyticsConfiguration) {
        this.analyticsConfiguration = analyticsConfiguration;
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
