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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Note:
 * 1) "services" are equivalent to "workitem handlers"
 * 2) "modules" are equivalent to "workitem groups", one workitem group can include many workitem handlers
 */
public class RepoService {

    private static final Logger logger = LoggerFactory.getLogger(RepoService.class);

    private String serviceInfoVarDeclaration = "var serviceinfo = ";
    private List<RepoData> services;
    private List<RepoModule> modules = new ArrayList<>();

    public RepoService() {
        loadServices(null);
    }

    public RepoService(String jsonInput) {
        loadServices(jsonInput);
    }

    private void loadServices(String jsonInput) {
        try {
            if (jsonInput == null || jsonInput.length() < 1) {
                InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("serviceinfo.js");
                jsonInput = IOUtils.toString(inputStream,
                                             StandardCharsets.UTF_8.name());
            }

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

            loadModules();
        } catch (Exception e) {
            logger.error("Unable to load service info: " + e.getMessage());
        }
    }

    private void loadModules() {
        for (RepoData repoData : services) {
            boolean found = false;
            for (RepoModule repoModule : modules) {
                if (repoModule.getName().equals(repoData.getModule())) {
                    repoModule.addRepoData(repoData);
                    repoModule.setVersion(repoData.getVersion());
                    found = true;
                }
            }

            if (!found) {
                RepoModule repoModule = new RepoModule();
                repoModule.setName(repoData.getModule());
                repoModule.setVersion(repoData.getVersion());
                repoModule.addRepoData(repoData);
                modules.add(repoModule);
            }
        }
    }

    public List<RepoData> getServices() {
        return services;
    }

