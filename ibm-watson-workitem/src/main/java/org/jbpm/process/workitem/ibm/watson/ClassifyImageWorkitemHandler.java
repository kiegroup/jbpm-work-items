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
package org.jbpm.process.workitem.ibm.watson;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImage;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifierResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ErrorInfo;
import org.jbpm.document.Document;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.ibm.watson.result.ImageClassificationResult;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "ClassifyImageDefinitions.wid", name = "IBMWatsonClassifyImage",
        displayName = "IBMWatsonClassifyImage",
        defaultHandler = "mvel: new org.jbpm.process.workitem.ibm.watson.ClassifyImageWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "ImageToClassify", required = true)
        },
        results = {
                @WidResult(name = "Classification")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        })
public class ClassifyImageWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClassifyImageWorkitemHandler.class);
    private static final String RESULT_VALUE = "Classification";
    private WatsonAuth auth = new WatsonAuth();
    private String apiKey;

    public ClassifyImageWorkitemHandler(String apiKey) {
        this.apiKey = apiKey;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Document classificationImage = (Document) workItem.getParameter("ImageToClassify");

            Map<String, Object> widResults = new HashMap<String, Object>();


            VisualRecognition service = auth.getService(apiKey);

            ByteArrayInputStream imageStream = new ByteArrayInputStream(classificationImage.getContent());

            ClassifyOptions classifyOptions = new ClassifyOptions.Builder()
                    .imagesFile(imageStream)
                    .imagesFilename(classificationImage.getName())
                    .parameters("{\"owners\": [\"me\"]}")
                    .build();
            ClassifiedImage result = service.classify(classifyOptions).execute().getImages().get(0);
            if (result.getError() != null) {
                ErrorInfo errorInfo = result.getError();
                logger.error("Error classifying image: " + errorInfo.getDescription());
                workItemManager.abortWorkItem(workItem.getId());
            } else {
                List<ImageClassificationResult> resultList = new ArrayList<>();
                for (ClassifierResult classification : result.getClassifiers()) {
                    for (ClassResult classResult : classification.getClasses()) {
                        resultList.add(new ImageClassificationResult(classification,
                                                                     classResult));
                    }

                    widResults.put(RESULT_VALUE,
                                   resultList);
                }
                workItemManager.completeWorkItem(workItem.getId(),
                                                 widResults);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setAuth(WatsonAuth auth) {
        this.auth = auth;
    }
}
