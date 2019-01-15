/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.process.workitem.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

public class DockerClientConnector {

    public DockerClient getDockerClient() {
        return DockerClientBuilder.getInstance().build();
    }

    public DockerClient getDockerClient(String serverURL) {
        return DockerClientBuilder.getInstance(serverURL).build();
    }

    public DockerClient getDockerClient(String serverURL,
                                        String certPath) {
        DefaultDockerClientConfig config
                = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(serverURL).withDockerCertPath(certPath).build();
        return DockerClientBuilder.getInstance(config).build();
    }

    public DockerClient getDockerClient(String registryUsername,
                                        String registryPassword,
                                        String registryEmail,
                                        String dockerCertPath,
                                        String dockerConfig,
                                        String dockerTlsVerify,
                                        String dockerHost) {
        DefaultDockerClientConfig config
                = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withRegistryEmail(registryEmail)
                .withRegistryPassword(registryPassword)
                .withRegistryUsername(registryUsername)
                .withDockerCertPath(dockerCertPath)
                .withDockerConfig(dockerConfig)
                .withDockerTlsVerify(dockerTlsVerify)
                .withDockerHost(dockerHost).build();

        return DockerClientBuilder.getInstance(config).build();
    }
}
