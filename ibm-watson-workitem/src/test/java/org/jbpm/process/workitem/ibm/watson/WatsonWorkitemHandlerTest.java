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

import java.util.ArrayList;
import java.util.List;

import com.ibm.watson.developer_cloud.http.ServiceCall;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImage;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifierResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.DetectFacesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.DetectedFaces;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.Face;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.FaceAge;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.FaceGender;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.FaceIdentity;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ImageWithFaces;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.document.service.impl.DocumentImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.process.workitem.ibm.watson.result.FaceDetectionResult;
import org.jbpm.process.workitem.ibm.watson.result.ImageClassificationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WatsonWorkitemHandlerTest {

    @Mock
    WatsonAuth auth;

    @Mock
    VisualRecognition associationService;

    @Mock
    ServiceCall<ClassifiedImages> associationServiceCall;

    @Mock
    ClassifiedImages assoiationClassifiedImages;

    @Mock
    ClassifiedImage associationClassifiedImage;

    @Mock
    ClassifierResult associationClassifierResult;

    @Mock
    VisualRecognition recognitionService;

    @Mock
    ServiceCall<DetectedFaces> recognitionServiceCall;

    @Mock
    DetectedFaces recognitionDetectFaces;

    @Mock
    ImageWithFaces recognitionImageWithFaces;

    //@Mock
    //Face recognitionFace;

    @Before
    public void setUp() {

        // image classification
        ClassResult classResultObj = new ClassResult();
        classResultObj.setClassName("testClassName");
        classResultObj.setScore(new Float(1));
        classResultObj.setTypeHierarchy("testTypeHierarchy");

        List<ClassifiedImage> classifiedImageList = new ArrayList<>();
        classifiedImageList.add(associationClassifiedImage);

        List<ClassifierResult> classifierResults = new ArrayList<>();
        classifierResults.add(associationClassifierResult);

        List<ClassResult> classResults = new ArrayList<>();
        classResults.add(classResultObj);

        when(associationService.classify(any(ClassifyOptions.class))).thenReturn(associationServiceCall);
        when(associationServiceCall.execute()).thenReturn(assoiationClassifiedImages);
        when(assoiationClassifiedImages.getImages()).thenReturn(classifiedImageList);
        when(associationClassifiedImage.getClassifiers()).thenReturn(classifierResults);
        when(associationClassifierResult.getClasses()).thenReturn(classResults);
        when(associationClassifierResult.getClassifierId()).thenReturn("testClassifierId");

        // face detection
        List<ImageWithFaces> recognitionImageList = new ArrayList<>();
        recognitionImageList.add(recognitionImageWithFaces);

        FaceAge recognitionFaceAge = new FaceAge();
        recognitionFaceAge.setMin(20);
        recognitionFaceAge.setMax(35);

        FaceGender recognitionFaceGender = new FaceGender();
        recognitionFaceGender.setGender("male");

        FaceIdentity recognitionFaceIdentity = new FaceIdentity();
        recognitionFaceIdentity.setName("testPerson");

        Face recognitionFace = new Face();
        recognitionFace.setAge(recognitionFaceAge);
        recognitionFace.setGender(recognitionFaceGender);
        recognitionFace.setIdentity(recognitionFaceIdentity);

        List<Face> recognitionFaceList = new ArrayList<>();
        recognitionFaceList.add(recognitionFace);

        when(recognitionService.detectFaces(any(DetectFacesOptions.class))).thenReturn(recognitionServiceCall);
        when(recognitionServiceCall.execute()).thenReturn(recognitionDetectFaces);
        when(recognitionDetectFaces.getImages()).thenReturn(recognitionImageList);
        when(recognitionImageWithFaces.getFaces()).thenReturn(recognitionFaceList);
        when(recognitionImageWithFaces.getImage()).thenReturn("testImage");
    }

    @Test
    public void testClassifyImage() throws Exception {
        when(auth.getService(anyString())).thenReturn(associationService);

        TestWorkItemManager manager = new TestWorkItemManager();
        DocumentImpl imageToClassify = new DocumentImpl();
        imageToClassify.setName("testImageToClassify.png");
        imageToClassify.setContent(new String("testImageContent").getBytes());

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ImageToClassify",
                              imageToClassify);

        ClassifyImageWorkitemHandler handler = new ClassifyImageWorkitemHandler("{testApiKey}");
        handler.setAuth(auth);
        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("Classification") instanceof List);

        List<ImageClassificationResult> returnValues = (List<ImageClassificationResult>) (manager.getResults().get(workItem.getId())).get("Classification");
        assertNotNull(returnValues);
        assertEquals(1,
                     returnValues.size());
        ImageClassificationResult result = returnValues.get(0);

        assertTrue(result.getClassScore() == 1);
        assertEquals("testClassName",
                     result.getClassName());
        assertEquals("testTypeHierarchy",
                     result.getClassTypeHierarchy());
    }

    @Test
    public void testDetectFaces() throws Exception {
        when(auth.getService(anyString())).thenReturn(recognitionService);

        TestWorkItemManager manager = new TestWorkItemManager();
        DocumentImpl imagetoDetect = new DocumentImpl();
        imagetoDetect.setName("testImageToDetect.png");
        imagetoDetect.setContent(new String("testImageContent").getBytes());
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ImageToDetect",
                              imagetoDetect);

        DetectFacesWorkitemHandler handler = new DetectFacesWorkitemHandler("{testApiKey}");
        handler.setAuth(auth);
        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("Detection") instanceof List);

        List<FaceDetectionResult> returnValues = (List<FaceDetectionResult>) (manager.getResults().get(workItem.getId())).get("Detection");
        assertNotNull(returnValues);
        assertEquals(1,
                     returnValues.size());

        FaceDetectionResult result = returnValues.get(0);

        assertTrue(result.getMinAge() == 20);
        assertTrue(result.getMaxAge() == 35);
        assertEquals("male",
                     result.getGender());
        assertEquals("testPerson",
                     result.getIdentity());
    }
}
