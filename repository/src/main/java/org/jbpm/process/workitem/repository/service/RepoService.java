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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.jbpm.process.workitem.repository.RepositoryEventListener;
import org.jbpm.process.workitem.repository.RepositoryStorage;
import org.jbpm.process.workitem.repository.ServiceTaskNotFoundException;
import org.jbpm.process.workitem.repository.storage.InMemoryRepositoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class RepoService {

    private static final Logger logger = LoggerFactory.getLogger(RepoService.class);

    private String serviceInfoVarDeclaration = "var serviceinfo = ";
    private List<RepoData> services;
    private List<RepoModule> modules = new ArrayList<>();

    private RepositoryStorage<?> storage = new InMemoryRepositoryStorage();

    private Set<RepositoryEventListener> listeners = new LinkedHashSet<>();

    public RepoService() {
        loadServices(null);
    }

    public RepoService(String jsonInput) {
        loadServices(jsonInput);
    }

    public RepoService(RepositoryStorage<?> storage,
                       RepositoryEventListener... eventListeners) {
        this.storage = storage;
        this.listeners.addAll(Arrays.asList(eventListeners));
        loadServices(null);
    }

    protected void loadServices(String jsonInput) {
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

            List<RepoData> currentServices = mapper.readValue(jsonInput,
                                                              new TypeReference<List<RepoData>>() {
                                                              });
            // remove all nulls from list if any (can happen due to trailing commas in json)
            currentServices.removeAll(Collections.singleton(null));
            // synchronize with the storage to make sure all services are up to date
            services = storage.synchronizeServices(currentServices);

            loadModules();
        } catch (Exception e) {
            logger.error("Unable to load service info: " + e.getMessage());
        }
    }

    protected void loadModules() {
        for (RepoData repoData : services) {
            boolean found = false;
            for (RepoModule repoModule : modules) {
                if (repoModule.getName().equals(repoData.getModule())) {
                    repoModule.addRepoData(repoData);
                    found = true;
                }
            }

            if (!found) {
                RepoModule repoModule = new RepoModule();
                repoModule.setName(repoData.getModule());
                repoModule.addRepoData(repoData);
                modules.add(repoModule);
            }
        }
    }
    
    /*
     * Service related operations
     */

    public List<RepoData> getServices() {
        return storage.loadServices(0,
                                    100);
    }

    public RepoData getServiceByName(String name) {
        List<RepoData> servicesByName = storage.loadServices((service) -> service.getName().equalsIgnoreCase(name),
                                                             0,
                                                             100);

        if (servicesByName.isEmpty()) {
            return null;
        }

        return servicesByName.get(0);
    }

    public void enableService(String serviceId) {
        for (RepoData service : services) {
            if (service.getId().equals(serviceId)) {
                service.setEnabled(true);

                storage.onEnabled(service);

                listeners.forEach(listener -> listener.onServiceTaskEnabled(service));
                return;
            }
        }
        throw new ServiceTaskNotFoundException("Service with id " + serviceId + " was not found");
    }

    public void disableService(String serviceId) {
        for (RepoData service : services) {
            if (service.getId().equals(serviceId)) {
                service.setEnabled(false);

                storage.onDisabled(service);

                listeners.forEach(listener -> listener.onServiceTaskDisabled(service));
                return;
            }
        }

        throw new ServiceTaskNotFoundException("Service with id " + serviceId + " was not found");
    }

    public void installService(String serviceId,
                               String target) {
        for (RepoData service : services) {
            if (service.getId().equals(serviceId)) {
                service.install(target);

                storage.onInstalled(service,
                                    target);
                listeners.forEach(listener -> listener.onServiceTaskInstalled(service,
                                                                              target));
                return;
            }
        }

        throw new ServiceTaskNotFoundException("Service with id " + serviceId + " was not found");
    }

    public void uninstallService(String serviceId,
                                 String target) {
        for (RepoData service : services) {
            if (service.getId().equals(serviceId)) {
                service.uninstall(target);

                storage.onUninstalled(service,
                                      target);
                listeners.forEach(listener -> listener.onServiceTaskUninstalled(service,
                                                                                target));
                return;
            }
        }

        throw new ServiceTaskNotFoundException("Service with id " + serviceId + " was not found");
    }

    public List<RepoData> getServicesByCategory(String category) {

        return storage.loadServices((service) -> service.getCategory().equalsIgnoreCase(category),
                                    0,
                                    100);
    }

    public List<RepoData> getTriggerServices() {

        return storage.loadServices((service) -> service.getIstrigger().equalsIgnoreCase("true"),
                                    0,
                                    100);
    }

    public List<RepoData> getActionServices() {

        return storage.loadServices((service) -> service.getIsaction().equalsIgnoreCase("true"),
                                    0,
                                    100);
    }
    
    /*
     * Module related operations
     */

    public List<RepoModule> getModules() {
        return modules;
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
                    enableService(rd.getId());
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
                    disableService(rd.getId());
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

    public void installModule(String moduleName,
                              String target) {
        modules.stream().forEach(rm -> {
            if (rm.getName().equals(moduleName)) {
                rm.setInstalled(true);
                // set all services in module as installed as well
                for (RepoData rd : rm.getRepoData()) {
                    installService(rd.getId(),
                                   target);
                }
            }
        });
    }

    public void uninstallModule(String moduleName,
                                String target) {
        modules.stream().forEach(rm -> {
            if (rm.getName().equals(moduleName)) {
                rm.setInstalled(false);
                // set all services in module as uninstalled as well
                for (RepoData rd : rm.getRepoData()) {
                    uninstallService(rd.getId(),
                                     target);
                }
            }
        });
    }
}
