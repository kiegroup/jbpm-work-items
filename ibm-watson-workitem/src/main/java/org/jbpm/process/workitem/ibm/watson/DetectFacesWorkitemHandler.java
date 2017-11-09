/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.DetectFacesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.DetectedFaces;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.Face;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ImageWithFaces;
import org.jbpm.document.Document;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.ibm.watson.result.FaceDetectionResult;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "DetectFacesDefinitions.wid", name = "IBMWatsonDetectFaces",
        displayName = "IBMWatsonDetectFaces",
        defaultHandler = "mvel: new org.jbpm.process.workitem.ibm.watson.DetectFacesWorkitemHandler()",
        parameters = {
                @WidParameter(name = "ImageToDetect")
        },
        results = {
                @WidResult(name = "Detection")
        },
        mavenDepends = {
                @WidMavenDepends(group = "com.ibm.watson.developer_cloud", artifact = "java-sdk", version = "4.0.0")
        })
public class DetectFacesWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(DetectFacesWorkitemHandler.class);
    private static final String RESULT_VALUE = "Detection";

    private WatsonAuth auth = new WatsonAuth();
    private String apiKey;

    public DetectFacesWorkitemHandler(String apiKey) {
        this.apiKey = apiKey;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        Document detectionImage = (Document) workItem.getParameter("ImageToDetect");
        Map<String, Object> widResults = new HashMap<String, Object>();

        if (detectionImage != null) {

            try {
                VisualRecognition service = auth.getService(apiKey);

                ByteArrayInputStream imageStream = new ByteArrayInputStream(detectionImage.getContent());

                DetectFacesOptions detectFacesOptions = new DetectFacesOptions.Builder()
                        .imagesFile(imageStream)
                        .build();
                DetectedFaces result = service.detectFaces(detectFacesOptions).execute();
                if (result == null || result.getImages() == null || result.getImages().size() < 1) {
                    logger.error("Unable to detect faces on provided image.");
                    workItemManager.abortWorkItem(workItem.getId());
                } else {
                    List<FaceDetectionResult> resultList = new ArrayList<>();
                    for (ImageWithFaces imageWithFaces : result.getImages()) {
                        for (Face face : imageWithFaces.getFaces()) {
                            resultList.add(new FaceDetectionResult(imageWithFaces,
                                                                   face));
                        }
                    }

                    widResults.put(RESULT_VALUE,
                                   resultList);
                    workItemManager.completeWorkItem(workItem.getId(),
                                                     widResults);
                }
            } catch (Exception e) {
                handleException(e);
            }
        } else {
            logger.error("Missing image for face detection.");
            throw new IllegalArgumentException("Missing image for face detection.");
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
