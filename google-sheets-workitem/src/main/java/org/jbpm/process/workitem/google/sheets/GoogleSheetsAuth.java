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
package org.jbpm.process.workitem.google.sheets;

import java.io.StringReader;
import java.util.Collections;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;

public class GoogleSheetsAuth {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;

    public Sheets getSheetsService(String appName,
                                   String clientSecretJSON) {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = authorize(clientSecretJSON);
            return new Sheets.Builder(HTTP_TRANSPORT,
                                      JSON_FACTORY,
                                      credential)
                    .setApplicationName(appName)
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Credential authorize(String clientSecretJSON) throws Exception {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                                                                     new StringReader(clientSecretJSON));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                clientSecrets,
                Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY))
                .build();

        return new AuthorizationCodeInstalledApp(flow,
                                                 new LocalServerReceiver()).authorize("user");
    }
}
