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
package org.jbpm.process.workitem.repositoryspringboot.autoconfig;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "workitems")
public class WorkitemsAutoConfigurationProperties {

    // Camel workitem properties
    private Set<String> camelHeaders;
    private String camelSchema;
    private String camelPathLocation;
    private String camelResponseLocation;

    // Dropbox workitem properties
    private String dropboxClientIdentifier;
    private String dropboxAccessToken;

    // Ethereum workitem properties
    private String ethereumWalletPassword;
    private String ethereumWalletPath;

    // ExecSQL workitem properties
    private String execSqlDataSourceName;

    // Github workitem properties
    private String githubUserName;
    private String githubPassword;

    // Google calendar properties
    private String googleCalendarAppName;
    private String googleCalendarClientSecret;

    // Google drive properties
    private String googleDriveAppName;
    private String googleDriveClientSecret;

    // Google mail properties
    private String googleMailAppName;
    private String googleMailClientSecret;

    // Google sheets properties
    private String googleSheetsAppName;
    private String googleSheetsClientSecret;

    // Google tasks properties
    private String googleTasksAppName;
    private String googleTasksClientSecret;

    // Google Translate properties
    private String googleTranslateApiKey;

    // IBM Watson properties
    private String ibmWatsonApiKey;

    // IFTTT properties
    private String iftttKey;

    // Jira properties
    private String jiraUserName;
    private String jiraPassword;
    private String jiraRepoURI;

    // OpenWeatherMap properties
    private String openWeatherMapApiKey;

    // Pastebin properties
    private String pastebinDevelKey;

    // Riot properties
    private String riotApiKey;

    // Slack properties
    private String slackAccessToken;

    // Twitter properties
    private String twitterConsumerKey;
    private String twitterConsumerSecret;
    private String twitterAccessKey;
    private String twitterAccessSecret;

    // Vimeo properties
    private String vimeoAccessToken;

    public Set<String> getCamelHeaders() {
        return camelHeaders;
    }

    public void setCamelHeaders(Set<String> camelHeaders) {
        this.camelHeaders = camelHeaders;
    }

    public String getCamelSchema() {
        return camelSchema;
    }

    public void setCamelSchema(String camelSchema) {
        this.camelSchema = camelSchema;
    }

    public String getCamelPathLocation() {
        return camelPathLocation;
    }

    public void setCamelPathLocation(String camelPathLocation) {
        this.camelPathLocation = camelPathLocation;
    }

    public String getCamelResponseLocation() {
        return camelResponseLocation;
    }

    public void setCamelResponseLocation(String camelResponseLocation) {
        this.camelResponseLocation = camelResponseLocation;
    }

    public String getDropboxClientIdentifier() {
        return dropboxClientIdentifier;
    }

    public void setDropboxClientIdentifier(String dropboxClientIdentifier) {
        this.dropboxClientIdentifier = dropboxClientIdentifier;
    }

    public String getDropboxAccessToken() {
        return dropboxAccessToken;
    }

    public void setDropboxAccessToken(String dropboxAccessToken) {
        this.dropboxAccessToken = dropboxAccessToken;
    }

    public String getEthereumWalletPassword() {
        return ethereumWalletPassword;
    }

    public void setEthereumWalletPassword(String ethereumWalletPassword) {
        this.ethereumWalletPassword = ethereumWalletPassword;
    }

    public String getEthereumWalletPath() {
        return ethereumWalletPath;
    }

    public void setEthereumWalletPath(String ethereumWalletPath) {
        this.ethereumWalletPath = ethereumWalletPath;
    }

    public String getExecSqlDataSourceName() {
        return execSqlDataSourceName;
    }

    public void setExecSqlDataSourceName(String execSqlDataSourceName) {
        this.execSqlDataSourceName = execSqlDataSourceName;
    }

    public String getGithubUserName() {
        return githubUserName;
    }

    public void setGithubUserName(String githubUserName) {
        this.githubUserName = githubUserName;
    }

    public String getGithubPassword() {
        return githubPassword;
    }

    public void setGithubPassword(String githubPassword) {
        this.githubPassword = githubPassword;
    }

    public String getGoogleDriveAppName() {
        return googleDriveAppName;
    }

    public void setGoogleDriveAppName(String googleDriveAppName) {
        this.googleDriveAppName = googleDriveAppName;
    }

    public String getGoogleCalendarAppName() {
        return googleCalendarAppName;
    }

    public void setGoogleCalendarAppName(String googleCalendarAppName) {
        this.googleCalendarAppName = googleCalendarAppName;
    }

    public String getGoogleCalendarClientSecret() {
        return googleCalendarClientSecret;
    }

    public void setGoogleCalendarClientSecret(String googleCalendarClientSecret) {
        this.googleCalendarClientSecret = googleCalendarClientSecret;
    }

