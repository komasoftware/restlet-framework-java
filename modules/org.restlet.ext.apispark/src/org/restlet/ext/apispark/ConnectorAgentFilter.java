/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.ext.apispark.internal.connector.AgentModulesConfigurer;
import org.restlet.ext.apispark.internal.connector.module.ModulesSettingsModule;
import org.restlet.ext.apispark.internal.connector.bean.ModulesSettings;
import org.restlet.ext.apispark.internal.connector.AgentModulesHelper;
import org.restlet.routing.Filter;

public class ConnectorAgentFilter extends Filter {

    private AgentModulesConfigurer agentModulesConfigurer;

    /**
     * Create a new ConnectorAgentFilter with the specified configuration.
     *
     * @param connectorAgentConfig
     *          The connector agent configuration.
     */
    public ConnectorAgentFilter(ConnectorAgentConfig connectorAgentConfig) {
        this(connectorAgentConfig, null);
    }

    /**
     * Create a new ConnectorAgentFilter with the specified configuration.
     *
     * @param connectorAgentConfig
     *          The connector agent configuration.
     * @param context
     *          The context
     */
    public ConnectorAgentFilter(ConnectorAgentConfig connectorAgentConfig, Context context) {
        super(context);
        configureAgent(connectorAgentConfig);
    }

    /**
     * Configure the filter with the specified configuration.
     *
     * Retrieve the modules settings from the connector service.
     *
     * @param connectorAgentConfig
     *          The connector agent configuration.
     */
    public void configureAgent(ConnectorAgentConfig connectorAgentConfig) {
        connectorAgentConfig.validate();

        ModulesSettingsModule modulesSettingsModule = new ModulesSettingsModule(connectorAgentConfig);
        ModulesSettings modulesSettings = modulesSettingsModule.getModulesSettings();

        agentModulesConfigurer = AgentModulesHelper.buildFromSettings(connectorAgentConfig, modulesSettings, getContext());
    }



    @Override
    public Restlet getNext() {
        return agentModulesConfigurer.getNext();
    }

    @Override
    public void setNext(Restlet next) {
        agentModulesConfigurer.setNext(next);
    }

}
