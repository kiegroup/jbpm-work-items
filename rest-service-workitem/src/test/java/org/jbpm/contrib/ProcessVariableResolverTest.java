/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.contrib;

import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.mockito.Mockito;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ProcessVariableResolverTest {

    @Test
    public void propertyProvider() {
        WorkflowProcessInstance processInstance = Mockito.mock(WorkflowProcessInstance.class);

        Mockito.when(processInstance.getVariable(Mockito.eq("simple"))).thenReturn("as that");
        Mockito.when(processInstance.getVariable(Mockito.eq("resultA"))).thenReturn("{\"name\":\"Matej\"}");
        Mockito.when(processInstance.getVariable(Mockito.eq("resultB"))).thenReturn("{\"complex\":{\"name\":\"Matej\"}}");
        ProcessVariableResolver processVariableResolver = new ProcessVariableResolver(processInstance);

        Assert.assertEquals("as that", processVariableResolver.get("proc.simple"));
        Assert.assertEquals("Matej", processVariableResolver.get("proc.resultA.name"));
        Assert.assertEquals("{\"name\":\"Matej\"}", processVariableResolver.get("proc.resultB.complex"));
    }
}