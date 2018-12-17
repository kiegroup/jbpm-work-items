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
import java.util.List;
import java.util.Map;

import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.core.OWM.Country;
import net.aksingh.owmjapis.model.DailyWeatherForecast;
import net.aksingh.owmjapis.model.param.ForecastData;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.jbpm.process.workitem.owm.DailyForecastData.DailyForecastDay;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "DailyForecastDefinitions.wid", name = "DailyForecast",
        displayName = "DailyForecast",
        defaultHandler = "mvel: new org.jbpm.process.workitem.owm.DailyForecastWorkitemHandler(\"apiKey\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        parameters = {
                @WidParameter(name = "CityName", required = true),
                @WidParameter(name = "CountryCode")
        },
        results = {
                @WidResult(name = "DailyForecastData", runtimeType = "org.jbpm.process.workitem.owm.DailyForecastData")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "openweathermap,weather,daily,forecase",
                action = @WidAction(title = "Get the weather daily forecast for a location"),
                authinfo = @WidAuth(required = true, params = {"apiKey"},
                        paramsdescription = {"OpenWeatherMap api key"},
                        referencesite = "https://openweathermap.org/appid")
        ))
public class DailyForecastWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(CurrentWeatherWorkitemHandler.class);
    private static final String RESULTS_VALUES = "DailyForecastData";

    private String apiKey;
    private OWM owm;

    public DailyForecastWorkitemHandler(String apiKey) {
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

            DailyForecastData dfd = new DailyForecastData();

            if (owm == null) {
                owm = new OWM(apiKey);
            }

            DailyWeatherForecast dailyWeatherForecast;

            if (countryCode == null) {
                dailyWeatherForecast = owm.dailyWeatherForecastByCityName(cityName);
            } else {
                dailyWeatherForecast = owm.dailyWeatherForecastByCityName(cityName,
                                                                          Country.valueOf(countryCode));
            }

            if (dailyWeatherForecast.hasRespCode() && dailyWeatherForecast.getRespCode().equals("200")) {
                if (dailyWeatherForecast.hasCityData()) {
                    dfd.setCityName(dailyWeatherForecast.getCityData().getName());
                }

                if (dailyWeatherForecast.hasDataCount()) {
                    dfd.setDataCount(dailyWeatherForecast.getDataCount().intValue());
                }

                if (dailyWeatherForecast.hasDataList()) {
                    List<ForecastData> forecastDataList = dailyWeatherForecast.getDataList();
                    for (ForecastData forecastData : forecastDataList) {
                        DailyForecastDay dailyForecastDay = dfd.new DailyForecastDay();

                        dailyForecastDay.setDate(forecastData.getDateTime());
                        dailyForecastDay.setCloud(forecastData.getCloud());
                        dailyForecastDay.setHumidity(forecastData.getHumidity());
                        dailyForecastDay.setPressure(forecastData.getPressure());
                        dailyForecastDay.setRain(forecastData.getRain());
                        dailyForecastDay.setSnow(forecastData.getSnow());
                        dailyForecastDay.setSpeed(forecastData.getSpeed());
                        dailyForecastDay.setMaxTemp(forecastData.getTempData().getTempMax());
                        dailyForecastDay.setMinTemp(forecastData.getTempData().getTempMin());
                        dailyForecastDay.setDayTemp(forecastData.getTempData().getTempDay());
                        dailyForecastDay.setMorningTemp(forecastData.getTempData().getTempMorning());
                        dailyForecastDay.setEveningTemp(forecastData.getTempData().getTempEvening());
                        dailyForecastDay.setNightTemp(forecastData.getTempData().getTempNight());

                        dfd.getDailyForecastDayList().add(dailyForecastDay);
                    }
                }
            } else {
                logger.error("Unable to retrieve weather info.");
            }

            results.put(RESULTS_VALUES,
                        dfd);

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
