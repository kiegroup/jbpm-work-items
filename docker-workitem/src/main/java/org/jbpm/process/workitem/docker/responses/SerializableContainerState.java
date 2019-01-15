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

import com.github.dockerjava.api.command.InspectContainerResponse;

public class SerializableContainerState implements Serializable {

    private String status;
    private Boolean running;
    private Boolean paused;
    private Boolean restarting;
    private Boolean oomKilled;
    private Boolean dead;
    private Integer pid;
    private Integer exitCode;
    private String error;
    private String startedAt;
    private String finishedAt;

    public SerializableContainerState(InspectContainerResponse.ContainerState containerState) {
        if(containerState != null) {
            this.status = containerState.getStatus();
            this.startedAt = containerState.getStartedAt();
            this.running = containerState.getRunning();
            this.paused = containerState.getPaused();
            this.restarting = containerState.getRestarting();
            this.oomKilled = containerState.getOOMKilled();
            this.dead = containerState.getDead();
            this.pid = containerState.getPid();
            this.exitCode = containerState.getExitCode();
            this.error = containerState.getError();
            this.startedAt = containerState.getStartedAt();
            this.finishedAt = containerState.getFinishedAt();
        }
    }

    public String getStatus() {
        return status;
    }

    public Boolean getRunning() {
        return running;
    }

    public Boolean getPaused() {
        return paused;
    }

    public Boolean getRestarting() {
        return restarting;
    }

    public Boolean getOomKilled() {
        return oomKilled;
    }

    public Boolean getDead() {
        return dead;
    }

    public Integer getPid() {
        return pid;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public String getError() {
        return error;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public String getFinishedAt() {
        return finishedAt;
    }
}
