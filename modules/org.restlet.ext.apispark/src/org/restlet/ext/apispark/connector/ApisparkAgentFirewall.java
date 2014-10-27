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

import java.util.ArrayList;
import java.util.List;

import org.restlet.ext.apispark.FirewallFilter;
import org.restlet.ext.apispark.connector.configuration.ApisparkAgentFirewallConfiguration;
import org.restlet.ext.apispark.connector.configuration.ApisparkAgentRateLimitation;
import org.restlet.ext.apispark.internal.firewall.handler.BlockingHandler;
import org.restlet.ext.apispark.internal.firewall.handler.ThresholdHandler;
import org.restlet.ext.apispark.internal.firewall.handler.policy.UniqueLimitPolicy;
import org.restlet.ext.apispark.internal.firewall.rule.FirewallCounterRule;
import org.restlet.ext.apispark.internal.firewall.rule.FirewallRule;
import org.restlet.ext.apispark.internal.firewall.rule.PeriodicFirewallCounterRule;
import org.restlet.ext.apispark.internal.firewall.rule.policy.HostDomainCountingPolicy;
import org.restlet.ext.apispark.internal.firewall.rule.policy.IpAddressCountingPolicy;

public class ApisparkAgentFirewall extends FirewallFilter {

    private ApisparkAgentFirewallConfiguration configuration;

    public ApisparkAgentFirewall(ApisparkAgentFirewallConfiguration configuration) {
        this.configuration = configuration;
        configure();
    }

    public ApisparkAgentFirewallConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ApisparkAgentFirewallConfiguration configuration) {
        this.configuration = configuration;
    }

    public void configure() {
        setRules(new ArrayList<FirewallRule>());
        configure(configuration.getRateLimitations());
    }

    public void configure(List<ApisparkAgentRateLimitation> firewalls) {

        for (ApisparkAgentRateLimitation rateLimitation : firewalls) {
            if (rateLimitation.getType() == ApisparkAgentRateLimitation.GLOBAL) {
                FirewallCounterRule rule = new PeriodicFirewallCounterRule(
                        rateLimitation.getPeriod(),
                        new HostDomainCountingPolicy());

                // Create the Threshold handler.
                UniqueLimitPolicy limitPolicy = new UniqueLimitPolicy(
                        rateLimitation.getLimit());
                ThresholdHandler handler = new BlockingHandler(limitPolicy);

                rule.addHandler(handler);
                this.add(rule);
            } else if (rateLimitation.getType() == ApisparkAgentRateLimitation.INDIVIDUAL) {
                FirewallCounterRule rule = new PeriodicFirewallCounterRule(
                        rateLimitation.getPeriod(),
                        new IpAddressCountingPolicy());

                // Create the Threshold handler.
                UniqueLimitPolicy limitPolicy = new UniqueLimitPolicy(
                        rateLimitation.getLimit());
                ThresholdHandler handler = new BlockingHandler(limitPolicy);

                rule.addHandler(handler);
                this.add(rule);
            }
        }
    }
}
