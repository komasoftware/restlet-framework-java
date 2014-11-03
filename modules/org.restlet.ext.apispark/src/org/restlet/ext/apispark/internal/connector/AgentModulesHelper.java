package org.restlet.ext.apispark.internal.connector;

import org.restlet.Context;
import org.restlet.ext.apispark.ConnectorAgentConfig;
import org.restlet.ext.apispark.internal.connector.bean.ModulesSettings;
import org.restlet.ext.apispark.internal.connector.module.AnalyticsModule;
import org.restlet.ext.apispark.internal.connector.module.AuthenticationModule;
import org.restlet.ext.apispark.internal.connector.module.AuthorizationModule;
import org.restlet.ext.apispark.internal.connector.module.FirewallModule;
import org.restlet.routing.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utilities for create agents modules.
 *
 * @author Manuel Boillod
 */
public class AgentModulesHelper {

    /** Internal logger. */
    protected static Logger LOGGER = Logger
            .getLogger(AgentModulesHelper.class.getName());

    /**
     * Create each agent module enabled by the settings
     * @param connectorAgentConfig
     *          The connector agent configuration
     * @param modulesSettings
     *          The modules settings
     * @param context
     *          The context
     */
    public static AgentModulesConfigurer buildFromSettings(ConnectorAgentConfig connectorAgentConfig,
                                                 ModulesSettings modulesSettings,
                                                 Context context) {
        List<Filter> filters = new ArrayList<>();

        if (modulesSettings.isAuthenticationModuleEnabled()) {
            LOGGER.info("Add authentication module");
            filters.add(new AuthenticationModule(connectorAgentConfig, modulesSettings, context));
        }
        if (modulesSettings.isAuthorizationModuleEnabled()) {
            if (!modulesSettings.isAuthenticationModuleEnabled()) {
                throw new ConnectorConfigurationException("Authorization module requires Authentication module which is not enabled");
            }
            LOGGER.info("Add authorization module");
            filters.add(new AuthorizationModule(connectorAgentConfig, modulesSettings, context));
        }
        if (modulesSettings.isFirewallModuleEnabled()) {
            LOGGER.info("Add firewall module");
            filters.add(new FirewallModule(connectorAgentConfig, modulesSettings, context));
        }
        if (modulesSettings.isAnalyticsModuleEnabled()) {
            LOGGER.info("Add analytics module");
            filters.add(new AnalyticsModule(connectorAgentConfig, modulesSettings, context));
        }


        if (filters.isEmpty()) {
            LOGGER.warning("No modules are enabled");
        }
        return new AgentModulesConfigurer(filters);
    }
}
