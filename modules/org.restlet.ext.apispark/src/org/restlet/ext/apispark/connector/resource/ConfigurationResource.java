package org.restlet.ext.apispark.connector.resource;

import org.restlet.ext.apispark.connector.configuration.ApisparkAgentConfiguration;
import org.restlet.resource.Get;

public interface ConfigurationResource {

    @Get("yaml")
    public ApisparkAgentConfiguration represent();
}
