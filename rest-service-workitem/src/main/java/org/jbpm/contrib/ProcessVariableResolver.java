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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.jboss.util.Strings.isEmpty;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ProcessVariableResolver {

    private final Logger logger = LoggerFactory.getLogger(ProcessVariableResolver.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private WorkflowProcessInstance processInstance;

    public ProcessVariableResolver(WorkflowProcessInstance processInstance) {

        this.processInstance = processInstance;
    }

    /**
     * Each task in the chain should store its result in the process.
     * Results is expected to be in json format stored under parameter with the same name as the task name which create the result.
     * task.taskName.result
     *
     * Replace placeholders with values.
     *
     * Expecting placeholders:
     *
     * ${proc.param1} //process variable
     * ${proc.param2}
     * ${proc.param3.path.to.json.node} //extract node addressed by path form param
     */
    public String get(String key) {

        String[] split = key.split("\\.", 3);
        //proc=process variables
        //task=completed task variable
        String varibleSpace = "";
        String variableName = "";
        String variableNodePath = "";
        if (split.length > 1) {
            varibleSpace = split[0];
            variableName = split[1];
        }
        if (split.length == 3) {
            variableNodePath = split[2];
        }

        if (isEmpty(varibleSpace)) {
            logger.warn("Variable space is empty.");
            return "";
        }
        if (isEmpty(variableName)) {
            logger.warn("Variable name is empty.");
            return "";
        }

        if (varibleSpace.equals("proc")) {
            String value = processInstance.getVariable(variableName).toString();
            if (isEmpty(variableNodePath)) {
                return value;
            } else {
                JsonNode root = null;
                try {
                    root = objectMapper.readTree(value);
                } catch (IOException e) {
                    logger.warn("Cannot parse json tree.", e);
                    return "";
                }
                JsonNode node = root.at("/" + variableNodePath);
                if (!node.isMissingNode()) {
                    if (node.isTextual()) {
                        return node.asText();
                    } else {
                        return node.toString();
                    }
                } else {
                    return "";
                }
            }
        } else {
            logger.warn("Unknown variable space {}.", varibleSpace);
            return "";
        }
    }

}
