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
package org.jbpm.contrib.demoservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Path("/service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Service {

    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<Integer, RunningJob> runningJobs = new ConcurrentHashMap<>();
    private static final AtomicInteger sequence = new AtomicInteger();

    @GET
    @Path("/")
    public Response test() {
        Map<String, Object> response = new HashMap<>();
        response.put("time", new Date());

        return Response.status(200).entity(response).build();
    }

    @POST
    @Path("/A")
    public Response actionA(
            RequestA request,
            @QueryParam("callbackDelay") @DefaultValue("3") int callbackDelay,
            @QueryParam("cancelDelay") @DefaultValue("1") String cancelDelay)
            throws JsonProcessingException {
        System.out.println(">>> Action A requested.");
        System.out.println(">>> request object: " + objectMapper.writeValueAsString(request));
        String callbackUrl = request.getCallbackUrl();

        Map<String, String> person = new HashMap<>();
        person.put("name", request.getName());

        Map<String, Object> result = new HashMap<>();
        result.put("person", person);

        int jobId = scheduleCallback(callbackUrl, callbackDelay, result);

        String cancelUrl = "http://localhost:8080/demo-service/service/cancel/" + jobId;
        cancelUrl += "?delay=" + cancelDelay;
        Map<String, Object> response = new HashMap<>();
        response.put("cancelUrl", cancelUrl);

        return Response.status(200).entity(response).build();
    }

    @POST
    @Path("/B")
    public Response actionB(
            RequestB request,
            @QueryParam("callbackDelay") @DefaultValue("5") int callbackDelay)
            throws JsonProcessingException {
        System.out.println(">>> Action B requested.");
        System.out.println(">>> request object: " + objectMapper.writeValueAsString(request));

        String callbackUrl = request.getCallbackUrl();

        Map<String, Object> result = new HashMap<>();
        result.put("fullName", request.getNameFromA() + " " + request.getSurname());

        int jobId = scheduleCallback(callbackUrl, callbackDelay, result);

        String cancelUrl = "http://localhost:8080/demo-service/service/cancel/" + jobId;
        Map<String, Object> response = new HashMap<>();
        response.put("cancelUrl", cancelUrl);

        return Response.status(200).entity(response).build();
    }

    @POST
    @Path("/cancel/{id}")
    public Response cancelAction(@PathParam("id") int jobId, @QueryParam("delay") @DefaultValue("1") int delay)
            throws JsonProcessingException {
        System.out.println(">>> Action Cancel requested for job:" + jobId);

        RunningJob runningJob = runningJobs.get(jobId);
        runningJob.future.cancel(true);

        Map<String, Object> result = new HashMap<>();
        result.put("canceled", "true");

        scheduleCallback(runningJob.callbackUrl, delay, result);

        return Response.status(200).entity(null).build();
    }

    private int scheduleCallback(String callbackUrl, int delay, Map<String, Object> result) {
        ScheduledFuture<?> future = executorService.schedule(() -> executeCallback(callbackUrl, result), delay, TimeUnit.SECONDS);
        int id = sequence.getAndIncrement();
        runningJobs.put(id, new RunningJob(future, callbackUrl));
        return id;
    }

    private class RunningJob {
        ScheduledFuture future;
        String callbackUrl;

        public RunningJob(ScheduledFuture future, String callbackUrl) {
            this.future = future;
            this.callbackUrl = callbackUrl;
        }
    }

    private void executeCallback(String callbackUrl, Map<String, Object> result) {

        RequestConfig config = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setAuthenticationEnabled(true)
                .build();


        try {

            URI requestUri = new URI(callbackUrl);

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
            HttpPut request = new HttpPut(requestUri);

            request.setHeader("Content-Type","application/json");

            System.out.println(">>> Calling back to: " + requestUri);

            Map<String, Map<String, Object>> wrappedResult = new HashMap<>();
            wrappedResult.put("content", result);

            String jsonContent = objectMapper.writeValueAsString(wrappedResult);

            System.out.println(">>> Result data:" + jsonContent);

            StringEntity entity = new StringEntity(jsonContent, ContentType.APPLICATION_JSON);
            request.setEntity(entity);
            HttpResponse response = httpClient.execute(request);
            System.out.println("Callback executed. Returned status: " + response.getStatusLine().getStatusCode());

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

    }

}
