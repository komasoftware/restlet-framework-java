package org.restlet.ext.apispark.internal.connector.module;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.ext.apispark.ConnectorAgentConfig;
import org.restlet.ext.apispark.internal.connector.AgentUtils;
import org.restlet.ext.apispark.internal.connector.ConnectorConfigurationException;
import org.restlet.ext.apispark.internal.connector.bean.OperationAuthorization;
import org.restlet.ext.apispark.internal.connector.config.ModulesSettings;
import org.restlet.ext.apispark.internal.connector.resource.AuthorizationOperationsResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.security.Role;

import java.util.List;
import java.util.logging.Logger;

/**
 * Authorization module for connector agent.
 *
 * @author Manuel Boillod
 */
public class AuthorizationModule extends Filter {

    /** Internal logger. */
    protected static Logger LOGGER = Logger
            .getLogger(AuthorizationModule.class.getName());

    public static final String MODULE_PATH = "/authorization";
    public static final String AUTHORIZATION_OPERATIONS_PATH = MODULE_PATH + "/operations";

    /**
     * Router is used for finding the Operation corresponding to a incoming request.
     */
    private Router router;

    /**
     * Create a new Authorization module with the specified settings.
     * @param connectorAgentConfig
     *          The connector agent configuration.
     * @param modulesSettings
     *          The modules settings.
     */
    public AuthorizationModule(ConnectorAgentConfig connectorAgentConfig, ModulesSettings modulesSettings) {
        this(connectorAgentConfig, modulesSettings, null);
    }

    /**
     * Create a new Authorization module with the specified settings.
     * @param connectorAgentConfig
     *          The connector agent configuration.
     * @param modulesSettings
     *          The modules settings.
     * @param context
     *          The context
     */
    public AuthorizationModule(ConnectorAgentConfig connectorAgentConfig, ModulesSettings modulesSettings, Context context) {
        super(context);

        AuthorizationOperationsResource authorizationOperationsResource = AgentUtils.getConfiguredClientResource(
                connectorAgentConfig, modulesSettings, AuthorizationOperationsResource.class, AUTHORIZATION_OPERATIONS_PATH);

        List<OperationAuthorization> operationAuthorizations;
        try {
            operationAuthorizations = authorizationOperationsResource.getAuthorizations();
        } catch (Exception e) {
            throw new ConnectorConfigurationException("Could not get authorization module configuration from APISpark connector service", e);
        }

        //Initialize the router
        router = new Router();
        for (OperationAuthorization operationAuthorization : operationAuthorizations) {
            router.attach(operationAuthorization.getPathTemplate(), new RestletOperationAuthorization(operationAuthorization));
        }
    }

    /**
     * Find the best {@link OperationAuthorization} for the incoming request and check user authorization.
     *
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return {@link org.restlet.routing.Filter#CONTINUE} if the user is authorized.
     */
    @Override
    protected int beforeHandle(Request request, Response response) {

        //find the corresponding Operation
        RestletOperationAuthorization restletOperationAuthorization =
                (RestletOperationAuthorization)router.getNext(request, response);

        //check route exists
        if (restletOperationAuthorization == null) {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return STOP;
        }

        List<Role> userRoles = request.getClientInfo().getRoles();


        //check that user has at least one authorized role (named group in apispark)
        boolean authorized = false;
        List<String> groupsAllowed = restletOperationAuthorization.getOperationAuthorization().getGroupsAllowed();
        for (String groupAllowed : groupsAllowed) {
            if (hasRole(userRoles, groupAllowed)) {
                authorized = true;
                break;
            }
        }

        if (authorized) {
            return CONTINUE;
        } else {
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return STOP;
        }
    }

    /**
     * Indicates if the given role is in the list of roles.
     * @param roles
     *            The list of roles.
     * @param roleName
     *            The name of the role to look for.
     * @return True if the list of roles contains the given role.
     */
    protected boolean hasRole(List<Role> roles, String roleName) {
        for (Role role : roles) {
            if (role.getName().equals(roleName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Wrap an {@link OperationAuthorization} in a {@link Restlet} class for reuse of
     * {@link Router#getNext(org.restlet.Request, org.restlet.Response)} logic.
     */
    private static class RestletOperationAuthorization extends Restlet {

        private OperationAuthorization operationAuthorization;

        private RestletOperationAuthorization(OperationAuthorization operationAuthorization) {
            this.operationAuthorization = operationAuthorization;
        }

        public OperationAuthorization getOperationAuthorization() {
            return operationAuthorization;
        }

        public void setOperationAuthorization(OperationAuthorization operationAuthorization) {
            this.operationAuthorization = operationAuthorization;
        }
    }
}
