package org.restlet.ext.apispark.internal.connector.module;

import org.restlet.Context;
import org.restlet.ext.apispark.ConnectorAgentConfig;
import org.restlet.ext.apispark.internal.connector.config.ModulesSettings;
import org.restlet.routing.Filter;

/**
 * @author Manuel Boillod
 */
public class FirewallModule extends Filter {
    public FirewallModule(ConnectorAgentConfig connectorAgentConfig, ModulesSettings modulesSettings) {
        this(connectorAgentConfig, modulesSettings, null);
    }
    public FirewallModule(ConnectorAgentConfig connectorAgentConfig, ModulesSettings modulesSettings, Context context) {
        super(context);
    }
}


//configuration

//private List<ApisparkAgentRateLimitation> rateLimitations;


//ApisparkAgentRateLimitation

//public class ApisparkAgentRateLimitation {
//
//    public static final int GLOBAL = 0;
//
//    public static final int INDIVIDUAL = 1;
//
//    private String name;
//
//    private int type;
//
//    private int period;
//
//    private int limit;
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public int getType() {
//        return type;
//    }
//
//    public void setType(int type) {
//        this.type = type;
//    }
//
//    public int getPeriod() {
//        return period;
//    }
//
//    public void setPeriod(int period) {
//        this.period = period;
//    }
//
//    public int getLimit() {
//        return limit;
//    }
//
//    public void setLimit(int limit) {
//        this.limit = limit;
//    }
//}



//configure firewall


//    public void configure(List<ApisparkAgentRateLimitation> firewalls) {
//
//        for (ApisparkAgentRateLimitation rateLimitation : firewalls) {
//            if (rateLimitation.getType() == ApisparkAgentRateLimitation.GLOBAL) {
//                FirewallCounterRule rule = new PeriodicFirewallCounterRule(
//                        rateLimitation.getPeriod(),
//                        new HostDomainCountingPolicy());
//
//                // Create the Threshold handler.
//                UniqueLimitPolicy limitPolicy = new UniqueLimitPolicy(
//                        rateLimitation.getLimit());
//                ThresholdHandler handler = new BlockingHandler(limitPolicy);
//
//                rule.addHandler(handler);
////                this.add(rule);
//            } else if (rateLimitation.getType() == ApisparkAgentRateLimitation.INDIVIDUAL) {
//                FirewallCounterRule rule = new PeriodicFirewallCounterRule(
//                        rateLimitation.getPeriod(),
//                        new IpAddressCountingPolicy());
//
//                // Create the Threshold handler.
//                UniqueLimitPolicy limitPolicy = new UniqueLimitPolicy(
//                        rateLimitation.getLimit());
//                ThresholdHandler handler = new BlockingHandler(limitPolicy);
//
//                rule.addHandler(handler);
////                this.add(rule);
//            }
//        }
//    }