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
package org.jbpm.process.workitem.docker.responses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ContainerConfig;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.NetworkSettings;
import com.github.dockerjava.api.model.VolumeBind;
import com.github.dockerjava.api.model.VolumeRW;

public class SerializableInspectContainerResponse extends InspectContainerResponse implements Serializable {

    private String[] args;
    private ContainerConfig config;
    private String created;
    private String driver;
    private String execDriver;
    private HostConfig hostConfig;
    private String hostnamePath;
    private String hostsPath;
    private String logPath;
    private String id;
    private Integer sizeRootFs;
    private String imageId;
    private String mountLabel;
    private String name;
    private Integer restartCount;
    private NetworkSettings networkSettings;
    private String path;
    private String processLabel;
    private String resolvConfPath;
    private List<String> execIds;
    private SerializableContainerState serializableState;
    private VolumeBind[] volumes;
    private VolumeRW[] volumesRW;
    private List<SerializableMount> serializableMounts;

    public SerializableInspectContainerResponse() {
        super();
    }

    public SerializableInspectContainerResponse(InspectContainerResponse inspectContainerResponse) {
        this.created = inspectContainerResponse.getCreated();
        this.args = inspectContainerResponse.getArgs();
        this.config = inspectContainerResponse.getConfig();
        this.driver = inspectContainerResponse.getDriver();
        this.execDriver = inspectContainerResponse.getDriver();
        this.hostConfig = inspectContainerResponse.getHostConfig();
        this.hostnamePath = inspectContainerResponse.getHostnamePath();
        this.hostsPath = inspectContainerResponse.getHostsPath();
        this.logPath = inspectContainerResponse.getLogPath();
        this.id = inspectContainerResponse.getId();
        this.sizeRootFs = inspectContainerResponse.getSizeRootFs();
        this.imageId = inspectContainerResponse.getImageId();
        this.mountLabel = inspectContainerResponse.getMountLabel();
        this.name = inspectContainerResponse.getName();
        this.restartCount = inspectContainerResponse.getRestartCount();
        this.networkSettings = inspectContainerResponse.getNetworkSettings();
        this.path = inspectContainerResponse.getPath();
        this.processLabel = inspectContainerResponse.getProcessLabel();
        this.resolvConfPath = inspectContainerResponse.getResolvConfPath();
        this.execIds = inspectContainerResponse.getExecIds();
        this.serializableState = new SerializableContainerState(inspectContainerResponse.getState());
        this.volumes = inspectContainerResponse.getVolumes();
        this.volumesRW = inspectContainerResponse.getVolumesRW();

        if (inspectContainerResponse.getMounts() != null) {
            List<SerializableMount> serializableMountList = new ArrayList<>();
            for (Mount mount : inspectContainerResponse.getMounts()) {
                serializableMountList.add(new SerializableMount(mount));
            }
            this.serializableMounts = serializableMountList;
        }
    }

    @Override
    public String[] getArgs() {
        return args;
    }

    @Override
    public ContainerConfig getConfig() {
        return config;
    }

    @Override
    public String getCreated() {
        return created;
    }

    @Override
    public String getDriver() {
        return driver;
    }

    @Override
    public String getExecDriver() {
        return execDriver;
    }

    @Override
    public HostConfig getHostConfig() {
        return hostConfig;
    }

    @Override
    public String getHostnamePath() {
        return hostnamePath;
    }

    @Override
    public String getHostsPath() {
        return hostsPath;
    }

    @Override
    public String getLogPath() {
        return logPath;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Integer getSizeRootFs() {
        return sizeRootFs;
    }

    @Override
    public String getImageId() {
        return imageId;
    }

    @Override
    public String getMountLabel() {
        return mountLabel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getRestartCount() {
        return restartCount;
    }

    @Override
    public NetworkSettings getNetworkSettings() {
        return networkSettings;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getProcessLabel() {
        return processLabel;
    }

    @Override
    public String getResolvConfPath() {
        return resolvConfPath;
    }

    @Override
    public List<String> getExecIds() {
        return execIds;
    }

    public SerializableContainerState getSerializableState() {
        return serializableState;
    }

    @Override
    public VolumeBind[] getVolumes() {
        return volumes;
    }

    @Override
    public VolumeRW[] getVolumesRW() {
        return volumesRW;
    }

    public List<SerializableMount> getSerializableMounts() {
        return serializableMounts;
    }
}
