/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.application;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.service.LogService;
import org.restlet.util.Helper;

import com.noelios.restlet.LogFilter;

/**
 * Application implementation.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ApplicationHelper extends Helper {
    /** The application to help. */
    private Application application;

    /** The first Restlet. */
    private Restlet first;

    /** The last Filter. */
    private Filter last;

    /** The parent context, typically the component's context. */
    private Context parentContext;

    /**
     * Constructor.
     * 
     * @param application
     *            The application to help.
     * @param parentContext
     *            The parent context, typically the component's context.
     */
    public ApplicationHelper(Application application, Context parentContext) {
        this.application = application;
        this.parentContext = parentContext;
        this.first = null;
    }

    /**
     * Creates a new context.
     * 
     * @param loggerName
     *            The JDK's logger name to use for contextual logging.
     * @return The new context.
     */
    public Context createContext(String loggerName) {
        return new ApplicationContext(getApplication(), getParentContext(),
                Logger.getLogger(loggerName));
    }

    /**
     * Allows filtering before processing by the next Restlet. Does nothing by
     * default.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    public void handle(Request request, Response response) {
        if (getFirst() != null) {
            // Set the request's root reference
            request.setRootRef(request.getResourceRef().getBaseRef());

            // Dispatch the call to the first Restlet
            getFirst().handle(request, response);
        } else {
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
            getApplication()
                    .getLogger()
                    .log(Level.SEVERE,
                            "The application wasn't properly started, it can't handle calls.");
        }
    }

    /**
     * Returns the application to help.
     * 
     * @return The application to help.
     */
    public Application getApplication() {
        return this.application;
    }

    /**
     * Returns the parent context, typically the component's context.
     * 
     * @return The parent context.
     */
    public Context getParentContext() {
        return this.parentContext;
    }

    /** Start hook. */
    public void start() throws Exception {
        // Addition of tunnel filter
        if (getApplication().getTunnelService().isEnabled()) {
            addFilter(createTunnelFilter(getApplication()));
        }

        // Addition of status pages
        if (getApplication().getStatusService().isEnabled()) {
            addFilter(createStatusFilter(getApplication()));
        }

        // Addition of decoder filter
        if (getApplication().getDecoderService().isEnabled()) {
            addFilter(createDecoderFilter(getApplication()));
        }

        // Attach the Application's root Restlet
        if (getFirst() == null) {
            setFirst(getApplication().getRoot());
        } else {
            getLast().setNext(getApplication().getRoot());
        }
    }

    /**
     * Adds a new filter to the chain.
     * 
     * @param filter
     *            The filter to add.
     */
    private void addFilter(Filter filter) {
        if (getLast() != null) {
            getLast().setNext(filter);
            setLast(filter);
        } else {
            setFirst(filter);
            setLast(filter);
        }
    }

    /**
     * Creates a new log filter. Allows overriding.
     * 
     * @param context
     *            The context.
     * @param logService
     *            The log service descriptor.
     * @return The new log filter.
     */
    protected Filter createLogFilter(Context context, LogService logService) {
        return new LogFilter(context, logService);
    }

    /**
     * Creates a new decoder filter. Allows overriding.
     * 
     * @param application
     *            The parent application.
     * @return The new decoder filter.
     */
    protected Filter createDecoderFilter(Application application) {
        return new DecoderFilter(application.getContext(), true, false);
    }

    /**
     * Creates a new status filter. Allows overriding.
     * 
     * @param application
     *            The parent application.
     * @return The new status filter.
     */
    protected Filter createStatusFilter(Application application) {
        return new ApplicationStatusFilter(application);
    }

    /**
     * Creates a new tunnel filter. Allows overriding.
     * 
     * @param application
     *            The parent application.
     * @return The new tunnel filter.
     */
    protected Filter createTunnelFilter(Application application) {
        return new TunnelFilter(application);
    }

    /** Stop callback. */
    public void stop() throws Exception {

    }

    /**
     * Returns the first Restlet.
     * 
     * @return the first Restlet.
     */
    private Restlet getFirst() {
        return this.first;
    }

    /**
     * Sets the first Restlet.
     * 
     * @param first
     *            The first Restlet.
     */
    private void setFirst(Restlet first) {
        this.first = first;
    }

    /**
     * Returns the last Filter.
     * 
     * @return the last Filter.
     */
    private Filter getLast() {
        return this.last;
    }

    /**
     * Sets the last Filter.
     * 
     * @param last
     *            The last Filter.
     */
    private void setLast(Filter last) {
        this.last = last;
    }

}
