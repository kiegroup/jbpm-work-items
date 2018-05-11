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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.core.OWM.Country;
import net.aksingh.owmjapis.model.CurrentWeather;
import net.aksingh.owmjapis.model.DailyWeatherForecast;
import net.aksingh.owmjapis.model.param.City;
import net.aksingh.owmjapis.model.param.ForecastData;
import net.aksingh.owmjapis.model.param.Main;
import net.aksingh.owmjapis.model.param.Temp;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.process.workitem.owm.DailyForecastData.DailyForecastDay;
import org.jbpm.test.AbstractBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OWM.class, CurrentWeather.class, DailyWeatherForecast.class, Main.class, City.class, ForecastData.class, Temp.class})
public class OpenWeatherMapWorkitemHandlerTest extends AbstractBaseTest {

    private OWM owm;

    @Before
    public void setUp() {
        try {
            owm = PowerMockito.mock(OWM.class);
            CurrentWeather currentWeather = PowerMockito.mock(CurrentWeather.class);
            DailyWeatherForecast dailyWeatherForecast = PowerMockito.mock(DailyWeatherForecast.class);
            Main mainData = PowerMockito.mock(Main.class);
            City cityData = PowerMockito.mock(City.class);
            ForecastData forecastData = PowerMockito.mock(ForecastData.class);
            List<ForecastData> forecastDataList = new ArrayList<>();
            forecastDataList.add(forecastData);
            Temp temp = PowerMockito.mock(Temp.class);

            when(owm.currentWeatherByCityName(anyString())).thenReturn(currentWeather);
            when(owm.currentWeatherByCityName(anyString(),
                                              any(Country.class))).thenReturn(currentWeather);

            when(owm.dailyWeatherForecastByCityName(anyString())).thenReturn(dailyWeatherForecast);
            when(owm.dailyWeatherForecastByCityName(anyString(),
                                                    any(Country.class))).thenReturn(dailyWeatherForecast);

            when(currentWeather.hasRespCode()).thenReturn(true);
            when(currentWeather.getRespCode()).thenReturn(200);
            when(currentWeather.hasCityName()).thenReturn(true);
            when(currentWeather.getCityName()).thenReturn("testCityName");
            when(currentWeather.hasMainData()).thenReturn(true);

            when(currentWeather.getMainData()).thenReturn(mainData);
            when(mainData.getTemp()).thenReturn(Double.valueOf(1));
            when(mainData.getTempMin()).thenReturn(Double.valueOf(2));
            when(mainData.getTempMax()).thenReturn(Double.valueOf(3));
            when(mainData.getPressure()).thenReturn(Double.valueOf(4));
            when(mainData.getHumidity()).thenReturn(Double.valueOf(5));

            when(currentWeather.hasDateTime()).thenReturn(false);
            when(currentWeather.hasRainData()).thenReturn(false);
            when(currentWeather.hasSnowData()).thenReturn(false);
            when(currentWeather.hasCloudData()).thenReturn(false);
            when(currentWeather.hasWindData()).thenReturn(false);
            when(currentWeather.hassystemData()).thenReturn(false);

            when(dailyWeatherForecast.hasRespCode()).thenReturn(true);
            when(dailyWeatherForecast.getRespCode()).thenReturn("200");
            when(dailyWeatherForecast.hasCityData()).thenReturn(true);
            when(dailyWeatherForecast.getCityData()).thenReturn(cityData);
            when(cityData.getName()).thenReturn("testCityName");
            when(dailyWeatherForecast.hasDataCount()).thenReturn(true);
            when(dailyWeatherForecast.getDataCount()).thenReturn(Integer.valueOf(1));
            when(dailyWeatherForecast.hasDataList()).thenReturn(true);
            when(dailyWeatherForecast.getDataList()).thenReturn(forecastDataList);
            when(forecastData.getDateTime()).thenReturn(new Date());
            when(forecastData.getCloud()).thenReturn(Double.valueOf(1));
            when(forecastData.getHumidity()).thenReturn(Double.valueOf(2));
            when(forecastData.getCloud()).thenReturn(Double.valueOf(3));
            when(forecastData.getPressure()).thenReturn(Double.valueOf(4));
            when(forecastData.getRain()).thenReturn(Double.valueOf(5));
            when(forecastData.getSnow()).thenReturn(Double.valueOf(6));
            when(forecastData.getSpeed()).thenReturn(Double.valueOf(7));
            when(forecastData.getTempData()).thenReturn(temp);

            when(temp.getTempMax()).thenReturn(Double.valueOf(8));
            when(temp.getTempMin()).thenReturn(Double.valueOf(9));
            when(temp.getTempDay()).thenReturn(Double.valueOf(10));
            when(temp.getTempMorning()).thenReturn(Double.valueOf(11));
            when(temp.getTempEvening()).thenReturn(Double.valueOf(12));
            when(temp.getTempNight()).thenReturn(Double.valueOf(13));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCurrentWeather() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("CityName",
                              "Atlanta");
        workItem.setParameter("CountryCode",
                              "UNITED_STATES");

        CurrentWeatherWorkitemHandler handler = new CurrentWeatherWorkitemHandler("testAPIKey");
        handler.setOWM(owm);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        CurrentWeatherData currentWeatherData = (CurrentWeatherData) (manager.getResults().get(workItem.getId())).get("CurrentWeatherData");
        assertNotNull(currentWeatherData);
        assertEquals(Double.valueOf(1),
                     currentWeatherData.getTemp());
        assertEquals(Double.valueOf(2),
                     currentWeatherData.getMinTemp());
        assertEquals(Double.valueOf(3),
                     currentWeatherData.getMaxTemp());
        assertEquals(Double.valueOf(4),
                     currentWeatherData.getPressure());
        assertEquals(Double.valueOf(5),
                     currentWeatherData.getHumidity());
    }

    @Test
    public void testDailyForecast() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("CityName",
                              "Atlanta");
        workItem.setParameter("CountryCode",
                              "UNITED_STATES");

        DailyForecastWorkitemHandler handler = new DailyForecastWorkitemHandler("testAPIKey");
        handler.setOWM(owm);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        DailyForecastData dailyForecastData = (DailyForecastData) (manager.getResults().get(workItem.getId())).get("DailyForecastData");
        assertNotNull(dailyForecastData);
        assertEquals("testCityName",
                     dailyForecastData.getCityName());
        assertEquals(1,
                     dailyForecastData.getDataCount());

        List<DailyForecastDay> dailyForecastDays = dailyForecastData.getDailyForecastDayList();
        assertNotNull(dailyForecastDays);
        assertEquals(1,
                     dailyForecastDays.size());

        assertEquals(Double.valueOf(8),
                     dailyForecastDays.get(0).getMaxTemp());
        assertEquals(Double.valueOf(3),
                     dailyForecastDays.get(0).getCloud());
        assertEquals(Double.valueOf(4),
                     dailyForecastDays.get(0).getPressure());
    }
}
