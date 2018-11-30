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
package org.jbpm.process.workitem.executesql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.test.util.db.DataSourceFactory;
import org.kie.test.util.db.PoolingDataSourceWrapper;

import static org.junit.Assert.*;

public class ExecuteSqlWorkItemHandlerTest {

    private static final String DS_NAME = "executeSqlData";
    private static TestH2Server h2Server;

    @BeforeClass
    public static void configure() {
        try {
            setupPoolingDataSource();
            insertData();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() {
        try {
            if (h2Server != null) {
                h2Server.finalize();
            }
        } catch (Throwable e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testFetchAllRows() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("SQLStatement",
                              "select * from Person");

        ExecuteSqlWorkItemHandler handler = new ExecuteSqlWorkItemHandler(DS_NAME);
        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("Result") instanceof List);
        List<String> resultLines = (List<String>) manager.getResults().get(workItem.getId()).get("Result");
        assertNotNull(resultLines);
        assertEquals(3,
                     resultLines.size());
    }

    @Test
    public void testFetchSpecificRow() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("SQLStatement",
                              "select * from Person where id = 1");
        ExecuteSqlWorkItemHandler handler = new ExecuteSqlWorkItemHandler(DS_NAME);
        handler.executeWorkItem(workItem,
                                manager);

        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        List<String> resultLines = (List<String>) manager.getResults().get(workItem.getId()).get("Result");
        assertNotNull(resultLines);
        assertEquals(1,
                     resultLines.size());
        assertEquals("1,Anthony,3",
                     resultLines.get(0));
    }

    @Test
    public void testNoResults() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("SQLStatement",
                              "select * from Person where id = 100");
        ExecuteSqlWorkItemHandler handler = new ExecuteSqlWorkItemHandler(DS_NAME);
        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        List<String> resultLines = (List<String>) manager.getResults().get(workItem.getId()).get("Result");
        assertNotNull(resultLines);
        assertEquals(0,
                     resultLines.size());
    }

    private static void insertData() throws Exception {
        DataSource ds = InitialContext.doLookup(DS_NAME);

        Connection connection = ds.getConnection();
        PreparedStatement createTableStatement = connection
                .prepareStatement("" + "create table Person(" + "id int, " + "name varchar2(255), " + "age int);");
        createTableStatement.executeUpdate();
        PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO Person ( id, name, age) "
                                                                                + "VALUES (1, 'Anthony', 3), " + "(2, 'Will', 30), " + "(3, 'Moon', 28);");
        insertStatement.executeUpdate();
    }

    private static PoolingDataSourceWrapper setupPoolingDataSource() throws Exception {
        h2Server = new TestH2Server();
        h2Server.start();

        Properties driverProperties = new Properties();
        driverProperties.setProperty("className",
                                     "org.h2.jdbcx.JdbcDataSource");
        driverProperties.setProperty("user",
                                     "sa");
        driverProperties.setProperty("password",
                                     "sa");
        driverProperties.setProperty("url",
                                     "jdbc:h2:mem:jpa-wih;MVCC=true");
        driverProperties.setProperty("driverClassName",
                                     "org.h2.Driver");

        PoolingDataSourceWrapper pds = DataSourceFactory.setupPoolingDataSource(DS_NAME,
                                                                                driverProperties);
        return pds;
    }

    private static class TestH2Server {

        private Server realH2Server;

        public void start() {
            if (realH2Server == null || !realH2Server.isRunning(false)) {
                try {
                    realH2Server = Server.createTcpServer(new String[0]);
                    realH2Server.start();
                    System.out.println("Started H2 Server...");
                } catch (SQLException e) {
                    throw new RuntimeException("can't start h2 server db",
                                               e);
                }
            }
        }

        @Override
        protected void finalize() throws Throwable {
            if (realH2Server != null) {
                System.out.println("Stopping H2 Server...");
                realH2Server.stop();
            }
            DeleteDbFiles.execute("",
                                  "target/executeSql-data",
                                  true);
            super.finalize();
        }
    }
}
