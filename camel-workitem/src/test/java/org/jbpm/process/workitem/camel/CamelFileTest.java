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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.impl.DefaultWorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.test.AbstractBaseTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.persistence.jpa.JPAKnowledgeService;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import static org.jbpm.test.persistence.util.PersistenceUtil.createEnvironment;
import static org.jbpm.test.persistence.util.PersistenceUtil.setupWithPoolingDataSource;

public class CamelFileTest extends AbstractBaseTest {

    private static final String PROCESS_DEFINITION = "BPMN2-CamelFileProcess.bpmn2";

    private static File tempDir;
    private static File testDir;
    private static File testFile;

    @BeforeClass
    public static void initialize() {
        tempDir = new File(System.getProperty("java.io.tmpdir"));
        testDir = new File(tempDir,
                           "test_dir");
        String fileName = "test_file_" + CamelFileTest.class.getName() + "_" + UUID.randomUUID().toString();
        testFile = new File(tempDir,
                            fileName);
    }

    @AfterClass
    public static void clean() throws IOException {
        FileUtils.deleteDirectory(testDir);
    }

    @Test
    public void testSingleFileProcess() throws IOException {
        final String testData = "test-data";

        HashMap<String, Object> context = setupWithPoolingDataSource("org.jbpm.contrib.camel-workitem");
        Environment env = createEnvironment(context);

        KieBase kbase = createBase();
        StatefulKnowledgeSession kieSession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase,
                                                                                              null,
                                                                                              env);

        kieSession.getWorkItemManager().registerWorkItemHandler("CamelFile",
                                                                new FileCamelWorkitemHandler());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("payloadVar",
                   testData);
        params.put("pathVar",
                   tempDir.getAbsolutePath());
        params.put("fileNameVar",
                   testFile.getName());

        ProcessInstance pi = kieSession.startProcess("camelFileProcess",
                                                     params);

        ProcessInstance result = kieSession.getProcessInstance(pi.getId());
        Assert.assertNull(result);

        Assert.assertTrue("Expected file does not exist.",
                          testFile.exists());

        String resultText = FileUtils.readFileToString(testFile, StandardCharsets.UTF_8);
        Assert.assertEquals(resultText,
                            testData);
    }

    @Test
    public void testSingleFileWithHeaders() throws IOException {
        Set<String> headers = new HashSet<String>();
        headers.add("CamelFileName");
        FileCamelWorkitemHandler handler = new FileCamelWorkitemHandler(headers);
        handler.setLogThrownException(true);

        final String testData = "test-data";
        final WorkItem workItem = new WorkItemImpl();
        workItem.setParameter("path",
                              tempDir.getAbsolutePath());
        workItem.setParameter("payload",
                              testData);
        workItem.setParameter("CamelFileName",
                              testFile.getName());

        WorkItemManager manager = new DefaultWorkItemManager(null);
        handler.executeWorkItem(workItem,
                                manager);

        Assert.assertTrue("Expected file does not exist.",
                          testFile.exists());

        String resultText = FileUtils.readFileToString(testFile, StandardCharsets.UTF_8);
        Assert.assertEquals(resultText,
                            testData);
    }

    private KieBase createBase() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource(PROCESS_DEFINITION),
                     ResourceType.BPMN2);
        Assert.assertFalse(kbuilder.getErrors().toString(),
                           kbuilder.hasErrors());

        return kbuilder.newKieBase();
    }
}

