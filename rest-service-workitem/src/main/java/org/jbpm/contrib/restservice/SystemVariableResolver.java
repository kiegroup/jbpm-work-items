package org.jbpm.contrib.restservice;

import org.jbpm.workflow.instance.impl.ProcessInstanceResolverFactory;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.mvel2.integration.VariableResolver;
import org.mvel2.integration.impl.SimpleValueResolver;

import java.util.Map;

/**
 * Resolve 'system' variables.
 * Eg. to resolve @{system.callbackUrl} provide a map with a 'callbackUrl' key.
 */
public class SystemVariableResolver extends ProcessInstanceResolverFactory {

    private final Map<String, Object> system;

    public SystemVariableResolver(WorkflowProcessInstance processInstance, Map<String, Object> system) {
        super(processInstance);
        this.system = system;
    }

    public boolean isResolveable(String name) {
        return "system".equals(name) || super.isResolveable(name);
    }

    public VariableResolver getVariableResolver(String name) {
        if ("system".equals(name)) {
            return new SimpleValueResolver(system);
        } else {
            return super.getVariableResolver(name);
        }
    }

}
