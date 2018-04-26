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
package org.jbpm.process.workitem.google.calendar;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Events;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "GoogleGetEventsDefinitions.wid", name = "GoogleGetEvents",
        displayName = "GoogleGetEvents",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.calendar.GetEventsWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "CalendarSummary")
        },
        results = {
                @WidResult(name = "AllEvents")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        })
public class GetEventsWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetEventsWorkitemHandler.class);
    private static final String RESULTS_ALL_EVENTS = "AllEvents";

    private String appName;
    private String clientSecret;
    private GoogleCalendarAuth auth = new GoogleCalendarAuth();

    public GetEventsWorkitemHandler(String appName,
                                       String clentSecret) {
        this.appName = appName;
        this.clientSecret = clentSecret;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        String paramCalendarSummary = (String) workItem.getParameter("CalendarSummary");
        Map<String, Object> results = new HashMap<String, Object>();

        try {

            com.google.api.services.calendar.Calendar client = auth.getAuthorizedCalendar(appName,
                                                                                          clientSecret);

            results.put(RESULTS_ALL_EVENTS,
                        getAllEvents(client,
                                     getCalendarIdBySummary(client,
                                                            paramCalendarSummary)));
        } catch (Exception e) {
            handleException(e);
        }

        workItemManager.completeWorkItem(workItem.getId(),
                                         results);
    }

    public String getCalendarIdBySummary(com.google.api.services.calendar.Calendar client,
                                         String summary) {
        String resultId = null;
        try {
            CalendarList calendarList = getAllCalendars(client);
            List<CalendarListEntry> entryList = calendarList.getItems();
            for (CalendarListEntry entry : entryList) {
                if (entry.getSummary().equalsIgnoreCase(summary)) {
                    resultId = entry.getId();
                }
            }
        } catch (Exception e) {
            logger.error(MessageFormat.format("Error retrieveing calendars: {0}",
                                              e.getMessage()));
        }
        return resultId;
    }

    public CalendarList getAllCalendars(com.google.api.services.calendar.Calendar client) {
        try {
            return client.calendarList().list().execute();
        } catch (Exception e) {
            logger.error(MessageFormat.format("Error trying to get calendars: {0}.",
                                              e.getMessage()));
            return null;
        }
    }

    public Events getAllEvents(com.google.api.services.calendar.Calendar client,
                               String calendarId) throws IOException {
        return client.events().list(calendarId).execute();
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setAuth(GoogleCalendarAuth auth) {
        this.auth = auth;
    }
}
