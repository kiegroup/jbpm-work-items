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
package org.jbpm.process.workitem.google.tasks;

import java.util.Date;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.TaskList;

public class TaskInfo {

    private String etag;
    private String id;
    private String kind;
    private String selfLink;
    private String title;
    private String updated;

    public TaskInfo(TaskList taskList) {
        this.etag = taskList.getEtag();
        this.id = taskList.getId();
        this.kind = taskList.getKind();
        this.selfLink = taskList.getSelfLink();
        this.title = taskList.getTitle();
        if (taskList.getUpdated() != null) {
            this.updated = taskList.getUpdated().toString();
        } else {
            this.updated = new DateTime(new Date()).toString();
        }
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }
}
