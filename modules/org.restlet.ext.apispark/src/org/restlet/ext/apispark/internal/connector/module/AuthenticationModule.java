package org.restlet.ext.apispark.internal.connector.module;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;
import org.restlet.ext.apispark.ConnectorAgentConfig;
import org.restlet.ext.apispark.internal.connector.AgentUtils;
import org.restlet.ext.apispark.internal.connector.ConnectorException;
import org.restlet.ext.apispark.internal.connector.bean.Credentials;
import org.restlet.ext.apispark.internal.connector.bean.User;
import org.restlet.ext.apispark.internal.connector.config.AuthenticationSettings;
import org.restlet.ext.apispark.internal.connector.config.ModulesSettings;
import org.restlet.ext.apispark.internal.connector.resource.AuthenticationAuthenticateResource;
import org.restlet.resource.ResourceException;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Role;
import org.restlet.security.Verifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Authentication module for connector agent.
 * This class extends {@link ChallengeAuthenticator} and is responsible
 * to fill {@link org.restlet.data.ClientInfo} on the request.
 *
 * @author Manuel Boillod
 */
public class AuthenticationModule extends ChallengeAuthenticator {

    /** Internal logger. */
    protected static Logger LOGGER = Logger
            .getLogger(AuthenticationModule.class.getName());

    public static final String MODULE_PATH = "/authentication";
    public static final String AUTHENTICATE_PATH = MODULE_PATH + "/authenticate";

    private AuthenticationSettings authenticationSettings;
    private AuthenticationAuthenticateResource authenticateClientResource;
    private LoadingCache<UserIdentifier, UserInfo> userLoadingCache;

    /**
     * Create a new Authentication module with the specified settings.
     * @param connectorAgentConfig
     *          The connector agent configuration.
     * @param modulesSettings
     *          The modules settings.
     */
    public AuthenticationModule(ConnectorAgentConfig connectorAgentConfig, ModulesSettings modulesSettings) {
        this(connectorAgentConfig, modulesSettings, null);
    }

    /**
     * Create a new Authentication module with the specified settings.
     * @param connectorAgentConfig
     *          The connector agent configuration.
     * @param modulesSettings
     *          The modules settings.
     * @param context
     *          The context
     */
    public AuthenticationModule(ConnectorAgentConfig connectorAgentConfig, ModulesSettings modulesSettings, Context context) {
        super(context, ChallengeScheme.HTTP_BASIC, "realm");

        authenticationSettings = new AuthenticationSettings();

        authenticateClientResource = AgentUtils.getConfiguredClientResource(
                connectorAgentConfig, modulesSettings, AuthenticationAuthenticateResource.class, AUTHENTICATE_PATH);


        //config ChallengeAuthenticator
        setOptional(authenticationSettings.isOptional());
        setVerifier(new ConnectorVerifier());

        //Initialize the cache
        initializeCache();
    }

    /**
     * Initialize the user cache and the cache loader instance.
     */
    private void initializeCache() {
        //Cache loader get user from apispark. Never returns null
        CacheLoader<UserIdentifier, UserInfo> userLoader = new CacheLoader<UserIdentifier, UserInfo>() {
            public UserInfo load(UserIdentifier userIdentifier) {
                Credentials credentials = new Credentials(userIdentifier.getIdentifier(), userIdentifier.getSecret());
                User user = authenticateClientResource.authenticate(credentials);
                if (user == null) {
                    //Authentication should throw an error instead of returning null
                    throw new ConnectorException("Authentication should not return null");
                }
                return new UserInfo(user, userIdentifier.getSecret());
            }
        };

        userLoadingCache = CacheBuilder
                .newBuilder()
                .maximumSize(authenticationSettings.getCacheSize())
                .expireAfterWrite(authenticationSettings.getCacheTimeToLiveSeconds(), TimeUnit.SECONDS)
                .build(userLoader);
    }

    private static class UserInfo {
        private User user;
        private char[] secret;

        private UserInfo(User user, char[] secret) {
            this.user = user;
            this.secret = secret;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public char[] getSecret() {
            return secret;
        }

        public void setSecret(char[] secret) {
            this.secret = secret;
        }
    }

    /**
     * This class is used as Cache Key. The {@link #secret} is not used in the
     * key, but the {@link CacheLoader} need it.
     *
     * Warning: The {@link #hashCode()} and {@link #equals(Object)}
     * methods only use the {@link #identifier} attribute.
     * The secret should be compared separately.
     */
    public static class UserIdentifier {

        private String identifier;

        private char[] secret;

        public UserIdentifier(String identifier, char[] secret) {
            this.identifier = identifier;
            this.secret = secret;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public char[] getSecret() {
            return secret;
        }

        public void setSecret(char[] secret) {
            this.secret = secret;
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof UserIdentifier) {
                UserIdentifier userIdentifier = (UserIdentifier) obj;
                return Objects.equals(identifier, userIdentifier.identifier);
            } else {
                return false;
            }

        }
    }


    private class ConnectorVerifier implements Verifier {

        @Override
        public int verify(Request request, Response response) {
                int result;

                if (request.getChallengeResponse() == null) {
                    result = RESULT_MISSING;
                } else {
                    String identifier = request.getChallengeResponse().getIdentifier();
                    char[] secret = request.getChallengeResponse().getSecret();
                    UserIdentifier userIdentifier = new UserIdentifier(identifier, secret);

                    try {
                        //we have to add secret in cache key because cache loader needs the secret.
                        UserInfo userInfo = userLoadingCache.getUnchecked(userIdentifier);
                        if (userInfo == null) {
                            throw new ConnectorException("User could not be null");
                        }
                        //verify password (only after getting user from cache).
                        //See UserIdenfifier javadoc for more details.
                        if (!Arrays.equals(secret, userInfo.getSecret())) {
                            result = RESULT_INVALID;
                        } else {
                            //set user on request client info
                            User user = userInfo.getUser();
                            org.restlet.security.User securityUser = new org.restlet.security.User(identifier, (char[]) null, user.getFirstName(), user.getLastName(), user.getEmail());
                            request.getClientInfo().setUser(securityUser);

                            //set roles on request client info
                            List<Role> securityRoles = new ArrayList<>();
                            Application application = Application.getCurrent();
                            if (user.getRoles() != null) {
                                for (String role : user.getRoles()) {
                                    securityRoles.add(new Role(application, role));
                                }
                            }
                            request.getClientInfo().setRoles(securityRoles);

                            result = RESULT_VALID;
                        }
                    } catch (UncheckedExecutionException e) {
                        if (e.getCause() instanceof ResourceException) {
                            //client resource exception (status 401 is normal)
                            ResourceException rex = (ResourceException) e.getCause();
                            if (Status.CLIENT_ERROR_UNAUTHORIZED.equals(rex.getStatus())) {
                                response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                                result = RESULT_INVALID;
                            } else {
                                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Agent service error during user authentication of user: " + identifier, rex);
                            }
                        } else {
                            throw new ConnectorException("Unexpected error during user authentication error of user: " + identifier, e);
                        }
                    }
                }

                return result;
            }

    }
}
