package org.restlet.ext.apispark.connector;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.ext.apispark.connector.configuration.ApisparkAgentConfiguration;
import org.restlet.routing.Redirector;

public class ReverseProxyApplication extends Application {

    public ApisparkAgentConfiguration configuration;

    public ReverseProxyApplication(ApisparkAgentConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Restlet createInboundRoot() {
        Redirector redirector = new Redirector(getContext(), "",
                Redirector.MODE_SERVER_OUTBOUND);
        redirector.setTargetTemplate(configuration.getApiEndpoint()
                + "{rr}");
        return redirector;
    }
}
