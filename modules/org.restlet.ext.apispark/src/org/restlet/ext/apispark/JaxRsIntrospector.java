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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.apispark.internal.conversion.DefinitionTranslator;
import org.restlet.ext.apispark.internal.info.ApplicationInfo;
import org.restlet.ext.apispark.internal.info.MethodInfo;
import org.restlet.ext.apispark.internal.info.ParameterInfo;
import org.restlet.ext.apispark.internal.info.ParameterStyle;
import org.restlet.ext.apispark.internal.info.RepresentationInfo;
import org.restlet.ext.apispark.internal.info.RequestInfo;
import org.restlet.ext.apispark.internal.info.ResourceInfo;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;
import org.restlet.ext.apispark.internal.reflect.ReflectUtils;
import org.restlet.ext.apispark.internal.utils.IntrospectionUtils;
import org.restlet.representation.Variant;
import org.restlet.routing.Template;

/**
 * Extract and push the web API documentation of a JAX-RS API-based
 * {@link javax.ws.rs.core.Application} to the APISpark console.
 * 
 * @author Thierry Boileau
 */
public class JaxRsIntrospector {

    /** Internal logger. */
    protected static Logger LOGGER = Context.getCurrentLogger();

    /**
     * Completes or creates the "Web form" representation handled by a method,
     * according to the value of the provided {@link FormParam} annotation. Such
     * annotation describes the name of one field of the provided entity.
     * 
     * @param method
     *            The current method.
     * @param formParam
     *            The {@link FormParam} annotation.
     */
    private static void addRepresentation(MethodInfo method, FormParam formParam) {
        if (formParam != null) {
            // gives an indication of the expected entity
            RepresentationInfo ri = null;
            // gives an indication on the kind of representation handled
            for (RepresentationInfo r : method.getRequest()
                    .getRepresentations()) {
                if (r.getMediaType().equals(MediaType.APPLICATION_WWW_FORM)) {
                    ri = r;
                    break;
                }
            }
            if (ri == null) {
                ri = new RepresentationInfo();
                ri.setIdentifier(method.getMethod().getName() + "Form");
                ri.setName(method.getMethod().getName());
                ri.setMediaType(MediaType.APPLICATION_WWW_FORM);
                method.getRequest().getRepresentations().add(ri);
            }
            ParameterInfo pi = new ParameterInfo(formParam.value(),
                    ParameterStyle.PLAIN, "body parameter: "
                            + formParam.value());
            method.getParameters().add(pi);
        }
    }

    /**
     * Returns a clean path (especially variables are cleaned from routing
     * regexp).
     * 
     * @param path
     *            The path to clean.
     * @return The cleand path.
     */
    private static String cleanPath(String path) {
        if (path != null) {
            StringBuilder sb = new StringBuilder();
            char next;
            boolean inVariable = false;
            boolean endVariable = false;
            StringBuilder varBuffer = null;

            for (int i = 0; i < path.length(); i++) {
                next = path.charAt(i);

                if (inVariable) {
                    if (next == '}') {
                        // End of variable detected
                        if (varBuffer.length() == 0) {
                            LOGGER.warning("Empty pattern variables are not allowed : "
                                    + path);
                        } else {
                            sb.append(varBuffer.toString());

                            // Reset the variable name buffer
                            varBuffer = new StringBuilder();
                        }

                        endVariable = false;
                        inVariable = false;
                        sb.append(next);
                    } else if (endVariable) {
                        continue;
                    } else if (Reference.isUnreserved(next)) {
                        // Append to the variable name
                        varBuffer.append(next);
                    } else if (next == ':') {
                        // In this case, the following is the regexp that helps
                        // routing requests
                        // TODO in the future, use the following as roles for
                        // controlling the values of the variables.
                        endVariable = true;
                    }
                } else {
                    sb.append(next);
                    if (next == '{') {
                        inVariable = true;
                        varBuffer = new StringBuilder();
                    } else if (next == '}') {
                        LOGGER.warning("An invalid character was detected inside a pattern variable : "
                                + path);
                    }
                }
            }

            return sb.toString();
        }

        return null;
    }

