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

package org.restlet.test.ext.jaxrs.services.resources;

import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.restlet.test.ext.jaxrs.services.others.Issue971Object;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Martin Krasser
 */
@Path("test")
public class Issue971Resource {

    @GET
    @Path("971")
    @Produces("text/plain")
    public Issue971Object getIssue971() {
        return new Issue971Object("issue 971 description");
    }


    private static boolean isResourceMethod(Method method) {
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            if (annotation instanceof HttpMethod) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws NoSuchMethodException {
        Method method = new Issue971Resource().getClass().getDeclaredMethod("getIssue971");

        System.out.println(method.getAnnotation(HttpMethod.class));
        System.out.println(method.getAnnotation(GET.class));
        System.out.println(isResourceMethod(method));
    }
}
