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

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.document.Document;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.jpastebin.pastebin.PastebinLink;
import org.jpastebin.pastebin.PastebinPaste;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "CreatePastebin.wid", name = "CreatePastebin",
        displayName = "CreatePastebin",
        defaultHandler = "mvel: new org.jbpm.process.workitem.pastebin.CreatePastebinWorkitemHandler(\"develKey\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "icon.png",
        parameters = {
                @WidParameter(name = "Title"),
                @WidParameter(name = "Content", required = true, runtimeType = "java.lang.Object"),
                @WidParameter(name = "Format"),
                @WidParameter(name = "Visibility"),
                @WidParameter(name = "Author")
        },
        results = {
                @WidResult(name = "PasteURL", runtimeType = "java.net.URL")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "paste,pastebin,create",
                action = @WidAction(title = "Create a paste on Pastebin"),
                authinfo = @WidAuth(required = true, params = {"develKey"},
                        paramsdescription = {"Pastebin developer key"},
                        referencesite = "https://pastebin.com/api.php")
        ))
public class CreatePastebinWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String develKey;

    private static final Logger logger = LoggerFactory
            .getLogger(CreatePastebinWorkitemHandler.class);
    private static final String RESULTS_VALUE = "PasteURL";

    public CreatePastebinWorkitemHandler(String develKey) {
        this.develKey = develKey;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        try {
            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Map<String, Object> results = new HashMap<String, Object>();
            String title = (String) workItem.getParameter("Title");
            Object content = workItem.getParameter("Content");
            String format = (String) workItem.getParameter("Format");
            String visibility = (String) workItem.getParameter("Visibility");
            String author = (String) workItem.getParameter("Author");

            PastebinPaste pastebin = (PastebinPaste) workItem.getParameter("Pastebin");
            if (pastebin == null) {
                pastebin = new PastebinPaste();
            }

            pastebin.setDeveloperKey(develKey);

            if (content instanceof Document) {
                pastebin.setContents(new String(((Document) content).getContent(),
                                                StandardCharsets.UTF_8));
            } else if (content instanceof String) {
                pastebin.setContents((String) content);
            } else {
                throw new IllegalArgumentException("Invalid type for " + content + ". Should be Document or String type");
            }

            if (title != null && title.trim().length() > 0) {
                pastebin.setPasteTitle(title);
            }

            if (format != null && format.trim().length() > 0) {
                pastebin.setPasteFormat(format);
            }

            if (visibility != null && visibility.trim().length() > 0 && visibility.matches("-?\\d+")) {
                int visibilityInt = Integer.parseInt(visibility);

                switch (visibilityInt) {
                    case 0:
                        pastebin.setVisibility(PastebinPaste.VISIBILITY_PUBLIC);
                        break;
                    case 1:
                        pastebin.setVisibility(PastebinPaste.VISIBILITY_UNLISTED);
                        break;
                    case 2:
                        pastebin.setVisibility(PastebinPaste.VISIBILITY_PRIVATE);
                        break;
                    default:
                        pastebin.setVisibility(PastebinPaste.VISIBILITY_PUBLIC);
                        break;
                }
            } else {
                pastebin.setVisibility(PastebinPaste.VISIBILITY_PUBLIC);
            }

            if (author != null) {
                pastebin.setPasteAuthor(author);
            }

            PastebinLink pastebinLink = pastebin.paste();
            results.put(RESULTS_VALUE,
                        pastebinLink.getLink());
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
