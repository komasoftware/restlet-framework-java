package org.restlet.ext.apispark.connector.configuration;

import java.util.ArrayList;
import java.util.List;

public class ApisparkAgentFirewallConfiguration {

    private List<ApisparkAgentRateLimitation> rateLimitations;

    private boolean enabled;

    public List<ApisparkAgentRateLimitation> getRateLimitations() {
        if (rateLimitations == null) {
            rateLimitations = new ArrayList<ApisparkAgentRateLimitation>();
        }
        return rateLimitations;
    }

    public void setRateLimitations(List<ApisparkAgentRateLimitation> rateLimitations) {
        this.rateLimitations = rateLimitations;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
