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

package org.jbpm.process.workitem.transform;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "TransformDefinitions.wid", name = "Transform",
        displayName = "Transform",
        defaultHandler = "mvel: new org.jbpm.process.workitem.transform.TransformWorkItemHandler()",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "InputObject"),
                @WidParameter(name = "OutputType")
        },
        results = {
                @WidResult(name = "OutputObject")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        })
public class TransformWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(TransformWorkItemHandler.class);

    private static String INPUT_KEY = "InputObject";
    private static String OUTPUT_TYPE_KEY = "OutputType";
    private static String VARIABLE_OUTPUT_NAME = "OutputObject";

    // Keyed on the return type of the transformer with a second key of the parameter type
    private Map<Class<?>, Map<Class<?>, Method>> transforms =
            new HashMap<Class<?>, Map<Class<?>, Method>>();

    public void executeWorkItem(WorkItem inputItem,
                                WorkItemManager itemMgr) {
        try {
            Object in = inputItem.getParameter(INPUT_KEY);
            String outputType = (String) inputItem.getParameter(OUTPUT_TYPE_KEY);
            Object output = Class.forName(outputType).newInstance();
            Method txMethod = this.findTransform(output.getClass(),
                                                 in.getClass());

            if (txMethod != null) {
                Object out = txMethod.invoke(null,
                                             in);
                Map<String, Object> result = new HashMap<String, Object>();
                result.put(VARIABLE_OUTPUT_NAME,
                           out);
                itemMgr.completeWorkItem(inputItem.getId(),
                                         result);
            } else {
                logger.error("Failed to find a transform ");
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void registerTransformer(Class<?> transformer) {
        Method[] methods = transformer.getMethods();
        if (methods == null) {
            return;
        }
        for (Method meth : methods) {
            // Only consider methods that have the @Transformer annotation
            if (meth.getAnnotation(Transformer.class) == null) {
                continue;
            }
            Class<?> returnType = meth.getReturnType();
            Class<?> paramType = meth.getParameterTypes()[0];

            Map<Class<?>, Method> index = transforms.get(returnType);
            if (index == null) {
                index = new HashMap<Class<?>, Method>();
                transforms.put(returnType,
                               index);
            }
            index.put(paramType,
                      meth);
        }
    }

    private Method findTransform(Class<?> returnClass,
                                 Class<?> paramClass) {
        Map<Class<?>, Method> indexedTxForm = transforms.get(returnClass);
        if (indexedTxForm == null) {
            return null;
        }
        return indexedTxForm.get(paramClass);
    }

    public void abortWorkItem(WorkItem arg0,
                              WorkItemManager arg1) {
        // Do nothing
    }
}