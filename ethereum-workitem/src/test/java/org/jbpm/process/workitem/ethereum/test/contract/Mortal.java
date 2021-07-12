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
package org.jbpm.process.workitem.ethereum.test.contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j">codegen module</a> to update.
 * <p>
 * <p>Generated with web3j version 3.0.2.
 */
public final class Mortal extends Contract {

    private static final String BINARY = "6060604052341561000c57fe5b5b60008054600160a060020a03191633600160a060020a03161790555b5b609c806100386000396000f300606060405263ffffffff60e060020a60003504166341c0e1b581146020575bfe5b3415602757fe5b602d602f565b005b6000543373ffffffffffffffffffffffffffffffffffffffff90811691161415606d5760005473ffffffffffffffffffffffffffffffffffffffff16ff5b5b5600a165627a7a7230582026f6f00e6980582e4d0dd31e652bd6627082567d1983f2d7e3a0605ef29e45630029";

    private Mortal(String contractAddress,
                   Web3j web3j,
                   Credentials credentials,
                   BigInteger gasPrice,
                   BigInteger gasLimit) {
        super(BINARY,
              contractAddress,
              web3j,
              credentials,
              gasPrice,
              gasLimit);
    }

    private Mortal(String contractAddress,
                   Web3j web3j,
                   TransactionManager transactionManager,
                   BigInteger gasPrice,
                   BigInteger gasLimit) {
        super(BINARY,
              contractAddress,
              web3j,
              transactionManager,
              gasPrice,
              gasLimit);
    }

    public RemoteCall<TransactionReceipt> kill() {
        Function function = new Function(
                "kill",
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public static RemoteCall<Mortal> deploy(Web3j web3j,
                                            Credentials credentials,
                                            BigInteger gasPrice,
                                            BigInteger gasLimit) {
        return deployRemoteCall(Mortal.class,
                                web3j,
                                credentials,
                                gasPrice,
                                gasLimit,
                                BINARY,
                                "");
    }

    public static RemoteCall<Mortal> deploy(Web3j web3j,
                                            TransactionManager transactionManager,
                                            BigInteger gasPrice,
                                            BigInteger gasLimit) {
        return deployRemoteCall(Mortal.class,
                                web3j,
                                transactionManager,
                                gasPrice,
                                gasLimit,
                                BINARY,
                                "");
    }

    public static Mortal load(String contractAddress,
                              Web3j web3j,
                              Credentials credentials,
                              BigInteger gasPrice,
                              BigInteger gasLimit) {
        return new Mortal(contractAddress,
                          web3j,
                          credentials,
                          gasPrice,
                          gasLimit);
    }

    public static Mortal load(String contractAddress,
                              Web3j web3j,
                              TransactionManager transactionManager,
                              BigInteger gasPrice,
                              BigInteger gasLimit) {
        return new Mortal(contractAddress,
                          web3j,
                          transactionManager,
                          gasPrice,
                          gasLimit);
    }
}
