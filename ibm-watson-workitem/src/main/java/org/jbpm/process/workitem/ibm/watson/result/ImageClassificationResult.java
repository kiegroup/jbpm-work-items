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
package org.jbpm.process.workitem.ibm.watson.result;

import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifierResult;

public class ImageClassificationResult {

    private String classifierId;
    private String classifierName;
    private String className;
    private Float classScore;
    private String classTypeHierarchy;

    public ImageClassificationResult(ClassifierResult classifierResult,
                                     ClassResult classResult) {
        this.classifierId = classifierResult.getClassifierId();
        this.classifierName = classifierResult.getName();
        this.className = classResult.getClassName();
        this.classScore = classResult.getScore();
        this.classTypeHierarchy = classResult.getTypeHierarchy();
    }

    public String getClassifierId() {
        return classifierId;
    }

    public void setClassifierId(String classifierId) {
        this.classifierId = classifierId;
    }

    public String getClassifierName() {
        return classifierName;
    }

    public void setClassifierName(String classifierName) {
        this.classifierName = classifierName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Float getClassScore() {
        return classScore;
    }

    public void setClassScore(Float classScore) {
        this.classScore = classScore;
    }

    public String getClassTypeHierarchy() {
        return classTypeHierarchy;
    }

    public void setClassTypeHierarchy(String classTypeHierarchy) {
        this.classTypeHierarchy = classTypeHierarchy;
    }
}
