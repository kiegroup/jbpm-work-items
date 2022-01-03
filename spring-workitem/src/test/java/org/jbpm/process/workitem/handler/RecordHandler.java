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

package org.jbpm.process.workitem.handler;

import java.util.Map;

import org.jbpm.process.workitem.config.CustomConfig;
import org.kie.api.runtime.process.ProcessContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Configuration
@EnableConfigurationProperties(CustomConfig.class)
@Service
public class RecordHandler implements SpringHandler {

    @Autowired
    CustomConfig customConfig;

    public Map<String, Object> execute(ProcessContext kcontext) {
        // some custom config from the spring context is used in the java process
        // will throw a NPE as Spring Autowiring will not work with construction by reflection
        // https://github.com/kiegroup/jbpm-work-items/blob/main/java-workitem/src/main/java/org/jbpm/process/workitem/handler/JavaHandlerWorkItemHandler.java#L79
        // It should be possible to make a SpringHandlerWorkItemHandler that does a bean lookup instead of creating a new instance
        //customConfig.getName();
        System.out.println(customConfig.getName());

        String employeeId = (String) kcontext.getVariable("employeeId");
        // look up employee in for example db
        // we will just create one here for demo purposes
        Employee employee = new Employee(employeeId,
                                         "krisv");
        kcontext.setVariable("employee",
                             employee);
        return null;
    }
}
