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

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.document.Document;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.junit.Test;

import static org.junit.Assert.*;

public class GeneratePDFWorkitemHandlerTest {

    @Test
    public void testGeneratePDF() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();

        String testTemplate = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n" +
                "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<body>\n" +
                "<p>Hello ${testData.firstName} ${testData.lastName}</p>\n" +
                "</body>\n" +
                "</html>";

        TestTemplateData testTemplateData = new TestTemplateData();
        testTemplateData.setFirstName("testFirstName");
        testTemplateData.setLastName("testLastName");

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("TemplateXHTML",
                              testTemplate);
        workItem.setParameter("testData",
                              testTemplateData);

        GeneratePDFWorkitemHandler handler = new GeneratePDFWorkitemHandler();
        handler.setLogThrownException(true);
        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("PDFDocument") instanceof Document);

        Document generatedPDFDoc = (Document) manager.getResults().get(workItem.getId()).get("PDFDocument");
        assertNotNull(generatedPDFDoc);
        assertEquals(generatedPDFDoc.getName(),
                     "generatedpdf.pdf");
        assertNotNull(generatedPDFDoc.getContent());

        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n" +
                             "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                             "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                             "<body>\n" +
                             "<p>Hello testFirstName testLastName</p>\n" +
                             "</body>\n" +
                             "</html>",
                     handler.getResultXHTML());
    }

    public class TestTemplateData {

        public String firstName;
        public String lastName;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
}
