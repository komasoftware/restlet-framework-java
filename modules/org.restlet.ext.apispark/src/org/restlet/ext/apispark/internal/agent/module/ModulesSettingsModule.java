package org.restlet.ext.apispark.internal.agent.module;

import org.restlet.ext.apispark.internal.agent.AgentConfig;
import org.restlet.ext.apispark.internal.agent.AgentUtils;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.ext.apispark.internal.agent.resource.ModulesSettingsResource;

/**
 * Get modules settings from connector service
 *
 * @author Manuel Boillod
 */
public class ModulesSettingsModule {

    public static final String MODULE_PATH = "/settings";

    private ModulesSettings modulesSettings;

    public ModulesSettingsModule(AgentConfig agentConfig) {
        ModulesSettingsResource modulesSettingsResource = AgentUtils.getConfiguredClientResource(
                agentConfig, null, ModulesSettingsResource.class, MODULE_PATH);
        modulesSettings = modulesSettingsResource.getSettings();
    }


    public ModulesSettings getModulesSettings() {
        return modulesSettings;
    }
}
