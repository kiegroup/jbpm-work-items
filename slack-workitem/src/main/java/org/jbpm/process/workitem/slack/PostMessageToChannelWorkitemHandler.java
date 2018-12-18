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

import java.util.List;

import com.github.seratch.jslack.api.methods.response.channels.ChannelsListResponse;
import com.github.seratch.jslack.api.model.Channel;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;

@Wid(widfile = "SlackPostMessageToChannelDefinitions.wid", name = "SlackPostMessageToChannel",
        displayName = "SlackPostMessageToChannel",
        defaultHandler = "mvel: new org.jbpm.process.workitem.slack.PostMessageToChannelWorkitemHandler(\"accessToken\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        parameters = {
                @WidParameter(name = "ChannelName", required = true),
                @WidParameter(name = "Message", required = true)
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "slack,message,send,channel",
                action = @WidAction(title = "Send message to a Slack channel"),
                authinfo = @WidAuth(required = true, params = {"accessToken"},
                        paramsdescription = {"Slack access token"},
                        referencesite = "https://api.slack.com/tokens")
        ))
public class PostMessageToChannelWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String accessToken;
    private SlackAuth auth = new SlackAuth();

    public PostMessageToChannelWorkitemHandler(String accessToken) {
        this.accessToken = accessToken;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {
            String channelName = (String) workItem.getParameter("ChannelName");
            String message = (String) workItem.getParameter("Message");
            boolean foundChannel = false;

            ChannelsListResponse channelListResponse = auth.authChannelListRequest(accessToken);
            List<Channel> channelList = channelListResponse.getChannels();
            for (Channel channel : channelList) {
                if (channel.getName().equals(channelName)) {
                    auth.authChatPostMessageRequest(channel.getId(),
                                                    accessToken,
                                                    message);

                    foundChannel = true;
                }
            }

            if (foundChannel) {
                workItemManager.completeWorkItem(workItem.getId(),
                                                 null);
            } else {
                throw new IllegalArgumentException("Unable to find channel: " + channelName);
            }
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
