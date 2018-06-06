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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.google.api.services.calendar.model.CalendarList;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "GoogleGetCalendarsDefinitions.wid", name = "GoogleGetCalendars",
        displayName = "GoogleGetCalendars",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.calendar.GetCalendarsWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        results = {
                @WidResult(name = "AllCalendars")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "google,calendar,get,all",
                action = @WidAction(title = "Get all existing Google Calendars")
        ))
public class GetCalendarsWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetCalendarsWorkitemHandler.class);
    private static final String RESULTS_ALL_CALENDARS = "AllCalendars";

    private String appName;
    private String clientSecret;
    private GoogleCalendarAuth auth = new GoogleCalendarAuth();

    public GetCalendarsWorkitemHandler(String appName,
                                       String clentSecret) {
        this.appName = appName;
        this.clientSecret = clentSecret;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        Map<String, Object> results = new HashMap<String, Object>();
        try {
            com.google.api.services.calendar.Calendar client = auth.getAuthorizedCalendar(appName,
                                                                                          clientSecret);

            results.put(RESULTS_ALL_CALENDARS,
                        getAllCalendars(client));
            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
        } catch (Exception e) {
            handleException(e);
        }

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
