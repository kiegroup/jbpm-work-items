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
package org.jbpm.process.workitem.pdf;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.document.Document;
import org.jbpm.document.service.impl.DocumentImpl;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openhtmltopdf.bidi.support.ICUBidiReorderer;
import com.openhtmltopdf.bidi.support.ICUBidiSplitter;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;



@Wid(widfile = "GeneratePDFDefinitions.wid", name = "GeneratePDF",
        displayName = "GeneratePDF",
        defaultHandler = "mvel: new org.jbpm.process.workitem.pdf.GeneratePDFWorkitemHandler()",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "GeneratePDF.png",
        parameters = {
                @WidParameter(name = "TemplateXHTML", required = true),
                @WidParameter(name = "PDFName")
        },
        results = {
                @WidResult(name = "PDFDocument", runtimeType = "org.jbpm.document.Document")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "pdf,generate,template,document,freemarker,xhtml",
                action = @WidAction(title = "Generate PDF document"),
                authinfo = @WidAuth
        ))
public class GeneratePDFWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(GeneratePDFWorkitemHandler.class);
    private static final String RESULTS_VALUE = "PDFDocument";
    private String resultXHTML;

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {


        try {        	
            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Map<String, Object> results = new HashMap<>();
            String templateXHTML = (String) workItem.getParameter("TemplateXHTML");
            String pdfName = (String) workItem.getParameter("PDFName");

            if (pdfName == null || pdfName.isEmpty()) {
                pdfName = "generatedpdf";
            }

            logger.info(templateXHTML);

            StringWriter stringWriter = new StringWriter();
            resultXHTML = stringWriter.toString();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(templateXHTML, null);
            builder.toStream(baos);

            // Enable RTL support
            builder.useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory());
            builder.useUnicodeBidiReorderer(new ICUBidiReorderer());
            builder.defaultTextDirection(PdfRendererBuilder.TextDirection.RTL);

            builder.run();

            Document document = new DocumentImpl();
            document.setName(pdfName + ".pdf");
            document.setLastModified(new Date());
            document.setContent(baos.toByteArray());

            results.put(RESULTS_VALUE, document);

            workItemManager.completeWorkItem(workItem.getId(), results);
        } catch (Exception e) {
            logger.error(e.getMessage());
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem workItem,
                              WorkItemManager manager) {
    }
  
}
