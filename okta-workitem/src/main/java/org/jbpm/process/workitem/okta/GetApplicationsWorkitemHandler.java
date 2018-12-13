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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.application.ApplicationList;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.jbpm.process.workitem.okta.model.OktaApplication;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;

@Wid(widfile = "OktaGetApplications.wid", name = "OktaGetApplications",
        displayName = "OktaGetApplications",
        defaultHandler = "mvel: new org.jbpm.process.workitem.okta.GetApplicationsWorkitemHandler(\"apiToken\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        parameters = {
                @WidParameter(name = "AppIds")
        },
        results = {
                @WidResult(name = "Applications", runtimeType = "java.util.List")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "okta,auth,application,get",
                action = @WidAction(title = "Get applications from Okta"),
                authinfo = @WidAuth(required = true, params = {"apiToken"},
                        paramsdescription = {"Okta api token"},
                        referencesite = "https://developer.okta.com/")
        ))
public class GetApplicationsWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private Client oktaClient;
    private OktaAuth auth = new OktaAuth();
    private static final String RESULTS_VALUE = "Applications";

    public GetApplicationsWorkitemHandler() throws Exception {
        try {
            oktaClient = auth.authorize();
        } catch (Exception e) {
            throw new IllegalAccessException("Unable to authenticate with Okta: " + e.getMessage());
        }
    }

    public GetApplicationsWorkitemHandler(String apiToken) throws Exception {
        try {
            oktaClient = auth.authorize(apiToken);
        } catch (Exception e) {
            throw new IllegalAccessException("Unable to authenticate with Okta: " + e.getMessage());
        }
    }

    public GetApplicationsWorkitemHandler(Client oktaClient) {
        this.oktaClient = oktaClient;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String appids = (String) workItem.getParameter("AppIds");

            List<OktaApplication> retAppList = new ArrayList<>();
            ApplicationList appList = oktaClient.listApplications();

            if (appids != null && appids.length() > 0) {
                List<String> appidsList = Arrays.asList(appids.split(","));
                appList.stream()
                        .filter(app -> appidsList.contains(app.getId()))
                        .forEach(app -> retAppList.add(new OktaApplication(app)));
            } else {
                // get all
                appList.stream().forEach(app -> retAppList.add(new OktaApplication(app)));
            }

            Map<String, Object> results = new HashMap<>();
            results.put(RESULTS_VALUE,
                        retAppList);

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
