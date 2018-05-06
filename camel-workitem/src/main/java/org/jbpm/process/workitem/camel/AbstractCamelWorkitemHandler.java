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
package org.jbpm.process.workitem.camel;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.jbpm.process.workitem.camel.request.RequestMapper;
import org.jbpm.process.workitem.camel.response.ResponseMapper;
import org.jbpm.process.workitem.camel.uri.URIMapper;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCamelWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    static final Logger logger = LoggerFactory.getLogger(AbstractCamelWorkitemHandler.class);

    protected ResponseMapper responseMapper;
    protected RequestMapper requestMapper;
    protected URIMapper uriConverter;
    protected CamelContext context;

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager manager) {
        try {
            manager.completeWorkItem(workItem.getId(),
                                     send(workItem));
        } catch (Exception e) {
            logger.error("Error executing workitem: " + e.getMessage());
            handleException(e);
        }
    }

    private Map<String, Object> send(WorkItem workItem) throws URISyntaxException {
        if (context == null) {
            context = CamelContextService.getInstance();
        }
        ProducerTemplate template = context.createProducerTemplate();

        Map<String, Object> params = new HashMap<String, Object>(workItem.getParameters());
        // filtering out TaskName
        params.remove("TaskName");
        Processor processor = requestMapper.mapToRequest(params);
        URI uri = uriConverter.toURI(params);
        String s;
        try {
            s = URLDecoder.decode(uri.toString(),
                                  "UTF-8");
        } catch (UnsupportedEncodingException e) {
            s = uri.toString();
        }
        Endpoint endpoint = context.getEndpoint(s);

        Exchange exchange = template.send(endpoint,
                                          processor);
        return this.responseMapper.mapFromResponse(exchange);
    }

    public void abortWorkItem(WorkItem workItem,
                              WorkItemManager manager) {
        // Do nothing, this work item cannot be aborted
    }

    protected void setCamelContext(CamelContext camelContext) {
        this.context = camelContext;
    }
}
