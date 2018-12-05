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

@Wid(widfile = "GoogleAddCalendarDefinitions.wid", name = "GoogleAddCalendar",
        displayName = "GoogleAddCalendar",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.calendar.AddCalendarWorkitemHandler(\"appName\", \"clentSecret\")",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "CalendarSummary", required = true)
        },
        results = {
                @WidResult(name = "Calendar")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "google,calendar,add",
                action = @WidAction(title = "Add a new Google Calendar"),
                authinfo = @WidAuth(required = true, params = {"appName", "clentSecret"},
                        paramsdescription = {"Google app name", "Google client secret"},
                        referencesite = "https://developers.google.com/calendar/auth")
        ))
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

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            com.google.api.services.calendar.Calendar client = auth.getAuthorizedCalendar(appName,
                                                                                          clientSecret);

            results.put(RESULTS_CALENDAR,
                        addCalendar(client,
                                    paramCalendarSummary));

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
        } catch (Exception e) {
            handleException(e);
        }
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
