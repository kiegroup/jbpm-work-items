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
package org.jbpm.process.workitem.vimeo;

import java.util.HashMap;
import java.util.Map;

import com.clickntap.vimeo.Vimeo;
import com.clickntap.vimeo.VimeoResponse;
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

@Wid(widfile = "UpdateVimeoDefinitions.wid", name = "UpdateVimeo",
        displayName = "UpdateVimeo",
        defaultHandler = "mvel: new org.jbpm.process.workitem.vimeo.UpdateVideoMetadataWorkitemHandler(\"accessToken\")",
        documentation = "${artifactId}/index.html",
        module = "${artifactId}", version = "${version}",
        parameters = {
                @WidParameter(name = "VideoEndpoint", required = true),
                @WidParameter(name = "Name"),
                @WidParameter(name = "Description"),
                @WidParameter(name = "License"),
                @WidParameter(name = "PrivacyView"),
                @WidParameter(name = "PrivacyEmbed"),
                @WidParameter(name = "ReviewLink")
        },
        results = {
                @WidResult(name = "ResponseStatusCode", runtimeType = "java.lang.Integer")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "vimeo,video,update,metadata",
                action = @WidAction(title = "Update existing video metadata"),
                authinfo = @WidAuth(required = true, params = {"accessToken"},
                        paramsdescription = {"Vimeo access token"},
                        referencesite = "https://developer.vimeo.com/api/authentication")
        ))
public class UpdateVideoMetadataWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String accessToken;
    public static final String RESULT = "ResponseStatusCode";

    public UpdateVideoMetadataWorkitemHandler(String accessToken) {
        this.accessToken = accessToken;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {
            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String videoEndPoint = (String) workItem.getParameter("VideoEndpoint");
            String name = (String) workItem.getParameter("Name");
            String description = (String) workItem.getParameter("Description");
            String license = (String) workItem.getParameter("License");
            String privacyView = (String) workItem.getParameter("PrivacyView");
            String privacyEmbed = (String) workItem.getParameter("PrivacyEmbed");
            String reviewLink = (String) workItem.getParameter("ReviewLink");

            Vimeo vimeo = (Vimeo) workItem.getParameter("Vimeo");

            Map<String, Object> results = new HashMap<>();

            if (vimeo == null) {
                vimeo = new Vimeo(accessToken);
            }

            VimeoResponse vimeoResponse = vimeo.updateVideoMetadata(videoEndPoint,
                                                                    name,
                                                                    description,
                                                                    license,
                                                                    privacyView,
                                                                    privacyEmbed,
                                                                    Boolean.parseBoolean(reviewLink));

            results.put(RESULT,
                        vimeoResponse.getStatusCode());

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem workItem,
                              WorkItemManager manager) {
    }
}
