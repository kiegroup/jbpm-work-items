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

import java.util.List;

public class RepoData {

    private String name;
    private String displayName;
    private String defaultHandler;
    private String documentation;
    private String icon;
    private String category;
    private String description;
    private List<String> keywords;
    private String isaction;
    private String istrigger;
    private String actiontitle;
    private String triggertitle;
    private List<RepoParameter> parameters;
    private List<RepoResult> results;
    private List<RepoMavenDepend> mavenDependencies;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDefaultHandler() {
        return defaultHandler;
    }

    public void setDefaultHandler(String defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getIsaction() {
        return isaction == null ? "" : isaction;
    }

    public void setIsaction(String isaction) {
        this.isaction = isaction;
    }

    public String getActiontitle() {
        return actiontitle;
    }

    public void setActiontitle(String actiontitle) {
        this.actiontitle = actiontitle;
    }

    public List<RepoParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<RepoParameter> parameters) {
        this.parameters = parameters;
    }

    public List<RepoResult> getResults() {
        return results;
    }

    public void setResults(List<RepoResult> results) {
        this.results = results;
    }

    public List<RepoMavenDepend> getMavenDependencies() {
        return mavenDependencies;
    }

    public void setMavenDependencies(List<RepoMavenDepend> mavenDependencies) {
        this.mavenDependencies = mavenDependencies;
    }

    public String getIstrigger() {
        return istrigger == null ? "" : istrigger;
    }

    public void setIstrigger(String istrigger) {
        this.istrigger = istrigger;
    }

    public String getTriggertitle() {
        return triggertitle;
    }

    public void setTriggertitle(String triggertitle) {
        this.triggertitle = triggertitle;
    }
}
