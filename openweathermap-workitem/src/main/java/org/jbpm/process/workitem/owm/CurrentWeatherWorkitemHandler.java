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
package org.jbpm.process.workitem.owm;

import java.util.HashMap;
import java.util.Map;

import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.core.OWM.Country;
import net.aksingh.owmjapis.model.CurrentWeather;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "CurrentWeatherDefinitions.wid", name = "CurrentWeather",
        displayName = "CurrentWeather",
        defaultHandler = "mvel: new org.jbpm.process.workitem.owm.CurrentWeatherWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "CityName", required = true),
                @WidParameter(name = "CountryCode")
        },
        results = {
                @WidResult(name = "CurrentWeatherData")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        })
public class CurrentWeatherWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(CurrentWeatherWorkitemHandler.class);
    private static final String RESULTS_VALUES = "CurrentWeatherData";

    private String apiKey;
    private OWM owm;

    public CurrentWeatherWorkitemHandler(String apiKey) {
        this.apiKey = apiKey;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String cityName = (String) workItem.getParameter("CityName");
            String countryCode = (String) workItem.getParameter("CountryCode");
            Map<String, Object> results = new HashMap<String, Object>();
            CurrentWeatherData cwd = new CurrentWeatherData();

            if (owm == null) {
                owm = new OWM(apiKey);
            }

            CurrentWeather currentWeather;

            if (countryCode == null) {
                currentWeather = owm.currentWeatherByCityName(cityName);
            } else {
                currentWeather = owm.currentWeatherByCityName(cityName,
                                                              Country.valueOf(countryCode));
            }

            if (currentWeather.hasRespCode() && currentWeather.getRespCode() == 200) {
                if (currentWeather.hasCityName()) {
                    cwd.setCityName(currentWeather.getCityName());
                }

                if (currentWeather.hasDateTime()) {
                    cwd.setDate(currentWeather.getDateTime());
                }

                if (currentWeather.hasMainData()) {
                    cwd.setTemp(currentWeather.getMainData().getTemp());
                    cwd.setMinTemp(currentWeather.getMainData().getTempMin());
                    cwd.setMaxTemp(currentWeather.getMainData().getTempMax());
                    cwd.setPressure(currentWeather.getMainData().getPressure());
                    cwd.setHumidity(currentWeather.getMainData().getHumidity());
                }

                if (currentWeather.hasRainData()) {
                    cwd.setPrecipitation(currentWeather.getRainData().getPrecipVol3h());
                }

                if (currentWeather.hasCloudData()) {
                    cwd.setCloud(currentWeather.getCloudData().getCloud());
                }

                if (currentWeather.hasSnowData()) {
                    cwd.setSnow(currentWeather.getSnowData().getSnowVol3h());
                }

                if (currentWeather.hasWindData()) {
                    cwd.setWidDegree(currentWeather.getWindData().getDegree());
                    cwd.setWindSpeed(currentWeather.getWindData().getSpeed());
                    cwd.setWindGust(currentWeather.getWindData().getGust());
                }

                if (currentWeather.hassystemData()) {
                    cwd.setSunrise(currentWeather.getSystemData().getSunriseDateTime());
                    cwd.setSunset(currentWeather.getSystemData().getSunsetDateTime());
                }
            } else {
                logger.error("Unable to retrieve weather info.");
            }

            results.put(RESULTS_VALUES,
                        cwd);

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem workItem,
                              WorkItemManager manager) {
    }

    // for testing
    public void setOWM(OWM owm) {
        this.owm = owm;
    }
}
