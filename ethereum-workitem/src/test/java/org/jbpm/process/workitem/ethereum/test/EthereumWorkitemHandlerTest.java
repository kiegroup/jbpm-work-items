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
package org.jbpm.process.workitem.ethereum.test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.process.workitem.ethereum.DeployContractWorkitemHandler;
import org.jbpm.process.workitem.ethereum.EthereumAuth;
import org.jbpm.process.workitem.ethereum.GetBalanceWorkitemHandler;
import org.jbpm.process.workitem.ethereum.ObserveContractEventWorkitemHandler;
import org.jbpm.process.workitem.ethereum.QueryExistingContractWorkitemHandler;
import org.jbpm.process.workitem.ethereum.SendEtherWorkitemHandler;
import org.jbpm.process.workitem.ethereum.TransactExistingContractWorkitemHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert.Unit;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Observable.class})
@PowerMockIgnore({"javax.crypto.*"})
public class EthereumWorkitemHandlerTest {

    private static final String TEST_WALLET_PASSWORD = "jbpmtest123";
    private List signalList;

    @Mock
    EthereumAuth auth;

    @Mock
    Web3j web3j;

    @Mock
    Request balanceRequest;

    @Mock
    EthGetBalance ethGetBalance;

    @Mock
    Transfer transfer;

    @Mock
    TransactionReceipt transactionReceipt;

    @Mock
    RemoteCall transferRequest;

    @Mock
    Request ethCallRequest;

    @Mock
    EthCall ethCall;

    @Mock
    CompletableFuture ethCallCompletable;

    @Mock
    Request ethCountRequest;

    @Mock
    CompletableFuture ethCountCompletable;

    @Mock
    EthGetTransactionCount transactionCount;

    @Mock
    EthSendTransaction ethSendTransaction;

    @Mock
    Request ethSendRequest;

    @Mock
    CompletableFuture ethSendCompletable;

    @Mock
    Request ethSendRawRequest;

    @Mock
    CompletableFuture ethSendRawCompletable;

    @Mock
    EthSendTransaction ethSendRawTransaction;

    @Mock
    Request ethSendTransactionReceipt;

    @Mock
    EthGetTransactionReceipt ethGetTransactionReceipt;

    @Mock
    TransactionReceipt rawTransactionalRecept;

    @Mock
    KieSession kieSession;

    @Mock
    Subscription subscription;

    @Mock
    Action1 action1;

    @Before
    public void setUp() {
        try {
            when(web3j.ethGetBalance(anyString(),
                                     any(DefaultBlockParameterName.class))).thenReturn(balanceRequest);
            when(balanceRequest.send()).thenReturn(ethGetBalance);
            when(ethGetBalance.getBalance()).thenReturn(BigInteger.valueOf(100));
            when(transfer.sendFunds(anyString(),
                                    any(BigDecimal.class),
                                    any(Unit.class))).thenReturn(transferRequest);
            when(transferRequest.send()).thenReturn(transactionReceipt);
            when(transactionReceipt.getStatus()).thenReturn("testStatus");
            when(transactionReceipt.getBlockHash()).thenReturn("testBlockHash");
            when(transactionReceipt.getBlockNumber()).thenReturn(BigInteger.valueOf(1));
            when(web3j.ethCall(any(Transaction.class),
                               any(DefaultBlockParameterName.class))).thenReturn(ethCallRequest);
            when(ethCallRequest.sendAsync()).thenReturn(ethCallCompletable);
            when(ethCallCompletable.get()).thenReturn(ethCall);
            when(ethCall.getValue()).thenReturn("testResultValue");
            when(web3j.ethGetTransactionCount(anyString(),
                                              any(DefaultBlockParameterName.class))).thenReturn(ethCountRequest);
            when(ethCountRequest.sendAsync()).thenReturn(ethCountCompletable);
            when(ethCountCompletable.get()).thenReturn(transactionCount);
            when(transactionCount.getTransactionCount()).thenReturn(BigInteger.valueOf(10));
            when(web3j.ethSendTransaction(any(Transaction.class))).thenReturn(ethSendRequest);
            when(ethSendRequest.sendAsync()).thenReturn(ethSendCompletable);
            when(ethSendCompletable.get()).thenReturn(ethSendTransaction);
            when(ethSendTransaction.getTransactionHash()).thenReturn("testTransactionHash");
            when(web3j.ethSendRawTransaction(anyString())).thenReturn(ethSendRawRequest);
            when(ethSendRawRequest.sendAsync()).thenReturn(ethSendRawCompletable);
            when(ethSendRawCompletable.get()).thenReturn(ethSendRawTransaction);
            when(ethSendRawTransaction.getTransactionHash()).thenReturn("testTransactionHash");
            when(web3j.ethGetTransactionReceipt(anyString())).thenReturn(ethSendTransactionReceipt);
            when(ethSendTransactionReceipt.send()).thenReturn(ethGetTransactionReceipt);
            when(ethGetTransactionReceipt.getTransactionReceipt()).thenReturn(Optional.of(rawTransactionalRecept));
            when(ethGetTransactionReceipt.getResult()).thenReturn(rawTransactionalRecept);
            when(rawTransactionalRecept.getContractAddress()).thenReturn("testContractAddress");

            signalList = new ArrayList();
            doAnswer(new Answer() {
                public Void answer(InvocationOnMock invocation) {
                    signalList.add(Arrays.toString(invocation.getArguments()));
                    return null;
                }
            }).when(kieSession).signalEvent(anyString(),
                                            any(Object.class));

            Observable logObservable = PowerMockito.mock(Observable.class);
            when(web3j.ethLogObservable(any(EthFilter.class))).thenReturn(logObservable);
            when(logObservable.subscribe()).thenReturn(subscription);
            when(logObservable.subscribe(any(Action1.class))).thenReturn(subscription);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetBalance() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ServiceURL",
                              "http://localhost:8545/");

        GetBalanceWorkitemHandler handler = new GetBalanceWorkitemHandler(TEST_WALLET_PASSWORD,
                                                                          "wallet/testwallet.json");
        handler.setWeb3j(web3j);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("Balance") instanceof BigDecimal);

