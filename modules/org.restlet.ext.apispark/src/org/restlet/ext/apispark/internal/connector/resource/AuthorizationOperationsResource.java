package org.restlet.ext.apispark.internal.connector.resource;

import org.restlet.ext.apispark.internal.connector.bean.OperationsAuthorization;
import org.restlet.resource.Get;

public interface AuthorizationOperationsResource {

    @Get
    public OperationsAuthorization getAuthorizations();
}
