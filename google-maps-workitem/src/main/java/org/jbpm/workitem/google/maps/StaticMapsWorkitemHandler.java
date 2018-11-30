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

import com.google.maps.GeoApiContext;
import com.google.maps.ImageResult;
import com.google.maps.StaticMapsApi;
import com.google.maps.StaticMapsRequest;
import com.google.maps.model.Size;
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

@Wid(widfile = "GoogleMapsStaticMapsDefinitions.wid", name = "GoogleMapsStaticMaps",
        displayName = "GoogleMapsStaticMaps",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.maps.StaticMapsWorkitemHandler(\"apiKey\")",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "Width", required = true),
                @WidParameter(name = "Height", required = true),
                @WidParameter(name = "CenterLocation", required = true),
                @WidParameter(name = "Zoom"),
                @WidParameter(name = "Scale"),
                @WidParameter(name = "MapType"),
                @WidParameter(name = "Markers")
        },
        results = {
                @WidResult(name = "StaticMap")
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
public class StaticMapsWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String apiKey;
    private GeoApiContext geoApiContext;
    private GoogleMapsAuth auth = new GoogleMapsAuth();
    private static final String RESULTS_VALUE = "StaticMap";

    public StaticMapsWorkitemHandler(String apiKey) throws Exception {
        this.apiKey = apiKey;
        try {
            geoApiContext = auth.authorize(apiKey);
        } catch (Exception e) {
            throw new IllegalAccessException("Unable to authenticate with google maps: " + e.getMessage());
        }
    }

    public StaticMapsWorkitemHandler(GeoApiContext geoApiContext) {
        this.geoApiContext = geoApiContext;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        String width = (String) workItem.getParameter("Width");
        String height = (String) workItem.getParameter("Height");
        String centerLocation = (String) workItem.getParameter("CenterLocation");
        String zoom = (String) workItem.getParameter("Zoom");
        String scale = (String) workItem.getParameter("Scale");
        String mapType = (String) workItem.getParameter("MapType");
        String markers = (String) workItem.getParameter("Markers");

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Map<String, Object> results = new HashMap<>();

            StaticMapsRequest staticMapsRequest = StaticMapsApi.newRequest(geoApiContext,
                                                                           new Size(Integer.parseInt(width),
                                                                                    Integer.parseInt(height)));
            staticMapsRequest.center(centerLocation);

            if (zoom != null) {
                staticMapsRequest = staticMapsRequest.zoom(Integer.parseInt(zoom));
            }

            if (scale != null) {
                staticMapsRequest = staticMapsRequest.scale(Integer.parseInt(scale));
            }

            if (mapType != null) {
                staticMapsRequest = staticMapsRequest.maptype(StaticMapsRequest.StaticMapType.valueOf(mapType));
            }

            if (markers != null) {
                // markers format: color,label,location;color,label,location ....
                String[] markersArray = markers.split(";");
                for (String newMarker : markersArray) {
                    String[] newMarkerInfo = newMarker.split(",");
                    StaticMapsRequest.Markers staticMarker = new StaticMapsRequest.Markers();
                    staticMarker.color(newMarkerInfo[0]);
                    staticMarker.label(newMarkerInfo[1]);
                    staticMarker.addLocation(newMarkerInfo[2]);

                    staticMapsRequest = staticMapsRequest.markers(staticMarker);
                }
            }

            ImageResult imageResult = staticMapsRequest.await();

            results.put(RESULTS_VALUE,
                        imageResult);

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
