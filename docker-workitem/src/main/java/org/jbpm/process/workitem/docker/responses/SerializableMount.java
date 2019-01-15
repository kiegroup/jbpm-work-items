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
import com.github.dockerjava.api.model.Volume;

public class SerializableMount implements Serializable {

    private String name;
    private String source;
    private Volume destination;
    private String driver;
    private String mode;
    private Boolean rw;

    public SerializableMount(InspectContainerResponse.Mount mount) {
        if(mount != null) {
            this.name = mount.getName();
            this.source = mount.getSource();
            this.driver = mount.getDriver();
            this.mode = mount.getMode();
            this.rw = mount.getRW();
            this.destination = mount.getDestination();
        }
    }

    public String getName() {
        return name;
    }

    public String getSource() {
        return source;
    }

    public Volume getDestination() {
        return destination;
    }

    public String getDriver() {
        return driver;
    }

    public String getMode() {
        return mode;
    }

    public Boolean getRw() {
        return rw;
    }
}