    public String getGoogleDriveClientSecret() {
        return googleDriveClientSecret;
    }

    public void setGoogleDriveClientSecret(String googleDriveClientSecret) {
        this.googleDriveClientSecret = googleDriveClientSecret;
    }

    public String getGoogleMailAppName() {
        return googleMailAppName;
    }

    public void setGoogleMailAppName(String googleMailAppName) {
        this.googleMailAppName = googleMailAppName;
    }

    public String getGoogleMailClientSecret() {
        return googleMailClientSecret;
    }

    public void setGoogleMailClientSecret(String googleMailClientSecret) {
        this.googleMailClientSecret = googleMailClientSecret;
    }

    public String getGoogleSheetsAppName() {
        return googleSheetsAppName;
    }

    public void setGoogleSheetsAppName(String googleSheetsAppName) {
        this.googleSheetsAppName = googleSheetsAppName;
    }

    public String getGoogleSheetsClientSecret() {
        return googleSheetsClientSecret;
    }

    public void setGoogleSheetsClientSecret(String googleSheetsClientSecret) {
        this.googleSheetsClientSecret = googleSheetsClientSecret;
    }

    public String getGoogleTasksAppName() {
        return googleTasksAppName;
    }

    public void setGoogleTasksAppName(String googleTasksAppName) {
        this.googleTasksAppName = googleTasksAppName;
    }

    public String getGoogleTasksClientSecret() {
        return googleTasksClientSecret;
    }

    public void setGoogleTasksClientSecret(String googleTasksClientSecret) {
        this.googleTasksClientSecret = googleTasksClientSecret;
    }

    public String getGoogleTranslateApiKey() {
        return googleTranslateApiKey;
    }

    public void setGoogleTranslateApiKey(String googleTranslateApiKey) {
        this.googleTranslateApiKey = googleTranslateApiKey;
    }

    public String getIbmWatsonApiKey() {
        return ibmWatsonApiKey;
    }

    public void setIbmWatsonApiKey(String ibmWatsonApiKey) {
        this.ibmWatsonApiKey = ibmWatsonApiKey;
    }

    public String getIftttKey() {
        return iftttKey;
    }

    public void setIftttKey(String iftttKey) {
        this.iftttKey = iftttKey;
    }

    public String getJiraUserName() {
        return jiraUserName;
    }

    public void setJiraUserName(String jiraUserName) {
        this.jiraUserName = jiraUserName;
    }

    public String getJiraPassword() {
        return jiraPassword;
    }

    public void setJiraPassword(String jiraPassword) {
        this.jiraPassword = jiraPassword;
    }

    public String getJiraRepoURI() {
        return jiraRepoURI;
    }

    public void setJiraRepoURI(String jiraRepoURI) {
        this.jiraRepoURI = jiraRepoURI;
    }

    public String getOpenWeatherMapApiKey() {
        return openWeatherMapApiKey;
    }

    public void setOpenWeatherMapApiKey(String openWeatherMapApiKey) {
        this.openWeatherMapApiKey = openWeatherMapApiKey;
    }

    public String getPastebinDevelKey() {
        return pastebinDevelKey;
    }

    public void setPastebinDevelKey(String pastebinDevelKey) {
        this.pastebinDevelKey = pastebinDevelKey;
    }

    public String getRiotApiKey() {
        return riotApiKey;
    }

    public void setRiotApiKey(String riotApiKey) {
        this.riotApiKey = riotApiKey;
    }

    public String getSlackAccessToken() {
        return slackAccessToken;
    }

    public void setSlackAccessToken(String slackAccessToken) {
        this.slackAccessToken = slackAccessToken;
    }

    public String getTwitterConsumerKey() {
        return twitterConsumerKey;
    }

    public void setTwitterConsumerKey(String twitterConsumerKey) {
        this.twitterConsumerKey = twitterConsumerKey;
    }

    public String getTwitterConsumerSecret() {
        return twitterConsumerSecret;
    }

    public void setTwitterConsumerSecret(String twitterConsumerSecret) {
        this.twitterConsumerSecret = twitterConsumerSecret;
    }

    public String getTwitterAccessKey() {
        return twitterAccessKey;
    }

    public void setTwitterAccessKey(String twitterAccessKey) {
        this.twitterAccessKey = twitterAccessKey;
    }

    public String getTwitterAccessSecret() {
        return twitterAccessSecret;
    }

    public void setTwitterAccessSecret(String twitterAccessSecret) {
        this.twitterAccessSecret = twitterAccessSecret;
    }

    public String getVimeoAccessToken() {
        return vimeoAccessToken;
    }

    public void setVimeoAccessToken(String vimeoAccessToken) {
        this.vimeoAccessToken = vimeoAccessToken;
    }
}
