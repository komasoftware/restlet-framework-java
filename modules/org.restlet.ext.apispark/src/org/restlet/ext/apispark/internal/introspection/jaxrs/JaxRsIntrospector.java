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

package org.restlet.ext.apispark.internal.introspection.jaxrs;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.util.BeanInfoUtils;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.DocumentedApplication;
import org.restlet.ext.apispark.internal.introspection.IntrospectorPlugin;
import org.restlet.ext.apispark.internal.model.Contract;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;
import org.restlet.ext.apispark.internal.model.Header;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.PayLoad;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;
import org.restlet.ext.apispark.internal.model.Section;
import org.restlet.ext.apispark.internal.model.Types;
import org.restlet.ext.apispark.internal.reflect.ReflectUtils;
import org.restlet.ext.apispark.internal.utils.IntrospectionUtils;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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
import javax.ws.rs.core.Context;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Publish the documentation of a Jaxrs-based Application to the APISpark
 * console.
 *
 * @author Thierry Boileau
 */
public class JaxRsIntrospector extends IntrospectionUtils {

    /** Internal logger. */
    protected static Logger LOGGER = Logger
            .getLogger(JaxRsIntrospector.class.getName());

    private static final String SUFFIX_SERVER_RESOURCE = "ServerResource";
    private static final String SUFFIX_RESOURCE = "Resource";

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
     * @param introspectorPlugins
     * @return An application description.
     */
    public static void scanResources(CollectInfo collectInfo, Application application, List<? extends IntrospectorPlugin> introspectorPlugins) {
        for (Class<?> clazz : application.getClasses()) {
            scanClazz(collectInfo, clazz, introspectorPlugins);
        }
        for (Object singleton : application.getSingletons()) {
            if (singleton != null) {
                scanClazz(collectInfo, singleton.getClass(), introspectorPlugins);
            }
        }
    }

