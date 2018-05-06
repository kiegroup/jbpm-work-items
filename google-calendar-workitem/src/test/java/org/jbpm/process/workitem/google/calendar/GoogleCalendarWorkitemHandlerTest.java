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

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.Calendar.Calendars;
import com.google.api.services.calendar.model.CalendarList;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.test.AbstractBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class GoogleCalendarWorkitemHandlerTest extends AbstractBaseTest {

    @Mock
    GoogleCalendarAuth auth;

    @Mock
    Calendar client;

    @Mock
    Calendar.Events clientEvents;

    @Mock
    Calendars calendars;

    @Mock
    Calendar.CalendarList calendarsList;

    @Mock
    Calendar.CalendarList.List calendarsListList;

    @Mock
    Calendar.Calendars.Insert calendarsInsert;

    @Mock
    Calendar.Events.Insert calendarEventsInsert;

    @Mock
    Calendar.Events.List calendarEventsList;

    @Before
    public void setUp() {
        try {
            CalendarList calendarListModel = new com.google.api.services.calendar.model.CalendarList();
            when(client.calendars()).thenReturn(calendars);
            when(calendars.insert(anyObject())).thenReturn(calendarsInsert);
            when(calendarsInsert.execute()).thenReturn(new com.google.api.services.calendar.model.Calendar());
            when(client.calendarList()).thenReturn(calendarsList);
            when(calendarsList.list()).thenReturn(calendarsListList);
            when(calendarsListList.execute()).thenReturn(calendarListModel);
            when(auth.getAuthorizedCalendar(anyString(),
                                            anyString())).thenReturn(client);
            when(client.events()).thenReturn(clientEvents);
            when(clientEvents.insert(anyString(),
                                     anyObject())).thenReturn(calendarEventsInsert);
            when(calendarEventsInsert.execute()).thenReturn(new com.google.api.services.calendar.model.Event());
            when(clientEvents.list(anyString())).thenReturn(calendarEventsList);
            when(calendarEventsList.execute()).thenReturn(new com.google.api.services.calendar.model.Events());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAddCalendarHandler() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("CalendarSummary",
                              "mycalendarsummary");

        AddCalendarWorkitemHandler handler = new AddCalendarWorkitemHandler("myAppName",
                                                                            "{}");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("Calendar") instanceof com.google.api.services.calendar.model.Calendar);
    }

    @Test
    public void testAddEventHandler() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("CalendarSummary",
                              "mycalendarsummary");
        workItem.setParameter("EventSummary",
                              "myeventsummary");
        workItem.setParameter("EventStart",
                              "Tue, 6 Aug 2017 01:19:39 +0530");
        workItem.setParameter("EventEnd",
                              "Tue, 6 Aug 2017 06:19:39 +0530");
        workItem.setParameter("EventAttendees",
                              "myeventattendees");
        workItem.setParameter("EventCreator",
                              "myeventcreator");

        AddEventWorkitemHandler handler = new AddEventWorkitemHandler("myAppName",
                                                                      "{}");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("Event") instanceof com.google.api.services.calendar.model.Event);
    }

    @Test
    public void testGetCalendarsHandler() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        GetCalendarsWorkitemHandler handler = new GetCalendarsWorkitemHandler("myAppName",
                                                                              "{}");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("AllCalendars") instanceof com.google.api.services.calendar.model.CalendarList);
    }

    @Test
    public void testGetEventsHandler() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("CalendarSummary",
                              "mycalendarsummary");

        GetEventsWorkitemHandler handler = new GetEventsWorkitemHandler("myAppName",
                                                                        "{}");
        handler.setAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("AllEvents") instanceof com.google.api.services.calendar.model.Events);
    }
}
