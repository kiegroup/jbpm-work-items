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
package org.jbpm.process.workitem.repositoryspringboot;

import java.util.List;

import org.jbpm.process.workitem.repository.service.RepoData;
import org.jbpm.process.workitem.repository.service.RepoMavenDepend;
import org.jbpm.process.workitem.repository.service.RepoParameter;
import org.jbpm.process.workitem.repository.service.RepoResult;
import org.jbpm.process.workitem.repository.service.RepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RepositoryController {

    @Autowired
    private RepoService repoService;

    @RequestMapping(value = "/rest/services", method = RequestMethod.GET, produces = "application/json")
    public List<RepoData> getAllServices() {
        try {
            return repoService.getServices();
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
    }

    @RequestMapping(value = "/rest/services/count", method = RequestMethod.GET, produces = "application/json")
    public int getAllServicesCount() {
        try {
            return repoService.getServices().size();
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
    }

    @RequestMapping(value = "/rest/services/{name}", method = RequestMethod.GET, produces = "application/json")
    public RepoData getServiceByName(@PathVariable("name") String name) {
        try {
            return repoService.getServiceByName(name);
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
    }

    @RequestMapping(value = "/rest/services/{name}/parameters", method = RequestMethod.GET, produces = "application/json")
    public List<RepoParameter> getParametersByServiceName(@PathVariable("name") String name) {
        try {
            return repoService.getServiceByName(name).getParameters();
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
    }

    @RequestMapping(value = "/rest/services/{name}/results", method = RequestMethod.GET, produces = "application/json")
    public List<RepoResult> getResultsByServiceName(@PathVariable("name") String name) {
        try {
            return repoService.getServiceByName(name).getResults();
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
    }

    @RequestMapping(value = "/rest/services/{name}/mavendepends", method = RequestMethod.GET, produces = "application/json")
    public List<RepoMavenDepend> getMavenDependsByServiceName(@PathVariable("name") String name) {
        try {
            return repoService.getServiceByName(name).getMavenDependencies();
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
    }

    @RequestMapping(value = "/rest/services/category/{category}", method = RequestMethod.GET, produces = "application/json")
    public List<RepoData> getResultsByServiceCategory(@PathVariable("category") String category) {
        try {
            return repoService.getServicesByCategory(category);
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
    }

    @RequestMapping(value = "/rest/servicetriggers", method = RequestMethod.GET, produces = "application/json")
    public List<RepoData> getTriggerServices() {
        try {
            return repoService.getTriggerServices();
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IllegalArgumentException(t);
        }
    }

    @RequestMapping(value = "/rest/serviceactions", method = RequestMethod.GET, produces = "application/json")
    public List<RepoData> getActionServices() {
        try {
            return repoService.getActionServices();
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
    }

    @RequestMapping(value = "/rest/servicetriggers/count", method = RequestMethod.GET, produces = "application/json")
    public int getTriggerServicesCount() {
        try {
            return repoService.getTriggerServices().size();
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
    }

    @RequestMapping(value = "/rest/serviceactions/count", method = RequestMethod.GET, produces = "application/json")
    public int getActionServicesCount() {
        try {
            return repoService.getActionServices().size();
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
    }
}
