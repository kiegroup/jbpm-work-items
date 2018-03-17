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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

@Wid(widfile = "EthereumTransactExistingContract.wid", name = "EthereumTransactExistingContract",
        displayName = "EthereumTransactExistingContract",
        defaultHandler = "mvel: new org.jbpm.process.workitem.ethereum.TransactExistingContractWorkitemHandler()",
        parameters = {
                @WidParameter(name = "ServiceURL"),
                @WidParameter(name = "ContractAddress"),
                @WidParameter(name = "MethodName"),
                @WidParameter(name = "MethodInputType"),
                @WidParameter(name = "WaitForReceipt"),
                @WidParameter(name = "DepositAmount"),
        },
        results = {
                @WidResult(name = "Receipt")
        },
        mavenDepends = {
                @WidMavenDepends(group = "org.web3j", artifact = "core", version = "3.3.1")
        })
public class TransactExistingContractWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String walletPassword;
    private String walletPath;
    private EthereumAuth auth;
    private Web3j web3j;
    private ClassLoader classLoader;

    private static final String RESULTS = "Receipt";
    private static final Logger logger = LoggerFactory.getLogger(TransactExistingContractWorkitemHandler.class);

    public TransactExistingContractWorkitemHandler(String walletPassword,
                                                   String walletPath) {
        this(walletPassword,
             walletPath,
             null);
    }

    public TransactExistingContractWorkitemHandler(String walletPassword,
                                                   String walletPath,
                                                   ClassLoader classLoader) {
        this.walletPassword = walletPassword;
        this.walletPath = walletPath;
        if (classLoader == null) {
            this.classLoader = this.getClass().getClassLoader();
        } else {
            this.classLoader = classLoader;
        }
        ;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {
            String serviceURL = (String) workItem.getParameter("ServiceURL");
            String contractAddress = (String) workItem.getParameter("ContractAddress");
            String waitForReceiptStr = (String) workItem.getParameter("WaitForReceipt");
            String methodName = (String) workItem.getParameter("MethodName");
            Type methodInputType = (Type) workItem.getParameter("MethodInputType");
            String depositAmount = (String) workItem.getParameter("DepositAmount");

            if (StringUtils.isNotEmpty(serviceURL) && StringUtils.isNotEmpty(contractAddress) && StringUtils.isNotEmpty(methodName)) {

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

                List<Type> methodInputTypeList = new ArrayList<>();
                if (methodInputType != null) {
                    methodInputTypeList = Collections.singletonList(methodInputType);
                }

                TransactionReceipt transactionReceipt = EthereumUtils.transactExistingContract(
                        credentials,
                        web3j,
                        depositEtherAmountToSend,
                        EthereumUtils.DEFAULT_GAS_PRICE,
                        EthereumUtils.DEFAULT_GAS_LIMIT,
                        contractAddress,
                        methodName,
                        methodInputTypeList,
                        null,
                        waitForReceipt,
                        EthereumUtils.DEFAULT_SLEEP_DURATION,
                        EthereumUtils.DEFAULT_ATTEMPTS);

                results.put(RESULTS,
                            transactionReceipt);

                workItemManager.completeWorkItem(workItem.getId(),
                                                 results);
            } else {
                logger.error("Missing service url, valid toAddress or method name to execute.");
                throw new IllegalArgumentException("Missing service url, valid toAddress or method name to execute.");
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
