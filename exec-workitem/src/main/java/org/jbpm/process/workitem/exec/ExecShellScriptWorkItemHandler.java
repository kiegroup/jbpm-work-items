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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

/**
 * WorkItemHandler that is capable of executing shell script file
 * <ul>
 * <li>ShellScriptLocation - Absolute or relative path of shell script file - mandatory</li>
 * <li>TimeoutInMillis - Timeout in Milliseconds for execution. Default value is 1000 MilliSeconds</li>
 * <li>AddEnvironmentVariable - A Map of environment variable to be added</li>
 * <li>RemoveEnvironmentVariable -A List of environment variable to be removed</li>
 * </ul>
 */


@Wid(widfile = "ExecShellScriptDefinitions.wid", name = "ExecShellScript",
        displayName = "ExecShellScript",
        defaultHandler = "mvel: new org.jbpm.process.workitem.exec.ExecShellScriptWorkItemHandler()",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "ExecShellScript.png",
        parameters = {
                @WidParameter(name = "ShellScriptLocation", required = true),
                @WidParameter(name = "TimeoutInMillis"),
                @WidParameter(name = "AddEnvironmentVariable", runtimeType = "java.util.Map"),
                @WidParameter(name = "RemoveEnvironmentVariable", runtimeType = "java.util.List")
        },
        results = {
                @WidResult(name = "Output")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "execute,shell script",
                action = @WidAction(title = "Execute a shell script"),
                authinfo = @WidAuth
        ))
public class ExecShellScriptWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    public static final String RESULT = "Output";
    private static final Logger logger = LoggerFactory.getLogger(ExecShellScriptWorkItemHandler.class);

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager manager) {

    	try {

    		RequiredParameterValidator.validate(this.getClass(),
    				workItem);
    		String shellScriptLocation = (String) workItem.getParameter("ShellScriptLocation");
    		String tout = (String) workItem.getParameter("TimeoutInMillis");
    		Long timeout=null;
    		if(tout!=null){
    			timeout = Long.parseLong(tout);
    		}else {
    			timeout=1000l; //Default timeout in Milliseconds if timeout is not passed as a parameter
    		}
    		Map<String, String> addEnvironmentVariables = (Map<String, String>) workItem.getParameter("AddEnvironmentVariable");
    		List<String> removeEnvironmentVariables = (List<String>) workItem.getParameter("RemoveEnvironmentVariable");
    		logger.debug("ShellScriptLocation " + shellScriptLocation + " Timeout " + timeout );
    		List<String> output = new ArrayList<>();

    		Map<String, Object> results = new HashMap<>();

    		Process process;
    		try {

    			List<String> commandList = new ArrayList<String>();
    			// adding command and script location to the list
    			commandList.add("sh");
    			commandList.add(shellScriptLocation);
    			ProcessBuilder processBuilder = new ProcessBuilder(commandList);

    			Map<String, String> envVariables= processBuilder.environment();

    			if(null!= addEnvironmentVariables && !addEnvironmentVariables.isEmpty()) {
    				logger.debug("addEnvironmentVariables " + addEnvironmentVariables);
    				envVariables.putAll(addEnvironmentVariables);
    			}

    			if(null!= removeEnvironmentVariables && !removeEnvironmentVariables.isEmpty()) {
    				logger.debug("removeEnvironmentVariables " + removeEnvironmentVariables);
    				removeEnvironmentVariables.stream().forEach(variable -> envVariables.remove(variable));
    			}

    			process = processBuilder.start();

    			process.waitFor(timeout,TimeUnit.MILLISECONDS); 
    			BufferedReader reader=new BufferedReader(new InputStreamReader(
    					process.getInputStream())); 
    			String line; 
    			while((line = reader.readLine()) != null) { 
    				output.add(line);
    				logger.debug("Output line " + line);
    			} 
    		} catch (IOException e) {
    			logger.error("Error executing the work item IO Exception: " + e.getMessage());
    			handleException(e);
    		} catch (InterruptedException e) {
    			logger.error("Error executing the work item Interrupted Exception: " + e.getMessage());
    			handleException(e);
    		} catch (Exception e) {
    			logger.error("Error executing the work item with Exception: " + e.getMessage());
    			handleException(e);
    		}


    		results.put(RESULT,
    				output);


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

}

