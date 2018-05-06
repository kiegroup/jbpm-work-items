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
package org.jbpm.process.workitem.camel.request;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class RequestPayloadMapper implements RequestMapper {

    private final String requestLocation;
    private final Set<String> headerLocations;

    public RequestPayloadMapper() {
        this("request");
    }

    public RequestPayloadMapper(String requestLocation) {
        this(requestLocation,
             new HashSet<String>());
    }

    public RequestPayloadMapper(String requestLocation,
                                Set<String> headerLocations) {
        this.requestLocation = requestLocation;
        this.headerLocations = headerLocations;
    }

    public Processor mapToRequest(Map<String, Object> params) {
        Object request = params.remove(requestLocation);

        Map<String, Object> headers = new HashMap<String, Object>();

        for (String headerLocation : this.headerLocations) {
            // remove from the request params, move to header locations.
            if (params.containsKey(headerLocation)) {
                headers.put(headerLocation,
                            params.remove(headerLocation));
            }
        }

        return new RequestProcessor(request,
                                    headers);
    }

    protected class RequestProcessor implements Processor {

        private Object payload;
        private Map<String, Object> headers;

        public RequestProcessor(Object payload,
                                Map<String, Object> headers) {
            this.payload = payload;
            this.headers = headers;
        }

        @Override
        public void process(Exchange exchange) throws Exception {
            exchange.getIn().setBody(payload);
            exchange.getIn().setHeaders(headers);
        }
    }
}