    /**
     * Returns an instance of what must be a subclass of {@link Application}.
     * Returns null in case of errors.
     * 
     * @param className
     *            The name of the application class.
     * @return An instance of what must be a subclass of {@link Application}.
     */
    protected static Application getApplication(
            String className) {
        Application result = null;

        if (className == null) {
            return result;
        }

        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
            if (Application.class.isAssignableFrom(clazz)) {
                result = (Application) clazz.getConstructor()
                        .newInstance();
            } else {
                LOGGER.log(Level.SEVERE, className
                        + " does not seem to be a valid subclass of "
                        + Application.class.getName() + " class.");
            }
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Cannot locate the application class.", e);
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE,
                    "Cannot instantiate the application class.", e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE,
                    "Cannot instantiate the application class.", e);
        } catch (IllegalArgumentException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Check that the application class has an empty constructor.",
                    e);
        } catch (InvocationTargetException e) {
            LOGGER.log(Level.SEVERE,
                    "Cannot instantiate the application class.", e);
        } catch (NoSuchMethodException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Check that the application class has an empty constructor.",
                    e);
        } catch (SecurityException e) {
            LOGGER.log(Level.SEVERE,
                    "Cannot instantiate the application class.", e);
        }

        return result;
    }

    /**
     * Returns a APISpark description of the current application. By default,
     * this method discovers all the resources attached to this application. It
     * can be overridden to add documentation, list of representations, etc.
     * 
     * @param application
     *            The application.
     * @param baseRef
     *            The base ref.
     * @return An application description.
     */
    protected static ApplicationInfo getApplicationInfo(
            Application application, Reference baseRef) {
        ApplicationInfo applicationInfo = new ApplicationInfo();

        for (Class<?> clazz : application.getClasses()) {
            scan(clazz, applicationInfo, baseRef);
        }
        for (Object singleton : application.getSingletons()) {
            if (singleton != null) {
                scan(singleton.getClass(), applicationInfo, baseRef);
            }
        }
        applicationInfo.getResources().setBaseRef(baseRef);

        return applicationInfo;
    }

    /**
     * Returns the value according to its index.
     * 
     * @param args
     *            The argument table.
     * @param index
     *            The index of the argument.
     * @return The value of the given argument.
     */
    private static String getParameter(String[] args, int index) {
        if (index >= args.length) {
            return null;
        } else {
            String value = args[index];
            if ("-s".equals(value) || "-u".equals(value) || "-p".equals(value)
                    || "-d".equals(value) || "-c".equals(value)) {
                // In case the given value is actually an option, reset it.
                value = null;
            }
            return value;
        }
    }

    private static String getPath(Path rootPath, Path relativePath) {
        return getPath(((rootPath != null) ? rootPath.value() : null),
                ((relativePath != null) ? relativePath.value() : null));
    }

    private static String getPath(String rootPath, String relativePath) {
        String result = null;

        if (rootPath == null) {
            rootPath = "/";
        } else if (!rootPath.startsWith("/")) {
            rootPath = "/" + rootPath;
        }
        if (relativePath == null) {
            result = rootPath;
        } else if (rootPath.endsWith("/")) {
            if (relativePath.startsWith("/")) {
                result = rootPath + relativePath.substring(1);
            } else {
                result = rootPath + relativePath;
            }
        } else {
            if (relativePath.startsWith("/")) {
                result = rootPath + relativePath;
            } else {
                result = rootPath + "/" + relativePath;
            }
        }

        return result;
    }

    private static void scan(Annotation[] annotations, Class<?> parameterClass,
            Type parameterType, ApplicationInfo info, ResourceInfo resource,
            MethodInfo method, Consumes consumes) {
        // Indicates that this parameter is instantiated from annotation
        boolean valueComputed = false;
        // TODO sounds like there are several level of parameters, be carefull

        for (Annotation annotation : annotations) {
            // Introduced by Jax-rs 2.0
            // BeanParam
            if (annotation instanceof CookieParam) {
                valueComputed = true;
                String value = ((CookieParam) annotation).value();
                ParameterInfo pi = new ParameterInfo(value,
                        ParameterStyle.COOKIE, "Cookie parameter: " + value);
                method.getRequest().getParameters().add(pi);
            } else if (annotation instanceof DefaultValue) {
                // TODO Do we support DefaultValue annotation?
                // DefaultValue defaultvalue = (DefaultValue) annotation;
            } else if (annotation instanceof Encoded) {
                // TODO Do we support encoded annotation?
                // Encoded encoded = (Encoded) annotation;
            } else if (annotation instanceof FormParam) {
                valueComputed = true;
                addRepresentation(method, (FormParam) annotation);
            } else if (annotation instanceof HeaderParam) {
                valueComputed = true;
                String value = ((HeaderParam) annotation).value();
                ParameterInfo pi = new ParameterInfo(value,
                        ParameterStyle.HEADER, "header parameter: " + value);
                method.getParameters().add(pi);
            } else if (annotation instanceof MatrixParam) {
                valueComputed = true;
                String value = ((MatrixParam) annotation).value();
                ParameterInfo pi = new ParameterInfo(value,
                        ParameterStyle.MATRIX, "matrix parameter: " + value);
                method.getParameters().add(pi);
            } else if (annotation instanceof PathParam) {
                valueComputed = true;
                String value = ((PathParam) annotation).value();
                boolean found = false;
                for (ParameterInfo p : resource.getParameters()) {
                    if (p.getName().equals(value)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ParameterInfo pi = new ParameterInfo(value,
                            ParameterStyle.TEMPLATE, "Path parameter: " + value);
                    resource.getParameters().add(pi);
                }

            } else if (annotation instanceof QueryParam) {
                valueComputed = true;
                String value = ((QueryParam) annotation).value();
                ParameterInfo pi = new ParameterInfo(value,
                        ParameterStyle.QUERY, "Query parameter: " + value);
                method.getParameters().add(pi);
            } else if (annotation instanceof javax.ws.rs.core.Context) {
                valueComputed = true;
                javax.ws.rs.core.Context context = (javax.ws.rs.core.Context) annotation;
                // TODO scan context annotation.
            }
        }

        if (!valueComputed) {
            // We make the assumption this represents the body...
            if (parameterClass != null && !Void.class.equals(parameterClass)) {
                String[] mediaTypes = null;
                if (consumes == null || consumes.value() == null
                        || consumes.value().length == 0) {
                    // We assume this can't really happen...
                    // Perhaps, we should rely on Produces annotations?
                    mediaTypes = new String[1];
                    mediaTypes[0] = MediaType.APPLICATION_ALL.getName();
                } else {
                    mediaTypes = consumes.value();
                }
                for (String consume : mediaTypes) {
                    Variant variant = new Variant(MediaType.valueOf(consume));
                    RepresentationInfo representationInfo = null;

                    representationInfo = RepresentationInfo.describe(method,
                            parameterClass, parameterType, variant);
                    if (method.getRequest() == null) {
                        method.setRequest(new RequestInfo());
                    }
                    method.getRequest().getRepresentations()
                            .add(representationInfo);
                }
            }
        }
    }

    private static void scan(Class<?> clazz, ApplicationInfo info,
            Reference baseRef) {
        info.getResources().setBaseRef(baseRef);

        // List of common annotations, defined at the level of the class, or at
        // the level of the fields.
        List<CookieParam> cookieParams = new ArrayList<CookieParam>();
        List<FormParam> formParams = new ArrayList<FormParam>();
        List<HeaderParam> headerParams = new ArrayList<HeaderParam>();
        List<MatrixParam> matrixParams = new ArrayList<MatrixParam>();
        List<PathParam> pathParams = new ArrayList<PathParam>();
        List<QueryParam> queryParams = new ArrayList<QueryParam>();
        List<javax.ws.rs.core.Context> contextList = new ArrayList<javax.ws.rs.core.Context>();

        // Introduced by Jax-rs 2.0
        // ConstrainedTo ct = clazz.getAnnotation(ConstrainedTo.class);
        // value = RuntimeType.SERVER

        Consumes c = clazz.getAnnotation(Consumes.class);
        // TODO Do we support encoded annotation?
        // Encoded e = clazz.getAnnotation(Encoded.class);

        Path path = clazz.getAnnotation(Path.class);
        Produces p = clazz.getAnnotation(Produces.class);

        Field[] fields = ReflectUtils.getAllDeclaredFields(clazz);
        if (fields != null) {
            for (Field field : fields) {
                // Apply the values gathered at fields level at the method
                // level.
                scan(field, cookieParams, formParams, headerParams,
                        matrixParams, pathParams, queryParams, contextList);
            }
        }

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                scan(method, info, path, c, p, cookieParams, formParams,
                        headerParams, matrixParams, pathParams, queryParams,
                        contextList);
            }
        }
    }

    private static void scan(Field field, List<CookieParam> cookieParams,
            List<FormParam> formParams, List<HeaderParam> headerParams,
            List<MatrixParam> matrixParams, List<PathParam> pathParams,
            List<QueryParam> queryParams,
            List<javax.ws.rs.core.Context> contextList) {
        // Introduced by Jax-rs 2.0
        // BeanParam beanparam = field.getAnnotation(BeanParam.class);
        CookieParam cookieParam = field.getAnnotation(CookieParam.class);
        if (cookieParam != null) {
            cookieParams.add(cookieParam);
        }

        // TODO handle default value annotation?
        // DefaultValue defaultvalue = field.getAnnotation(DefaultValue.class);

        // TODO Do we support encoded annotation?
        // Encoded encoded = field.getAnnotation(Encoded.class);

        FormParam formParam = field.getAnnotation(FormParam.class);
        if (formParam != null) {
            formParams.add(formParam);
        }

        HeaderParam headerParam = field.getAnnotation(HeaderParam.class);
        if (headerParam != null) {
            headerParams.add(headerParam);
        }

        MatrixParam matrixParam = field.getAnnotation(MatrixParam.class);
        if (matrixParam != null) {
            matrixParams.add(matrixParam);
        }
        PathParam pathParam = field.getAnnotation(PathParam.class);
        if (pathParam != null) {
            pathParams.add(pathParam);
        }
        QueryParam queryParam = field.getAnnotation(QueryParam.class);
        if (queryParam != null) {
            queryParams.add(queryParam);
        }

        javax.ws.rs.core.Context context = field
                .getAnnotation(javax.ws.rs.core.Context.class);
        // TODO hanlde context annotation
    }

    private static void scan(Method method, ApplicationInfo info, Path cPath,
            Consumes cConsumes, Produces cProduces,
            List<CookieParam> cookieParams, List<FormParam> formParams,
            List<HeaderParam> headerParams, List<MatrixParam> matrixParams,
            List<PathParam> pathParams, List<QueryParam> queryParams,
            List<javax.ws.rs.core.Context> contextList) {
        MethodInfo mi = new MethodInfo();
        // TODO set documentation?

        for (FormParam formParam : formParams) {
            addRepresentation(mi, formParam);
        }

        // "Path" decides on which resource to put this method
        Path path = method.getAnnotation(Path.class);
        String fullPath = getPath(cPath, path);
        String cleanPath = cleanPath(fullPath);

        ResourceInfo resource = null;
        for (ResourceInfo ri : info.getResources().getResources()) {
            if (cleanPath.equals(ri.getPath())) {
                resource = ri;
                break;
            }
        }
        if (resource == null) {
            resource = new ResourceInfo();
            // TODO how to set the identifier?
            resource.setIdentifier(cleanPath);
            resource.setPath(cleanPath);
            info.getResources().getResources().add(resource);
        }
        resource.getMethods().add(mi);

        PathParam pathParam = method.getAnnotation(PathParam.class);
        if (pathParam != null) {
            pathParams.add(pathParam);
            ParameterInfo pi = new ParameterInfo(pathParam.value(),
                    ParameterStyle.TEMPLATE, "Path parameter: "
                            + pathParam.value());
            pi.setRequired(true);
            resource.getParameters().add(pi);
        } else {
            // let's check that parameters are rightly specified
            // TODO in the future, don't use the clean path as the full path
            // shows more variables attributes.
            Template template = new Template(cleanPath);
            for (String var : template.getVariableNames()) {
                boolean found = false;
                for (ParameterInfo pi : resource.getParameters()) {
                    if (pi.getStyle().equals(ParameterStyle.TEMPLATE)
                            && var.equals(pi.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ParameterInfo pi = new ParameterInfo(var,
                            ParameterStyle.TEMPLATE, "Path parameter: " + var);
                    pi.setRequired(true);
                    resource.getParameters().add(pi);
                }
            }
        }

        // Introduced by Jax-rs 2.0
        // BeanParam beanparam = method.getAnnotation(BeanParam.class);

        CookieParam cookieParam = method.getAnnotation(CookieParam.class);
        if (cookieParam != null) {
            ParameterInfo pi = new ParameterInfo(cookieParam.value(),
                    ParameterStyle.COOKIE, "Cookie parameter: "
                            + cookieParam.value());
            mi.getParameters().add(pi);
        }
        // TODO Do we support encoded annotation?
        // Encoded encoded = method.getAnnotation(Encoded.class);

        FormParam formParam = method.getAnnotation(FormParam.class);
        addRepresentation(mi, formParam);

        HeaderParam headerParam = method.getAnnotation(HeaderParam.class);
        if (headerParam != null) {
            ParameterInfo pi = new ParameterInfo(headerParam.value(),
                    ParameterStyle.HEADER, "Header parameter: "
                            + cookieParam.value());
            mi.getParameters().add(pi);
        }
        MatrixParam matrixParam = method.getAnnotation(MatrixParam.class);
        if (matrixParam != null) {
            ParameterInfo pi = new ParameterInfo(matrixParam.value(),
                    ParameterStyle.MATRIX, "Matrix parameter: "
                            + cookieParam.value());
            mi.getParameters().add(pi);
        }
        QueryParam queryParam = method.getAnnotation(QueryParam.class);
        if (queryParam != null) {
            ParameterInfo pi = new ParameterInfo(queryParam.value(),
                    ParameterStyle.QUERY, "Query parameter: "
                            + cookieParam.value());
            mi.getParameters().add(pi);
        }

        // TODO do we support default value annotation?
        // DefaultValue defaultvalue = method.getAnnotation(DefaultValue.class);

        DELETE delete = method.getAnnotation(DELETE.class);
        GET get = method.getAnnotation(GET.class);
        HEAD head = method.getAnnotation(HEAD.class);
        OPTIONS options = method.getAnnotation(OPTIONS.class);
        POST post = method.getAnnotation(POST.class);
        PUT put = method.getAnnotation(PUT.class);
        HttpMethod httpMethod = method.getAnnotation(HttpMethod.class);
        if (delete != null) {
            mi.setMethod(org.restlet.data.Method.DELETE);
        } else if (get != null) {
            mi.setMethod(org.restlet.data.Method.GET);
        } else if (head != null) {
            mi.setMethod(org.restlet.data.Method.HEAD);
        } else if (httpMethod != null) {
            mi.setMethod(org.restlet.data.Method.valueOf(httpMethod.value()));
        } else if (options != null) {
            mi.setMethod(org.restlet.data.Method.OPTIONS);
        } else if (post != null) {
            mi.setMethod(org.restlet.data.Method.POST);
        } else if (put != null) {
            mi.setMethod(org.restlet.data.Method.PUT);
        }

        Produces produces = method.getAnnotation(Produces.class);
        if (produces == null) {
            produces = cProduces;
        }

        Class<?> outputClass = method.getReturnType();
        if (produces != null && outputClass != null
                && !Void.class.equals(outputClass)) {
            for (String produce : produces.value()) {
                Variant variant = new Variant(MediaType.valueOf(produce));
                RepresentationInfo representationInfo = null;

                if (javax.ws.rs.core.Response.class
                        .isAssignableFrom(outputClass)) {
                    // We can't interpret such responses, do we try to check the
                    // "Web form" representation?
                    representationInfo = new RepresentationInfo(variant);
                    representationInfo
                            .setType(org.restlet.representation.Representation.class);
                    representationInfo.setIdentifier(representationInfo
                            .getType().getCanonicalName());
                    representationInfo.setName(representationInfo.getType()
                            .getSimpleName());
                    representationInfo.setRaw(true);
                } else {
                    representationInfo = RepresentationInfo
                            .describe(mi, outputClass,
                                    method.getGenericReturnType(), variant);
                }
                mi.getResponse().getRepresentations().add(representationInfo);
            }
        }

        // Cope with the incoming representation
        Consumes consumes = method.getAnnotation(Consumes.class);
        if (consumes == null) {
            consumes = cConsumes;
        }
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class<?>[] parameterTypes = method.getParameterTypes();
        int i = 0;
        for (Annotation[] annotations : parameterAnnotations) {
            Class<?> parameterType = parameterTypes[i];
            scan(annotations, parameterType,
                    method.getGenericParameterTypes()[i], info, resource, mi,
                    consumes);
            i++;
        }
        if (mi.getResponse().getStatuses().isEmpty()) {
            mi.getResponse().getStatuses().add(Status.SUCCESS_OK);
            mi.getResponse().setName("Success");
        }

        // Introduced by Jax-rs 2.0,
        // Context context = method.getAnnotation(Context.class);
    }
//
//    private static void scanAnnotation() {
//        // HttpMethod x
//        // NameBinding x
//    }
//
//    private static void scanConstructor() {
//        // Encoded x
//    }

    /** The current Web API definition. */
    private Definition definition;

    /**
     * Constructor.
     * 
     * @param application
     *            An {@link javax.ws.rs.core.Application} to introspect.
     */
    public JaxRsIntrospector(Application application) {
        ApplicationInfo applicationInfo = getApplicationInfo(application, null);
        definition = DefinitionTranslator.toDefinition(applicationInfo);

        if (definition != null) {
            LOGGER.fine("Look for the endpoint.");
            Endpoint endpoint = null;
            ApplicationPath ap = application.getClass().getAnnotation(
                    ApplicationPath.class);
            if (ap != null) {
                endpoint = new Endpoint(ap.value());
            }
            definition.getEndpoints().add(endpoint);
        }
    }

    /**
     * Returns the current definition.
     * 
     * @return The current definition.
     */
    public Definition getDefinition() {
        return definition;
    }
}