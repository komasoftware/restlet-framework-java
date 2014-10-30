package org.restlet.ext.apispark.internal.connector;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.Header;
import org.restlet.ext.apispark.ConnectorAgentConfig;
import org.restlet.ext.apispark.internal.connector.config.ModulesSettings;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

/**
 * @author Manuel Boillod
 */
public abstract class AgentUtils {

    /**
     * Returns a client resource configured to communicate with the apispark connector cell.
     *
     * @param connectorAgentConfig
     *          The connector agent configuration
     * @param modulesSettings
     *          The modules settings. Could be null.
     * @param resourceClass
     *          The resource class
     * @param resourcePath
     *          The resource path
     *
     * @return a client resource configured to communicate with the apispark connector cell.
     */
    public static <T> T getConfiguredClientResource(ConnectorAgentConfig connectorAgentConfig,
                                                    ModulesSettings modulesSettings,
                                                             Class<T> resourceClass,
                                                             String resourcePath) {
        String path = connectorAgentConfig.getAgentServicePath() + resourcePath;

        ClientResource clientResource = new ClientResource(path);

        //add authentication scheme
        clientResource.setChallengeResponse(ChallengeScheme.HTTP_BASIC,
                connectorAgentConfig.getAgentUsername(),
                connectorAgentConfig.getAgentSecretKey());

        //send connector agent version to apispark in headers
        Series<Header> headers = clientResource.getRequest().getHeaders();
        headers.add(ConnectorConstants.REQUEST_HEADER_CONNECTOR_AGENT_VERSION, ConnectorAgentConfig.AGENT_VERSION);

        //send connector cell revision to apispark in headers
        if (modulesSettings != null) {
            headers.add(ConnectorConstants.REQUEST_HEADER_CONNECTOR_CELL_REVISION, modulesSettings.getCellRevision());
        }

        //send cellId and cellVersion in queryParams
        clientResource.setQueryValue("cellId", connectorAgentConfig.getCellId().toString());
        clientResource.setQueryValue("cellVersion", connectorAgentConfig.getCellVersion().toString());


        return clientResource.wrap(resourceClass);
    }

}
