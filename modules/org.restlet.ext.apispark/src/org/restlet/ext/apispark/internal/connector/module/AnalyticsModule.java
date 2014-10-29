package org.restlet.ext.apispark.internal.connector.module;

import org.restlet.Context;
import org.restlet.ext.apispark.ConnectorAgentConfig;
import org.restlet.ext.apispark.internal.connector.config.ModulesSettings;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Filter;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Manuel Boillod
 */
public class AnalyticsModule extends Filter {
    public AnalyticsModule(ConnectorAgentConfig connectorAgentConfig, ModulesSettings modulesSettings) {
        this(connectorAgentConfig, modulesSettings, null);
    }
    public AnalyticsModule(ConnectorAgentConfig connectorAgentConfig, ModulesSettings modulesSettings, Context context) {
        super(context);
    }
}


//    public String post(String request) throws ResourceException, IOException {
//        ClientResource cr = new ClientResource("");
//        String response = cr.post(request).getText();
//        return response;
//    }
//
//    public Future<String> postAsync(final String request) {
//        Future<String> future = getExecutor().submit(new Callable<String>() {
//            public String call() throws Exception {
//                return post(request);
//            }
//        });
//        return future;
//    }
//
//    protected ThreadPoolExecutor getExecutor() {
//        if (executor == null) {
//            executor = createExecutor();
//        }
//        return executor;
//    }
//
//    protected synchronized ThreadPoolExecutor createExecutor() {
//        return new ThreadPoolExecutor(0, configuration.getMaxThreads(), 5,
//                TimeUnit.MINUTES, new LinkedBlockingDeque<Runnable>(),
//                createThreadFactory());
//    }
//
//    protected ApisparkAnalyticsThreadFactory createThreadFactory() {
//        return new ApisparkAnalyticsThreadFactory(
//                configuration.getThreadNameFormat());
//    }
//
//class ApisparkAnalyticsThreadFactory implements ThreadFactory {
//    private final AtomicInteger threadNumber = new AtomicInteger(1);
//
//    private String threadNameFormat = null;
//
//    public ApisparkAnalyticsThreadFactory(String threadNameFormat) {
//        this.threadNameFormat = threadNameFormat;
//    }
//
//    public Thread newThread(Runnable r) {
//        Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
//                r, MessageFormat.format(threadNameFormat,
//                threadNumber.getAndIncrement()), 0);
//        thread.setDaemon(true);
//        thread.setPriority(Thread.MIN_PRIORITY);
//        return thread;
//    }
//}