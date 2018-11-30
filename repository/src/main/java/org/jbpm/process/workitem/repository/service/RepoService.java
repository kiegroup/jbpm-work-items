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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepoService {

    private static final Logger logger = LoggerFactory.getLogger(RepoService.class);

    private String serviceInfoVarDeclaration = "var serviceinfo = ";
    private List<RepoData> services;

    public RepoService() {

        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("serviceinfo.js");
            String jsonInput = IOUtils.toString(inputStream,
                                                StandardCharsets.UTF_8.name());
            if (jsonInput.startsWith(serviceInfoVarDeclaration)) {
                jsonInput = jsonInput.substring(serviceInfoVarDeclaration.length());
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_MISSING_VALUES,
                             true);
            mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED,
                             true);
            mapper.configure(JsonParser.Feature.ALLOW_MISSING_VALUES,
                             true);

            mapper.configure(SerializationFeature.INDENT_OUTPUT,
                             true);

            services = mapper.readValue(jsonInput,
                                        new TypeReference<List<RepoData>>() {
                                        });
            // remove all nulls from list if any (can happen due to trailing commas in json)
            services.removeAll(Collections.singleton(null));
        } catch (Exception e) {
            logger.error("Unable to load service info: " + e.getMessage());
        }
    }

    public List<RepoData> getServices() {
        return services;
    }

    public void setServices(List<RepoData> services) {
        this.services = services;
    }

    public RepoData getServiceByName(String name) {
        for (RepoData data : this.services) {
            if (data != null && data.getName() != null && data.getName().equalsIgnoreCase(name)) {
                return data;
            }
        }
        return null;
    }

    public List<RepoData> getServicesByCategory(String category) {
        List<RepoData> response = new ArrayList<>();
        for (RepoData data : this.services) {
            if (data != null && data.getCategory() != null && data.getCategory().equalsIgnoreCase(category)) {
                response.add(data);
            }
        }
        return response;
    }

    public List<RepoData> getTriggerServices() {
        List<RepoData> response = new ArrayList<>();

        for (RepoData data : this.services) {
            if (data != null && data.getIstrigger() != null && data.getIstrigger().equalsIgnoreCase("true")) {
                response.add(data);
            }
        }

        return response;
    }

    public List<RepoData> getActionServices() {
        List<RepoData> response = new ArrayList<>();
        for (RepoData data : this.services) {
            if (data != null && data.getIsaction() != null && data.getIsaction().equalsIgnoreCase("true")) {
                response.add(data);
            }
        }
        return response;
    }
}
