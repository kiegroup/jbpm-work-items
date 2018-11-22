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
package org.jbpm.workitem.google.maps;

import com.google.maps.GeoApiContext;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GoogleMapsWorkitemHandlerTest {

    @Mock
    GeoApiContext context;

    @Test
    public void testDirections() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Origin",
                              "Atlanta");
        workItem.setParameter("Destination",
                              "Dallas");

        DirectionsWorkitemHandler handler = new DirectionsWorkitemHandler(context);
        handler.setLogThrownException(true);
        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
    }

    @Test
    public void testSpeedLimits() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("PlaceIds",
                              "id1,id2,id3,id4");

        SpeedLimitsWorkitemHandler handler = new SpeedLimitsWorkitemHandler(context);
        handler.setLogThrownException(true);
        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
    }

    @Test
    public void testStaticMap() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Width",
                              "100");
        workItem.setParameter("Height",
                              "100");
        workItem.setParameter("CenterLocation",
                              "Atlanta");

        StaticMapsWorkitemHandler handler = new StaticMapsWorkitemHandler(context);
        handler.setLogThrownException(true);
        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
    }

    @Test
    public void testGeocoding() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("SearchType",
                              "byaddress");
        workItem.setParameter("SearchLocation",
                              "Atlanta, GA, USA");

        GeocodingWorkitemHandler handler = new GeocodingWorkitemHandler(context);
        handler.setLogThrownException(true);
        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
    }

    @Test
    public void testTimezone() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("LatLong",
                              "-33.8688, 151.2093");

        TimezoneWorkitemHandler handler = new TimezoneWorkitemHandler(context);
        handler.setLogThrownException(true);
        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
    }
}
