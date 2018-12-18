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

import java.util.HashMap;
import java.util.Map;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;
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

@Wid(widfile = "GoogleMapsDirectionsDefinitions.wid", name = "GoogleMapsDirections",
        displayName = "GoogleMapsDirections",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.maps.DirectionsWorkitemHandler(\"apiKey\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        parameters = {
                @WidParameter(name = "Origin", required = true),
                @WidParameter(name = "Destination", required = true),
                @WidParameter(name = "Mode"),
                @WidParameter(name = "Avoid"),
                @WidParameter(name = "Units")
        },
        results = {
                @WidResult(name = "Directions", runtimeType = "com.google.maps.model.DirectionsResult")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "google,maps,directions,locations",
                action = @WidAction(title = "Get directions using Google Maps"),
                authinfo = @WidAuth(required = true, params = {"apiKey"},
                        paramsdescription = {"Google maps api key"},
                        referencesite = "https://developers.google.com/maps/premium/previous-licenses/clientside/auth")
        ))
public class DirectionsWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String apiKey;
    private GeoApiContext geoApiContext;
    private GoogleMapsAuth auth = new GoogleMapsAuth();
    private static final String RESULTS_VALUE = "Directions";

    public DirectionsWorkitemHandler(String apiKey) throws Exception {
        this.apiKey = apiKey;
        try {
            geoApiContext = auth.authorize(apiKey);
        } catch (Exception e) {
            throw new IllegalAccessException("Unable to authenticate with google maps: " + e.getMessage());
        }
    }

    public DirectionsWorkitemHandler(GeoApiContext geoApiContext) {
        this.geoApiContext = geoApiContext;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        String origin = (String) workItem.getParameter("Origin");
        String destination = (String) workItem.getParameter("Destination");
        String mode = (String) workItem.getParameter("Mode");
        String avoid = (String) workItem.getParameter("Avoid");
        String units = (String) workItem.getParameter("Units");

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Map<String, Object> results = new HashMap<>();

            DirectionsApiRequest directionsApiRequest = DirectionsApi.newRequest(geoApiContext);
            directionsApiRequest = directionsApiRequest.origin(origin);
            directionsApiRequest = directionsApiRequest.destination(destination);

            if (mode != null) {
                switch (mode) {
                    case "driving":
                        directionsApiRequest = directionsApiRequest.mode(TravelMode.DRIVING);
                        break;
                    case "walking":
                        directionsApiRequest = directionsApiRequest.mode(TravelMode.WALKING);
                        break;
                    case "bicycling":
                        directionsApiRequest = directionsApiRequest.mode(TravelMode.BICYCLING);
                        break;
                    case "transit":
                        directionsApiRequest = directionsApiRequest.mode(TravelMode.TRANSIT);
                        break;
                    default:
                        directionsApiRequest = directionsApiRequest.mode(TravelMode.DRIVING);
                }
            }

            if (avoid != null) {
                switch (avoid) {
                    case "tolls":
                        directionsApiRequest = directionsApiRequest.avoid(DirectionsApi.RouteRestriction.TOLLS);
                        break;
                    case "highways":
                        directionsApiRequest = directionsApiRequest.avoid(DirectionsApi.RouteRestriction.HIGHWAYS);
                        break;
                    case "ferries":
                        directionsApiRequest = directionsApiRequest.avoid(DirectionsApi.RouteRestriction.FERRIES);
                        break;
                    default:
                }
            }

            if (units != null) {
                switch (units) {
                    case "metric":
                        directionsApiRequest = directionsApiRequest.units(Unit.METRIC);
                        break;
                    case "imperial":
                        directionsApiRequest = directionsApiRequest.units(Unit.IMPERIAL);
                        break;
                    default:
                        directionsApiRequest = directionsApiRequest.units(Unit.METRIC);
                }
            }

            DirectionsResult directionsResult = directionsApiRequest.await();
            results.put(RESULTS_VALUE,
                        directionsResult);

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }
}
