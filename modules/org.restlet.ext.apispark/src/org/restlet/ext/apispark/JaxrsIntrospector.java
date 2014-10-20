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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

import org.restlet.data.ChallengeScheme;
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
import org.restlet.ext.apispark.internal.model.Header;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Section;
import org.restlet.ext.apispark.internal.model.Types;
import org.restlet.ext.apispark.internal.reflect.ReflectUtils;
import org.restlet.ext.apispark.internal.utils.IntrospectionUtils;
import org.restlet.ext.apispark.internal.utils.StringUtils;
import org.restlet.representation.Variant;
import scala.actors.threadpool.Arrays;

/**
 * Publish the documentation of a Jaxrs-based Application to the APISpark
 * console.
 *
 * @author Thierry Boileau
 */
public class JaxrsIntrospector extends IntrospectionUtils {

    /** Internal logger. */
    protected static Logger LOGGER = Logger
            .getLogger(JaxrsIntrospector.class.getName());

    private static final String SUFFIX_SERVER_RESOURCE = "ServerResource";
    private static final String SUFFIX_RESOURCE = "Resource";

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
//    private static void addRepresentation(MethodInfo method, FormParam formParam) {
//        if (formParam != null) {
//            // gives an indication of the expected entity
//            RepresentationInfo ri = null;
//            // gives an indication on the kind of representation handled
//            for (RepresentationInfo r : method.getRequest()
//                    .getRepresentations()) {
//                if (r.getMediaType().equals(MediaType.APPLICATION_WWW_FORM)) {
//                    ri = r;
//                    break;
//                }
//            }
//            if (ri == null) {
//                ri = new RepresentationInfo();
//                ri.setIdentifier(method.getMethod().getName() + "Form");
//                ri.setName(method.getMethod().getName());
//                ri.setMediaType(MediaType.APPLICATION_WWW_FORM);
//                method.getRequest().getRepresentations().add(ri);
//            }
//            ParameterInfo pi = new ParameterInfo(formParam.value(),
//                    ParameterStyle.PLAIN, "body parameter: "
//                            + formParam.value());
//            method.getParameters().add(pi);
//        }
//    }
//


    /**
     * Returns an instance of what must be a subclass of {@link Application}.
     * Returns null in case of errors.
     *
     * @param className
     *            The name of the application class.
     * @return An instance of what must be a subclass of {@link Application}.
     */
    public static Application getApplication(
            String className) {
        return ReflectUtils.newInstance(className, Application.class);
    }

    /**
     * Returns a APISpark description of the current application. By default,
     * this method discovers all the resources attached to this application. It
     * can be overridden to add documentation, list of representations, etc.
     *
     *
     * @param collectInfo
     * @param application
     *            The application.
     * @return An application description.
     */
    public static void scanResources(CollectInfo collectInfo, Application application) {
        for (Class<?> clazz : application.getClasses()) {
            scanClazz(collectInfo, clazz);
        }
        for (Object singleton : application.getSingletons()) {
            if (singleton != null) {
                scanClazz(collectInfo, singleton.getClass());
            }
        }
    }

