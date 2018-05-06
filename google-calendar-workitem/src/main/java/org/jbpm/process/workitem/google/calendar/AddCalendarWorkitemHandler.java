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

@Wid(widfile = "GoogleAddCalendarDefinitions.wid", name = "GoogleAddCalendar",
        displayName = "GoogleAddCalendar",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.calendar.AddCalendarWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "CalendarSummary")
        },
        results = {
                @WidResult(name = "Calendar")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        })
public class AddCalendarWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final String RESULTS_CALENDAR = "Calendar";

    private String appName;
    private String clientSecret;
    private GoogleCalendarAuth auth = new GoogleCalendarAuth();

    public AddCalendarWorkitemHandler(String appName,
                                      String clentSecret) {
        this.appName = appName;
        this.clientSecret = clentSecret;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        Map<String, Object> results = new HashMap<String, Object>();

        String paramCalendarSummary = (String) workItem.getParameter("CalendarSummary");

        try {

            com.google.api.services.calendar.Calendar client = auth.getAuthorizedCalendar(appName,
                                                                                          clientSecret);

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
