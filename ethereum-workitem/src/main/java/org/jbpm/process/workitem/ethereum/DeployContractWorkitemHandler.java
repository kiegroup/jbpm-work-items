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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Wid(widfile = "EthereumDeployContract.wid", name = "EthereumDeployContract",
        displayName = "EthereumDeployContract",
        defaultHandler = "mvel: new org.jbpm.process.workitem.ethereum.DeployContractWorkitemHandler()",
        parameters = {
                @WidParameter(name = "ServiceURL"),
                @WidParameter(name = "ContracBinary"),
                @WidParameter(name = "DepositAmount"),
                @WidParameter(name = "WaitForReceipt")
        },
        results = {
                @WidResult(name = "ContractAddress")
        },
        mavenDepends = {
                @WidMavenDepends(group = "org.web3j", artifact = "core", version = "3.3.1")
        })
public class DeployContractWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String walletPassword;
    private String walletPath;
    private EthereumAuth auth;
    private Web3j web3j;
    private ClassLoader classLoader;

    private static final String RESULTS = "ContractAddress";
    private static final Logger logger = LoggerFactory.getLogger(DeployContractWorkitemHandler.class);

    public DeployContractWorkitemHandler(String walletPassword,
                                         String walletPath) {
        this(walletPassword,
             walletPath,
             null);
    }

    public DeployContractWorkitemHandler(String walletPassword,
                                         String walletPath,
                                         ClassLoader classLoader) {
        this.walletPassword = walletPassword;
        this.walletPath = walletPath;
        if (classLoader == null) {
            this.classLoader = this.getClass().getClassLoader();
        } else {
            this.classLoader = classLoader;
        }
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {
            String serviceURL = (String) workItem.getParameter("ServiceURL");
            String contractBinary = (String) workItem.getParameter("ContracBinary");
            String depositAmount = (String) workItem.getParameter("DepositAmount");
            String waitForReceiptStr = (String) workItem.getParameter("WaitForReceipt");

            if (StringUtils.isNotEmpty(serviceURL) && StringUtils.isNotEmpty(contractBinary)) {

                Map<String, Object> results = new HashMap<String, Object>();

                if (web3j == null) {
                    web3j = Web3j.build(new HttpService(serviceURL));
                }

                auth = new EthereumAuth(walletPassword,
                                        walletPath,
                                        classLoader);
                Credentials credentials = auth.getCredentials();

                int depositEtherAmountToSend = 0;
                if (depositAmount != null) {
                    depositEtherAmountToSend = Integer.parseInt(depositAmount);
                }

                boolean waitForReceipt = false;
                if (waitForReceiptStr != null) {
                    waitForReceipt = Boolean.parseBoolean(waitForReceiptStr);
                }

                String createdContractAddress = EthereumUtils.deployContract(credentials,
                                                                             web3j,
                                                                             contractBinary,
                                                                             depositEtherAmountToSend,
                                                                             waitForReceipt,
                                                                             EthereumUtils.DEFAULT_SLEEP_DURATION,
                                                                             org.jbpm.process.workitem.ethereum.EthereumUtils.DEFAULT_ATTEMPTS);

                results.put(RESULTS,
                            createdContractAddress);

                workItemManager.completeWorkItem(workItem.getId(),
                                                 results);
            } else {
                logger.error("Missing service url, or contract creation code.");
                throw new IllegalArgumentException("Missing service url, or contract creation code.");
            }
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
