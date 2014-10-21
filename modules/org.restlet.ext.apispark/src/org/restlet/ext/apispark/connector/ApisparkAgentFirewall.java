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
