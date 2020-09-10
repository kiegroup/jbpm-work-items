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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        		@WidParameter(name = "CommandExecutionTimeout", runtimeType = "java.lang.String")
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
                authinfo = @WidAuth
        ))
public class ExecWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExecWorkItemHandler.class);
    public static final String RESULT = "Output";
    private String parsedCommandStr = "";
	private long defaultTimeoutInSeconds=0;

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager manager) {

        try {

            RequiredParameterValidator.validate(this.getClass(),workItem);

            String command = (String) workItem.getParameter("Command");
            List<String> arguments = (List<String>) workItem.getParameter("Arguments");
			String commandExecutionTimeout = (String) workItem.getParameter("CommandExecutionTimeout");
            
        	if (commandExecutionTimeout ==null) {
				this.SetDefaultTimeoutInSeconds(4L);
			} else {
				this.SetDefaultTimeoutInSeconds(Long.parseLong(commandExecutionTimeout));
			}

            Map<String, Object> results = new HashMap<>();

            CommandLine commandLine = CommandLine.parse(command);
            if (arguments != null && arguments.size() > 0) {
                commandLine.addArguments(arguments.toArray(new String[0]),
                                         true);
            }

            parsedCommandStr = commandLine.toString();
            
            ExecuteWatchdog watchdog = new ExecuteWatchdog(java.time.Duration.ofSeconds(defaultTimeoutInSeconds).toMillis());
            DefaultExecutor executor = new DefaultExecutor();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            executor.setStreamHandler(streamHandler);
		    executor.setWatchdog(watchdog);
            try {
				  executor.execute(commandLine);
			    } catch (IOException e) {
			        if (watchdog.killedProcess()) {
			        	logger.debug("A timeout occured after "+java.time.Duration.ofSeconds(defaultTimeoutInSeconds).toMillis()+"ms while executing a command "+parsedCommandStr.replace(",", ""));
						results.put(RESULT, outputStream.toString());
					    outputStream.close();
						manager.completeWorkItem(workItem.getId(), results);
			        } else {
			            throw e;
			        }
			    }
            results.put(RESULT,
                        outputStream.toString());

            outputStream.close();

            manager.completeWorkItem(workItem.getId(),
                                     results);
        } catch (Throwable t) {
            handleException(t);
        }
    }

    public void abortWorkItem(WorkItem workItem,
                              WorkItemManager manager) {
        // Do nothing, this work item cannot be aborted
    }

    public void SetDefaultTimeoutInSeconds(long defaultTimeoutInSeconds) {
		this.defaultTimeoutInSeconds = defaultTimeoutInSeconds;
	}
    
    public String getParsedCommandStr() {
        return parsedCommandStr;
    }
}


