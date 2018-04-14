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
package org.jbpm.process.workitem.ethereum;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.TypeReference;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Wid(widfile = "EthereumObserveContractEvent.wid", name = "EthereumObserveContractEvent",
        displayName = "EthereumObserveContractEvent",
        defaultHandler = "mvel: new org.jbpm.process.workitem.ethereum.ObserveContractEventWorkitemHandler()",
        parameters = {
                @WidParameter(name = "ServiceURL", required = true),
                @WidParameter(name = "ContractAddress", required = true),
                @WidParameter(name = "EventName", required = true),
                @WidParameter(name = "EventReturnType"),
                @WidParameter(name = "EventIndexedParameter"),
                @WidParameter(name = "EventNonIndexedParameter"),
                @WidParameter(name = "SignalName", required = true),
                @WidParameter(name = "AbortOnUpdate")
        },
        mavenDepends = {
                @WidMavenDepends(group = "org.web3j", artifact = "core", version = "3.3.1")
        })
public class ObserveContractEventWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private Web3j web3j;
    private KieSession kieSession;

    private static final String RESULTS = "ContractAddress";
    private static final Logger logger = LoggerFactory.getLogger(DeployContractWorkitemHandler.class);

    public ObserveContractEventWorkitemHandler(KieSession kieSession) {
        this.kieSession = kieSession;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {
            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String serviceURL = (String) workItem.getParameter("ServiceURL");
            String contractAddress = (String) workItem.getParameter("ContractAddress");
            String eventName = (String) workItem.getParameter("EventName");
            List<TypeReference<?>> eventIndexedParameter = (List<TypeReference<?>>) workItem.getParameter("EventIndexedParameter");
            List<TypeReference<?>> eventNonIndexedParameter = (List<TypeReference<?>>) workItem.getParameter("EventNonIndexedParameter");
            String eventReturnType = (String) workItem.getParameter("EventReturnType");
            String signalName = (String) workItem.getParameter("SignalName");
            String abortOnUpdate = (String) workItem.getParameter("AbortOnUpdate");

            boolean doAbortOnUpdate = Boolean.parseBoolean(abortOnUpdate);

            if (eventIndexedParameter == null) {
                eventIndexedParameter = new ArrayList<>();
            }

            if (eventNonIndexedParameter == null) {
                eventNonIndexedParameter = new ArrayList<>();
            }

            if (web3j == null) {
                web3j = Web3j.build(new HttpService(serviceURL));
            }

            EthereumUtils.observeContractEvent(web3j,
                                               eventName,
                                               contractAddress,
                                               eventIndexedParameter,
                                               eventNonIndexedParameter,
                                               eventReturnType,
                                               kieSession,
                                               signalName,
                                               doAbortOnUpdate,
                                               workItemManager,
                                               workItem);
        } catch (Exception e) {
            logger.error("Error executing workitem: " + e.getMessage());
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }
}
