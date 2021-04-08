/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.process.longrest.demoservices;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hibernate.cfg.NotYetImplementedException;
import org.jbpm.process.longrest.demoservices.dto.BuildRequest;
import org.jbpm.process.longrest.demoservices.dto.CompleteRequest;
import org.jbpm.process.longrest.demoservices.dto.PreBuildRequest;
import org.jbpm.process.longrest.demoservices.dto.Request;
import org.jbpm.process.longrest.demoservices.dto.Scm;
import org.jbpm.process.longrest.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/demo-service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Service {

    public static final String SERVICE_LISTENER_KEY = "serviceListener";
    public static final String COOKIE_LISTENER_KEY = "cookieListener";

    private static final Logger logger = LoggerFactory.getLogger(Service.class);
    public static final String PRE_BUILD_COOKIE_NAME = "myCookieName";
    public static final String PRE_BUILD_COOKIE_VALUE = "myCookieValue";

    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<Integer, RunningJob> runningJobs = new ConcurrentHashMap<>();
    private static final AtomicInteger sequence = new AtomicInteger();

    @Context
    ServletContext servletContext;

    @GET
    @Path("/")
    public Response test() {
        Map<String, Object> response = new HashMap<>();
        response.put("time", new Date());

        return Response.status(200).entity(response).build();
    }

    @POST
    @Path("/prebuild")
    public Response prebuild(
            PreBuildRequest request,
            @QueryParam("callbackDelay") @DefaultValue("3") int callbackDelay,
            @QueryParam("cancelDelay") @DefaultValue("1") String cancelDelay,
            @QueryParam("cancelHeartBeatAfter") @DefaultValue("5") String cancelHeartBeatAfter)
            throws JsonProcessingException {
        logger.info("> PreBuild requested.");
        logger.info("> Request object: " + objectMapper.writeValueAsString(request));
        Request callback = request.getCallback();

        Scm scm = new Scm();
        scm.setUrl(request.getScm().getUrl());
        scm.setRevision("new-scm-tag");
        Map<String, Object> result = new HashMap<>();
        result.put("scm", scm);
        result.put("status", "SUCCESS");

        if (request.getHeartBeat() != null) {
            ScheduledFuture<?> heartBeat = startHeartBeat(request.getHeartBeat().getUrl());
            //end hearth beat after cancelHeartBeatAfter
            executorService.schedule(
                    () -> heartBeat.cancel(true), Long.parseLong(cancelHeartBeatAfter), TimeUnit.SECONDS);
        }

        Map<String, Object> response = new HashMap<>();
        if (callback != null && !Strings.isEmpty(callback.getUrl())) {
            int jobId = scheduleCallback(callback.getUrl(), callback.getMethod(), null, callbackDelay, result);
            String cancelUrl = "http://localhost:8080/demo-service/cancel/" + jobId;
            cancelUrl += "?delay=" + cancelDelay;
            response.put("cancelUrl", cancelUrl);
        }
        return Response.status(200)
                .header("Set-Cookie", PRE_BUILD_COOKIE_NAME + "=" + PRE_BUILD_COOKIE_VALUE)
                .entity(response).build();
    }

    @POST
    @Path("/build")
    public Response build(
            BuildRequest request,
            @QueryParam("callbackDelay") @DefaultValue("5") int callbackDelay)
            throws JsonProcessingException {
        logger.info("> Build requested.");
        logger.info("> Request object: " + objectMapper.writeValueAsString(request));
        serviceListener().fire(EventType.BUILD_REQUESTED, request);
        Request callback = request.getCallback();

        Map<String, Object> result = new HashMap<>();
        result.put("status", "SUCCESS");

        int jobId = scheduleCallback(callback.getUrl(), callback.getMethod(), null, callbackDelay, result);

        String cancelUrl = "http://localhost:8080/demo-service/cancel/" + jobId;
        Map<String, Object> response = new HashMap<>();
        response.put("cancelUrl", cancelUrl);

        return Response.status(200).entity(response).build();
    }

    @POST
    @Path("/complete/{buildConfigId}")
    public Response complete(CompleteRequest completeRequest, @PathParam("buildConfigId") String buildConfigId) throws JsonProcessingException {
        logger.info("> Complete requested.");
        logger.info("> Request object: " + objectMapper.writeValueAsString(completeRequest));
        Map<String, Object> response = new HashMap<>();
        response.put("labels", completeRequest.getLabels());
        return Response.status(200).entity(response).build();
    }

    @POST
    @Path("/cancel/{id}")
    public Response cancelAction(
            @PathParam("id") int jobId,
            @QueryParam("delay") @DefaultValue("1") int delay,
            @Context HttpHeaders httpHeaders) {
        logger.info("> Action Cancel requested for job:" + jobId);

        cookieListener().accept(httpHeaders.getCookies());

        RunningJob runningJob = runningJobs.get(jobId);
        runningJob.future.cancel(true);

        Map<String, Object> result = new HashMap<>();
        result.put("cancelled", "true");

        scheduleCallback(runningJob.callbackUrl, "POST", null, delay, result);

        Map<String, Object> response = new HashMap<>();
        return Response.status(200).entity(response).build();
    }

    private int scheduleCallback(String callbackUrl, String callbackMethod, List<NameValuePair> callbackParams, int delay, Object result) {
        ServiceListener serviceListener = serviceListener();
        ScheduledFuture<?> future = executorService.schedule(() -> executeRequest(callbackUrl, callbackMethod, callbackParams, result, serviceListener), delay, TimeUnit.SECONDS);
        int id = sequence.getAndIncrement();
        runningJobs.put(id, new RunningJob(future, callbackUrl));
        return id;
    }

    private ScheduledFuture<?> startHeartBeat(String url) {
        ServiceListener serviceListener = serviceListener();
        ScheduledFuture<?> future = executorService.scheduleAtFixedRate(()
                                                                                -> executeRequest(url, "POST", Collections.emptyList(), null, serviceListener), 300L, 300L, TimeUnit.MILLISECONDS);
        return future;
    }

    private class RunningJob {

        ScheduledFuture future;
        String callbackUrl;

        public RunningJob(ScheduledFuture future, String callbackUrl) {
            this.future = future;
            this.callbackUrl = callbackUrl;
        }
    }

    private void executeRequest(String url, String method, List<NameValuePair> parameters, Object result, ServiceListener serviceListener) {
        RequestConfig config = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setAuthenticationEnabled(true)
                .build();

        try {
            URI requestUri = new URI(url);

            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(requestUri.getHost(),
                                                       requestUri.getPort(),
                                                       AuthScope.ANY_REALM),
                                         new UsernamePasswordCredentials("admin", "admin")
            );

            HttpClientBuilder clientBuilder = HttpClientBuilder.create()
                    .setDefaultRequestConfig(config)
                    .setDefaultCredentialsProvider(credsProvider);

            HttpClient httpClient = clientBuilder.build();
            HttpEntityEnclosingRequestBase request = null;
            if (method == null || method.contentEquals(HttpPut.METHOD_NAME)) {
                request = new HttpPut(requestUri);
            } else if (method.contentEquals(HttpPost.METHOD_NAME)) {
                request = new HttpPost(requestUri);
            } else {
                throw new NotYetImplementedException("This HTTP method is not implemented yet in this dummy service handler: " + method);
            }

            request.setHeader("Content-Type", "application/json");

            logger.info("> Invoking to: " + requestUri);

            String jsonContent = objectMapper.writeValueAsString(result);

            logger.info("> Data:" + jsonContent);

            StringEntity entity = new StringEntity(jsonContent, ContentType.APPLICATION_JSON);
            request.setEntity(entity);
            HttpResponse response = httpClient.execute(request);
            logger.info("> Invocation executed. Returned status: " + response.getStatusLine().getStatusCode());
            serviceListener.fire(EventType.CALLBACK_COMPLETED, url);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private ServiceListener serviceListener() {
        try {
            return (ServiceListener) servletContext.getAttribute(SERVICE_LISTENER_KEY);
        } catch (Throwable t) {
            logger.error("> Cannot get service listener.", t);
            return null;
        }
    }

    private CookieListener cookieListener() {
        try {
            return ((CookieListener) servletContext.getAttribute(COOKIE_LISTENER_KEY));
        } catch (Throwable t) {
            logger.error("> Cannot get cookie listener.", t);
            return null;
        }
    }
}
