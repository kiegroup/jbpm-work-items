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
package org.jbpm.process.workitem.exec;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;

@Wid(widfile = "ExecDefinitions.wid", name = "Exec",
        displayName = "Exec",
        defaultHandler = "mvel: new org.jbpm.process.workitem.exec.ExecWorkItemHandler()",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "Command", required = true)
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        })
public class ExecWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager manager) {

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String command = (String) workItem.getParameter("Command");

            CommandLine commandLine = CommandLine.parse(command);

            DefaultExecutor executor = new DefaultExecutor();

            executor.execute(commandLine);

            manager.completeWorkItem(workItem.getId(),
                                     null);
        } catch (Throwable t) {
            handleException(t);
        }
    }

    public void abortWorkItem(WorkItem workItem,
                              WorkItemManager manager) {
        // Do nothing, this work item cannot be aborted
    }
}

