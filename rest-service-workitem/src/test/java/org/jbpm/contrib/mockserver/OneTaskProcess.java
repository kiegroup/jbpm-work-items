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

import org.jbpm.process.core.datatype.impl.type.StringDataType;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author Ryszard Kozmik
 */
public class OneTaskProcess extends GeneratedTestProcessBase {

    public OneTaskProcess() {
        String processName = "oneTaskProcess";;

        RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess("org.jbpm." + processName);
        factory
                // Header
                .name(processName)
                .version("1.0")
                .packageName("org.jbpm")
                .variable("resultA", new StringDataType())
                .variable("serviceATemplate", new StringDataType())
                .variable("mainProcessData", new StringDataType())
                .variable("containerId", new StringDataType())
                // Nodes
                .startNode(1).name("Start").done()

                .subProcessNode(2)
                .name("serviceA")
                .processId("executerest")
                .onEntryAction("mvel", ""
                        + "String serviceATemplate = '{\n"
                        + "    \"callbackUrl\": \"@{system.callbackUrl}\",\n"
                        + "    \"callbackMethod\": \"POST\",\n"
                        + "    \"name\": \"@{processData.username}\"\n"
                        + "}';\n"
                        + "\n"
                        + "kcontext.setVariable(\"serviceATemplate\",serviceATemplate);")
                .inMapping("requestMethod", "\"POST\"")
                .inMapping("requestUrl", "http://localhost:8080/demo-service/service/A?callbackDelay=1")
                .inMapping("requestBody", "serviceATemplate")
                .inMapping("processData", "mainProcessData")
                .inMapping("cancelUrlJsonPointer", "\"/cancelUrl\"")
                .inMapping("containerId", "\"mock\"")
                .outMapping("result", "resultA")
                .done()

                .endNode(3).name("End").done()
                // Connections
                .connection(1, 2)
                .connection(2, 3);
        process = factory.validate().getProcess();
    }
    
}
