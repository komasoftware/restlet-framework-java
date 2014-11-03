package org.restlet.ext.apispark.internal.connector.resource;

import org.restlet.ext.apispark.internal.connector.bean.Credentials;
import org.restlet.ext.apispark.internal.connector.bean.FirewallSettings;
import org.restlet.ext.apispark.internal.connector.bean.User;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

public interface FirewallSettingsResource {

    /**
     * Retrieve the firewall settings from apispark connector cell.
     */
    @Get
    public FirewallSettings getSettings();
}
