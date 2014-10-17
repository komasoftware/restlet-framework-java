package org.restlet.ext.apispark.connector.resource;

import java.util.List;

import org.restlet.ext.apispark.connector.Credentials;
import org.restlet.resource.Post;

public interface AuthenticationResource {

    @Post
    public List<String> getRoles(Credentials credentials);
}
