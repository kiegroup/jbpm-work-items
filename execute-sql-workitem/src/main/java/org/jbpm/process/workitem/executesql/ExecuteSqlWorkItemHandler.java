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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.runtime.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "ExecuteSQLDefinitions.wid", name = "ExecuteSQL",
        displayName = "ExecuteSQL",
        defaultHandler = "mvel: new org.jbpm.process.workitem.executesql.ExecuteSqlWorkItemHandler(\"dataSourceName\")",
        documentation = "${artifactId}/index.html",
        module = "${artifactId}", version = "${version}",
        parameters = {
                @WidParameter(name = "SQLStatement", required = true),
                @WidParameter(name = "MaxResults"),
                @WidParameter(name = "ColumnSeparator")
        },
        results = {
                @WidResult(name = "Result", runtimeType = "java.lang.Object")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "database,fetch,sql,execute",
                action = @WidAction(title = "Execute SQL statements")
        ))
public class ExecuteSqlWorkItemHandler extends AbstractLogOrThrowWorkItemHandler implements Cacheable {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteSqlWorkItemHandler.class);
    private static final String RESULT = "Result";
    private static final int DEFAULT_MAX_RESULTS = 10;
    private static final String DEFAULT_COLUMN_SEPARATOR = ",";
    private DataSource ds;
    private int maxResults;
    private String columnSeparator;

    public ExecuteSqlWorkItemHandler(String dataSourceName) {
        try {
            ds = InitialContext.doLookup(dataSourceName);
        } catch (NamingException e) {
            throw new RuntimeException("Unable to look up data source: " + dataSourceName + " - " + e.getMessage());
        }
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            Map<String, Object> results = new HashMap<>();
            String sqlStatement = (String) workItem.getParameter("SQLStatement");
            String maxResultsInput = (String) workItem.getParameter("MaxResults");
            String columnSeparatorInput = (String) workItem.getParameter("ColumnSeparator");

            maxResults = maxResultsInput != null && !maxResultsInput.trim().isEmpty() ? Integer.parseInt(maxResultsInput) : DEFAULT_MAX_RESULTS;
            columnSeparator = columnSeparatorInput != null && !columnSeparatorInput.isEmpty() ? columnSeparatorInput : DEFAULT_COLUMN_SEPARATOR;

            List<String> lines = new ArrayList<>();
            try {
                connection = ds.getConnection();
                statement = connection.prepareStatement(sqlStatement);
                statement.setMaxRows(maxResults);
                resultSet = statement.executeQuery();

                results.put(RESULT,
                            processResults(resultSet));
                workItemManager.completeWorkItem(workItem.getId(),
                                                 results);
            } finally {
                try {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            handleException(e);
        }
    }

    // overwrite to implement custom resultset processing
    protected Object processResults(ResultSet resultSet) throws Exception {
        List<String> lines = new ArrayList<>();

        while (resultSet.next()) {
            int columnCount = resultSet.getMetaData().getColumnCount();
            List<String> values = new ArrayList<>();
            for (int i = 0; i < columnCount; i++) {
                values.add(resultSet.getString(i + 1));
            }
            lines.add(values.stream().collect(Collectors.joining(columnSeparator)));
        }

        return lines;
    }

    public void abortWorkItem(WorkItem workItem,
                              WorkItemManager manager) {
    }

    public void close() {
    }
}
