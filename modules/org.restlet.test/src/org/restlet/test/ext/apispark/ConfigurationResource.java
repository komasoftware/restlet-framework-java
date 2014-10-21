package org.restlet.test.ext.apispark;

import org.restlet.ext.apispark.connector.configuration.ApisparkAgentConfiguration;
import org.restlet.resource.Get;

public interface ConfigurationResource {

    @Get
    public ApisparkAgentConfiguration represent();
}
