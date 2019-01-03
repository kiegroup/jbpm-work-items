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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.UserBuilder;
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

@Wid(widfile = "OktaCreateUser.wid", name = "OktaCreateUser",
        displayName = "OktaCreateUser",
        defaultHandler = "mvel: new org.jbpm.process.workitem.okta.CreateUserWorkitemHandler(\"apiToken\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "icon.png",
        parameters = {
                @WidParameter(name = "UserEmail", required = true),
                @WidParameter(name = "UserFirstName", required = true),
                @WidParameter(name = "UserLastName", required = true),
                @WidParameter(name = "UserActive"),
                @WidParameter(name = "UserGroupIds"),
                @WidParameter(name = "UserLogin"),
                @WidParameter(name = "UserPassword"),
                @WidParameter(name = "UserSecurityQuestion"),
                @WidParameter(name = "UserSecurityAnswer")
        },
        results = {
                @WidResult(name = "UserId")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "okta,auth,user,create",
                action = @WidAction(title = "Create new user to Okta"),
                authinfo = @WidAuth(required = true, params = {"apiToken"},
                        paramsdescription = {"Okta api token"},
                        referencesite = "https://developer.okta.com/")
        ))
public class CreateUserWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private Client oktaClient;
    private OktaAuth auth = new OktaAuth();
    private static final String RESULTS_VALUE = "UserId";

    public CreateUserWorkitemHandler() throws Exception {
        try {
            oktaClient = auth.authorize();
        } catch (Exception e) {
            throw new IllegalAccessException("Unable to authenticate with Okta: " + e.getMessage());
        }
    }

    public CreateUserWorkitemHandler(String apiToken) throws Exception {
        try {
            oktaClient = auth.authorize(apiToken);
        } catch (Exception e) {
            throw new IllegalAccessException("Unable to authenticate with Okta: " + e.getMessage());
        }
    }

    public CreateUserWorkitemHandler(Client oktaClient) {
        this.oktaClient = oktaClient;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String userEmail = (String) workItem.getParameter("UserEmail");
            String userFirstName = (String) workItem.getParameter("UserFirstName");
            String userLastName = (String) workItem.getParameter("UserLastName");
            String userActive = (String) workItem.getParameter("UserActive");
            String userGroupIds = (String) workItem.getParameter("UserGroupIds");
            String userLogin = (String) workItem.getParameter("UserLogin");
            String userPassword = (String) workItem.getParameter("UserPassword");
            String userSecurityQuestion = (String) workItem.getParameter("UserSecurityQuestion");
            String userSecurityAnswer = (String) workItem.getParameter("UserSecurityAnswer");

            UserBuilder userBuilder = UserBuilder.instance()
                    .setEmail(userEmail)
                    .setFirstName(userFirstName)
                    .setLastName(userLastName);

            if (userActive != null) {
                userBuilder = userBuilder.setActive(Boolean.parseBoolean(userActive));
            }

            if (userGroupIds != null) {
                List<String> groupIdsList = Arrays.asList(userGroupIds.split(","));
                for (String grp : groupIdsList) {
                    userBuilder = userBuilder.addGroup(grp);
                }
            }

            if (userLogin != null) {
                userBuilder = userBuilder.setLogin(userLogin);
            }

            if (userPassword != null) {
                userBuilder = userBuilder.setPassword(userPassword.toCharArray());
            }

            if (userSecurityQuestion != null) {
                userBuilder = userBuilder.setSecurityQuestion(userSecurityQuestion);
            }

            if (userSecurityAnswer != null) {
                userBuilder = userBuilder.setSecurityQuestionAnswer(userSecurityAnswer);
            }

            User user = userBuilder.buildAndCreate(oktaClient);

            Map<String, Object> results = new HashMap<>();
            results.put(RESULTS_VALUE,
                        user.getId());

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
