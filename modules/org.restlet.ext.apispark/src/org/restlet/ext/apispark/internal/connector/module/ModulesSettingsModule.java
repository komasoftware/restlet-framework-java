package org.restlet.ext.apispark.internal.connector.module;

import org.restlet.ext.apispark.ConnectorAgentConfig;
import org.restlet.ext.apispark.internal.connector.AgentUtils;
import org.restlet.ext.apispark.internal.connector.config.ModulesSettings;
import org.restlet.ext.apispark.internal.connector.resource.ModulesSettingsResource;

/**
 * Get modules settings from connector service
 *
 * @author Manuel Boillod
 */
public class ModulesSettingsModule {

    public static final String MODULE_PATH = "/settings";

    private ModulesSettings modulesSettings;

    public ModulesSettingsModule(ConnectorAgentConfig connectorAgentConfig) {
        ModulesSettingsResource modulesSettingsResource = AgentUtils.getConfiguredClientResource(
                connectorAgentConfig, null, ModulesSettingsResource.class, MODULE_PATH);
        modulesSettings = modulesSettingsResource.getSettings();
    }


    public ModulesSettings getModulesSettings() {
        return modulesSettings;
    }
}
