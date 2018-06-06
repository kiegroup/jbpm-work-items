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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "GoogleReadSheetValuesDefinitions.wid", name = "GoogleReadSheetValues",
        displayName = "GoogleReadSheetValues",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.sheets.ReadSheetValuesWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "SheetId", required = true),
                @WidParameter(name = "Range", required = true),
        },
        results = {
                @WidResult(name = "SheetValues")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "google,sheets,read",
                action = @WidAction(title = "Read spreadsheet using Google Sheets")
        ))
public class ReadSheetValuesWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReadSheetValuesWorkitemHandler.class);
    private static final String RESULTS_VALUES = "SheetValues";

    private GoogleSheetsAuth auth = new GoogleSheetsAuth();
    private String appName;
    private String clientSecret;

    public ReadSheetValuesWorkitemHandler(String appName,
                                          String clientSecret) {
        this.appName = appName;
        this.clientSecret = clientSecret;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        Map<String, Object> results = new HashMap<String, Object>();
        String paramSheetId = (String) workItem.getParameter("SheetId");
        // to learn google spreadsheet ranges go to
        // https://developers.google.com/sheets/api/guides/concepts
        String paramRange = (String) workItem.getParameter("Range");

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Sheets service = auth.getSheetsService(appName,
                                                   clientSecret);
            ValueRange sheetResponse = service.spreadsheets().values()
                    .get(paramSheetId,
                         paramRange)
                    .execute();

            List<List<Object>> values = sheetResponse.getValues();

            results.put(RESULTS_VALUES,
                        values);

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setAuth(GoogleSheetsAuth auth) {
        this.auth = auth;
    }
}
