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

package org.jbpm.process.workitem.repository.storage;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jbpm.process.workitem.repository.RepositoryStorage;
import org.jbpm.process.workitem.repository.service.RepoData;


public class InMemoryRepositoryStorage implements RepositoryStorage {

    protected List<RepoData> services;
    
    @Override
    public List<RepoData> synchronizeServices(List<RepoData> currentServices) {
        this.services = currentServices;
        this.services.forEach(service -> enforceId(service));
        return this.services;
    }

    @Override
    public List<RepoData> loadServices(int start, int offset) {
        return services.stream().skip(start).limit(offset).collect(Collectors.toList());
    }

    @Override
    public List<RepoData> loadServices(Predicate<RepoData> predicate, int start, int offset) {
        return services.stream().filter(predicate).skip(start).limit(offset).collect(Collectors.toList());
    }

    @Override
    public void onEnabled(RepoData service) {
        // no op

    }

    @Override
    public void onDisabled(RepoData service) {
        // no op

    }

    @Override
    public void onInstalled(RepoData service, String target) {
        // no op

    }

    @Override
    public void onUninstalled(RepoData service, String target) {
        // no op

    }
    
    protected void enforceId(RepoData service) {
        if (service.getId() == null) {
            try {
                service.setId(UUID.nameUUIDFromBytes(service.getName().getBytes("UTF-8")).toString());
            } catch (UnsupportedEncodingException e) {                
            }
        }
    }

}
