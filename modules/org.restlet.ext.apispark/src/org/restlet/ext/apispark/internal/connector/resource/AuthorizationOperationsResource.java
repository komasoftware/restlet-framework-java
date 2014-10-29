package org.restlet.ext.apispark.internal.connector.resource;

import org.restlet.ext.apispark.internal.connector.bean.OperationAuthorization;
import org.restlet.resource.Get;

import java.util.List;

public interface AuthorizationOperationsResource {

    //todo query param is a path variable ?
    @Get("?cellId&cellVersion")
    public List<OperationAuthorization> getAuthorizations();
}
