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
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.LocationType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "GoogleMapsGeocodingDefinitions.wid", name = "GoogleMapsGeocoding",
        displayName = "GoogleMapsGeocoding",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.maps.GeocodingWorkitemHandler(\"apiKey\")",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "SearchType", required = true),
                @WidParameter(name = "SearchLocation", required = true),
                @WidParameter(name = "Bounds"),
                @WidParameter(name = "LocationType")
        },
        results = {
                @WidResult(name = "GeocodingResults")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "google,maps,geocoding,locations",
                action = @WidAction(title = "Get geocoding information using Google Maps"),
                authinfo = @WidAuth(required = true, params = {"apiKey"},
                        paramsdescription = {"Google maps api key"},
                        referencesite = "https://developers.google.com/maps/premium/previous-licenses/clientside/auth")
        ))
public class GeocodingWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String apiKey;
    private GeoApiContext geoApiContext;
    private GoogleMapsAuth auth = new GoogleMapsAuth();
    private static final String RESULTS_VALUE = "GeocodingResults";
    private static final Logger logger = LoggerFactory.getLogger(GeocodingWorkitemHandler.class);

    public GeocodingWorkitemHandler(String apiKey) throws Exception {
        this.apiKey = apiKey;
        try {
            geoApiContext = auth.authorize(apiKey);
        } catch (Exception e) {
            throw new IllegalAccessException("Unable to authenticate with google maps: " + e.getMessage());
        }
    }

    public GeocodingWorkitemHandler(GeoApiContext geoApiContext) {
        this.geoApiContext = geoApiContext;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        String searchType = (String) workItem.getParameter("SearchType");
        String searchLocation = (String) workItem.getParameter("SearchLocation");
        String bounds = (String) workItem.getParameter("Bounds");
        String locationType = (String) workItem.getParameter("LocationType");

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            GeocodingApiRequest geocodingApiRequest = GeocodingApi.newRequest(geoApiContext);
            Map<String, Object> results = new HashMap<>();

            switch (searchType) {
                case "byaddress":
                    geocodingApiRequest = geocodingApiRequest.address(searchLocation);
                    break;
                case "byplaceid":
                    geocodingApiRequest = geocodingApiRequest.place(searchLocation);
                    break;
                case "bylatlong":
                    try {
                        String[] searchLocationParts = searchLocation.split(",");
                        geocodingApiRequest = geocodingApiRequest.latlng(new LatLng(Double.parseDouble(searchLocationParts[0]),
                                                                                    Double.parseDouble(searchLocationParts[1])));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Unable to perform search by latlong - invalid location: " + searchLocation);
                    }
                    break;
                default:
                    // default to address search
                    geocodingApiRequest = geocodingApiRequest.address(searchLocation);
            }

            if (bounds != null && bounds.length() > 0) {
                String[] boundsParts = bounds.split(",");
                if (boundsParts.length == 4) {
                    try {
                        geocodingApiRequest = geocodingApiRequest.bounds(new LatLng(Double.parseDouble(boundsParts[0]),
                                                                                    Double.parseDouble(boundsParts[1])),
                                                                         new LatLng(Double.parseDouble(boundsParts[2]),
                                                                                    Double.parseDouble(boundsParts[3])));
                    } catch (NumberFormatException e) {
                        logger.error("Invalid bounds format: " + bounds + ". Valid example could be: 34.172684,-118.604794,34.236144,-118.500938");
                    }
                } else {
                    logger.error("Invalid bounds: " + bounds + ". Valid example could be: 34.172684,-118.604794,34.236144,-118.500938");
                }
            }

            if (locationType != null) {
                switch (locationType) {
                    case "rooftop":
                        geocodingApiRequest = geocodingApiRequest.locationType(LocationType.ROOFTOP);
                        break;
                    case "approximate":
                        geocodingApiRequest = geocodingApiRequest.locationType(LocationType.APPROXIMATE);
                        break;
                    case "range":
                        geocodingApiRequest = geocodingApiRequest.locationType(LocationType.RANGE_INTERPOLATED);
                        break;
                    case "center":
                        geocodingApiRequest = geocodingApiRequest.locationType(LocationType.GEOMETRIC_CENTER);
                        break;
                    default:
                }
            }

            GeocodingResult[] geocodingResults = geocodingApiRequest.await();
            results.put(RESULTS_VALUE,
                        geocodingResults);

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
