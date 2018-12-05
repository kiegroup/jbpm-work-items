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
package org.jbpm.process.workitem.google.translate;

import java.util.HashMap;
import java.util.Map;

import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
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

@Wid(widfile = "GoogleDetectLanguageDefinitions.wid", name = "GoogleDetectLanguage",
        displayName = "GoogleDetectLanguage",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.translate.DetectLanguageWorkitemHandler(\"apiKey\")",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "ToDetectText", required = true)
        },
        results = {
                @WidResult(name = "DetectedLanguage")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "google,translate,detect,language",
                action = @WidAction(title = "Detect the language of provided text"),
                authinfo = @WidAuth(required = true, params = {"apiKey"},
                        paramsdescription = {"Google cloud api key"},
                        referencesite = "https://cloud.google.com/translate/docs/quickstart")
        ))
public class DetectLanguageWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(DetectLanguageWorkitemHandler.class);

    private static final String RESULTS_DETECTION = "DetectedLanguage";

    private GoogleTranslateAuth googleTranslateAuth = new GoogleTranslateAuth();
    private Translate translationService;
    private String apiKey;

    public DetectLanguageWorkitemHandler(String apiKey) {
        this.apiKey = apiKey;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String toDetectText = (String) workItem.getParameter("ToDetectText");
            Map<String, Object> results = new HashMap<String, Object>();

            translationService = googleTranslateAuth.getTranslationService(apiKey);
            Detection detection = translationService.detect(toDetectText);

            results.put(RESULTS_DETECTION,
                        detection.getLanguage());

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
        } catch (Exception e) {
            logger.error("Error executing workitem: " + e.getMessage());
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setTranslationAuth(GoogleTranslateAuth googleTranslateAuth) {
        this.googleTranslateAuth = googleTranslateAuth;
    }
}