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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.AbiTypes;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

public class EthereumUtils {

    public static final BigInteger DEFAULT_GAS_PRICE = BigInteger.valueOf(20000000000L);
    public static final BigInteger DEFAULT_GAS_LIMIT = BigInteger.valueOf(500000L);
    public static final int DEFAULT_SLEEP_DURATION = 15000;
    public static final int DEFAULT_ATTEMPTS = 40;
    public static final String TMP_FILE_PREFIX = "tmpfile";
    public static final String TMP_FILE_SUFFIX = ".tmp";

    private static final Logger logger = LoggerFactory.getLogger(EthereumUtils.class);

    public static Function getFunction(String queryName,
                                       List<Type> queryInputTypes,
                                       List<TypeReference<?>> queryOutputTypes) {

        List<Type> inputParameters;
        if (queryInputTypes != null) {
            inputParameters = queryInputTypes;
        } else {
            inputParameters = new ArrayList<>();
        }

        List<TypeReference<?>> outputTypes;
        if (queryOutputTypes != null) {
            outputTypes = queryOutputTypes;
        } else {
            outputTypes = new ArrayList<>();
        }

        Function function = new Function(queryName,
                                         inputParameters,
                                         outputTypes);

        return function;
    }

    public static String getEncodedFunction(String queryName,
                                            List<Type> queryInputTypes,
                                            List<TypeReference<?>> queryOutputTypes) {

        return FunctionEncoder.encode(getFunction(queryName,
                                                  queryInputTypes,
                                                  queryOutputTypes));
    }

    public static String getEncodedFunction(Function function) {
        return FunctionEncoder.encode(function);
    }

