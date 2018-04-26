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
package org.jbpm.process.workitem.camel.response;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

public class ResponsePayloadMapper implements ResponseMapper {

    private final String responseLocation;
    private final Set<String> headerLocations;

    public ResponsePayloadMapper() {
        this("response");
    }

    public ResponsePayloadMapper(String responseLocation) {
        this(responseLocation,
             new HashSet<String>());
    }

    public ResponsePayloadMapper(String responseLocation,
                                 Set<String> headerLocations) {
        this.responseLocation = responseLocation;
        this.headerLocations = headerLocations;
    }

    @Override
    public Map<String, Object> mapFromResponse(Exchange exchange) {
        Map<String, Object> results = new HashMap<String, Object>();
        if (exchange.hasOut()) {
            Message out = exchange.getOut();
            Object response = out.getBody();
            results.put(responseLocation,
                        response);
            Map<String, Object> headerValues = out.getHeaders();
            for (String headerLocation : this.headerLocations) {
                results.put(headerLocation,
                            headerValues.get(headerLocation));
            }
        }
        return results;
    }
}

