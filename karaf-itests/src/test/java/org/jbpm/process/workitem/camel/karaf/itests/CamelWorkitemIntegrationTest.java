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

package org.jbpm.process.workitem.camel.karaf.itests;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.impl.DefaultWorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.camel.FileCamelWorkitemHandler;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemManager;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Constants;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.streamBundle;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

/**
 * Uploading a file via File endpoint using Camel in Fuse.
 */
@Ignore
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class CamelWorkitemIntegrationTest extends AbstractKarafIntegrationTest {

    private static File tempDir;
    private static File testDir;
    private static File testFile;

    private static final String SPRING_XML_LOCATION = "/org/jbpm/process/workitem/camel/karaf/itests/workitem-camel-service.xml";
    private static final String PROCESS_LOCATION = "/org/jbpm/process/workitem/camel/karaf/itests/workitem/BPMN2-CamelFileProcess.bpmn2";
    private static final String CWD_LOCATION = "/org/jbpm/process/workitem/camel/karaf/itests/workitem/CamelWorkDefinitions.wid";

    @Inject
    private KieSession kieSession;

    @Configuration
    public static Option[] configure() {
        return new Option[]{
                // Install Karaf Container
                getKarafDistributionOption(),

                // Don't bother with local console output as it just ends up cluttering the logs
                configureConsole().ignoreLocalConsole(),
                // Force the log level to INFO so we have more details during the test.  It defaults to WARN.
                logLevel(LogLevelOption.LogLevel.DEBUG),

                // Option to be used to do remote debugging
                // debugConfiguration("5005", true),

                loadKieFeatures("jbpm",
                                "drools-module",
                                "jbpm-workitems-camel",
                                "kie-spring"),
                features(getFeaturesUrl("org.apache.karaf.features",
                                        "spring-legacy",
                                        getKarafVersion()),
                         "aries-blueprint-spring"),
                wrappedBundle(mavenBundle().groupId("commons-io").artifactId("commons-io").versionAsInProject()),
                wrappedBundle(mavenBundle().groupId("junit").artifactId("junit").versionAsInProject()),

                // wrap and install junit bundle - the DRL imports a class from it
                // (simulates for instance a bundle with domain classes used in rules)
                wrappedBundle(mavenBundle().groupId("junit").artifactId("junit").versionAsInProject()),

                // Create a bundle with META-INF/spring/kie-beans.xml - this should be processed automatically by Spring
                streamBundle(bundle()
                                     .set(Constants.BUNDLE_MANIFESTVERSION,
                                          "2")
                                     .add("META-INF/spring/workitem-camel-service.xml",
                                          CamelWorkitemIntegrationTest.class.getResource(SPRING_XML_LOCATION))
                                     .add(PROCESS_LOCATION.substring(1),
                                          CamelWorkitemIntegrationTest.class.getResource(PROCESS_LOCATION))
                                     .add(CWD_LOCATION.substring(1),
                                          CamelWorkitemIntegrationTest.class.getResource(CWD_LOCATION))
                                     .set(Constants.IMPORT_PACKAGE,
                                          "org.kie.osgi.spring," +
                                                  "org.kie.api," +
                                                  "org.kie.api.runtime," +
                                                  "org.kie.api.runtime.manager," +
                                                  "org.kie.api.runtime.process," +
                                                  "org.kie.api.task," +
                                                  "org.jbpm.persistence.processinstance," +
                                                  "org.jbpm.runtime.manager.impl," +
                                                  "org.jbpm.process.instance.impl," +
                                                  "org.jbpm.services.task.identity," +
                                                  "org.jbpm.services.task.impl.model," +
                                                  "org.kie.internal.runtime.manager.context," +
                                                  "javax.transaction," +
                                                  "javax.persistence," +
                                                  "*")
                                     .set(Constants.DYNAMICIMPORT_PACKAGE,
                                          "*")
                                     .set(Constants.BUNDLE_SYMBOLICNAME,
                                          "Test-Kie-Spring-Bundle")
                                     // alternative for enumerating org.kie.aries.blueprint packages in Import-Package:
                                     //.set(Constants.DYNAMICIMPORT_PACKAGE, "*")
                                     .build()).start()
        };
    }

    @BeforeClass
    public static void initialize() {
        tempDir = new File(System.getProperty("java.io.tmpdir"));
        testDir = new File(tempDir,
                           "test_dir");
        String fileName = "test_file_" + CamelWorkitemIntegrationTest.class.getName() + "_" + UUID.randomUUID().toString();
        testFile = new File(tempDir,
                            fileName);
    }

    @AfterClass
    public static void clean() throws IOException {
        FileUtils.deleteDirectory(testDir);
    }

    @Before
    public void prepare() {
        Assert.assertNotNull(this.kieSession);
    }

    @Test
    public void testSingleFileProcess() throws IOException {
        final String testData = "test-data";

        FileCamelWorkitemHandler handler = new FileCamelWorkitemHandler();
        kieSession.getWorkItemManager().registerWorkItemHandler("CamelFile",
                                                                handler);

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

        Assert.assertTrue(testFile.exists());

        String resultText = FileUtils.readFileToString(testFile);
        Assert.assertEquals(testData,
                            resultText);
    }

    @Test
    public void testSingleFileWithHeaders() throws IOException {
        Set<String> headers = new HashSet<String>();
        headers.add("CamelFileName");
        FileCamelWorkitemHandler handler = new FileCamelWorkitemHandler(headers);

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

        Assert.assertTrue(testFile.exists());

        String resultText = FileUtils.readFileToString(testFile);
        Assert.assertEquals(testData,
                            resultText);
    }
}