    public static BigInteger getNextNonce(String address,
                                          Web3j web3j) throws Exception {
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                address,
                DefaultBlockParameterName.LATEST).sendAsync().get();
        return ethGetTransactionCount.getTransactionCount();
    }

    public static BigDecimal getBalanceInEther(Credentials credentials,
                                               Web3j web3j) throws Exception {
        EthGetBalance ethGetBalance = web3j.ethGetBalance(credentials.getAddress(),
                                                          DefaultBlockParameterName.LATEST).send();

        return Convert.fromWei(ethGetBalance.getBalance().toString(),
                               Convert.Unit.ETHER);
    }

    public static String deployContract(Credentials credentials,
                                        Web3j web3j,
                                        String contractBinary,
                                        int toSendEther,
                                        boolean waitForReceipt,
                                        int sleepDuration,
                                        int attempts) throws Exception {

        BigInteger depositEtherAmountToSend = BigInteger.valueOf(toSendEther);

        RawTransaction rawTransaction = RawTransaction.createContractTransaction(
                getNextNonce(credentials.getAddress(),
                             web3j),
                DEFAULT_GAS_PRICE,
                DEFAULT_GAS_LIMIT,
                depositEtherAmountToSend,
                contractBinary);

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction,
                                                              credentials);

        String hexValue = Numeric.toHexString(signedMessage);

        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();

        if (waitForReceipt) {
            TransactionReceipt transReceipt = waitForTransactionReceipt(
                    ethSendTransaction.getTransactionHash(),
                    sleepDuration,
                    attempts,
                    web3j
            );
            if (transReceipt != null) {
                return transReceipt.getContractAddress();
            }
        }
        // we dont have a contract address
        logger.warn("Unable to retrieve contract address.");
        return null;
    }

    public static Object queryExistingContract(Credentials credentials,
                                               Web3j web3j,
                                               String contractAddress,
                                               String contractMethodName,
                                               List<Type> contractMethodInputTypes,
                                               List<TypeReference<?>> contractMethodOutputTypes
    ) throws Exception {

        Function function = getFunction(contractMethodName,
                                        contractMethodInputTypes,
                                        contractMethodOutputTypes);

        Transaction transaction = Transaction.createEthCallTransaction(credentials.getAddress(),
                                                                       contractAddress,
                                                                       getEncodedFunction(function));

        EthCall response = web3j.ethCall(
                transaction,
                DefaultBlockParameterName.LATEST).sendAsync().get();

        List<Type> responseTypeList = FunctionReturnDecoder.decode(
                response.getValue(),
                function.getOutputParameters());

        if (responseTypeList != null && responseTypeList.size() > 0) {
            return responseTypeList.get(0).getValue();
        } else {
            return null;
        }
    }

    public static TransactionReceipt transactExistingContract(Credentials credentials,
                                                              Web3j web3j,
                                                              int etherAmount,
                                                              BigInteger gasPrice,
                                                              BigInteger gasLimit,
                                                              String toAddress,
                                                              String methodName,
                                                              List<Type> methodInputTypes,
                                                              List<TypeReference<?>> methodOutputTypes,
                                                              boolean waitForReceipt,
                                                              int sleepDuration,
                                                              int attempts
    ) throws Exception {
        BigInteger etherAmountToSend = BigInteger.valueOf(etherAmount);

        Transaction transaction = Transaction.createFunctionCallTransaction(
                credentials.getAddress(),
                getNextNonce(credentials.getAddress(),
                             web3j),
                gasPrice,
                gasLimit,
                toAddress,
                etherAmountToSend,
                getEncodedFunction(methodName,
                                   methodInputTypes,
                                   methodOutputTypes));

        EthSendTransaction transactionResponse =
                web3j.ethSendTransaction(transaction).sendAsync().get();

        if (waitForReceipt) {
            TransactionReceipt transReceipt = waitForTransactionReceipt(
                    transactionResponse.getTransactionHash(),
                    sleepDuration,
                    attempts,
                    web3j
            );
            return transReceipt;
        }
        // we dont have a transaction receipt
        logger.warn("Unable to retrieve transaction receipt.");
        return null;
    }

    public static TransactionReceipt sendFundsToContract(Credentials credentials,
                                                         Web3j web3j,
                                                         int etherAmount,
                                                         String toAddress,
                                                         Transfer transfer) throws Exception {

        return transfer.sendFunds(
                toAddress,
                new BigDecimal(etherAmount),
                Convert.Unit.ETHER).send();
    }

    public static TransactionReceipt waitForTransactionReceipt(
            String transactionHash,
            int sleepDuration,
            int attempts,
            Web3j web3j) throws Exception {

        Optional<TransactionReceipt> transactionReceiptOptional =
                getTransactionReceipt(transactionHash,
                                      sleepDuration,
                                      attempts,
                                      web3j);

        if (!transactionReceiptOptional.isPresent()) {
            logger.warn("Transaction receipt not generated after " + attempts + " attempts.");
        }

        return transactionReceiptOptional.get();
    }

    public static Optional<TransactionReceipt> getTransactionReceipt(
            String transactionHash,
            int sleepDuration,
            int attempts,
            Web3j web3j) throws Exception {

        Optional<TransactionReceipt> receiptOptional =
                sendTransactionReceiptRequest(transactionHash,
                                              web3j);
        for (int i = 0; i < attempts; i++) {
            if (!receiptOptional.isPresent()) {
                Thread.sleep(sleepDuration);
                receiptOptional = sendTransactionReceiptRequest(transactionHash,
                                                                web3j);
            } else {
                break;
            }
        }

        return receiptOptional;
    }

    public static Optional<TransactionReceipt> sendTransactionReceiptRequest(
            String transactionHash,
            Web3j web3j) throws Exception {
        EthGetTransactionReceipt transactionReceipt =
                web3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();

        return transactionReceipt.getTransactionReceipt();
    }

    public static void observeContractEvent(Web3j web3j,
                                            String contractEventName,
                                            String contractAddress,
                                            List<TypeReference<?>> indexedParameters,
                                            List<TypeReference<?>> nonIndexedParameters,
                                            String eventReturnType,
                                            KieSession kieSession,
                                            String signalName,
                                            boolean doAbortOnUpdate,
                                            WorkItemManager workItemManager,
                                            WorkItem workItem) {

        Event event = new Event(contractEventName,
                                indexedParameters,
                                nonIndexedParameters);

        EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST,
                                         DefaultBlockParameterName.LATEST,
                                         contractAddress);

        filter.addSingleTopic(EventEncoder.encode(event));

        Class<Type> type = (Class<Type>) AbiTypes.getType(eventReturnType);
        TypeReference<Type> typeRef = TypeReference.create(type);

        web3j.ethLogObservable(filter).subscribe(
                eventTrigger -> {
                    kieSession.signalEvent(signalName,
                                           FunctionReturnDecoder.decode(
                                                   eventTrigger.getData(),
                                                   Arrays.asList(typeRef)).get(0).getValue());
                    if (doAbortOnUpdate) {
                        workItemManager.completeWorkItem(workItem.getId(),
                                                         null);
                    }
                }
        );
    }

    public static void observeContractEvent(Web3j web3j,
                                            String contractEventName,
                                            String contractAddress,
                                            List<TypeReference<?>> indexedParameters,
                                            List<TypeReference<?>> nonIndexedParameters,
                                            String eventReturnType,
                                            rx.functions.Action1 action1) {

        Event event = new Event(contractEventName,
                                indexedParameters,
                                nonIndexedParameters);

        EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST,
                                         DefaultBlockParameterName.LATEST,
                                         contractAddress);

        filter.addSingleTopic(EventEncoder.encode(event));

        Class<Type> type = (Class<Type>) AbiTypes.getType(eventReturnType);
        TypeReference<Type> typeRef = TypeReference.create(type);

        web3j.ethLogObservable(filter).subscribe(action1);
    }

    public static File createTmpFile(InputStream in) throws IOException {
        return createTmpFile(in,
                             TMP_FILE_PREFIX,
                             TMP_FILE_SUFFIX);
    }

    public static File createTmpFile(InputStream in,
                                     String filePrefix,
                                     String fileSuffix) throws IOException {
        final File tempFile = File.createTempFile(filePrefix,
                                                  fileSuffix);
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in,
                         out);
        }
        return tempFile;
    }

    public static String convertStreamToStr(InputStream inputStream) throws IOException {
        return IOUtils.toString(inputStream,
                                StandardCharsets.UTF_8);
    }
}
