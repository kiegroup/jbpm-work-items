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
package org.jbpm.process.workitem.pastebin;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.jpastebin.pastebin.Pastebin;
import org.jpastebin.pastebin.PastebinLink;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "GetPastebin.wid", name = "GetPastebin",
        displayName = "GetPastebin",
        defaultHandler = "mvel: new org.jbpm.process.workitem.pastebin.GetExistingPastebinWorkitemHandler(\"develKey\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        parameters = {
                @WidParameter(name = "PastebinKey", required = true)
        },
        results = {
                @WidResult(name = "PasteContent")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "paste,pastebin,get,existing",
                action = @WidAction(title = "Get an existing Pastebin"),
                authinfo = @WidAuth(required = true, params = {"develKey"},
                        paramsdescription = {"Pastebin developer key"},
                        referencesite = "https://pastebin.com/api.php")
        ))
public class GetExistingPastebinWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String develKey;

    private static final Logger logger = LoggerFactory
            .getLogger(CreatePastebinWorkitemHandler.class);
    private static final String RESULTS_VALUE = "PasteContent";

    public GetExistingPastebinWorkitemHandler(String develKey) {
        this.develKey = develKey;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {
            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Map<String, Object> results = new HashMap<String, Object>();

            String pastebinKey = (String) workItem.getParameter("PastebinKey");

            PastebinLink pastebinLink = (PastebinLink) workItem.getParameter("PastebinLink");
            if (pastebinLink == null) {
                pastebinLink = Pastebin.getPaste(pastebinKey);
            }

            pastebinLink.fetchContent();
            results.put(RESULTS_VALUE,
                        pastebinLink.getPaste().getContents());
            workItemManager.completeWorkItem(workItem.getId(),
                                             results);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }
}
