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
package org.jbpm.process.workitem.slack;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;

@Wid(widfile = "SlackAddReminderDefinitions.wid", name = "SlackAddReminder",
        displayName = "SlackAddReminder",
        defaultHandler = "mvel: new org.jbpm.process.workitem.slack.AddReminderWorkitemHandler(\"accessToken\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "icon.png",
        parameters = {
                @WidParameter(name = "ReminderText", required = true),
                @WidParameter(name = "ReminderTime", required = true)
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "slack,reminder,remind,send,message",
                action = @WidAction(title = "Add a reminder to Slack"),
                authinfo = @WidAuth(required = true, params = {"accessToken"},
                        paramsdescription = {"Slack access token"},
                        referencesite = "https://api.slack.com/tokens")
        ))
public class AddReminderWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String accessToken;
    private SlackAuth auth = new SlackAuth();

    public AddReminderWorkitemHandler(String accessToken) {
        this.accessToken = accessToken;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {

            String reminderText = (String) workItem.getParameter("ReminderText");
            String reminderTime = (String) workItem.getParameter("ReminderTime");

            auth.authAddReminderRequest(accessToken,
                                        reminderText,
                                        reminderTime);

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
    public void setAuth(SlackAuth auth) {
        this.auth = auth;
    }
}
