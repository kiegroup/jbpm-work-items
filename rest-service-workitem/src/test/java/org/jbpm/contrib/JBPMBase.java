package org.jbpm.contrib;

import org.jbpm.bpmn2.xml.XmlBPMNProcessDumper;
import org.jbpm.process.instance.event.DefaultSignalManagerFactory;
import org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory;
import org.jbpm.runtime.manager.impl.DefaultRegisterableItemsFactory;
import org.jbpm.runtime.manager.impl.SimpleRegisterableItemsFactory;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.workflow.core.WorkflowProcess;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.internal.io.ResourceFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JBPMBase extends JbpmJUnitBaseTestCase {
    public JBPMBase(boolean setupDataSource, boolean sessionPersistence) {
        super(setupDataSource, sessionPersistence);
    }

    protected RuntimeManager createRuntimeManager(WorkflowProcess testProcess) {
        Map<String, ResourceType> resources = Collections.singletonMap("execute-rest.bpmn2", ResourceType.BPMN2);
        WorkflowProcess[] binaryProcesses = { testProcess };
        return createRuntimeManager(Strategy.SINGLETON, resources, binaryProcesses, null);
    }

    protected RuntimeManager createRuntimeManager( WorkflowProcess[] binaryProcesses, String... processes ) {

        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        for (String p : processes) {
            resources.put(p, ResourceType.BPMN2);
        }

        return createRuntimeManager(Strategy.SINGLETON, resources, binaryProcesses, null);
    }

    protected RuntimeManager createRuntimeManager(Strategy strategy, Map<String, ResourceType> resources,
            WorkflowProcess[] binaryProcesses, String identifier) {
        if (manager != null) {
            throw new IllegalStateException("There is already one RuntimeManager active");
        }

        RuntimeEnvironmentBuilder builder = null;
        if (!setupDataSource){
            builder = RuntimeEnvironmentBuilder.Factory.get()
                    .newEmptyBuilder()
                    .addConfiguration("drools.processSignalManagerFactory", DefaultSignalManagerFactory.class.getName())
                    .addConfiguration("drools.processInstanceManagerFactory", DefaultProcessInstanceManagerFactory.class.getName())
                    .registerableItemsFactory(new SimpleRegisterableItemsFactory() {

                        @Override
                        public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime) {
                            Map<String, WorkItemHandler> handlers = new HashMap<String, WorkItemHandler>();
                            handlers.putAll(super.getWorkItemHandlers(runtime));
                            handlers.putAll(customHandlers);
                            return handlers;
                        }

                        @Override
                        public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
                            List<ProcessEventListener> listeners = super.getProcessEventListeners(runtime);
                            listeners.addAll(customProcessListeners);
                            return listeners;
                        }

                        @Override
                        public List<AgendaEventListener> getAgendaEventListeners( RuntimeEngine runtime) {
                            List<AgendaEventListener> listeners = super.getAgendaEventListeners(runtime);
                            listeners.addAll(customAgendaListeners);
                            return listeners;
                        }

                        @Override
                        public List<TaskLifeCycleEventListener> getTaskListeners() {
                            List<TaskLifeCycleEventListener> listeners = super.getTaskListeners();
                            listeners.addAll(customTaskListeners);
                            return listeners;
                        }

                    });

        } else if (sessionPersistence) {
            builder = RuntimeEnvironmentBuilder.Factory.get()
                    .newDefaultBuilder()
                    .entityManagerFactory(getEmf())
                    .registerableItemsFactory(new DefaultRegisterableItemsFactory() {

                        @Override
                        public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime) {
                            Map<String, WorkItemHandler> handlers = new HashMap<String, WorkItemHandler>();
                            handlers.putAll(super.getWorkItemHandlers(runtime));
                            handlers.putAll(customHandlers);
                            return handlers;
                        }

                        @Override
                        public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
                            List<ProcessEventListener> listeners = super.getProcessEventListeners(runtime);
                            listeners.addAll(customProcessListeners);
                            return listeners;
                        }

                        @Override
                        public List<AgendaEventListener> getAgendaEventListeners( RuntimeEngine runtime) {
                            List<AgendaEventListener> listeners = super.getAgendaEventListeners(runtime);
                            listeners.addAll(customAgendaListeners);
                            return listeners;
                        }

                        @Override
                        public List<TaskLifeCycleEventListener> getTaskListeners() {
                            List<TaskLifeCycleEventListener> listeners = super.getTaskListeners();
                            listeners.addAll(customTaskListeners);
                            return listeners;
                        }

                    });
        } else {
            builder = RuntimeEnvironmentBuilder.Factory.get()
                    .newDefaultInMemoryBuilder()
                    .entityManagerFactory(getEmf())
                    .registerableItemsFactory(new DefaultRegisterableItemsFactory() {

                        @Override
                        public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime) {
                            Map<String, WorkItemHandler> handlers = new HashMap<String, WorkItemHandler>();
                            handlers.putAll(super.getWorkItemHandlers(runtime));
                            handlers.putAll(customHandlers);
                            return handlers;
                        }

                        @Override
                        public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
                            List<ProcessEventListener> listeners = super.getProcessEventListeners(runtime);
                            listeners.addAll(customProcessListeners);
                            return listeners;
                        }

                        @Override
                        public List<AgendaEventListener> getAgendaEventListeners( RuntimeEngine runtime) {
                            List<AgendaEventListener> listeners = super.getAgendaEventListeners(runtime);
                            listeners.addAll(customAgendaListeners);
                            return listeners;
                        }

                        @Override
                        public List<TaskLifeCycleEventListener> getTaskListeners() {
                            List<TaskLifeCycleEventListener> listeners = super.getTaskListeners();
                            listeners.addAll(customTaskListeners);
                            return listeners;
                        }

                    });
        }
        builder.userGroupCallback(userGroupCallback);

        for (Map.Entry<String, Object> envEntry : customEnvironmentEntries.entrySet()) {
            builder.addEnvironmentEntry(envEntry.getKey(), envEntry.getValue());
        }

        for (Map.Entry<String, ResourceType> entry : resources.entrySet()) {
            builder.addAsset(ResourceFactory.newClassPathResource(entry.getKey()), entry.getValue());
        }
        if( binaryProcesses!=null ) {
            for( WorkflowProcess wp : binaryProcesses ) {
                builder.addAsset(ResourceFactory.newByteArrayResource(XmlBPMNProcessDumper.INSTANCE.dump(wp).getBytes()), ResourceType.BPMN2);
            }
        }

        return createRuntimeManager(strategy, resources, builder.get(), identifier);
    }

}
