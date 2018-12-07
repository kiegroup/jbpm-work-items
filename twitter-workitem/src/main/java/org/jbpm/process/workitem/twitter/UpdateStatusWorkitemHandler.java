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
package org.jbpm.process.workitem.twitter;

import java.io.ByteArrayInputStream;

import org.apache.commons.io.FilenameUtils;
import org.jbpm.document.Document;
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
import twitter4j.StatusUpdate;
import twitter4j.Twitter;

@Wid(widfile = "TwitterUpdateStatus.wid", name = "TwitterUpdateStatus",
        displayName = "TwitterUpdateStatus",
        defaultHandler = "mvel: new org.jbpm.process.workitem.twitter.UpdateStatusWorkitemHandler(\"consumerKey\", \"consumerSecret\", \"accessKey\", \"accessSecret\")",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "StatusUpdate", required = true),
                @WidParameter(name = "Media", runtimeType = "org.jbpm.document.Document"),
                @WidParameter(name = "DebugEnabled")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "twitter,tweet,update,status,message",
                action = @WidAction(title = "Update your Twitter status"),
                authinfo = @WidAuth(required = true, params = {"consumerKey", "consumerSecret", "accessKey", "accessSecret"},
                        paramsdescription = {"Twitter consumer key", "Twitter consumer secret", "Twitter access key", "Twitter access secret"},
                        referencesite = "https://developer.twitter.com/en/docs/basics/authentication/guides/access-tokens.html")
        ))
public class UpdateStatusWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpdateStatusWorkitemHandler.class);

    private TwitterAuth auth = new TwitterAuth();

    private String consumerKey;
    private String consumerSecret;
    private String accessKey;
    private String accessSecret;
    private StatusUpdate statusUpdate;

    public UpdateStatusWorkitemHandler(String consumerKey,
                                       String consumerSecret,
                                       String accessKey,
                                       String accessSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessKey = accessKey;
        this.accessSecret = accessSecret;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String statusMessage = (String) workItem.getParameter("StatusUpdate");

            // media is optional
            Document statusMedia = null;
            if (workItem.getParameter("Media") != null) {
                statusMedia = (Document) workItem.getParameter("Media");
            }

            // debug is optional (default to false)
            boolean debugOption = false;
            if (workItem.getParameter("DebugEnabled") != null) {
                debugOption = Boolean.parseBoolean((String) workItem.getParameter("DebugEnabled"));
            }

            Twitter twitter = auth.getTwitterService(this.consumerKey,
                                                     this.consumerSecret,
                                                     this.accessKey,
                                                     this.accessSecret,
                                                     debugOption);

            statusUpdate = new StatusUpdate(statusMessage);
            if (statusMedia != null) {

                statusUpdate.setMedia(FilenameUtils.getBaseName(statusMedia.getName()) +
                                              "." + FilenameUtils.getExtension(statusMedia.getName()),
                                      new ByteArrayInputStream(statusMedia.getContent()));
            }

            twitter.updateStatus(statusUpdate);

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
    public void setAuth(TwitterAuth auth) {
        this.auth = auth;
    }

    // for testing
    public StatusUpdate getStatusUpdate() {
        return this.statusUpdate;
    }
}
