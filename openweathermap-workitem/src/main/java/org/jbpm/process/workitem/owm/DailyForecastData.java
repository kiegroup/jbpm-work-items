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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DailyForecastData implements Serializable {

    int dataCount;
    private String cityName;
    private List<DailyForecastDay> dailyForecastDayList = new ArrayList<DailyForecastDay>();

    public int getDataCount() {
        return dataCount;
    }

    public void setDataCount(int dataCount) {
        this.dataCount = dataCount;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public List<DailyForecastDay> getDailyForecastDayList() {
        return dailyForecastDayList;
    }

    public void setDailyForecastDayList(List<DailyForecastDay> dailyForecastDayList) {
        this.dailyForecastDayList = dailyForecastDayList;
    }

    public class DailyForecastDay implements Serializable {

        private Date date;
        private Double cloud;
        private Double humidity;
        private Double pressure;
        private Double rain;
        private Double snow;
        private Double speed;
        private Double maxTemp;
        private Double minTemp;
        private Double dayTemp;
        private Double morningTemp;
        private Double eveningTemp;
        private Double nightTemp;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Double getCloud() {
            return cloud;
        }

        public void setCloud(Double cloud) {
            this.cloud = cloud;
        }

        public Double getHumidity() {
            return humidity;
        }

        public void setHumidity(Double humidity) {
            this.humidity = humidity;
        }

        public Double getPressure() {
            return pressure;
        }

        public void setPressure(Double pressure) {
            this.pressure = pressure;
        }

        public Double getRain() {
            return rain;
        }

        public void setRain(Double rain) {
            this.rain = rain;
        }

        public Double getSnow() {
            return snow;
        }

        public void setSnow(Double snow) {
            this.snow = snow;
        }

        public Double getSpeed() {
            return speed;
        }

        public void setSpeed(Double speed) {
            this.speed = speed;
        }

        public Double getMaxTemp() {
            return maxTemp;
        }

        public void setMaxTemp(Double maxTemp) {
            this.maxTemp = maxTemp;
        }

        public Double getMinTemp() {
            return minTemp;
        }

        public void setMinTemp(Double minTemp) {
            this.minTemp = minTemp;
        }

        public Double getDayTemp() {
            return dayTemp;
        }

        public void setDayTemp(Double dayTemp) {
            this.dayTemp = dayTemp;
        }

        public Double getMorningTemp() {
            return morningTemp;
        }

        public void setMorningTemp(Double morningTemp) {
            this.morningTemp = morningTemp;
        }

        public Double getEveningTemp() {
            return eveningTemp;
        }

        public void setEveningTemp(Double eveningTemp) {
            this.eveningTemp = eveningTemp;
        }

        public Double getNightTemp() {
            return nightTemp;
        }

        public void setNightTemp(Double nightTemp) {
            this.nightTemp = nightTemp;
        }
    }
}
