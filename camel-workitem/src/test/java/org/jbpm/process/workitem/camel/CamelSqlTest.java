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
import java.util.Properties;
import javax.sql.DataSource;

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.RunScript;
import org.jbpm.persistence.util.PersistenceUtil;
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
import org.kie.test.util.db.PoolingDataSourceWrapper;

import static org.jbpm.persistence.util.PersistenceUtil.JBPM_PERSISTENCE_UNIT_NAME;
import static org.jbpm.persistence.util.PersistenceUtil.createEnvironment;
import static org.jbpm.persistence.util.PersistenceUtil.setupWithPoolingDataSource;
import static org.junit.Assert.*;

public class CamelSqlTest {

    private static final String PROCESS_DEFINITION = "BPMN2-CamelSqlProcess.bpmn2";

    private SQLCamelWorkitemHandler handler;
    private HashMap<String, Object> context;
    private PoolingDataSourceWrapper pds;

    @Before
    public void setup() throws Exception {
        DeleteDbFiles.execute("~",
                              "jbpm-db-test",
                              true);

        setupDb();

        DataSource ds = setupDataSource();

        SimpleRegistry simpleRegistry = new SimpleRegistry();
        simpleRegistry.put("jdbc/testDS1",
                           ds);

        handler = new SQLCamelWorkitemHandler("queryResult",
                                              new DefaultCamelContext(simpleRegistry));
        context = setupWithPoolingDataSource(JBPM_PERSISTENCE_UNIT_NAME);
    }

    @After
    public void cleanup() throws Exception {
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
        RunScript.execute("jdbc:h2:mem:jbpm-db;MVCC=true",
                          "sa",
                          "",
                          script.getAbsolutePath(),
                          StandardCharsets.UTF_8,
                          false);
    }

    public PoolingDataSourceWrapper setupDataSource() {
        pds = PersistenceUtil.setupPoolingDataSource(getDefaultDSProperties(),
                                                     "jdbc/jbpm-ds");
        return pds;
    }

    private static Properties getDefaultDSProperties() {
        Properties defaultProperties = new Properties();
        String[] keyArr = {
                "serverName", "portNumber", "databaseName",
                "url",
                "user", "password",
                "driverClassName",
                "className",
                "maxPoolSize",
                "allowLocalTransactions"};
        String[] defaultPropArr = {
                "", "", "",
                "jdbc:h2:mem:jbpm-db;MVCC=true",
                "sa", "",
                "org.h2.Driver",
                "org.h2.jdbcx.JdbcDataSource",
                "16",
                "true"};
        Assert.assertTrue("Unequal number of keys for default properties",
                          keyArr.length == defaultPropArr.length);
        for (int i = 0; i < keyArr.length; ++i) {
            defaultProperties.put(keyArr[i],
                                  defaultPropArr[i]);
        }
        return defaultProperties;
    }

    @Test
    public void testSelect() throws Exception {

        HashMap<String, Object> context = setupWithPoolingDataSource(JBPM_PERSISTENCE_UNIT_NAME);
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
