package org.restlet.ext.apispark.internal.connector.bean;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Manuel Boillod
 */
public class OperationsAuthorization extends ArrayList<OperationAuthorization> {

    public OperationsAuthorization() {
    }

    public OperationsAuthorization(Collection<? extends OperationAuthorization> c) {
        super(c);
    }
}
