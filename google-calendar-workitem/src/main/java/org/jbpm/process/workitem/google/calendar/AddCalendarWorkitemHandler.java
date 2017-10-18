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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.api.services.calendar.model.Calendar;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "GoogleAddCalendarDefinitions.wid", name = "GoogleAddCalendar",
        displayName = "GoogleAddCalendar",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.calendar.AddCalendarWorkitemHandler()",
        parameters = {
                @WidParameter(name = "AppName"),
                @WidParameter(name = "ClientSecret"),
                @WidParameter(name = "CalendarSummary")
        },
        results = {
                @WidResult(name = "Calendar")
        },
        mavenDepends = {
                @WidMavenDepends(group = "com.google.apis", artifact = "google-api-services-calendar", version = "v3-rev87-1.19.0"),
                @WidMavenDepends(group = "com.google.oauth-client", artifact = "google-oauth-client-jetty", version = "1.19.0"),
                @WidMavenDepends(group = "com.google.http-client", artifact = "google-http-client-jackson2", version = "1.19.0")
        })
public class AddCalendarWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(AddCalendarWorkitemHandler.class);
    private static final String RESULTS_CALENDAR = "Calendar";

    private GoogleCalendarAuth auth = new GoogleCalendarAuth();

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        Map<String, Object> results = new HashMap<String, Object>();

        String paramCalendarSummary = (String) workItem.getParameter("CalendarSummary");
        String paramAppName = (String) workItem.getParameter("AppName");
        String paramClientSecretJSON = (String) workItem.getParameter("ClientSecret");

        try {

            com.google.api.services.calendar.Calendar client = auth.getAuthorizedCalendar(paramAppName,
                                                                                          paramClientSecretJSON);

            results.put(RESULTS_CALENDAR,
                        addCalendar(client,
                                    paramCalendarSummary));
        } catch (Exception e) {
            handleException(e);
        }

        workItemManager.completeWorkItem(workItem.getId(),
                                         results);
    }

    public Calendar addCalendar(com.google.api.services.calendar.Calendar client,
                                String calendarSummary) throws IOException {
        Calendar entry = new Calendar();
        entry.setSummary(calendarSummary);
        return client.calendars().insert(entry).execute();
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setAuth(GoogleCalendarAuth auth) {
        this.auth = auth;
    }
}
