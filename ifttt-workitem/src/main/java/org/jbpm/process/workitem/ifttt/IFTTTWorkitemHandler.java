/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.process.workitem.ifttt;

import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "IFTTTDefinitions.wid", name = "IFTTTCall",
        displayName = "IFTTTCall",
        defaultHandler = "mvel: new org.jbpm.process.workitem.ifttt.IFTTTWorkitemHandler()",
        parameters = {
                @WidParameter(name = "Trigger"),
                @WidParameter(name = "Value1"),
                @WidParameter(name = "Value2"),
                @WidParameter(name = "Value3")
        },
        mavenDepends = {
                @WidMavenDepends(group = "com.fasterxml.jackson.core", artifact = "jackson-databind", version = "2.6.2"),
                @WidMavenDepends(group = "org.jboss.spec.javax.ws.rs", artifact = "jboss-jaxrs-api_2.0_spec", version = "1.0.0.Final")
        })
public class IFTTTWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(IFTTTWorkitemHandler.class);
    private static final String IFTTT_TRIGGER_ENDPOINT = "https://maker.ifttt.com/trigger/%s/with/key/%s";

    private String key;
    private Client client;
    private IFTTTRequest iftttRequest;

    public IFTTTWorkitemHandler(String key) {
        this.key = key;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        String trigger = (String) workItem.getParameter("Trigger");
        // ifttt trigger endpoint allows up to three values atm
        String valueOne = (String) workItem.getParameter("Value1");
        String valueTwo = (String) workItem.getParameter("Value2");
        String valueThree = (String) workItem.getParameter("Value3");

        if (trigger != null) {
            try {

                if (client == null) {
                    client = ClientBuilder.newClient();
                }

                WebTarget target = client.
                        target(String.format(IFTTT_TRIGGER_ENDPOINT,
                                             trigger,
                                             key));

                iftttRequest = new IFTTTRequest(valueOne,
                                                valueTwo,
                                                valueThree);

                target.request()
                        .post(Entity.entity(iftttRequest.toJSON(),
                                            MediaType.APPLICATION_JSON),
                              String.class);

                workItemManager.completeWorkItem(workItem.getId(),
                                                 null);
            } catch (Exception e) {
                handleException(e);
            }
        } else {
            logger.error("Missing trigger for maker call.");
            throw new IllegalArgumentException("Missing trigger for maker call.");
        }
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setClient(Client client) {
        this.client = client;
    }

    // for testing
    public String getRequestBody() {
        return iftttRequest.toJSON();
    }

    private class IFTTTRequest {

        private String value1;
        private String value2;
        private String value3;

        public IFTTTRequest(String value1,
                            String value2,
                            String value3) {
            this.value1 = StringUtils.isNotEmpty(value1) ? value1 : null;
            this.value2 = StringUtils.isNotEmpty(value2) ? value1 : null;
            this.value3 = StringUtils.isNotEmpty(value3) ? value1 : null;
        }

        public String toJSON() {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(this);
            } catch (IOException e) {
                logger.error("Unable to map values to JSON : " + e.getMessage());
                return null;
            }
        }

        public String getValue1() {
            return value1;
        }

        public void setValue1(String value1) {
            this.value1 = value1;
        }

        public String getValue2() {
            return value2;
        }

        public void setValue2(String value2) {
            this.value2 = value2;
        }

        public String getValue3() {
            return value3;
        }

        public void setValue3(String value3) {
            this.value3 = value3;
        }
    }
}
