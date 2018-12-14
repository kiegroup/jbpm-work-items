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
import com.google.maps.RoadsApi;
import com.google.maps.model.SpeedLimit;
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

@Wid(widfile = "GoogleMapsSpeedLimitsDefinitions.wid", name = "GoogleMapsSpeedLimits",
        displayName = "GoogleMapsSpeedLimits",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.maps.SpeedLimitsWorkitemHandler(\"apiKey\")",
        documentation = "${artifactId}/index.html",
        module = "${artifactId}", version = "${version}",
        parameters = {
                @WidParameter(name = "PlaceIds", required = true)
        },
        results = {
                @WidResult(name = "Speedlimits", runtimeType = "com.google.maps.model.SpeedLimit")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "google,maps,speedlimit,places",
                action = @WidAction(title = "Get speedlimits for places using Google Maps"),
                authinfo = @WidAuth(required = true, params = {"apiKey"},
                        paramsdescription = {"Google maps api key"},
                        referencesite = "https://developers.google.com/maps/premium/previous-licenses/clientside/auth")
        ))
public class SpeedLimitsWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String apiKey;
    private GeoApiContext geoApiContext;
    private GoogleMapsAuth auth = new GoogleMapsAuth();
    private static final String RESULTS_VALUE = "Speedlimits";

    public SpeedLimitsWorkitemHandler(String apiKey) throws Exception {
        this.apiKey = apiKey;
        try {
            geoApiContext = auth.authorize(apiKey);
        } catch (Exception e) {
            throw new IllegalAccessException("Unable to authenticate with google maps: " + e.getMessage());
        }
    }

    public SpeedLimitsWorkitemHandler(GeoApiContext geoApiContext) {
        this.geoApiContext = geoApiContext;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        String[] placeIds = ((String) workItem.getParameter("PlaceIds")).split(",");

        try {
            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Map<String, Object> results = new HashMap<>();

            SpeedLimit[] speedLimits = RoadsApi.speedLimits(geoApiContext,
                                                            placeIds).await();

            results.put(RESULTS_VALUE,
                        speedLimits);

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
