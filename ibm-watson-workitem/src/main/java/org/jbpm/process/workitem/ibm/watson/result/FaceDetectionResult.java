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

import com.ibm.watson.developer_cloud.visual_recognition.v3.model.Face;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ImageWithFaces;

public class FaceDetectionResult {

    private String image;
    private Long minAge;
    private Long maxAge;
    private Float ageScore;
    private String gender;
    private Float genderScore;
    private String identity;
    private Float identityScore;

    public FaceDetectionResult(ImageWithFaces imageWithFaces,
                               Face face) {
        this.image = imageWithFaces.getImage();
        this.minAge = face.getAge().getMin();
        this.maxAge = face.getAge().getMax();
        this.ageScore = face.getAge().getScore();
        this.gender = face.getGender().getGender();
        this.genderScore = face.getGender().getScore();
        this.identity = face.getIdentity().getName();
        this.identityScore = face.getIdentity().getScore();
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getMinAge() {
        return minAge;
    }

    public void setMinAge(Long minAge) {
        this.minAge = minAge;
    }

    public Long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Long maxAge) {
        this.maxAge = maxAge;
    }

    public Float getAgeScore() {
        return ageScore;
    }

    public void setAgeScore(Float ageScore) {
        this.ageScore = ageScore;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Float getGenderScore() {
        return genderScore;
    }

    public void setGenderScore(Float genderScore) {
        this.genderScore = genderScore;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public Float getIdentityScore() {
        return identityScore;
    }

    public void setIdentityScore(Float identityScore) {
        this.identityScore = identityScore;
    }
}
