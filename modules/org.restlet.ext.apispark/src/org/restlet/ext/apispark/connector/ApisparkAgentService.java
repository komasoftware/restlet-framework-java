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
        ApisparkAgentAuthenticator guard = null;
        ApisparkAgentFirewall firewall = null;
        ApisparkAgentAnalytics analytics = null;
        if (configuration.getAuthenticationConfiguration().isEnabled()) {
            guard = new ApisparkAgentAuthenticator(getContext(), configuration);
        }
        if (configuration.getFirewallConfiguration().isEnabled()) {
            firewall = new ApisparkAgentFirewall(
                    configuration.getFirewallConfiguration());
        }
        if (configuration.getAnalyticsConfiguration().isEnabled()) {
            analytics = new ApisparkAgentAnalytics(
                    configuration.getAnalyticsConfiguration());
        }
        filter = new ApisparkAgentFilter(guard, firewall, analytics);
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
