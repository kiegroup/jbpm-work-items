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

package org.jbpm.process.workitem.java;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;

@Wid(widfile = "JavaInvocationDefinitions.wid", name = "JavaInvocation",
        displayName = "JavaInvocation",
        defaultHandler = "mvel: new org.jbpm.process.workitem.java.JavaInvocationWorkItemHandler()",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "JavaInvocation.png",
        parameters = {
                @WidParameter(name = "Class", required = true),
                @WidParameter(name = "Method"),
                @WidParameter(name = "Object", runtimeType = "java.lang.Object"),
                @WidParameter(name = "ParameterTypes", runtimeType = "java.util.List"),
                @WidParameter(name = "Parameters", runtimeType = "java.util.List")
        },
        results = {
                @WidResult(name = "Result", runtimeType = "java.lang.Object")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "java,class,execute,invoke",
                action = @WidAction(title = "Execute a method on a Java class")
        ))
public class JavaInvocationWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    @SuppressWarnings("unchecked")
    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager manager) {
        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String className = (String) workItem.getParameter("Class");
            String methodName = (String) workItem.getParameter("Method");
            Object object = workItem.getParameter("Object");
            List<String> paramTypes = (List<String>) workItem.getParameter("ParameterTypes");
            List<Object> params = (List<Object>) workItem.getParameter("Parameters");
            Object result;

            Class<?> c = Class.forName(className);
            Class<?>[] classes = null;
            Method method = null;
            if (params == null) {
                params = new ArrayList<>();
            }
            if (paramTypes == null) {
                classes = new Class<?>[0];
                try {
                    method = c.getMethod(methodName,
                                         classes);
                } catch (NoSuchMethodException e) {
                    for (Method m : c.getMethods()) {
                        if (m.getName().equals(methodName)
                                && (m.getParameterTypes().length == params.size())) {
                            method = m;
                            break;
                        }
                    }
                    if (method == null) {
                        throw new NoSuchMethodException(className + "." + methodName + "(..)");
                    }
                }
            } else {
                List<Class<?>> classesList = new ArrayList<Class<?>>();
                for (String paramType : paramTypes) {
                    classesList.add(Class.forName(paramType));
                }
                classes = classesList.toArray(new Class<?>[classesList.size()]);
                method = c.getMethod(methodName,
                                     classes);
            }
            if (!Modifier.isStatic(method.getModifiers())) {
                if (object == null) {
                    object = c.newInstance();
                }
            }
            result = method.invoke(object,
                                   params.toArray());
            Map<String, Object> results = new HashMap<>();
            results.put("Result",
                        result);
            manager.completeWorkItem(workItem.getId(),
                                     results);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem arg0,
                              WorkItemManager arg1) {
    }
}
