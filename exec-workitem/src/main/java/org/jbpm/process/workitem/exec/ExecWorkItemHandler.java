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
package org.jbpm.process.workitem.exec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
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

@Wid(widfile = "ExecDefinitions.wid", name = "Exec",
        displayName = "Exec",
        defaultHandler = "mvel: new org.jbpm.process.workitem.exec.ExecWorkItemHandler()",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "Exec.png",
        parameters = {
                @WidParameter(name = "Command", required = true),
                @WidParameter(name = "Arguments", runtimeType = "java.util.List"),
                @WidParameter(name = "TimeoutInMillis", runtimeType = "java.lang.String")
        },
        results = {
                @WidResult(name = "Output")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "execute,comand",
                action = @WidAction(title = "Execute a command"),
                authinfo = @WidAuth))
public class ExecWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExecWorkItemHandler.class);
    public static final String RESULT = "Output";
    private String parsedCommandStr = "";
    private long defaultTimeout = 4000L;

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager manager) {

        try {

            RequiredParameterValidator.validate(this.getClass(),
                    workItem);

            String command = (String) workItem.getParameter("Command");
            List<String> arguments = (List<String>) workItem.getParameter("Arguments");
            String commandExecutionTimeout = (String) workItem.getParameter("TimeoutInMillis");

            if (commandExecutionTimeout != null) {
                this.setDefaultTimeout(parsetimeout(commandExecutionTimeout));
            }

            String executionResult = executecommand(command, arguments, defaultTimeout);

            Map<String, Object> results = new HashMap<>();
            results.put(RESULT,
                    executionResult);

            manager.completeWorkItem(workItem.getId(),
                    results);
        } catch (Throwable t) {
            handleException(t);
        }
    }

    protected long parsetimeout(String durationStr) {
        try {
            if (durationStr.startsWith("PT")) { // ISO-8601 PTnHnMn.nS
                return Duration.parse(durationStr).toMillis();
            } else if (!durationStr.contains("T")) { // ISO-8601 PnYnMnWnD
                Period period = Period.parse(durationStr);
                OffsetDateTime now = OffsetDateTime.now();
                return Duration.between(now, now.plus(period)).toMillis();
            } else { // ISO-8601 PnYnMnWnDTnHnMn.nS
                String[] elements = durationStr.split("T");
                Period period = Period.parse(elements[0]);
                Duration duration = Duration.parse("PT" + elements[1]);
                OffsetDateTime now = OffsetDateTime.now();

                return Duration.between(now, now.plus(period).plus(duration)).toMillis();
            }
        } catch (Exception e) {
            logger.error("Exception occured while parsing provided timeout" + durationStr
                    + ".Default timeout of" + defaultTimeout
                    + "ms will be used for command execution");
            return defaultTimeout;
        }
    }

    protected String executecommand(String command, List<String> arguments, long timeout) throws IOException {

        String result = null;
        CommandLine commandLine = CommandLine.parse(command);
        if (arguments != null && arguments.size() > 0) {
            commandLine.addArguments(arguments.toArray(new String[0]), true);
        }

        parsedCommandStr = commandLine.toString();

        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);
        executor.setWatchdog(watchdog);
        try {
            executor.execute(commandLine);
        } catch (IOException e) {
            if (watchdog.killedProcess()) {
                logger.error("A timeout occured after " + timeout
                        + "ms while executing a command " + parsedCommandStr.replace(",", ""));
                outputStream.reset();
                outputStream.close();
                return result = "A timeout occured after " + timeout
                        + "ms while executing a command " + parsedCommandStr.replace(",", "");
            }
        }

        return outputStream.toString();

    }

    public void abortWorkItem(WorkItem workItem,
                              WorkItemManager manager) {
        // Do nothing, this work item cannot be aborted
    }

    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public String getParsedCommandStr() {
        return parsedCommandStr;
    }
}