        BigDecimal balanceResult = (BigDecimal) manager.getResults().get(workItem.getId()).get("Balance");
        assertNotNull(balanceResult);
    }

    @Test
    public void testSendEther() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ServiceURL",
                              "http://localhost:8545/");
        workItem.setParameter("Amount",
                              "10");
        workItem.setParameter("ToAddress",
                              "0x00211e7e");

        SendEtherWorkitemHandler handler = new SendEtherWorkitemHandler(TEST_WALLET_PASSWORD,
                                                                        "wallet/testwallet.json");
        handler.setWeb3j(web3j);
        handler.setTransfer(transfer);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        TransactionReceipt receipt = (TransactionReceipt) manager.getResults().get(workItem.getId()).get("Receipt");
        assertNotNull(receipt);
    }

    @Test
    public void testQueryExistingContract() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ServiceURL",
                              "http://localhost:8545/");
        workItem.setParameter("ContractAddress",
                              "0x00211e7e");
        workItem.setParameter("ContractMethodName",
                              "testQuery");
        workItem.setParameter("MethodOutputType",
                              null);

        QueryExistingContractWorkitemHandler handler = new QueryExistingContractWorkitemHandler(TEST_WALLET_PASSWORD,
                                                                                                "wallet/testwallet.json");
        handler.setWeb3j(web3j);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }

    @Test
    public void testTransactExistingContract() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ServiceURL",
                              "http://localhost:8545/");
        workItem.setParameter("ContractAddress",
                              "0x00211e7e");
        workItem.setParameter("MethodName",
                              "testQuery");
        workItem.setParameter("MethodInputType",
                              null);
        workItem.setParameter("WaitForReceipt",
                              "false");
        workItem.setParameter("DepositAmount",
                              "10");

        TransactExistingContractWorkitemHandler handler = new TransactExistingContractWorkitemHandler(TEST_WALLET_PASSWORD,
                                                                                                      "wallet/testwallet.json");
        handler.setWeb3j(web3j);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }

    @Test
    public void testDeployContract() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ServiceURL",
                              "http://localhost:8545/");
        workItem.setParameter("ContractPath",
                              "contract/Storage.bin");
        workItem.setParameter("DepositAmount",
                              "10");
        workItem.setParameter("WaitForReceipt",
                              "false");

        DeployContractWorkitemHandler handler = new DeployContractWorkitemHandler(TEST_WALLET_PASSWORD,
                                                                                  "wallet/testwallet.json");
        handler.setWeb3j(web3j);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
    }

    @Test
    public void testObserveContractUpdates() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("ServiceURL",
                              "http://localhost:8545/");
        workItem.setParameter("ContractAddress",
                              "0x00211e7e");
        workItem.setParameter("EventName",
                              "AmountUpdatedEvent");
        workItem.setParameter("EventReturnType",
                              "int256");
        workItem.setParameter("SignalName",
                              "mysignal");
        workItem.setParameter("AbortOnUpdate",
                              "true");

        ObserveContractEventWorkitemHandler handler = new ObserveContractEventWorkitemHandler(kieSession);
        handler.setWeb3j(web3j);

        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
    }

    @Test
    public void testInvalidParameters() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();

        DeployContractWorkitemHandler deployContractHandler = new DeployContractWorkitemHandler(TEST_WALLET_PASSWORD,
                                                                                                "wallet/testwallet.json");
        try {
            deployContractHandler.executeWorkItem(workItem,
                                                  manager);
            fail("Exception on invalid parameters no thrown");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }

        GetBalanceWorkitemHandler getBalanceHandler = new GetBalanceWorkitemHandler(TEST_WALLET_PASSWORD,
                                                                                    "wallet/testwallet.json");

        try {
            getBalanceHandler.executeWorkItem(workItem,
                                              manager);
            fail("Exception on invalid parameters no thrown");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }

        ObserveContractEventWorkitemHandler observeContractHandler = new ObserveContractEventWorkitemHandler(kieSession);
        try {
            observeContractHandler.executeWorkItem(workItem,
                                                   manager);
            fail("Exception on invalid parameters no thrown");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }

        QueryExistingContractWorkitemHandler queryExistingHandler = new QueryExistingContractWorkitemHandler(TEST_WALLET_PASSWORD,
                                                                                                             "wallet/testwallet.json");

        try {
            queryExistingHandler.executeWorkItem(workItem,
                                                 manager);
            fail("Exception on invalid parameters no thrown");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }

        SendEtherWorkitemHandler sendEtherHandler = new SendEtherWorkitemHandler(TEST_WALLET_PASSWORD,
                                                                                 "wallet/testwallet.json");
        try {
            sendEtherHandler.executeWorkItem(workItem,
                                             manager);
            fail("Exception on invalid parameters no thrown");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }

        TransactExistingContractWorkitemHandler transactExistingHandler = new TransactExistingContractWorkitemHandler(TEST_WALLET_PASSWORD,
                                                                                                                      "wallet/testwallet.json");
        try {
            transactExistingHandler.executeWorkItem(workItem,
                                                    manager);
            fail("Exception on invalid parameters no thrown");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }
    }
}
