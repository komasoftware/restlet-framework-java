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

package org.restlet.ext.raml;

import org.raml.emitter.RamlEmitter;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.apispark.internal.introspection.ApplicationIntrospector;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.raml.internal.RamlTranslator;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Restlet that generates RAML documentation in the format defined by the RAML
 * specifications.<br>
 * It helps to generate the documentation for the whole API (set by calling
 * {@link #setApiInboundRoot(Application)} or
 * {@link #setApiInboundRoot(Restlet)} methods.<br>
 * By default it instrospects the chain of Application's routers, filters,
 * restlets.<br>
 * 
 * @author Cyprien Quilici
 * @link http://raml.org/
 * @link http://raml.org/spec.html
 */
public class RamlSpecificationRestlet extends Restlet {

    /** The root Restlet to describe. */
    private Restlet apiInboundRoot;

    /** The version of the API. */
    private String apiVersion;

    /** The Application to describe. */
    private Application application;

    /** The base path of the API. */
    private String basePath;

    /** The definition of the API. */
    private Definition definition;

    /** The version of the supported RAML specifications. */
    private String ramlVersion;

    /**
     * Default constructor.<br>
     */
    public RamlSpecificationRestlet() {
        this(null);
    }

    /**
     * Constructor.<br>
     *
     * @param context
     *            The context.
     */
    public RamlSpecificationRestlet(Context context) {
        super(context);
        ramlVersion = "0.8";
    }

    /**
     * Returns the root Restlet for the given application.
     * 
     * @return The root Restlet for the given application.
     */
    public Restlet getApiInboundRoot() {
        if (apiInboundRoot == null) {
            if (application != null) {
                apiInboundRoot = application.getInboundRoot();
            }
        }

        return apiInboundRoot;
    }

    /**
     * Returns the API's version.
     * 
     * @return The API's version.
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Returns the base path of the API.
     * 
     * @return The base path of the API.
     */
    public String getBasePath() {
        return basePath;
    }

    /**
     * Returns the application's definition.
     * 
     * @return The application's definition.
     */
    private synchronized Definition getDefinition() {
        if (definition == null) {
            synchronized (RamlSpecificationRestlet.class) {
                definition = ApplicationIntrospector.getDefinition(application);
                if (definition.getVersion() == null) {
                    definition.setVersion("1.0");
                }
            }
        }

        return definition;
    }

    /**
     * Returns the representation of the whole resource listing of the
     * Application.
     * 
     * @return The representation of the whole resource listing of the
     *         Application.
     */
    public Representation getRaml() {
        return new StringRepresentation(new RamlEmitter().dump(RamlTranslator
                .getRaml(getDefinition())), MediaType.TEXT_PLAIN);
    }

    /**
     * Returns the version of RAML used to generate this documentation.
     * 
     * @return The version of RAML used to generate this documentation.
     */
    public String getRamlVersion() {
        return ramlVersion;
    }

    @Override
    public void handle(Request request, Response response) {
        if (!Method.GET.equals(request.getMethod())) {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }
        response.setEntity(getRaml());
    }

    /**
     * Sets the root Restlet for the given application.
     * 
     * @param application
     *            The application.
     */
    public void setApiInboundRoot(Application application) {
        this.application = application;
    }

    /**
     * Sets the root Restlet for the given application.
     * 
     * @param apiInboundRoot
     *            The application's root Restlet.
     */
    public void setApiInboundRoot(Restlet apiInboundRoot) {
        this.apiInboundRoot = apiInboundRoot;
    }

    /**
     * Sets the API's version.
     * 
     * @param apiVersion
     *            The API version.
     */
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * Sets the base path of the API.
     * 
     * @param basePath
     *            The base path of the API
     */
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    /**
     * Sets the version of RAML used to generate this documentation.
     * 
     * @param ramlVersion
     *            The version of RAML.
     */
    public void setRamlVersion(String ramlVersion) {
        this.ramlVersion = ramlVersion;
    }

}
