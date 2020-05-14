package org.jbpm.contrib.restservice;

import org.apache.commons.lang3.StringEscapeUtils;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.mvel2.ScriptRuntimeException;
import org.mvel2.UnresolveablePropertyException;
import org.mvel2.integration.VariableResolver;
import org.mvel2.integration.impl.BaseVariableResolverFactory;
import org.mvel2.integration.impl.SimpleValueResolver;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Process instance variable resolver with chaining (nextFactory) support
 *
 * @see org.jbpm.workflow.instance.impl.ProcessInstanceResolverFactory
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ProcessVariableResolverFactory extends BaseVariableResolverFactory {

    private final WorkflowProcessInstance processInstance;

    public ProcessVariableResolverFactory(WorkflowProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    @Override
    public VariableResolver createVariable(String name, Object value) {
        throw new ScriptRuntimeException("Cannot assign variables.");
    }

    @Override
    public VariableResolver createVariable(String name, Object value, Class<?> type) {
        throw new ScriptRuntimeException("Cannot assign variables.");
    }

    @Override
    public boolean isTarget(String name) {
        return false;
    }

    @Override
    public boolean isResolveable(String name) {
        Object processInstanceVariable = null;
        try {
            processInstanceVariable = processInstance.getVariable(name);
        } catch (NullPointerException e) {
            //workaround for NPE in processInstance.getVariable
        }
        if (processInstanceVariable != null) {
            return true;
        } else if (nextFactory != null) {
            return nextFactory.isResolveable(name);
        } else {
            return false;
        }
    }

    @Override
    public VariableResolver getVariableResolver(String name) {
        Object processInstanceVariable = null;
        try {
            processInstanceVariable = processInstance.getVariable(name);
        } catch (NullPointerException e) {
            //workaround for NPE in processInstance.getVariable
        }
        if (processInstanceVariable != null) {
            return new SimpleValueResolver(escape(processInstanceVariable));
        } else if (nextFactory != null) {
            return nextFactory.getVariableResolver(name);
        }
        throw new UnresolveablePropertyException("Unable to resolve variable '" + name + "'");
    }

    private Object escape(Object o) {
        if (o instanceof String) {
            return StringEscapeUtils.escapeJson((String) o);
        } else if (o instanceof Map) {
            Map<?, ?> m = (Map)o;
            return m.entrySet().stream()
                    .collect(Collectors.toMap(e -> escape(e.getKey()), e -> escape(e.getValue())));
        } else if (o instanceof List) {
            return ((List<?>) o).stream()
                    .map(this::escape)
                    .collect(Collectors.toList());
        } else if (o instanceof Set) {
            return ((Set<?>) o).stream()
                    .map(this::escape)
                    .collect(Collectors.toSet());
        } else {
            return o;
        }
    }
}