    private static void scanClazz(CollectInfo collectInfo, Class<?> clazz,
                                  List<? extends IntrospectorPlugin> introspectorPlugins) {
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

        //First scan bean properties methods ("simple"), then scan resource methods
        List<Method> resourceMethods = new ArrayList<>();

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                if (isResourceMethod(method)) {
                    resourceMethods.add(method);
                } else {
                    scanSimpleMethod(collectInfo, method, clazzInfo);
                }
            }
        }

        for (Method resourceMethod : resourceMethods) {
            scanResourceMethod(collectInfo, clazzInfo, resourceMethod, introspectorPlugins);
        }

    }

    private static void scanConstructor(Constructor<?> constructor, ClazzInfo clazzInfo) {

        //Scan parameters
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
        Class[] parameterTypes = constructor.getParameterTypes();
        Type[] genericParameterTypes = constructor.getGenericParameterTypes();

        scanParameters(clazzInfo, parameterAnnotations, parameterTypes, genericParameterTypes);
    }

    private static void scanParameters(ClazzInfo clazzInfo, Annotation[][] parameterAnnotations, Class[] parameterTypes, Type[] genericParameterTypes) {
        for (int i = 0; i < parameterTypes.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];

            for (Annotation annotation : annotations) {
                String defaultValue = null;

                if (annotation instanceof DefaultValue) {
                    defaultValue = ((DefaultValue) annotation).value();
                }
                if (annotation instanceof HeaderParam) {
                    Header header = getHeader(parameterTypes[i], genericParameterTypes[i],
                            defaultValue, (HeaderParam) annotation);
                    clazzInfo.addHeader(header);
                }
                if (annotation instanceof PathParam) {
                    PathVariable pathVariable = getPathVariable(parameterTypes[i], genericParameterTypes[i],
                            (PathParam) annotation);
                    clazzInfo.addPathVariable(pathVariable);
                }
                if (annotation instanceof QueryParam) {
                    QueryParameter queryParameter = getQueryParameter(parameterTypes[i], genericParameterTypes[i],
                            defaultValue, (QueryParam) annotation);
                    clazzInfo.addQueryParameter(queryParameter);
                }
            }
        }
    }

    private static void scanResourceMethod(CollectInfo collectInfo, ClazzInfo clazzInfo,
                                           Method method, List<? extends IntrospectorPlugin> introspectorPlugins) {
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
        Map<String, Header> headers = clazzInfo.getHeadersCopy();
        //Retrieve a copy of path variables declared at class level before
        // adding path variables declared at method level
        Map<String, PathVariable> pathVariables = clazzInfo.getPathVariablesCopy();
        //Retrieve a copy of query parameters declared at class level before
        // adding query parameters declared at method level
        Map<String, QueryParameter> queryParameters = clazzInfo.getQueryParametersCopy();

        List<Representation> representations = new ArrayList<>();

        //Scan method parameters
        //todo factorize code (OperationInfo create from ClazzInfo)
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();

        for (int i = 0; i < parameterTypes.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];

            for (Annotation annotation : annotations) {
                String defaultValue = null;

                boolean isEntity = true;

                if (annotation instanceof DefaultValue) {
                    defaultValue = ((DefaultValue) annotation).value();
                }
                if (annotation instanceof FormParam) {
                    isEntity = false;
                    addRepresentation(collectInfo, parameterTypes[i], genericParameterTypes[i], introspectorPlugins);
                }
                if (annotation instanceof HeaderParam) {
                    isEntity = false;
                    Header header = getHeader(parameterTypes[i], genericParameterTypes[i],
                            defaultValue, (HeaderParam) annotation);
                    headers.put(header.getName(), header);
                }
                if (annotation instanceof PathParam) {
                    isEntity = false;
                    PathVariable pathVariable = getPathVariable(parameterTypes[i], genericParameterTypes[i],
                            (PathParam) annotation);
                    pathVariables.put(pathVariable.getName(), pathVariable);
                }
                if (annotation instanceof QueryParam) {
                    isEntity = false;
                    QueryParameter queryParameter = getQueryParameter(parameterTypes[i], genericParameterTypes[i],
                            defaultValue, (QueryParam) annotation);
                    queryParameters.put(queryParameter.getName(), queryParameter);
                }
                if (annotation instanceof MatrixParam) {
                    //not supported
                    isEntity = false;
                }
                if (annotation instanceof CookieParam) {
                    //not supported
                    isEntity = false;
                }
                if (annotation instanceof Context) {
                    //not supported
                    isEntity = false;
                }

                //check if the parameter is an entity (no annotation)
                if (isEntity) {
                    addRepresentation(collectInfo, parameterTypes[i],
                            genericParameterTypes[i], introspectorPlugins);

                    PayLoad inputEntity = new PayLoad();
                    inputEntity.setType(Types.convertPrimitiveType(ReflectUtils.getSimpleClass(genericParameterTypes[i])));
                    inputEntity.setArray(ReflectUtils.isListType(parameterTypes[i]));
                    operation.setInputPayLoad(inputEntity);

                }
            }

        }
        operation.getQueryParameters().addAll(queryParameters.values());


        // Describe the success response

        Response response = new Response();
        Class<?> outputClass = method.getReturnType();
        Type outputType = method.getGenericReturnType();

        if (outputClass != Void.TYPE) {
            // Output representation
            addRepresentation(collectInfo, outputClass,
                    outputType, introspectorPlugins);

            PayLoad outputEntity = new PayLoad();
            Class<?> simpleClass = ReflectUtils.getSimpleClass(outputType);
            if (javax.ws.rs.core.Response.class.isAssignableFrom(simpleClass)) {
                outputEntity.setType("file");
            } else {
                outputEntity.setType(Types.convertPrimitiveType(simpleClass));
            }
            outputEntity.setArray(ReflectUtils.isListType(outputClass));

            response.setOutputPayLoad(outputEntity);
        }

        response.setCode(Status.SUCCESS_OK.getCode());
        response.setName("Success");
        response.setDescription("");
        response.setMessage(Status.SUCCESS_OK.getDescription());
        operation.getResponses().add(response);

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

            for (IntrospectorPlugin introspectorPlugin : introspectorPlugins) {
                introspectorPlugin.processResource(resource, clazzInfo.getClazz());
            }
        }

        resource.getOperations().add(operation);

        for (IntrospectorPlugin introspectorPlugin : introspectorPlugins) {
            introspectorPlugin.processOperation(resource, operation, clazzInfo.getClazz(), method);
        }
    }

    private static void addRepresentation(CollectInfo collectInfo, Class<?> clazz, Type type, List<? extends IntrospectorPlugin> introspectorPlugins) {
// Introspect the java class
        Representation representation = new Representation();
        representation.setDescription("");

        Class<?> c = ReflectUtils.getSimpleClass(type);
        Class<?> representationType = (c == null) ? clazz : c;
        boolean generic = c != null
                && !c.getCanonicalName().equals(clazz.getCanonicalName());
        boolean isList = ReflectUtils.isListType(clazz);
        // todo check generics use cases
        if (generic || isList) {
            // Collect generic type
            addRepresentation(collectInfo, representationType,
                    representationType.getGenericSuperclass(), introspectorPlugins);
            return;
        }

        if (Types.isPrimitiveType(representationType)
                || ReflectUtils.isJdkClass(representationType)) {
            // primitives and jdk classes are not collected
            return;
        }

        boolean isFile = org.restlet.representation.Representation.class
                .isAssignableFrom(clazz);

        if (isFile) {
            representation.setIdentifier("file");
            representation.setName("file");
        } else {
            // type is an Entity
            // Example: "java.util.Contact" or "String"
            representation.setIdentifier(Types
                    .convertPrimitiveType(representationType));

            // Sections
            String packageName = clazz.getPackage().getName();
            representation.getSections().add(packageName);
            if (collectInfo.getSection(packageName) == null) {
                collectInfo.addSection(new Section(packageName));
            }
            // Example: "Contact"
            representation.setName(representationType.getSimpleName());
        }
        boolean isRaw = isFile || ReflectUtils.isJdkClass(representationType);
        representation.setRaw(isRaw);

        // at this point, identifier is known - we check if it exists in cache
        boolean notInCache = collectInfo.getRepresentation(representation
                .getIdentifier()) == null;

        if (notInCache) {

            // add representation in cache before complete it to avoid infinite loop
            collectInfo.addRepresentation(representation);

            if (!isRaw) {
                // add properties definition
                BeanInfo beanInfo = BeanInfoUtils.getBeanInfo(representationType);
                for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                    Class<?> propertyClazz = pd.getReadMethod().getReturnType();
                    Type propertyType = pd.getReadMethod().getGenericReturnType();

                    Property property = new Property();
                    property.setName(pd.getName());
                    property.setDescription("");
                    property.setType(Types.convertPrimitiveType(ReflectUtils.getSimpleClass(propertyType)));
                    property.setMinOccurs(0);
                    boolean isCollection = ReflectUtils.isListType(propertyClazz);
                    property.setMaxOccurs(isCollection ? -1 : 1);

                    addRepresentation(collectInfo, propertyClazz,
                            propertyType, introspectorPlugins);

                    for (IntrospectorPlugin introspectorPlugin : introspectorPlugins) {
                        introspectorPlugin.processProperty(property, pd.getReadMethod());
                    }

                    representation.getProperties().add(property);
                }
            }

            for (IntrospectorPlugin introspectorPlugin : introspectorPlugins) {
                introspectorPlugin.processRepresentation(representation, representationType);
            }
        }
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

    private static void scanSimpleMethod(CollectInfo collectInfo, Method method, ClazzInfo clazzInfo) {

        //Scan parameters
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();

        scanParameters(clazzInfo, parameterAnnotations, parameterTypes, genericParameterTypes);
    }

    /**
     * Constructor.
     *
     * @param application
     *            An application to introspect.
     * @param introspectorPlugins
     */
    public static Definition getDefinition(Application application, Reference baseRef,
                                           List<? extends IntrospectorPlugin> introspectorPlugins) {
        // initialize the list to avoid to add a null check statement
        if (introspectorPlugins == null) {
            introspectorPlugins = new ArrayList<>();
        }
        Definition definition = new Definition();

        CollectInfo collectInfo = new CollectInfo();


        ApplicationPath applicationPath = application.getClass().getAnnotation(ApplicationPath.class);
        if (applicationPath != null) {
            collectInfo.setApplicationPath(applicationPath.value());
        }
        scanResources(collectInfo, application, introspectorPlugins);

        updateDefinitionContract(application, definition);

        Contract contract = definition.getContract();
        // add resources
        contract.setResources(collectInfo.getResources());
        // add representations
        contract.setRepresentations(collectInfo.getRepresentations());
        // add sections
        contract.setSections(collectInfo.getSections());

        addEndpoints(application, baseRef, definition);

        sortDefinition(definition);

        updateRepresentationsSectionsFromResources(definition);

        for (IntrospectorPlugin introspectorPlugin : introspectorPlugins) {
            introspectorPlugin.processDefinition(definition, application.getClass());
        }

        return definition;
    }

    private static void updateDefinitionContract(Application application, Definition definition) {
        // Contract
        Contract contract = new Contract();
        contract.setName(application.getClass().getName());

        // Sections
        org.restlet.ext.apispark.internal.introspection.application.CollectInfo collectInfo = new org.restlet.ext.apispark.internal.introspection.application.CollectInfo();
        if (application instanceof DocumentedApplication) {
            DocumentedApplication documentedApplication = (DocumentedApplication) application;
            collectInfo.setSections(documentedApplication.getSections());
        }
        definition.setContract(contract);
    }

    private static void addEndpoints(Application application, Reference baseRef, Definition definition) {
        ApplicationPath ap = application.getClass().getAnnotation(
                ApplicationPath.class);
        if (ap != null) {
            Endpoint endpoint = new Endpoint(ap.value());
            definition.getEndpoints().add(endpoint);
        }
        if (baseRef != null) {
            Endpoint endpoint = new Endpoint(baseRef.getHostDomain(),
                    baseRef.getHostPort(), baseRef.getSchemeProtocol()
                    .getSchemeName(), baseRef.getPath(), null);
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
        List<String> paths = new ArrayList<>();
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
        private Map<String, Header> headers = new LinkedHashMap<>();
        private Map<String, PathVariable> pathVariables = new LinkedHashMap<>();
        private Map<String, QueryParameter> queryParameters = new LinkedHashMap<>();


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

        public Map<String, Header> getHeadersCopy() {
            return new LinkedHashMap<String, Header>(headers);
        }

        public void addQueryParameter(QueryParameter queryParameter) {
            queryParameters.put(queryParameter.getName(), queryParameter);
        }

        public Map<String, QueryParameter> getQueryParametersCopy() {
            return new LinkedHashMap<String, QueryParameter>(queryParameters);
        }

        public void addPathVariable(PathVariable pathVariable) {
            pathVariables.put(pathVariable.getName(), pathVariable);
        }

        public Map<String, PathVariable> getPathVariablesCopy() {
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
            return new ArrayList<Resource>(resourcesByPath.values());
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
            resourcesByPath.put(resource.getResourcePath(), resource);
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
            return resourcesByPath.get(operationPath);
        }
    }
}