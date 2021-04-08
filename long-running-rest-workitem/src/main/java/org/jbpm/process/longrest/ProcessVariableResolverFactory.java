/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.process.longrest;

import org.jbpm.process.longrest.util.Json;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.mvel2.ScriptRuntimeException;
import org.mvel2.UnresolveablePropertyException;
import org.mvel2.integration.VariableResolver;
import org.mvel2.integration.impl.BaseVariableResolverFactory;
import org.mvel2.integration.impl.SimpleValueResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process instance variable resolver with chaining (nextFactory) support
 *
 * @see org.jbpm.workflow.instance.impl.ProcessInstanceResolverFactory
 */
public class ProcessVariableResolverFactory extends BaseVariableResolverFactory {

    private static final Logger logger = LoggerFactory.getLogger(ProcessVariableResolverFactory.class);

    private final WorkflowProcessInstance processInstance;

    public ProcessVariableResolverFactory(WorkflowProcessInstance processInstance) {
        this.processInstance = processInstance;
        logger.debug("Created new ProcessVariableResolverFactory for processInstance {}.", processInstance.getId());
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
        logger.trace("Is variable {} resolvable in the processInstance {}.", name, processInstance.getId());
        Object processInstanceVariable = null;
        try {
            processInstanceVariable = processInstance.getVariable(name);
        } catch (NullPointerException e) {
            //workaround for NPE in processInstance.getVariable
        }
        if (processInstanceVariable != null) {
            logger.trace("Variable {} is resolvable in the processInstance {}.", name, processInstance.getId());
            return true;
        } else if (nextFactory != null) {
            logger.trace("Variable {} is NOT resolvable in the processInstance {}, searching in the next factory.", name, processInstance.getId());
            return nextFactory.isResolveable(name);
        } else {
            logger.trace("Variable {} is NOT resolvable in the processInstance {}.", name, processInstance.getId());
            return false;
        }
    }

    @Override
    public VariableResolver getVariableResolver(String name) {
        logger.trace("Getting resolver for {} in the processInstance {}.", name, processInstance.getId());
        Object processInstanceVariable = null;
        try {
            processInstanceVariable = processInstance.getVariable(name);
        } catch (NullPointerException e) {
            //workaround for NPE in processInstance.getVariable
        }
        if (processInstanceVariable != null) {
            logger.trace("Returning SimpleValueResolver for {} in the processInstance {}.", name, processInstance.getId());
            return new SimpleValueResolver(Json.escape(processInstanceVariable));
        } else if (nextFactory != null) {
            logger.trace("Looking-up for next variable resolver for {}.", name);
            return nextFactory.getVariableResolver(name);
        }
        throw new UnresolveablePropertyException("Unable to resolve variable '" + name + "'");
    }
}
