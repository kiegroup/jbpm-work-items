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
package org.jbpm.process.workitem.camel;

import java.util.Set;

import org.jbpm.process.workitem.camel.request.RequestPayloadMapper;
import org.jbpm.process.workitem.camel.response.ResponsePayloadMapper;
import org.jbpm.process.workitem.camel.uri.XSLTURIMapper;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;

@Wid(widfile = "CamelXSLTConnector.wid", name = "CamelXSLTConnector",
        displayName = "CamelXSLTConnector",
        defaultHandler = "mvel: new org.jbpm.process.workitem.camel.XSLTCamelWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        parameters = {
                @WidParameter(name = "payload", required = true),
                @WidParameter(name = "templateName", required = true)
        },
        results = {
                @WidResult(name = "response")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "apache,camel,xslt,template,message,payload,route,connector",
                action = @WidAction(title = "Process a message using an XSLT template")
        ))
public class XSLTCamelWorkitemHandler extends AbstractCamelWorkitemHandler {

    public XSLTCamelWorkitemHandler() {
        this.uriConverter = new XSLTURIMapper();
        this.requestMapper = new RequestPayloadMapper("payload");
        this.responseMapper = new ResponsePayloadMapper();
    }

    public XSLTCamelWorkitemHandler(Set<String> headers) {
        this.uriConverter = new XSLTURIMapper();
        this.requestMapper = new RequestPayloadMapper("payload",
                                                      headers);
        this.responseMapper = new ResponsePayloadMapper();
    }
}
