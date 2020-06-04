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
package org.jbpm.contrib.mockserver;

import org.jbpm.contrib.restservice.Constant;
import org.jbpm.process.core.datatype.impl.type.BooleanDataType;
import org.jbpm.process.core.datatype.impl.type.StringDataType;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.workflow.core.node.Join;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RestTaskFailProcess extends GeneratedTestProcessBase {

    public static final String EXCEPTIONAL_PATH_KEY = "exceptionalPath";

    public RestTaskFailProcess() {
        String processName = "restTaskFailProcess";;

        RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess("org.jbpm." + processName);
        factory
                // Header
                .name(processName)
                .version("1.0")
                .packageName("org.jbpm")
                .variable("resultA", new StringDataType())
                .variable("serviceATemplate", new StringDataType())
                .variable("input", new StringDataType())
                .variable("containerId", new StringDataType())
                .variable(EXCEPTIONAL_PATH_KEY, new BooleanDataType(), false)
                // Nodes
                .startNode(1).name("Start").done()

                .subProcessNode(2)
                .name("serviceA")
                .processId("executerest")
                .onEntryAction("mvel", ""
                        + "String serviceATemplate = '{\n"
                        + "    \"invalid\": \"to make it fail\"\n"
                        + "}';\n"
                        + "\n"
                        + "kcontext.setVariable(\"serviceATemplate\",serviceATemplate);")
                .inMapping("requestMethod", "\"POST\"")
                .inMapping("requestUrl", "http://localhost:8080/demo-service/service/A?callbackDelay=1")
                .inMapping("requestBody", "serviceATemplate")
                .inMapping("containerId", "\"mock\"")
                .outMapping("result", "resultA")
                .done()

                .boundaryEventNode(3)
                .attachedTo(2)
                .eventType("Message-" + Constant.OPERATION_FAILED_SIGNAL_TYPE)
                .cancelActivity(true)
                .done()

                .actionNode(4)
                .action(context -> {
                    context.setVariable(EXCEPTIONAL_PATH_KEY, true);
                })
                .done()

                .joinNode(10)
                .type(Join.TYPE_OR)
                .done()

                .endNode(11).name("End").done()
                // Connections
                .connection(1, 2)
                .connection(2, 10)
//                .connection(2, 10)
                .connection(3, 4)
                .connection(4, 10)
                .connection(10, 11);
        process = factory.validate().getProcess();
    }
    
}
