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
package org.jbpm.process.workitem.okta;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.group.Group;
import com.okta.sdk.resource.user.User;
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

@Wid(widfile = "OktaAddUserToGroup.wid", name = "OktaAddUserToGroup",
        displayName = "OktaAddUserToGroup",
        defaultHandler = "mvel: new org.jbpm.process.workitem.okta.AddUserToGroupWorkitemHandler(\"apiToken\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "OktaAddUserToGroup.png",
        parameters = {
                @WidParameter(name = "UserId", required = true),
                @WidParameter(name = "GroupId", required = true)
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "okta,auth,user,add,group",
                action = @WidAction(title = "Add user to group in Okta"),
                authinfo = @WidAuth(required = true, params = {"apiToken"},
                        paramsdescription = {"Okta api token"},
                        referencesite = "https://developer.okta.com/")
        ))
public class AddUserToGroupWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private Client oktaClient;
    private OktaAuth auth = new OktaAuth();

    public AddUserToGroupWorkitemHandler() throws Exception {
        try {
            oktaClient = auth.authorize();
        } catch (Exception e) {
            throw new IllegalAccessException("Unable to authenticate with Okta: " + e.getMessage());
        }
    }

    public AddUserToGroupWorkitemHandler(String apiToken) throws Exception {
        try {
            oktaClient = auth.authorize(apiToken);
        } catch (Exception e) {
            throw new IllegalAccessException("Unable to authenticate with Okta: " + e.getMessage());
        }
    }

    public AddUserToGroupWorkitemHandler(Client oktaClient) {
        this.oktaClient = oktaClient;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String userId = (String) workItem.getParameter("UserId");
            String groupId = (String) workItem.getParameter("GroupId");

            User user = oktaClient.getUser(userId);
            if (user == null) {
                throw new IllegalArgumentException("Not able to find user with id: " + userId);
            }

            Group group = oktaClient.getGroup(groupId);
            if (group != null) {
                throw new IllegalArgumentException("Not able to find group with id: " + groupId);
            }

            user.addToGroup(groupId);

            workItemManager.completeWorkItem(workItem.getId(),
                                             null);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }
}
