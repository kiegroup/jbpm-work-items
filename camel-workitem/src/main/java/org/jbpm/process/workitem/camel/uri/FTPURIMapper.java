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
package org.jbpm.process.workitem.camel.uri;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class FTPURIMapper extends URIMapper {

    public FTPURIMapper(String schema) {
        super(schema);
    }

    @Override
    public URI toURI(Map<String, Object> params) throws URISyntaxException {
        String hostname = (String) params.remove("hostname");
        String username = (String) params.remove("username");
        String port = (String) params.remove("port");
        String directoryname = (String) params.remove("directoryname");

        String path = (username == null ? "" : username + "@") + (hostname == null ? "" : hostname) + (port == null ? "" : ":" + port)
                + (directoryname == null ? "" : "/" + directoryname);

        return prepareCamelUri(path,
                               params);
    }
}
