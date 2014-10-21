package org.restlet.ext.apispark.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.apispark.connector.client.AuthenticationClientResource;
import org.restlet.ext.apispark.connector.configuration.ApisparkAgentAuthenticationConfiguration;
import org.restlet.resource.ResourceException;
import org.restlet.security.Authenticator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ApisparkAgentAuthenticator extends Authenticator {

    private final String PATH = "/authentication";

    private String apisparkEndpoint;

    private String username;

    private char[] password;

    private ApisparkAgentAuthenticationConfiguration configuration;

    private LoadingCache<Credentials, List<String>> cache;

    public ApisparkAgentAuthenticationConfiguration getAuthenticationConfiguration() {
        return configuration;
    }

    public void setAuthenticationConfiguration(
            ApisparkAgentAuthenticationConfiguration configuration) {
        this.configuration = configuration;
    }

    public ApisparkAgentAuthenticator(Context context,
            ApisparkAgentAuthenticationConfiguration configuration,
            ApisparkAgentFilter filter) {
        super(context);
        this.apisparkEndpoint = filter.getApisparkEndpoint() + PATH;
        this.username = filter.getUsername();
        this.password = filter.getPassword();
        this.configuration = configuration;
        initializeCache();
    }

    private void initializeCache() {
        CacheLoader<Credentials, List<String>> loader = new CacheLoader<Credentials, List<String>>() {
            public List<String> load(Credentials key) {
                return new ArrayList<String>();
            }
        };
        this.cache = CacheBuilder
                .newBuilder()
                .maximumSize(configuration.getCacheSize())
                .expireAfterWrite(configuration.getCacheRefreshRate(),
                        TimeUnit.SECONDS).build(loader);
    }

    @Override
    protected boolean authenticate(Request request, Response response) {
        Credentials credentials = new Credentials(request
                .getChallengeResponse().getIdentifier(), request
                .getChallengeResponse().getSecret());
        if (cache.asMap().containsKey(credentials)) {
            return true;
        } else {
            AuthenticationClientResource cr = new AuthenticationClientResource(
                    apisparkEndpoint, username, new String(password));
            try {
                List<String> roles = cr.getRoles(new Credentials(request
                        .getChallengeResponse().getIdentifier(), request
                        .getChallengeResponse().getSecret()));
                if (roles != null) {
                    cache.put(credentials, roles);
                    return true;
                }
            } catch (ResourceException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
