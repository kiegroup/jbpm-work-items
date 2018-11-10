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
package org.jbpm.process.workitem.vimeo;

import java.io.Serializable;

import com.clickntap.vimeo.VimeoResponse;
import org.json.JSONObject;

public class SerializableVimeoResponse implements Serializable {

    private JSONObject json;
    private JSONObject headers;
    private int statusCode;

    public SerializableVimeoResponse(VimeoResponse vimeoResponse) {
        this.json = vimeoResponse.getJson();
        this.headers = vimeoResponse.getHeaders();
        this.statusCode = vimeoResponse.getStatusCode();
    }

    public JSONObject getJson() {
        return this.json;
    }

    public JSONObject getHeaders() {
        return this.headers;
    }

    public int getRateLimit() throws Exception {
        return this.getHeaders().getInt("X-RateLimit-Limit");
    }

    public int getRateLimitRemaining() throws Exception {
        return this.getHeaders().getInt("X-RateLimit-Remaining");
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String toString() {
        try {
            return "HTTP Status Code: \n" + this.getStatusCode() +
                    "\nJson: \n" + this.getJson().toString(2) +
                    "\nHeaders: \n" + this.getHeaders().toString(2);
        } catch (Exception e) {
            return "";
        }
    }
}
