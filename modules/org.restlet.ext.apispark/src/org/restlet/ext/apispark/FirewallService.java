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

package org.restlet.ext.apispark;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.restlet.Context;
import org.restlet.ext.apispark.internal.firewall.rule.FirewallRule;
import org.restlet.ext.apispark.internal.firewall.rule.FirewallRuleUtils;
import org.restlet.routing.Filter;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.restlet.service.Service;

/**
 * Service that controls the incoming requests by applying a set of rules.
 * 
 * @author Guillaume Blondeau
 */
public class FirewallService extends Service {
    /** The underlying instance of {@link FirewallFilter}. */
    private FirewallFilter firewall;

    /** The list of associated {@link FirewallRule}. */
    private List<FirewallRule> rules = new ArrayList<FirewallRule>();

    @Override
    public Filter createInboundFilter(Context context) {
        firewall = new FirewallFilter(context);
        return firewall;
    }


    /**
     * Add a rule that limits the number of concurrent requests by request's
     * host domain.
     *
     * @param limit
     *            The maximum number of requests allowed by host domain at the
     *            same time.
     */
    public void addHostDomainConcurrencyCounterRule(
            int limit) {
        FirewallRule rule = FirewallRuleUtils.createHostDomainConcurrencyCounterRule(limit);
        rules.add(rule);
    }

    /**
     * Add a rule that limits the number of requests for a given period of
     * time by request's host domain.
     *
     * @param period
     *            The period of time.
     * @param limit
     *            The maximum number of requests allowed by host domain for the
     *            given period of time.
     */
    public void addHostDomainPeriodicCounterRule(
            int period, int limit) {
        FirewallRule rule = FirewallRuleUtils.createHostDomainPeriodicCounterRule(period, limit);
        rules.add(rule);
    }

    /**
     * Add a rule that forbids access to the given set of IP addresses.
     *
     * @param blackList
     *            The list of rejected IP adresses.
     */
    public void addIpAddressesBlackListRule(List<String> blackList) {
        FirewallRule rule = FirewallRuleUtils.createIpAddressesBlackListRule(blackList);
        rules.add(rule);
    }

    /**
     * Add a rule that restricts access according to the IP address of the
     * request's client. A unique limit is applied for all IP addresses.
     *
     * @param limit
     *            The maximum number of accepted concurrent requests.
     */
    public void addIpAddressesConcurrencyCounterRule(
            int limit) {
        FirewallRule rule = FirewallRuleUtils.createIpAddressesConcurrencyCounterRule(limit);
        rules.add(rule);
    }

    /**
     * Add a rule that restricts access by period of time according to the
     * IP address of the request's client. A unique limit is applied for all IP
     * addresses.
     *
     * @param period
     *            The period of time.
     * @param limit
     *            The maximum number of accepted requests for a period of time.
     */
    public void addIpAddressesPeriodicCounterRule(
            int period, int limit) {
        FirewallRule rule = FirewallRuleUtils.createIpAddressesPeriodicCounterRule(period, limit);
        rules.add(rule);
    }

    /**
     * Add a rule that restricts access to the given set of IP addresses.
     *
     * @param whiteList
     *            The list of accepted IP adresses.
     */
    public void addIpAddressesWhiteListRule(List<String> whiteList) {
        FirewallRule rule = FirewallRuleUtils.createIpAddressesWhiteListRule(whiteList);
        rules.add(rule);
    }

    /**
     * Add a rule that restricts access according to the {@link org.restlet.security.Role} of the
     * current authenticated {@link org.restlet.security.User}. Each role is defined a limit in terms
     * of concurrent requests, in any other case the access is forbidden.
     *
     * @param limitsPerRole
     *            The limit assigned per role's name.
     */
    public void addRolesConcurrencyCounterRule(
            Map<String, Integer> limitsPerRole) {
        FirewallRule rule = FirewallRuleUtils.createRolesConcurrencyCounterRule(limitsPerRole);
        rules.add(rule);
    }



    /**
     * Add a rule that restricts access according to the {@link org.restlet.security.Role} of the
     * current authenticated {@link org.restlet.security.User}. Each role is defined a limit in terms
     * of concurrent requests, in any other case a default limit is applied.
     *
     * @param limitsPerRole
     *            The limit assigned per role's name.
     * @param defaultLimit
     *            The limit assigned for any other roles, or for user without
     *            assigned role.
     */
    public void addRolesConcurrencyCounterRule(
            Map<String, Integer> limitsPerRole, int defaultLimit) {
        FirewallRule rule = FirewallRuleUtils.createRolesConcurrencyCounterRule(limitsPerRole, defaultLimit);
        rules.add(rule);    }

    /**
     * Add a rule that restricts access according to the {@link org.restlet.security.Role} of the
     * current authenticated {@link org.restlet.security.User}. Each role is defined a limit in terms
     * of requests by period of time, in any other case the access is forbidden.
     *
     * @param period
     *            The period of time.
     * @param limitsPerRole
     *            The limit assigned per role's name.
     */
    public void addRolesPeriodicCounterRule(
            int period, Map<String, Integer> limitsPerRole) {
        FirewallRule rule = FirewallRuleUtils.createRolesPeriodicCounterRule(period, limitsPerRole);
        rules.add(rule);
    }

    /**
     * Add a rule that restricts access according to the {@link org.restlet.security.Role} of the
     * current authenticated {@link org.restlet.security.User}. Each role is defined a limit in terms
     * of concurrent requests, in any other case a default limit is applied.
     *
     * @param period
     *            The period of time.
     * @param limitsPerRole
     *            The limit assigned per role's name.
     * @param defaultLimit
     *            The limit assigned for any other roles, or for user without
     *            assigned role.
     */
    public void addRolesPeriodicCounterRule(
            int period, Map<String, Integer> limitsPerRole, int defaultLimit) {
        FirewallRule rule = FirewallRuleUtils.createRolesPeriodicCounterRule(period, limitsPerRole, defaultLimit);
        rules.add(rule);
    }

}
