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
package org.jbpm.process.workitem.repository.service;

import java.util.ArrayList;
import java.util.List;

public class RepoModule {
    private String name;
    private boolean enabled;
    private boolean installed;

    public RepoModule() {
        this.enabled = true;
        this.installed = false;
    }

    private List<RepoData> repoData = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RepoData> getRepoData() {
        return repoData;
    }

    public void setRepoData(List<RepoData> repoData) {
        this.repoData = repoData;
    }

    public void addRepoData(RepoData newRepoData) {
        this.repoData.add(newRepoData);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }
}
