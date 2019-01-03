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
package org.jbpm.process.workitem.google.tasks;

import java.util.Date;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.TaskList;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "GoogleAddTaskDefinitions.wid", name = "GoogleAddTask",
        displayName = "GoogleAddTask",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.tasks.AddTaskWorkitemHandler(\"appName\", \"clentSecret\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "GoogleAddTask.png",
        parameters = {
                @WidParameter(name = "TaskName", required = true),
                @WidParameter(name = "TaskKind")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "google,tasks,add",
                action = @WidAction(title = "Add a new task using Google Tasks"),
                authinfo = @WidAuth(required = true, params = {"appName", "clentSecret"},
                        paramsdescription = {"Google app name", "Google client secret"},
                        referencesite = "https://developers.google.com/tasks/auth")
        ))
public class AddTaskWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(AddTaskWorkitemHandler.class);

    private GoogleTasksAuth auth = new GoogleTasksAuth();
    private String appName;
    private String clientSecret;

    public AddTaskWorkitemHandler(String appName,
                                  String clientSecret) {
        this.appName = appName;
        this.clientSecret = clientSecret;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        String taskName = (String) workItem.getParameter("TaskName");
        String taskKind = (String) workItem.getParameter("TaskKind");

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Tasks service = auth.getTasksService(appName,
                                                 clientSecret);

            TaskList taskList = new TaskList();
            taskList.setTitle(taskName);
            taskList.setId(taskName);
            taskList.setKind(taskKind);
            taskList.setUpdated(new DateTime(new Date()));

            service.tasklists().insert(taskList).execute();

            workItemManager.completeWorkItem(workItem.getId(),
                                             null);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setAuth(GoogleTasksAuth auth) {
        this.auth = auth;
    }
}
