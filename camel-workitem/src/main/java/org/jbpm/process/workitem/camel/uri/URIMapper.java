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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.camel.util.URISupport;

public abstract class URIMapper {

    private String schema;

    public URIMapper(String schema) {
        this.schema = schema;
    }

    public abstract URI toURI(Map<String, Object> params) throws URISyntaxException;

    protected URI prepareCamelUri(String path,
                                  Map<String, Object> params) throws URISyntaxException {
        return prepareCamelUri(this.schema,
                               path,
                               params);
    }

    protected URI prepareCamelUri(String schema,
                                  String path,
                                  Map<String, Object> params) throws URISyntaxException {
        String url;
        if (schema == null) {
            url = path;
        } else {
            url = schema + "://" + path;
        }
        URI camelUri;
        try {
            camelUri = new URI(URISupport.normalizeUri(url));
        } catch (UnsupportedEncodingException e) {
            camelUri = new URI(url);
        }
        if (params.isEmpty()) {
            return camelUri;
        } else {
            return URISupport.createURIWithQuery(camelUri,
                                                 URISupport.createQueryString(params));
        }
    }
}
