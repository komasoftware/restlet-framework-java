package org.restlet.ext.apispark.connector;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Redirector;

public class ConnectorHostApplication extends Application {

    private String path;

    private String username;

    private char[] password;

    private ConnectorHostFilter filter;

    public ConnectorHostApplication(String path, String username,
            char[] password) {
        this.path = path;
        this.username = username;
        this.password = password;
    }

    @Override
    public Restlet createInboundRoot() {
        Redirector redirector = new Redirector(getContext(), "",
                Redirector.MODE_SERVER_OUTBOUND);
        filter = new ConnectorHostFilter(getContext(), path, username,
                password, redirector);
        redirector.setTargetTemplate(filter.getConfiguration().getApiEndpoint()
                + "{rr}");
        return filter;
    }

    public ConnectorHostFilter getFilter() {
        return filter;
    }

    public void setFilter(ConnectorHostFilter filter) {
        this.filter = filter;
    }
}
