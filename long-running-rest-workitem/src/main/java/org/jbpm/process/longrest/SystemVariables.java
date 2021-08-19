/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.process.longrest;

public class SystemVariables {

    private String callbackUrl;
    private String callbackMethod;
    private String heartBeatUrl;
    private String heartBeatMethod;

    public SystemVariables(String callbackUrl, String callbackMethod, String heartBeatUrl, String heartBeatMethod) {
        this.callbackUrl = callbackUrl;
        this.callbackMethod = callbackMethod;
        this.heartBeatUrl = heartBeatUrl;
        this.heartBeatMethod = heartBeatMethod;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getCallbackMethod() {
        return callbackMethod;
    }

    public String getHeartBeatUrl() {
        return heartBeatUrl;
    }

    public String getHeartBeatMethod() {
        return heartBeatMethod;
    }
}
