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

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.Translation;
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

@Wid(widfile = "GoogleTranslateDefinitions.wid", name = "GoogleTranslate",
        displayName = "GoogleTranslate",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.translate.TranslateWorkitemHandler(\"apiKey\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "icon.png",
        parameters = {
                @WidParameter(name = "ToTranslate", required = true),
                @WidParameter(name = "SourceLang", required = true),
                @WidParameter(name = "TargetLang", required = true)
        },
        results = {
                @WidResult(name = "Translation")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "google,translate,language",
                action = @WidAction(title = "Translate text to a different language"),
                authinfo = @WidAuth(required = true, params = {"apiKey"},
                        paramsdescription = {"Google cloud api key"},
                        referencesite = "https://cloud.google.com/translate/docs/quickstart")
        ))
public class TranslateWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(TranslateWorkitemHandler.class);

    private static final String RESULTS_TRANSLATION = "Translation";

    private GoogleTranslateAuth googleTranslateAuth = new GoogleTranslateAuth();
    private Translate translationService;
    private String apiKey;

    public TranslateWorkitemHandler(String apiKey) {
        this.apiKey = apiKey;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String toTranslate = (String) workItem.getParameter("ToTranslate");
            String sourceLang = (String) workItem.getParameter("SourceLang");
            String targetLang = (String) workItem.getParameter("TargetLang");
            Map<String, Object> results = new HashMap<String, Object>();

            translationService = googleTranslateAuth.getTranslationService(apiKey);

            TranslateOption srcLangOption = TranslateOption.sourceLanguage(sourceLang);
            TranslateOption targetLangOption = TranslateOption.targetLanguage(targetLang);

            Translation translation = translationService.translate(toTranslate,
                                                                   srcLangOption,
                                                                   targetLangOption);

            if (translation != null && translation.getTranslatedText() != null) {
                results.put(RESULTS_TRANSLATION,
                            translation.getTranslatedText());
            } else {
                logger.error("Could not translate provided text.");
                throw new IllegalArgumentException("Could not translate provided text.");
            }

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