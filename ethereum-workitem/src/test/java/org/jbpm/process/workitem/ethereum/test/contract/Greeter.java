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

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
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
public final class Greeter extends Contract {

    private static final String BINARY = "6060604052341561000c57fe5b6040516102f03803806102f0833981016040528051015b5b60008054600160a060020a03191633600160a060020a03161790555b805161005390600190602084019061005b565b505b506100fb565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061009c57805160ff19168380011785556100c9565b828001600101855582156100c9579182015b828111156100c95782518255916020019190600101906100ae565b5b506100d69291506100da565b5090565b6100f891905b808211156100d657600081556001016100e0565b5090565b90565b6101e68061010a6000396000f300606060405263ffffffff60e060020a60003504166341c0e1b5811461002c578063cfae32171461003e575bfe5b341561003457fe5b61003c6100ce565b005b341561004657fe5b61004e610110565b604080516020808252835181830152835191928392908301918501908083838215610094575b80518252602083111561009457601f199092019160209182019101610074565b505050905090810190601f1680156100c05780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b6000543373ffffffffffffffffffffffffffffffffffffffff9081169116141561010d5760005473ffffffffffffffffffffffffffffffffffffffff16ff5b5b565b6101186101a8565b60018054604080516020600284861615610100026000190190941693909304601f8101849004840282018401909252818152929183018282801561019d5780601f106101725761010080835404028352916020019161019d565b820191906000526020600020905b81548152906001019060200180831161018057829003601f168201915b505050505090505b90565b604080516020810190915260008152905600a165627a7a723058209fdbd3c1fde36a11d805a261fce90c3cf8345967079312dd0af7eb4af808f08c0029";

    private Greeter(String contractAddress,
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

    private Greeter(String contractAddress,
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

    public RemoteCall<String> greet() {
        Function function = new Function("greet",
                                         Arrays.<Type>asList(),
                                         Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                                         }));
        return executeRemoteCallSingleValueReturn(function,
                                                  String.class);
    }

    public static RemoteCall<Greeter> deploy(Web3j web3j,
                                             Credentials credentials,
                                             BigInteger gasPrice,
                                             BigInteger gasLimit,
                                             String _greeting) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(_greeting)));
        return deployRemoteCall(Greeter.class,
                                web3j,
                                credentials,
                                gasPrice,
                                gasLimit,
                                BINARY,
                                encodedConstructor);
    }

    public static RemoteCall<Greeter> deploy(Web3j web3j,
                                             TransactionManager transactionManager,
                                             BigInteger gasPrice,
                                             BigInteger gasLimit,
                                             String _greeting) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(_greeting)));
        return deployRemoteCall(Greeter.class,
                                web3j,
                                transactionManager,
                                gasPrice,
                                gasLimit,
                                BINARY,
                                encodedConstructor);
    }

    public static Greeter load(String contractAddress,
                               Web3j web3j,
                               Credentials credentials,
                               BigInteger gasPrice,
                               BigInteger gasLimit) {
        return new Greeter(contractAddress,
                           web3j,
                           credentials,
                           gasPrice,
                           gasLimit);
    }

    public static Greeter load(String contractAddress,
                               Web3j web3j,
                               TransactionManager transactionManager,
                               BigInteger gasPrice,
                               BigInteger gasLimit) {
        return new Greeter(contractAddress,
                           web3j,
                           transactionManager,
                           gasPrice,
                           gasLimit);
    }
}
