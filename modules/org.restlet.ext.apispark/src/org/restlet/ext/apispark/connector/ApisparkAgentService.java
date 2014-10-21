package org.restlet.ext.apispark.connector;

import org.restlet.Context;
import org.restlet.ext.apispark.connector.configuration.ApisparkAgentConfiguration;
import org.restlet.routing.Filter;
import org.restlet.service.Service;

public class ApisparkAgentService extends Service {

    private ApisparkAgentConfiguration configuration;

    private ApisparkAgentFilter filter;

    public ApisparkAgentService(ApisparkAgentConfiguration configuration) {
        this.configuration = configuration;
        filter = new ApisparkAgentFilter(configuration);
    }

    public ApisparkAgentConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ApisparkAgentConfiguration configuration) {
        this.configuration = configuration;
    }

    public ApisparkAgentFilter getFilter() {
        return filter;
    }

    public void setFilter(ApisparkAgentFilter filter) {
        this.filter = filter;
    }

    @Override
    public Filter createInboundFilter(Context context) {
        filter.setContext(context);
        return filter;
    }
}
