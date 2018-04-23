/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import org.apache.commons.lang3.StringUtils;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.DirectMessage;
import twitter4j.Twitter;

@Wid(widfile = "TwitterSendDirectMessage.wid", name = "TwitterSendDirectMessage",
        displayName = "TwitterSendDirectMessage",
        defaultHandler = "mvel: new org.jbpm.process.workitem.twitter.SendDirectMessageWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "Message"),
                @WidParameter(name = "ScreenName"),
                @WidParameter(name = "DebugEnabled")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        })
public class SendDirectMessageWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpdateStatusWorkitemHandler.class);

    private TwitterAuth auth = new TwitterAuth();

    private String consumerKey;
    private String consumerSecret;
    private String accessKey;
    private String accessSecret;
    private DirectMessage directMessage;

    public SendDirectMessageWorkitemHandler(String consumerKey,
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

        String message = (String) workItem.getParameter("Message");
        String screenName = (String) workItem.getParameter("ScreenName");

        // debug is optional (default to false)
        boolean debugOption = false;
        if (workItem.getParameter("DebugEnabled") != null) {
            debugOption = Boolean.parseBoolean((String) workItem.getParameter("DebugEnabled"));
        }

        if (StringUtils.isNotEmpty(message) && StringUtils.isNotEmpty(screenName)) {
            try {
                Twitter twitter = auth.getTwitterService(this.consumerKey,
                                                         this.consumerSecret,
                                                         this.accessKey,
                                                         this.accessSecret,
                                                         debugOption);

                directMessage = twitter.sendDirectMessage(screenName,
                                                          message);

                workItemManager.completeWorkItem(workItem.getId(),
                                                 null);
            } catch (Exception e) {
                handleException(e);
            }
        } else {
            logger.error("Missing direct message or recipient screen name.");
            throw new IllegalArgumentException("Missing direct message or recipient screen name.");
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
    public DirectMessage getDirectMessage() {
        return this.directMessage;
    }
}
