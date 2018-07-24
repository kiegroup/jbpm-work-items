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

import java.io.IOException;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.auth.AuthTestRequest;
import com.github.seratch.jslack.api.methods.request.channels.ChannelsListRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.reminders.RemindersAddRequest;
import com.github.seratch.jslack.api.methods.response.auth.AuthTestResponse;
import com.github.seratch.jslack.api.methods.response.channels.ChannelsListResponse;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.reminders.RemindersAddResponse;

public class SlackAuth {

    private Slack slack = Slack.getInstance();

    public AuthTestResponse authBaseRequest(String accessToken) throws IllegalAccessError, IOException, SlackApiException {
        AuthTestResponse response = slack.methods().
                authTest(AuthTestRequest.builder().token(accessToken).build());
        if (!response.isOk()) {
            throw new IllegalAccessError("Error authenticating: " + response.getError());
        }

        return response;
    }

    public ChannelsListResponse authChannelListRequest(String accessToken) throws IllegalAccessError, IOException, SlackApiException {
        ChannelsListResponse channelListResponse = slack.methods().channelsList(
                ChannelsListRequest.builder().token(accessToken).build());

        if (!channelListResponse.isOk()) {
            throw new IllegalAccessError("Error authenticating channel list: " + channelListResponse.getError());
        }

        return channelListResponse;
    }

    public ChatPostMessageResponse authChatPostMessageRequest(String channelId,
                                                              String accessToken,
                                                              String message) throws IllegalAccessError, IOException, SlackApiException {
        ChatPostMessageResponse chatMessageResponse = slack.methods().chatPostMessage(ChatPostMessageRequest.builder()
                                                                                              .channel(channelId)
                                                                                              .token(accessToken)
                                                                                              .text(message)
                                                                                              .replyBroadcast(false)
                                                                                              .build());

        if (!chatMessageResponse.isOk()) {
            throw new IllegalAccessError("Error authenticating chat message: " + chatMessageResponse.getError());
        }

        return chatMessageResponse;
    }

    public RemindersAddResponse authAddReminderRequest(String accessToken,
                                                       String reminderText,
                                                       String time) throws IllegalAccessError, IOException, SlackApiException {
        RemindersAddResponse addReminderResponse = slack.methods().remindersAdd(RemindersAddRequest.builder()
                                                                                        .token(accessToken)
                                                                                        .text(reminderText)
                                                                                        .time(time)
                                                                                        .build());

        if (!addReminderResponse.isOk()) {
            throw new IllegalAccessError("Error authenticating add reminder message: " + addReminderResponse.getError());
        }

        return addReminderResponse;
    }

    // for testing
    public void setSlack(Slack slack) {
        this.slack = slack;
    }
}
