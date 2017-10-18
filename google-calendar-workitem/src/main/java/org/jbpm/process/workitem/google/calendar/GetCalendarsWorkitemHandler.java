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
package org.jbpm.process.workitem.google.calendar;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.google.api.services.calendar.model.CalendarList;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "GoogleGetCalendarsDefinitions.wid", name = "GoogleGetCalendars",
        displayName = "GoogleGetCalendars",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.calendar.GetCalendarsWorkitemHandler()",
        parameters = {
                @WidParameter(name = "AppName"),
                @WidParameter(name = "ClientSecret")
        },
        results = {
                @WidResult(name = "AllCalendars")
        },
        mavenDepends = {
                @WidMavenDepends(group = "com.google.apis", artifact = "google-api-services-calendar", version = "v3-rev87-1.19.0"),
                @WidMavenDepends(group = "com.google.oauth-client", artifact = "google-oauth-client-jetty", version = "1.19.0"),
                @WidMavenDepends(group = "com.google.http-client", artifact = "google-http-client-jackson2", version = "1.19.0")
        })
public class GetCalendarsWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetCalendarsWorkitemHandler.class);
    private static final String RESULTS_ALL_CALENDARS = "AllCalendars";

    private GoogleCalendarAuth auth = new GoogleCalendarAuth();
    Map<String, Object> results = new HashMap<String, Object>();

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        String paramAppName = (String) workItem.getParameter("AppName");
        String paramClientSecretJSON = (String) workItem.getParameter("ClientSecret");

        try {
            com.google.api.services.calendar.Calendar client = auth.getAuthorizedCalendar(paramAppName,
                                                                                          paramClientSecretJSON);

            results.put(RESULTS_ALL_CALENDARS,
                        getAllCalendars(client));
        } catch (Exception e) {
            handleException(e);
        }

        workItemManager.completeWorkItem(workItem.getId(),
                                         results);
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

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setAuth(GoogleCalendarAuth auth) {
        this.auth = auth;
    }
}
