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
package org.jbpm.process.workitem.repository.wildfly;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import org.jbpm.process.workitem.repository.service.RepoService;

@Path("/")
public class RepositoryService {

    @Inject
    private RepoService repoService;

    @GET
    @Path("/services")
    @Produces({MediaType.APPLICATION_JSON})
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    public Response getAllServices() {
        try {
            Response.ResponseBuilder responseBuilder = Response.ok(repoService.getServices());
            return responseBuilder.build();
        } catch (Exception e) {
            throw new javax.ws.rs.NotFoundException();
        }
    }

    @GET
    @Path("/services/count")
    @Produces({MediaType.APPLICATION_JSON})
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    public Response getAllServicesCount() {
        try {
            Response.ResponseBuilder responseBuilder = Response.ok(repoService.getServices().size());
            return responseBuilder.build();
        } catch (Exception e) {
            throw new javax.ws.rs.NotFoundException();
        }
    }

    @GET
    @Path("/services/{name}")
    @Produces({MediaType.APPLICATION_JSON})
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    public Response getServiceByName(@PathParam("name") String name) {
        try {
            Response.ResponseBuilder responseBuilder = Response.ok(repoService.getServiceByName(name));
            return responseBuilder.build();
        } catch (Exception e) {
            throw new javax.ws.rs.NotFoundException();
        }
    }

    @GET
    @Path("/services/{name}/parameters")
    @Produces({MediaType.APPLICATION_JSON})
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    public Response getParametersByServiceName(@PathParam("name") String name) {
        try {
            Response.ResponseBuilder responseBuilder = Response.ok(repoService.getServiceByName(name).getParameters());
            return responseBuilder.build();
        } catch (Exception e) {
            throw new javax.ws.rs.NotFoundException();
        }
    }

    @GET
    @Path("/services/{name}/results")
    @Produces({MediaType.APPLICATION_JSON})
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    public Response getResultsByServiceName(@PathParam("name") String name) {
        try {
            Response.ResponseBuilder responseBuilder = Response.ok(repoService.getServiceByName(name).getResults());
            return responseBuilder.build();
        } catch (Exception e) {
            throw new javax.ws.rs.NotFoundException();
        }
    }

    @GET
    @Path("/services/{name}/mavendepends")
    @Produces({MediaType.APPLICATION_JSON})
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    public Response getMavenDependsByServiceName(@PathParam("name") String name) {
        try {
            Response.ResponseBuilder responseBuilder = Response.ok(repoService.getServiceByName(name).getMavenDependencies());
            return responseBuilder.build();
        } catch (Exception e) {
            throw new javax.ws.rs.NotFoundException();
        }
    }

    @GET
    @Path("/services/category/{category}")
    @Produces({MediaType.APPLICATION_JSON})
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    public Response getResultsByServiceCategory(@PathParam("category") String category) {
        try {
            Response.ResponseBuilder responseBuilder = Response.ok(repoService.getServicesByCategory(category));
            return responseBuilder.build();
        } catch (Exception e) {
            throw new javax.ws.rs.NotFoundException();
        }
    }

    @GET
    @Path("/servicetriggers")
    @Produces({MediaType.APPLICATION_JSON})
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    public Response getTriggerServices() {
        try {
            Response.ResponseBuilder responseBuilder = Response.ok(repoService.getTriggerServices());
            return responseBuilder.build();
        } catch (Exception e) {
            throw new javax.ws.rs.NotFoundException();
        }
    }

    @GET
    @Path("/servicetriggers/count")
    @Produces({MediaType.APPLICATION_JSON})
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    public Response getTriggerServiceCounts() {
        try {
            Response.ResponseBuilder responseBuilder = Response.ok(repoService.getTriggerServices().size());
            return responseBuilder.build();
        } catch (Exception e) {
            throw new javax.ws.rs.NotFoundException();
        }
    }

    @GET
    @Path("/serviceactions")
    @Produces({MediaType.APPLICATION_JSON})
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    public Response getActionServices() {
        try {
            Response.ResponseBuilder responseBuilder = Response.ok(repoService.getActionServices());
            return responseBuilder.build();
        } catch (Exception e) {
            throw new javax.ws.rs.NotFoundException();
        }
    }

    @GET
    @Path("/serviceactions/count")
    @Produces({MediaType.APPLICATION_JSON})
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    public Response getActionServicesCount() {
        try {
            Response.ResponseBuilder responseBuilder = Response.ok(repoService.getActionServices().size());
            return responseBuilder.build();
        } catch (Exception e) {
            throw new javax.ws.rs.NotFoundException();
        }
    }
}
