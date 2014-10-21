package org.restlet.ext.apispark.connector;

import org.restlet.Context;
import org.restlet.routing.Filter;
import org.restlet.service.Service;

public class ApisparkAgentService extends Service {

    private String apisparkEndpoint;

    private String username;

    private char[] password;

    private ApisparkAgentFilter filter;

    public ApisparkAgentService(String propertiesFileName) {
        // TODO
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

    public ApisparkAgentFilter getFilter() {
        return filter;
    }

    public void setFilter(ApisparkAgentFilter filter) {
        this.filter = filter;
    }

    @Override
    public Filter createInboundFilter(Context context) {
        filter = new ApisparkAgentFilter(context, apisparkEndpoint, username,
                password);
        return filter;
    }
}
