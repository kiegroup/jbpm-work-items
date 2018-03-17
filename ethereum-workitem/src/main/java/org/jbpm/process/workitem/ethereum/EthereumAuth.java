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

import org.apache.commons.io.IOUtils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

public class EthereumAuth {

    public static final String TMP_FILE_PREFIX = "tmpwalletfile";
    public static final String TMP_FILE_SUFFIX = ".tmp";

    private Credentials walletCredentials;

    public EthereumAuth(String walletPassword,
                        String walletFilePath) throws Exception {
        walletCredentials = WalletUtils.loadCredentials(walletPassword,
                                                        walletFilePath);
    }

    public EthereumAuth(String walletPassword,
                        File wallet) throws Exception {
        walletCredentials = WalletUtils.loadCredentials(walletPassword,
                                                        wallet);
    }

    public EthereumAuth(String walletPassword,
                        String walletFileName,
                        ClassLoader classLoader) throws Exception {
        walletCredentials = WalletUtils.loadCredentials(walletPassword,
                                                        createTmpWalletFile(classLoader.getResourceAsStream(walletFileName)));
    }

    public Credentials getCredentials() {
        return walletCredentials;
    }

    private File createTmpWalletFile(InputStream in) throws IOException {
        final File tempFile = File.createTempFile(TMP_FILE_PREFIX,
                                                  TMP_FILE_SUFFIX);
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in,
                         out);
        }
        return tempFile;
    }
}
