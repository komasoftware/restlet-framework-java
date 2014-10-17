package org.restlet.ext.apispark.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.ext.apispark.FirewallFilter;
import org.restlet.ext.apispark.connector.configuration.FirewallConfiguration;
import org.restlet.ext.apispark.connector.configuration.RateLimitation;
import org.restlet.ext.apispark.internal.firewall.handler.BlockingHandler;
import org.restlet.ext.apispark.internal.firewall.handler.ThresholdHandler;
import org.restlet.ext.apispark.internal.firewall.handler.policy.RoleLimitPolicy;
import org.restlet.ext.apispark.internal.firewall.rule.FirewallCounterRule;
import org.restlet.ext.apispark.internal.firewall.rule.PeriodicFirewallCounterRule;
import org.restlet.ext.apispark.internal.firewall.rule.policy.UserCountingPolicy;

public class ConnectorHostFirewall extends FirewallFilter {

    private FirewallConfiguration configuration;

    public ConnectorHostFirewall(FirewallConfiguration configuration) {
        this.configuration = configuration;
        configure();
    }

    public FirewallConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(FirewallConfiguration configuration) {
        this.configuration = configuration;
    }

    public void configure() {
        configure(configuration.getRateLimitations());
    }

    public void configure(List<RateLimitation> firewalls) {
        Map<Integer, List<RateLimitation>> sortedFirewalls = sortRateLimitsByPeriod(firewalls);
        for (int period : sortedFirewalls.keySet()) {
            FirewallCounterRule rule = new PeriodicFirewallCounterRule(period,
                    new UserCountingPolicy());

            // Create the Threshold handler.
            Map<String, Integer> limitsPerRole = new HashMap<String, Integer>();
            RoleLimitPolicy limitPolicy = new RoleLimitPolicy(limitsPerRole);
            ThresholdHandler handler = new BlockingHandler(limitPolicy);

            // Iterate through the rate limits.
            for (RateLimitation rateLimitation : sortedFirewalls.get(period)) {
                String group = rateLimitation.getGroup();
                if (group.equals("0")) {
                    // Limits all users
                    limitPolicy.defaultLimit = rateLimitation.getLimit();
                } else {
                    // Limits for a given group (role)
                    limitsPerRole.put(group, rateLimitation.getLimit());
                }
            }

            rule.addHandler(handler);
            this.add(rule);
        }

    }

    private static Map<Integer, List<RateLimitation>> sortRateLimitsByPeriod(
            List<RateLimitation> rateLimits) {
        Map<Integer, List<RateLimitation>> sortedRateLimits = new HashMap<Integer, List<RateLimitation>>();
        for (RateLimitation rateLimit : rateLimits) {
            if (sortedRateLimits.containsKey(rateLimit.getPeriod())) {
                sortedRateLimits.get(rateLimit.getPeriod()).add(rateLimit);
            } else {
                List<RateLimitation> limitations = new ArrayList<RateLimitation>();
                limitations.add(rateLimit);
                sortedRateLimits.put(rateLimit.getPeriod(), limitations);
            }
        }
        return sortedRateLimits;
    }
}
