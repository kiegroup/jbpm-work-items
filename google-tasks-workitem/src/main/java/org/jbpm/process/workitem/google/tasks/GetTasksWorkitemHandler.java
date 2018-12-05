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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "GoogleGetTasksDefinitions.wid", name = "GoogleGetTasks",
        displayName = "GoogleGetTasks",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.tasks.GetTasksWorkitemHandler(\"appName\", \"clentSecret\")",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "NumOfTasks", required = true)
        },
        results = {
                @WidResult(name = "FoundTasks")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "google,tasks,get",
                action = @WidAction(title = "Get an existing task using Google Tasks"),
                authinfo = @WidAuth(required = true, params = {"appName", "clentSecret"},
                        paramsdescription = {"Google app name", "Google client secret"},
                        referencesite = "https://developers.google.com/tasks/auth")
        ))
public class GetTasksWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetTasksWorkitemHandler.class);
    private static final String RESULTS_VALUES = "FoundTasks";

    private GoogleTasksAuth auth = new GoogleTasksAuth();
    private String appName;
    private String clientSecret;

    public GetTasksWorkitemHandler(String appName,
                                   String clientSecret) {
        this.appName = appName;
        this.clientSecret = clientSecret;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Map<String, Object> results = new HashMap<String, Object>();
            Long numOfTasksLong = Long.valueOf((String) workItem.getParameter("NumOfTasks"));
            List<TaskInfo> tasksResultsList = new ArrayList<>();

            if (numOfTasksLong <= 0) {
                logger.error("Number of tasks requested must be greater than zero.");
                throw new IllegalArgumentException("Number of tasks requested must be greater than zero.");
            }

            Tasks service = auth.getTasksService(appName,
                                                 clientSecret);

            TaskLists result = service.tasklists().list().setMaxResults(numOfTasksLong).execute();
            if (result == null) {
                logger.error("Invalid task list result.");
                throw new Exception("Invalid task list result.");
            }

            List<TaskList> tasklist = result.getItems();
            if (tasklist != null) {

                for (TaskList tl : tasklist) {
                    tasksResultsList.add(new TaskInfo(tl));
                }
            }

            results.put(RESULTS_VALUES,
                        tasksResultsList);

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
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
