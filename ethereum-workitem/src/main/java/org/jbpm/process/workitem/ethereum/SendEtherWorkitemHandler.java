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

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
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
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;

@Wid(widfile = "EthereumSendEther.wid", name = "EthereumSendEther",
        displayName = "EthereumSendEther",
        defaultHandler = "mvel: new org.jbpm.process.workitem.ethereum.SendEtherWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "ServiceURL", required = true),
                @WidParameter(name = "Amount", required = true),
                @WidParameter(name = "ToAddress", required = true)
        },
        results = {
                @WidResult(name = "Receipt")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}"),
                @WidMavenDepends(group = "org.web3j", artifact = "core", version = "3.3.1")
        })
public class SendEtherWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String walletPassword;
    private String walletPath;
    private EthereumAuth auth;
    private Web3j web3j;
    private Transfer transfer;
    private ClassLoader classLoader;

    private static final String RESULTS = "Receipt";

    private static final Logger logger = LoggerFactory.getLogger(SendEtherWorkitemHandler.class);

    public SendEtherWorkitemHandler(String walletPassword,
                                    String walletPath) {
        this(walletPassword,
             walletPath,
             null);
    }

    public SendEtherWorkitemHandler(String walletPassword,
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
            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String serviceURL = (String) workItem.getParameter("ServiceURL");
            String amount = (String) workItem.getParameter("Amount");
            String toAddress = (String) workItem.getParameter("ToAddress");

            Map<String, Object> results = new HashMap<String, Object>();

            if (web3j == null) {
                web3j = Web3j.build(new HttpService(serviceURL));
            }

            auth = new EthereumAuth(walletPassword,
                                    walletPath,
                                    classLoader);
            Credentials credentials = auth.getCredentials();

            TransactionManager transactionManager = new RawTransactionManager(web3j,
                                                                              credentials);

            if (transfer == null) {
                transfer = new Transfer(web3j,
                                        transactionManager);
            }

            int amountToSend = 0;
            if (amount != null) {
                amountToSend = Integer.parseInt(amount);
            }

            TransactionReceipt transactionReceipt = EthereumUtils.sendFundsToContract(credentials,
                                                                                      web3j,
                                                                                      amountToSend,
                                                                                      toAddress,
                                                                                      transfer);

            results.put(RESULTS,
                        transactionReceipt);

            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
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

    // for testing
    public void setTransfer(Transfer transfer) {
        this.transfer = transfer;
    }
}