    public List<RepoModule> getModules() {
        return modules;
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

    public RepoModule getModuleByName(String name) {
        for (RepoModule repoModule : this.modules) {
            if (repoModule != null && repoModule.getName() != null && repoModule.getName().equals(name)) {
                return repoModule;
            }
        }

        return null;
    }

    public List<RepoModule> getEnabledModules() {
        List<RepoModule> retList = modules.stream()
                .filter(rm -> rm.isEnabled())
                .collect(Collectors.toList());

        return retList;
    }

    public List<RepoModule> getDisabledModules() {
        List<RepoModule> retList = modules.stream()
                .filter(rm -> !rm.isEnabled())
                .collect(Collectors.toList());

        return retList;
    }

    public void enableModule(String moduleName) {
        modules.stream().forEach(rm -> {
            if (rm.getName().equals(moduleName)) {
                rm.setEnabled(true);
                // set all services in module as enabled as well
                for (RepoData rd : rm.getRepoData()) {
                    rd.setEnabled(true);
                }
            }
        });
    }

    public void disableModule(String moduleName) {
        modules.stream().forEach(rm -> {
            if (rm.getName().equals(moduleName)) {
                rm.setEnabled(false);
                // set all services in module as disabled as well
                for (RepoData rd : rm.getRepoData()) {
                    rd.setEnabled(false);
                }
            }
        });
    }

    public List<RepoModule> getInstalledModules() {
        List<RepoModule> retList = modules.stream()
                .filter(rm -> rm.isInstalled())
                .collect(Collectors.toList());

        return retList;
    }

    public List<RepoModule> getUninstalledModules() {
        List<RepoModule> retList = modules.stream()
                .filter(rm -> !rm.isInstalled())
                .collect(Collectors.toList());

        return retList;
    }

    public void installModule(String moduleName) {
        modules.stream().forEach(rm -> {
            if (rm.getName().equals(moduleName)) {
                rm.setInstalled(true);
                // set all services in module as installed as well
                for (RepoData rd : rm.getRepoData()) {
                    rd.setInstalled(true);
                }
            }
        });
    }

    public void uninstallModule(String moduleName) {
        modules.stream().forEach(rm -> {
            if (rm.getName().equals(moduleName)) {
                rm.setInstalled(false);
                // set all services in module as uninstalled as well
                for (RepoData rd : rm.getRepoData()) {
                    rd.setInstalled(false);
                }
            }
        });
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

    public URL getModuleDefaultBPMN2URL(String moduleName) {
        RepoModule repoModule = getModuleByName(moduleName);

        if (repoModule == null) {
            return null;
        }

        return Thread.currentThread().getContextClassLoader().getResource(repoModule.getName() +
                                                                                  FileSystems.getDefault().getSeparator() +
                                                                                  repoModule.getName() + ".bpmn2");
    }

    public URL getModuleDefaultBPMN2URL(String moduleName,
                                        ClassLoader classLoader) {
        RepoModule repoModule = getModuleByName(moduleName);

        if (repoModule == null) {
            return null;
        }

        return classLoader.getResource(repoModule.getName() +
                                               FileSystems.getDefault().getSeparator() +
                                               repoModule.getName() + ".bpmn2");
    }

    public URL getModuleWidURL(String moduleName) {
        RepoModule repoModule = getModuleByName(moduleName);

        if (repoModule == null) {
            return null;
        }

        return Thread.currentThread().getContextClassLoader().getResource(repoModule.getName() +
                                                                                  FileSystems.getDefault().getSeparator() +
                                                                                  repoModule.getName() + ".wid");
    }

    public URL getModuleWidURL(String moduleName,
                               ClassLoader classLoader) {
        RepoModule repoModule = getModuleByName(moduleName);

        if (repoModule == null) {
            return null;
        }

        return classLoader.getResource(repoModule.getName() +
                                               FileSystems.getDefault().getSeparator() +
                                               repoModule.getName() + ".wid");
    }

    public URL getModuleJarURL(String moduleName) {
        RepoModule repoModule = getModuleByName(moduleName);

        if (repoModule == null) {
            return null;
        }

        return Thread.currentThread().getContextClassLoader().getResource(repoModule.getName() +
                                                                                  FileSystems.getDefault().getSeparator() +
                                                                                  repoModule.getName() + "-" +
                                                                                  repoModule.getVersion() + ".jar");
    }

    public URL getModuleJarURL(String moduleName,
                               ClassLoader classLoader) {
        RepoModule repoModule = getModuleByName(moduleName);

        if (repoModule == null) {
            return null;
        }

        return classLoader.getResource(repoModule.getName() +
                                               FileSystems.getDefault().getSeparator() +
                                               repoModule.getName() + "-" +
                                               repoModule.getVersion() + ".jar");
    }

    public URL getModuleIconURL(String moduleName) {
        RepoModule repoModule = getModuleByName(moduleName);

        if (repoModule == null) {
            return null;
        }

        return Thread.currentThread().getContextClassLoader().getResource(repoModule.getName() +
                                                                                  FileSystems.getDefault().getSeparator() +
                                                                                  "icon.png");
    }

    public URL getModuleIconURL(String moduleName,
                                ClassLoader classLoader) {
        RepoModule repoModule = getModuleByName(moduleName);

        if (repoModule == null) {
            return null;
        }

        return classLoader.getResource(repoModule.getName() +
                                               FileSystems.getDefault().getSeparator() +
                                               "icon.png");
    }

    public URL getModuleDeploymentDescriptorURL(String moduleName) {
        RepoModule repoModule = getModuleByName(moduleName);

        if (repoModule == null) {
            return null;
        }

        return Thread.currentThread().getContextClassLoader().getResource(repoModule.getName() +
                                                                                  FileSystems.getDefault().getSeparator() +
                                                                                  "kie-deployment-descriptor.xml");
    }

    public URL getModuleDeploymentDescriptorURL(String moduleName,
                                                ClassLoader classLoader) {
        RepoModule repoModule = getModuleByName(moduleName);

        if (repoModule == null) {
            return null;
        }

        return classLoader.getResource(repoModule.getName() +
                                               FileSystems.getDefault().getSeparator() +
                                               "kie-deployment-descriptor.xml");
    }
}
