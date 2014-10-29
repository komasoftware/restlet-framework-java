package org.restlet.ext.apispark.internal.connector.bean;

import org.restlet.data.Method;

import java.util.List;

/**
 * @author Manuel Boillod
 */
public class OperationAuthorization {

    Method method;

    /**
     *  The URI path template that must match the relative part of the
     *  resource URI.
     */
    String pathTemplate;

    List<String> groupsAllowed;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getPathTemplate() {
        return pathTemplate;
    }

    public void setPathTemplate(String pathTemplate) {
        this.pathTemplate = pathTemplate;
    }

    public List<String> getGroupsAllowed() {
        return groupsAllowed;
    }

    public void setGroupsAllowed(List<String> groupsAllowed) {
        this.groupsAllowed = groupsAllowed;
    }
}
