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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.internal.firewall.rule;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * A {@link FirewallIpFilteringRule} filter user IP address from a white list or a black list/
 *
 * @author Manuel Boillod
 */
public class FirewallIpFilteringRule extends FirewallRule {

    private Set<String> filteredAddresses;

    private boolean whiteList;

    public FirewallIpFilteringRule() {

    }

    public FirewallIpFilteringRule(Collection<String> filteredAddresses, boolean whiteList) {
        this.filteredAddresses = new HashSet<>(filteredAddresses);
        this.whiteList = whiteList;
    }

    /**
     * Filters the request on the user IP address.
     *
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The continuation status.
     */
    public int beforeHandle(Request request, Response response) {
        String address = request.getClientInfo().getUpstreamAddress();

        if (filteredAddresses.contains(address)) {
            if (whiteList) {
                return Filter.CONTINUE;
            } else {
                Context.getCurrentLogger().log(
                        Level.FINE,
                        "The current request has been blocked because \""
                                + address
                                + "\" is in the black list.");

                response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                return Filter.STOP;
            }
        } else {
            if (whiteList) {
                Context.getCurrentLogger().log(
                        Level.FINE,
                        "The current request has been blocked because \""
                                + address
                                + "\" is not in the white list.");

                response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                return Filter.STOP;
            } else {
                return Filter.CONTINUE;
            }
        }
    }

    public Set<String> getFilteredAddresses() {
        return filteredAddresses;
    }

    public void setFilteredAddresses(Set<String> filteredAddresses) {
        this.filteredAddresses = filteredAddresses;
    }

    public boolean isWhiteList() {
        return whiteList;
    }

    public void setWhiteList(boolean whiteList) {
        this.whiteList = whiteList;
    }
}
