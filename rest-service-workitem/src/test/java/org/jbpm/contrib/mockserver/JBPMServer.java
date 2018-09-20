package org.jbpm.contrib.mockserver;

import org.jbpm.contrib.restservice.CancelAllActiveTasksWorkitemHandler;
import org.jbpm.contrib.restservice.RestServiceWorkItemHandler;
import org.jbpm.contrib.restservice.TaskTimeoutWorkitemHandler;
import org.jbpm.process.instance.event.DefaultSignalManagerFactory;
import org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory;
import org.jbpm.test.JBPMHelper;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class JBPMServer {

    private static ConcurrentMap<String, JBPMServer> instance = new ConcurrentHashMap<>();

    public static JBPMServer getInstance() {
        return getInstance("default");
    }

    public static JBPMServer getInstance(String key) {
        return instance.computeIfAbsent(key, (k) -> new JBPMServer());
    }

    private RuntimeManager manager;

    public JBPMServer() {
        manager = getInMemmoryRuntimeManager("service-orchestration.bpmn");
//        manager = getPersistentRuntimeManager("service-orchestration.bpmn");

        RuntimeEngine runtime = getRuntimeEngine();
        KieSession ksession = runtime.getKieSession();

        WorkItemManager workItemManager = ksession.getWorkItemManager();
        workItemManager.registerWorkItemHandler("RestServiceWorkItemHandler", new RestServiceWorkItemHandler(manager));
        workItemManager.registerWorkItemHandler("CancelAllActiveTasksWorkitemHandler", new CancelAllActiveTasksWorkitemHandler(manager));
        workItemManager.registerWorkItemHandler("TaskTimeoutWorkitemHandler", new TaskTimeoutWorkitemHandler(manager));
    }

    private static RuntimeManager getInMemmoryRuntimeManager(String processId) {
        RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
                //                .newDefaultInMemoryBuilder()
                .newEmptyBuilder()
                //                .knowledgeBase(KieServices.Factory.get().getKieClasspathContainer().getKieBase("my-knowledge-base"))
                .persistence(false)
                .addAsset(ResourceFactory.newClassPathResource(processId), ResourceType.BPMN2)
                .addConfiguration("drools.processSignalManagerFactory", DefaultSignalManagerFactory.class.getName())
                .addConfiguration("drools.processInstanceManagerFactory", DefaultProcessInstanceManagerFactory.class.getName())
                .get();
        return RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);
    }

    public RuntimeManager getManager() {
        return manager;
    }

    public RuntimeEngine getRuntimeEngine() {
        return manager.getRuntimeEngine(EmptyContext.get());
    }

    /**
     * Required to work with taskService.
     */
    private static RuntimeManager getPersistentRuntimeManager(String processId) {
        // load up the knowledge base
        JBPMHelper.startH2Server();
        JBPMHelper.setupDataSource();

        RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get().newDefaultBuilder()
                  .addAsset(ResourceFactory.newClassPathResource(processId), ResourceType.BPMN2)
//                .addAsset(KieServices.Factory.get().getResources().newClassPathResource(process), ResourceType.BPMN2)
                .get();
        return RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);
    }

    public static void cleanUp() {
//        String home = System.getProperty("user.home");
//        new File(home + "/jbpm-db.h2.db").delete();
//        new File(home + "/jbpm-db.lock.db").delete();
    }
}
