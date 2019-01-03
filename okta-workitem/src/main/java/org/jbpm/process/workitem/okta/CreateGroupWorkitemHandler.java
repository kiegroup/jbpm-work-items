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

import java.util.HashMap;
import java.util.Map;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.group.Group;
import com.okta.sdk.resource.group.GroupBuilder;
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

@Wid(widfile = "OktaCreateGroup.wid", name = "OktaCreateGroup",
        displayName = "OktaCreateGroup",
        defaultHandler = "mvel: new org.jbpm.process.workitem.okta.CreateGroupWorkitemHandler(\"apiToken\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "icon.png",
        parameters = {
                @WidParameter(name = "GroupName", required = true),
                @WidParameter(name = "GroupDescription", required = true)
        },
        results = {
                @WidResult(name = "GroupId")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "okta,auth,group,create",
                action = @WidAction(title = "Create new group to Okta"),
                authinfo = @WidAuth(required = true, params = {"apiToken"},
                        paramsdescription = {"Okta api token"},
                        referencesite = "https://developer.okta.com/")
        ))
public class CreateGroupWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private Client oktaClient;
    private OktaAuth auth = new OktaAuth();
    private static final String RESULTS_VALUE = "GroupId";

    public CreateGroupWorkitemHandler() throws Exception {
        try {
            oktaClient = auth.authorize();
        } catch (Exception e) {
            throw new IllegalAccessException("Unable to authenticate with Okta: " + e.getMessage());
        }
    }

    public CreateGroupWorkitemHandler(String apiToken) throws Exception {
        try {
            oktaClient = auth.authorize(apiToken);
        } catch (Exception e) {
            throw new IllegalAccessException("Unable to authenticate with Okta: " + e.getMessage());
        }
    }

    public CreateGroupWorkitemHandler(Client oktaClient) {
        this.oktaClient = oktaClient;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String groupName = (String) workItem.getParameter("GroupName");
            String groupDescription = (String) workItem.getParameter("GroupDescription");

            Group group = GroupBuilder.instance()
                    .setName(groupName)
                    .setDescription(groupDescription).buildAndCreate(oktaClient);

            Map<String, Object> results = new HashMap<>();
            results.put(RESULTS_VALUE,
                        group.getId());

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }
}