    private static void scanClazz(CollectInfo collectInfo, Class<?> clazz) {
        ClazzInfo clazzInfo = new ClazzInfo();

        // Introduced by Jax-rs 2.0
        // ConstrainedTo ct = clazz.getAnnotation(ConstrainedTo.class);
        // value = RuntimeType.SERVER

        clazzInfo.setClazz(clazz);

        Path path = clazz.getAnnotation(Path.class);
        clazzInfo.setPath(path);

        Consumes consumes = clazz.getAnnotation(Consumes.class);
        clazzInfo.setConsumes(consumes);

        Produces produces = clazz.getAnnotation(Produces.class);
        clazzInfo.setProduces(produces);

        // TODO Do we support encoded annotation?
        // Encoded e = clazz.getAnnotation(Encoded.class);


        //Scan constructor
        Constructor<?>[] constructors = clazz.getConstructors();
        if (constructors.length == 1) {
            scanConstructor(constructors[0], clazzInfo);
        } else if (constructors.length > 1) {
            Constructor<?> selectedConstructor = null;
            int fieldsCount = -1;
            //should select the constructor with the most fields (jaxrs specification)
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterTypes().length > fieldsCount) {
                    selectedConstructor = constructor;
                    fieldsCount = constructor.getParameterTypes().length;
                }
            }
            scanConstructor(selectedConstructor, clazzInfo);
        }

        //Scan Fields
        Field[] fields = ReflectUtils.getAllDeclaredFields(clazz);
        if (fields != null) {
            for (Field field : fields) {
                scanField(field, clazzInfo);
            }
        }

        //todo authentication protocol

        //First scan bean properties methods, then scan resource methods
        List<Method> resourceMethods = new ArrayList<Method>();

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                if (isResourceMethod(method)) {
                    resourceMethods.add(method);
                } else {
                    scanSimpleMethod(method, clazzInfo);
                }
            }
        }

        for (Method resourceMethod : resourceMethods) {
            scanResourceMethod(collectInfo, clazzInfo, resourceMethod);
        }

    }

    private static void scanConstructor(Constructor<?> constructor, ClazzInfo clazzInfo) {

        //Scan parameters
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
        Class[] parameterTypes = constructor.getParameterTypes();
        Type[] genericParameterTypes = constructor.getGenericParameterTypes();

        int i=0;
        for(Annotation[] annotations : parameterAnnotations){

            for(Annotation annotation : annotations){
                String defaultValue = null;

                if(annotation instanceof DefaultValue){
                    defaultValue = ((DefaultValue) annotation).value();
                }
                if(annotation instanceof HeaderParam){
                    Header header = getHeader(parameterTypes[i], genericParameterTypes[i],
                            defaultValue, (HeaderParam) annotation);
                    clazzInfo.addHeader(header);
                }
                if(annotation instanceof PathParam){
                    PathVariable pathVariable = getPathVariable(parameterTypes[i], genericParameterTypes[i],
                            (PathParam) annotation);
                    clazzInfo.addPathVariable(pathVariable);
                }
                if(annotation instanceof QueryParam){
                    QueryParameter queryParameter = getQueryParameter(parameterTypes[i], genericParameterTypes[i],
                            defaultValue, (QueryParam) annotation);
                    clazzInfo.addQueryParameter(queryParameter);
                }
            }
        }
    }

    private static void scan(Annotation[] annotations, Class<?> parameterClass,
            Type parameterType, ApplicationInfo info, clazzInfo resource,
            MethodInfo method, Consumes consumes) {
        // Indicates that this parameter is instantiated from annotation
        boolean valueComputed = false;
        // TODO sounds like there are several level of parameters, be carefull

        for (Annotation annotation : annotations) {
            // Introduced by Jax-rs 2.0
            // BeanParam
            if (annotation instanceof FormParam) {
                valueComputed = true;
                addRepresentation(method, (FormParam) annotation);
            }
// else if (annotation instanceof HeaderParam) {
//                valueComputed = true;
//                String value = ((HeaderParam) annotation).value();
//                ParameterInfo pi = new ParameterInfo(value,
//                        ParameterStyle.HEADER, "header parameter: " + value);
//                method.getParameters().add(pi);
//            } else if (annotation instanceof MatrixParam) {
//                valueComputed = true;
//                String value = ((MatrixParam) annotation).value();
//                ParameterInfo pi = new ParameterInfo(value,
//                        ParameterStyle.MATRIX, "matrix parameter: " + value);
//                method.getParameters().add(pi);
//            } else if (annotation instanceof PathParam) {
//                valueComputed = true;
//                String value = ((PathParam) annotation).value();
//                boolean found = false;
//                for (ParameterInfo p : resource.getParameters()) {
//                    if (p.getName().equals(value)) {
//                        found = true;
//                        break;
//                    }
//                }
//                if (!found) {
//                    ParameterInfo pi = new ParameterInfo(value,
//                            ParameterStyle.TEMPLATE, "Path parameter: " + value);
//                    resource.getParameters().add(pi);
//                }
//
//            } else if (annotation instanceof QueryParam) {
//                valueComputed = true;
//                String value = ((QueryParam) annotation).value();
//                ParameterInfo pi = new ParameterInfo(value,
//                        ParameterStyle.QUERY, "Query parameter: " + value);
//                method.getParameters().add(pi);
//            } else if (annotation instanceof javax.ws.rs.core.Context) {
//                valueComputed = true;
//                javax.ws.rs.core.Context context = (javax.ws.rs.core.Context) annotation;
//                // TODO scan context annotation.
//            }
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

    private static void scanResourceMethod(CollectInfo collectInfo, ClazzInfo clazzInfo,
                                           Method method) {
        // "Path" decides on which resource to put this method
        Path path = method.getAnnotation(Path.class);

        String fullPath = joinPaths(
                collectInfo.getApplicationPath(),
                getPathOrNull(clazzInfo.getPath()),
                getPathOrNull(path));

        String cleanPath = cleanPath(fullPath);

        //add operation
        Operation operation = new Operation();

        operation.setMethod(getResourceMethod(method));

        if (StringUtils.isNullOrEmpty(operation.getName())) {
            LOGGER.warning("Java method " + method
                    .getName() + " has no Method name.");
            operation.setName(method.getName());
        }

        Consumes consumes = method.getAnnotation(Consumes.class);
        if (consumes != null) {
            operation.setConsumes(Arrays.asList(consumes.value()));
        } else if (clazzInfo.getConsumes() != null) {
            operation.setConsumes(Arrays.asList(clazzInfo.getConsumes().value()));
        }

        Produces produces = method.getAnnotation(Produces.class);
        if (produces != null) {
            operation.setProduces(Arrays.asList(produces.value()));
        } else if (clazzInfo.getProduces() != null) {
            operation.setProduces(Arrays.asList(clazzInfo.getProduces().value()));
        }


        //Retrieve a copy of header parameters declared at class level before
        // adding header parameters declared at method level
        Map<String, Header> headers = clazzInfo.getHeaders();
        //Retrieve a copy of path variables declared at class level before
        // adding path variables declared at method level
        Map<String, PathVariable> pathVariables = clazzInfo.getPathVariables();
        //Retrieve a copy of query parameters declared at class level before
        // adding query parameters declared at method level
        Map<String, QueryParameter> queryParameters = clazzInfo.getQueryParameters();

        //Scan method parameters
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        int i=0;
        for(Annotation[] annotations : parameterAnnotations){

            for(Annotation annotation : annotations){
                String defaultValue = null;

                if(annotation instanceof DefaultValue){
                    defaultValue = ((DefaultValue) annotation).value();
                }
                if(annotation instanceof HeaderParam){
                    Header header = getHeader(parameterTypes[i], genericParameterTypes[i],
                            defaultValue, (HeaderParam) annotation);
                    headers.put(header.getName(), header);
                }
                if(annotation instanceof PathParam){
                    PathVariable pathVariable = getPathVariable(parameterTypes[i], genericParameterTypes[i],
                            (PathParam) annotation);
                    pathVariables.put(pathVariable.getName(), pathVariable);
                }
                if(annotation instanceof QueryParam){
                    QueryParameter queryParameter = getQueryParameter(parameterTypes[i], genericParameterTypes[i],
                            defaultValue, (QueryParam) annotation);
                    queryParameters.put(queryParameter.getName(), queryParameter);
                }
            }
        }
        operation.getQueryParameters().addAll(queryParameters.values());

        //todo input payload

        //todo responses (error & success)

        //todo introspector plugin
//        for (IntrospectorPlugin introspectorPlugin : introspectorPlugins) {
//            introspectorPlugin.processOperation(operation, methodAnnotationInfo);
//        }

        Resource resource = collectInfo.getResource(cleanPath);
        if (resource == null) {
            resource = new Resource();
            resource.setResourcePath(cleanPath);

            //set name from class
            String name = clazzInfo.getClazz().getSimpleName();
            if (name.endsWith(SUFFIX_SERVER_RESOURCE) && name.length() > SUFFIX_SERVER_RESOURCE.length()) {
                name = name.substring(0, name.length() - SUFFIX_SERVER_RESOURCE.length());
            }
            if (name.endsWith(SUFFIX_RESOURCE) && name.length() > SUFFIX_RESOURCE.length()) {
                name = name.substring(0, name.length() - SUFFIX_RESOURCE.length());
            }
            resource.setName(name);
            resource.getPathVariables().addAll(pathVariables.values());

            //set section from package
            String sectionName = clazzInfo.getClazz().getPackage().getName();
            resource.getSections().add(sectionName);

            collectInfo.addResource(resource);
        }

        resource.getOperations().add(operation);
    }

    private static boolean isResourceMethod(Method method) {
        return (
                method.getAnnotation(HEAD.class) != null
                ||
                method.getAnnotation(OPTIONS.class) != null
                ||
                method.getAnnotation(GET.class) != null
                ||
                method.getAnnotation(PUT.class) != null
                ||
                method.getAnnotation(POST.class) != null
                ||
                method.getAnnotation(DELETE.class) != null
                ||
                method.getAnnotation(HttpMethod.class) != null
        );
    }

    private static void scanField(Field field, ClazzInfo clazzInfo) {

        Class<?> elementClazz = field.getType();
        Type elementType = field.getGenericType();

        // Introduced by Jax-rs 2.0
        // BeanParam beanparam = field.getAnnotation(BeanParam.class);

        DefaultValue defaultvalue = field.getAnnotation(DefaultValue.class);
        String defaultValueString = defaultvalue != null ? defaultvalue.value() : null;

        // TODO Do we support encoded annotation?
        // Encoded encoded = field.getAnnotation(Encoded.class);

        //TODO Do we support cookie params?
        //CookieParam cookieParam = field.getAnnotation(CookieParam.class);

        //TODO Do we support matrix params?
        //MatrixParam matrixParam = field.getAnnotation(MatrixParam.class);

        FormParam formParam = field.getAnnotation(FormParam.class);
        if (formParam != null) {
            clazzInfo.getFormParams().add(formParam);
        }

        HeaderParam headerParam = field.getAnnotation(HeaderParam.class);
        if (headerParam != null) {
            Header header = getHeader(elementClazz, elementType, defaultValueString, headerParam);
            clazzInfo.addHeader(header);
        }

        PathParam pathParam = field.getAnnotation(PathParam.class);
        if (pathParam != null) {
            PathVariable pathVariable = getPathVariable(elementClazz, elementType, pathParam);
            clazzInfo.addPathVariable(pathVariable);
        }
        QueryParam queryParam = field.getAnnotation(QueryParam.class);
        if (queryParam != null) {
            QueryParameter queryParameter = getQueryParameter(elementClazz, elementType, defaultValueString, queryParam);
            clazzInfo.addQueryParameter(queryParameter);
        }
    }


    private static Header getHeader(Class<?> elementClazz, Type elementType, String defaultValue, HeaderParam headerParam) {
        Header header = new Header();
        header.setName(headerParam.value());
        header.setType(Types.convertPrimitiveType(ReflectUtils.getSimpleClass(elementType)));
        header.setAllowMultiple(ReflectUtils.isListType(elementClazz));
        header.setRequired(false);
        header.setDescription(
                StringUtils.isNullOrEmpty(defaultValue) ?
                        "" :
                        "Value: " + defaultValue);
        header.setDefaultValue(defaultValue);
        return header;
    }

    private static PathVariable getPathVariable(Class<?> elementClazz, Type elementType, PathParam pathParam) {
        PathVariable pathVariable = new PathVariable();
        pathVariable.setName(pathParam.value());
        pathVariable.setType(Types.convertPrimitiveType(ReflectUtils.getSimpleClass(elementType)));
        return pathVariable;
    }

    private static QueryParameter getQueryParameter(Class<?> elementClazz, Type elementType, String defaultValue, QueryParam queryParam) {
        QueryParameter queryParameter = new QueryParameter();
        queryParameter.setName(queryParam.value());
        queryParameter.setType(Types.convertPrimitiveType(ReflectUtils.getSimpleClass(elementType)));
        queryParameter.setAllowMultiple(ReflectUtils.isListType(elementClazz));
        queryParameter.setRequired(false);
        queryParameter.setDescription(
                StringUtils.isNullOrEmpty(defaultValue) ?
                        "" :
                        "Value: " + defaultValue);
        queryParameter.setDefaultValue(defaultValue);
        return queryParameter;
    }

    private static void scanSimpleMethod(Method method, ClazzInfo clazzInfo, CollectInfo collectInfo) {

        Operation operation = new Operation();

        // TODO set documentation?

        for (FormParam formParam : formParams) {
            addRepresentation(mi, formParam);
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

        if (mi.getResponse().getStatuses().isEmpty()) {
            mi.getResponse().getStatuses().add(Status.SUCCESS_OK);
            mi.getResponse().setName("Success");
        }

        // Introduced by Jax-rs 2.0,
        // Context context = method.getAnnotation(Context.class);
    }

    /**
     * Constructor.
     *
     * @param application
     *            An application to introspect.
     */
    public static Definition getDefinition(Application application) {
        Definition definition = new Definition();

        CollectInfo collectInfo = new CollectInfo();


        ApplicationPath applicationPath = application.getClass().getAnnotation(ApplicationPath.class);
        if (applicationPath != null) {
            collectInfo.setApplicationPath(applicationPath.value());
        }
        scanResources(collectInfo, application);

        //todo introspector plugin
//        for (Resource resource : definition.getContract().getResources()) {
//            for (IntrospectorPlugin introspectorPlugin : introspectorPlugins) {
//                introspectorPlugin.processResource(resource, sr);
//            }
//        }


        addEndpoints(application, definition);

        return definition;
    }

    private static void addEndpoints(Application application, Definition definition) {
        ApplicationPath ap = application.getClass().getAnnotation(
                ApplicationPath.class);
        if (ap != null) {
            Endpoint endpoint = new Endpoint(ap.value());
            definition.getEndpoints().add(endpoint);
        }
    }


    private static String getResourceMethod(Method method) {
        if (method.getAnnotation(HEAD.class) != null) {
            return org.restlet.data.Method.HEAD.getName();
        }
        if (method.getAnnotation(OPTIONS.class) != null) {
            return org.restlet.data.Method.OPTIONS.getName();
        }
        if (method.getAnnotation(GET.class) != null) {
            return org.restlet.data.Method.GET.getName();
        }
        if (method.getAnnotation(PUT.class) != null) {
            return org.restlet.data.Method.PUT.getName();
        }
        if (method.getAnnotation(POST.class) != null) {
            return org.restlet.data.Method.POST.getName();
        }
        if (method.getAnnotation(DELETE.class) != null) {
            return org.restlet.data.Method.DELETE.getName();
        }
        if (method.getAnnotation(HttpMethod.class) != null) {
            return method.getAnnotation(HttpMethod.class).value();
        }
        //not a resource method
        return null;
    }

    private static String getPathOrNull(Path path) {
        if (path != null) {
            return path.value();
        } else {
            return null;
        }
    }

    private static String joinPaths(String... nullablePaths) {
        StringBuilder result = new StringBuilder();

        //keep only not null paths
        List<String> paths = new ArrayList<String>();
        for (String path : nullablePaths) {
            if (!StringUtils.isNullOrEmpty(path)) {
                paths.add(path);
            }
        }

        //clean "/" and append paths
        int lastPathIndex = paths.size() - 1;
        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);

            if (!path.startsWith("/")) {
                result.append("/");
            }
            if (i != lastPathIndex && path.endsWith("/")) {
                //remove last "/" if path is not the last one
                result.append(path.substring(0, path.length() - 1));
            } else {
                result.append(path);
            }
        }

        if (result.length() == 0) {
            result.append("/");
        }

        return result.toString();
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

    private static class ClazzInfo {

        private Class<?> clazz;
        private Path path;
        private Consumes consumes;
        private Produces produces;
        private Resource resource;

//        // List of common annotations, defined at the level of the class, or at
//        // the level of the fields.
//        List<FormParam> formParams = new ArrayList<FormParam>();
        private Map<String, Header> headers = new LinkedHashMap<String, Header>();
        private Map<String, PathVariable> pathVariables = new LinkedHashMap<String, PathVariable>();
        private Map<String, QueryParameter> queryParameters = new LinkedHashMap<String, QueryParameter>();


        public Class<?> getClazz() {
            return clazz;
        }

        public void setClazz(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Path getPath() {
            return path;
        }

        public void setPath(Path path) {
            this.path = path;
        }

        public Consumes getConsumes() {
            return consumes;
        }

        public void setConsumes(Consumes consumes) {
            this.consumes = consumes;
        }

        public Produces getProduces() {
            return produces;
        }

        public void setProduces(Produces produces) {
            this.produces = produces;
        }

        public Resource getResource() {
            return resource;
        }

        public void setResource(Resource resource) {
            this.resource = resource;
        }

        public void addHeader(Header header) {
            headers.put(header.getName(), header);
        }

        public Map<String, Header> getHeaders() {
            return new LinkedHashMap<String, Header>(headers);
        }

        public void addQueryParameter(QueryParameter queryParameter) {
            queryParameters.put(queryParameter.getName(), queryParameter);
        }

        public Map<String, QueryParameter> getQueryParameters() {
            return new LinkedHashMap<String, QueryParameter>(queryParameters);
        }

        public void addPathVariable(PathVariable pathVariable) {
            pathVariables.put(pathVariable.getName(), pathVariable);
        }

        public Map<String, PathVariable> getPathVariables() {
            return new LinkedHashMap<String, PathVariable>(pathVariables);
        }
    }

    public static class CollectInfo {

        private String applicationPath;

        private Map<String, Resource> resourcesByPath = new LinkedHashMap<String, Resource>();

        private List<ChallengeScheme> schemes = new ArrayList<ChallengeScheme>();

        private Map<String, Representation> representations = new HashMap<String, Representation>();

        private Map<String, Section> sections = new HashMap<String, Section>();


        public String getApplicationPath() {
            return applicationPath;
        }

        public void setApplicationPath(String applicationPath) {
            this.applicationPath = applicationPath;
        }

        public List<Resource> getResources() {
            return new ArrayList<Resource>(resources);
        }

        public List<ChallengeScheme> getSchemes() {
            return new ArrayList<ChallengeScheme>(schemes);
        }

        public List<Representation> getRepresentations() {
            return new ArrayList<Representation>(representations.values());
        }

        public List<Section> getSections() {
            return new ArrayList<Section>(sections.values());
        }

        public void addResource(Resource resource) {
            resources.add(resource);
        }

        /**
         * Add scheme if it does not already exist
         *
         * @param scheme
         *            Scheme to add
         * @return true is the collection changed
         */
        public boolean addSchemeIfNotExists(ChallengeScheme scheme) {
            if (!schemes.contains(scheme)) {
                return schemes.add(scheme);
            } else {
                return false;
            }
        }

        public Representation getRepresentation(String identifier) {
            return representations.get(identifier);
        }

        public void addRepresentation(Representation representation) {
            representations.put(representation.getIdentifier(), representation);
        }

        public Section getSection(String identifier) {
            return sections.get(identifier);
        }

        public void addSection(Section section) {
            sections.put(section.getName(), section);
        }

        public void setSections(Map<String, Section> sections) {
            this.sections = sections;
        }

        public Resource getResource(String operationPath) {
            return resourcesInfoByPath.get(operationPath);
        }
    }
}