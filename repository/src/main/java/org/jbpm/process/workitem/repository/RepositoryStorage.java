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

package org.jbpm.process.workitem.repository;

import java.util.List;
import java.util.function.Predicate;

import org.jbpm.process.workitem.repository.service.RepoData;

public interface RepositoryStorage<T> {

    /**
     * Responsible for synchronizing internal state of the storage with currently available services
     * in the repository. This is the "default" set of services which will differ between releases
     * and thus must be kept in sync with the storage.
     * 
     * @param currentServices set of services that the repository comes with
     * 
     * @return complete set of services in the storage
     */
    List<RepoData> synchronizeServices(List<RepoData> currentServices);
    
    /**
     * Loads available services with pagination.
     * @param start start position to get the services from
     * @param offset number of services to retrieve
     * @return list of found services
     */
    List<RepoData> loadServices(int start, int offset);
    
    /**
     * Loads available services with pagination and filter
     * @param predicate filter criteria to load services
     * @param start start position to get the services from
     * @param offset number of services to retrieve
     * @return list of found services
     */
    List<RepoData> loadServices(Predicate<RepoData> predicate, int start, int offset);
    
    /**
     * Performs processing of enabled service
     * @param service service that was enabled
     */
    void onEnabled(RepoData service);
    
    /**
     * Performs processing of disabled service
     * @param service service that was disabled
     */
    void onDisabled(RepoData service);
    
    /**
     * Performs processing of installed service
     * @param service service that was installed
     * @param target component which the service was installed to
     */
    void onInstalled(RepoData service, String target);
    
    /**
     * Performs processing of uninstalled service
     * @param service service that was uninstalled
     * @param target component which the service was uninstalled from
     */
    void onUninstalled(RepoData service, String target);
    
    /**
     * Loads service repository configuration of custom type
     * @return returns loaded configuration
     */
    T loadConfiguration();
    
    /**
     * Stores repository configuration of custom type
     * @param configuration configuration to be stored
     */
    void storeConfiguration(T configuration);
}
