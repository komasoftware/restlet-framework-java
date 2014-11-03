package org.restlet.ext.apispark.internal.connector.resource;

import org.restlet.ext.apispark.internal.connector.bean.ModulesSettings;
import org.restlet.resource.Get;

/**
 * Resource used for communicate with apispark connector cell.
 *
 * @author Manuel Boillod
 */
public interface ModulesSettingsResource {

    /**
     * Retrieve the modules settings from apispark connector cell.
     */
    @Get
    ModulesSettings getSettings();
}
