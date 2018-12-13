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
package org.jbpm.process.workitem.okta;

import com.okta.sdk.authc.credentials.ClientCredentials;
import com.okta.sdk.authc.credentials.TokenClientCredentials;
import com.okta.sdk.client.Client;
import com.okta.sdk.client.Clients;

public class OktaAuth {

    public Client authorize() {
        return Clients.builder().build();
    }

    public Client authorize(String oktaApiToken) {
        ClientCredentials clientCredentials = new TokenClientCredentials(oktaApiToken);
        return Clients.builder().setClientCredentials(clientCredentials).build();

    }
}
