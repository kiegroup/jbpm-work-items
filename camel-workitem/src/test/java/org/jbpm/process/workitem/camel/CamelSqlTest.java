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
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.RunScript;
import org.jbpm.test.persistence.util.PersistenceUtil;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.persistence.jpa.JPAKnowledgeService;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import static org.jbpm.test.persistence.util.PersistenceUtil.DATASOURCE;
import static org.jbpm.test.persistence.util.PersistenceUtil.createEnvironment;
import static org.jbpm.test.persistence.util.PersistenceUtil.setupWithPoolingDataSource;
import static org.junit.Assert.*;

public class CamelSqlTest {

    private static final String PROCESS_DEFINITION = "BPMN2-CamelSqlProcess.bpmn2";

    private SQLCamelWorkitemHandler handler;
    private HashMap<String, Object> context;

    @Before
    public void setup() throws Exception {
        DeleteDbFiles.execute("~",
                              "jbpm-db-test",
                              true);

        setupDb();

        context = setupWithPoolingDataSource("org.jbpm.contrib.camel-workitem");

        SimpleRegistry simpleRegistry = new SimpleRegistry();
        simpleRegistry.put("jdbc/testDS1",
                           context.get(DATASOURCE));

        handler = new SQLCamelWorkitemHandler("queryResult",
                                              new DefaultCamelContext(simpleRegistry));
    }

    @After
    public void cleanup() {
        PersistenceUtil.cleanUp(context);
        DeleteDbFiles.execute("~",
                              "jbpm-db-test",
                              true);
    }

    /**
     * Prepares test table containing a single data row.
     */
    private static void setupDb() throws SQLException, URISyntaxException {
        File script = new File(CamelSqlTest.class.getResource("/init-db.sql").toURI());
        RunScript.execute("jdbc:h2:mem:jbpm-db;MODE=LEGACY;NON_KEYWORDS=VALUE",
                          "sa",
                          "",
                          script.getAbsolutePath(),
                          StandardCharsets.UTF_8,
                          false);
    }

    @Test
    public void testSelect() {
        Environment env = createEnvironment(context);

        KieBase kbase = createBase();
        StatefulKnowledgeSession kieSession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase,
                                                                                              null,
                                                                                              env);

        kieSession.getWorkItemManager().registerWorkItemHandler("CamelSql",
                                                                handler);

        String sqlQuery = "select NAME from TEST";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Query",
                   sqlQuery);
        params.put("DataSource",
                   "jdbc/testDS1");

        WorkflowProcessInstance wpi = (WorkflowProcessInstance) kieSession.startProcess("camelSqlProcess",
                                                                                        params);

        Assert.assertEquals(2,
                            ((RuleFlowProcessInstance) wpi).getVariables().size());

        kieSession.dispose();
    }

    private KieBase createBase() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource(PROCESS_DEFINITION),
                     ResourceType.BPMN2);
        assertFalse(kbuilder.getErrors().toString(),
                    kbuilder.hasErrors());

        return kbuilder.newKieBase();
    }
}
