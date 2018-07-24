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

import java.util.ArrayList;
import java.util.List;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.response.channels.ChannelsListResponse;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.reminders.RemindersAddResponse;
import com.github.seratch.jslack.api.model.Channel;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.test.AbstractBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SlackWorkitemHandlerTest extends AbstractBaseTest {

    @Mock
    Slack slack;

    @Mock
    MethodsClient methods;

    @Mock
    ChannelsListResponse channelsListResponse;

    @Mock
    ChatPostMessageResponse chatPostMessageResponse;

    @Mock
    RemindersAddResponse remindersAddResponse;

    @Before
    public void setUp() {
        try {

            List<Channel> channelList = new ArrayList<Channel>();
            Channel testChannelOne = new Channel();
            testChannelOne.setName("testChannel1");
            testChannelOne.setId("testChannel1ID");
            Channel testChannelTwo = new Channel();
            testChannelTwo.setName("testChannel2");
            testChannelTwo.setId("testChannel2ID");
            channelList.add(testChannelOne);
            channelList.add(testChannelTwo);

            when(slack.methods()).thenReturn(methods);
            when(methods.channelsList(any())).thenReturn(channelsListResponse);
            when(channelsListResponse.isOk()).thenReturn(true);
            when(channelsListResponse.getChannels()).thenReturn(channelList);

            when(methods.chatPostMessage(any())).thenReturn(chatPostMessageResponse);
            when(chatPostMessageResponse.isOk()).thenReturn(true);

            when(methods.remindersAdd(any())).thenReturn(remindersAddResponse);
            when(remindersAddResponse.isOk()).thenReturn(true);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPostMessageToChannel() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ChannelName",
                              "testChannel1");
        workItem.setParameter("Message",
                              "testMessage");

        PostMessageToChannelWorkitemHandler handler = new PostMessageToChannelWorkitemHandler("testAccessToken");
        SlackAuth auth = new SlackAuth();
        auth.setSlack(slack);
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }

    @Test
    public void testAddReminder() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ReminderText",
                              "this is a test reminder");
        workItem.setParameter("ReminderTime",
                              "in 10 minutes");

        AddReminderWorkitemHandler handler = new AddReminderWorkitemHandler("testAccessToken");
        SlackAuth auth = new SlackAuth();
        auth.setSlack(slack);
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }
}
