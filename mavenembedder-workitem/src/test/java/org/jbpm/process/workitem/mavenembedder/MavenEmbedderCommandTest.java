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
package org.jbpm.process.workitem.mavenembedder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.junit.Test;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;

import static org.junit.Assert.*;

public class MavenEmbedderCommandTest {

    @Test
    public void testCleanInstallSimpleProjectCommand() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        File simpleTestProjectDir = new File("src/test/resources/simple");

        Map<String, Object> commandContextData = new HashMap<>();
        commandContextData.put("Goals",
                               "clean install");
        commandContextData.put("WorkDirectory",
                               simpleTestProjectDir.getAbsolutePath());
        commandContextData.put("ProjectRoot",
                               simpleTestProjectDir.getAbsolutePath());

        CommandContext commandContext = new CommandContext(commandContextData);

        MavenEmbedderCommand command = new MavenEmbedderCommand();

        ExecutionResults commandResults = command.execute(commandContext);

        assertNotNull(commandResults);
        assertTrue(commandResults.getData("MavenResults") instanceof Map);

        Map<String, String> mavenResults = (Map<String, String>) commandResults.getData("MavenResults");
        assertNotNull(mavenResults);
        assertEquals("",
                     mavenResults.get("stderr"));

        // make sure the build ran - check the build dirs
        assertTrue(new File("src/test/resources/simple/target").exists());
        assertTrue(new File("src/test/resources/simple/target/classes").exists());
        assertTrue(new File("src/test/resources/simple/target/test-classes").exists());

        // execute command with "clean" goals to clean the test project and check
        commandContextData.put("Goals",
                               "clean");
        commandContext = new CommandContext(commandContextData);
        commandResults = command.execute(commandContext);

        assertNotNull(commandResults);
        assertTrue(commandResults.getData("MavenResults") instanceof Map);

        // make sure the sample maven project was cleaned
        assertFalse(new File("src/test/resources/simple/target").exists());
        assertFalse(new File("src/test/resources/simple/target/classes").exists());
        assertFalse(new File("src/test/resources/simple/target/test-classes").exists());
    }

    @Test
    public void testCleanInstallSimpleProjectWithCLOptionsCommand() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        File simpleTestProjectDir = new File("src/test/resources/simple");

        Map<String, Object> commandContextData = new HashMap<>();
        commandContextData.put("Goals",
                               "clean install");
        commandContextData.put("CLOptions",
                               "-DskipTests -X");
        commandContextData.put("WorkDirectory",
                               simpleTestProjectDir.getAbsolutePath());
        commandContextData.put("ProjectRoot",
                               simpleTestProjectDir.getAbsolutePath());

        CommandContext commandContext = new CommandContext(commandContextData);

        MavenEmbedderCommand command = new MavenEmbedderCommand();

        ExecutionResults commandResults = command.execute(commandContext);

        assertNotNull(commandResults);
        assertTrue(commandResults.getData("MavenResults") instanceof Map);

        Map<String, String> mavenResults = (Map<String, String>) commandResults.getData("MavenResults");
        assertNotNull(mavenResults);
        assertEquals("",
                     mavenResults.get("stderr"));
        // with -X debug option we should get a stdout message
        assertTrue(mavenResults.get("stdout").startsWith("Apache Maven"));

        // make sure the build ran - check the build dirs
        assertTrue(new File("src/test/resources/simple/target").exists());
        assertTrue(new File("src/test/resources/simple/target/classes").exists());
        assertTrue(new File("src/test/resources/simple/target/test-classes").exists());

        // execute command with "clean" goals to clean the test project and check
        commandContextData.put("Goals",
                               "clean");
        commandContext = new CommandContext(commandContextData);
        commandResults = command.execute(commandContext);

        assertNotNull(commandResults);
        assertTrue(commandResults.getData("MavenResults") instanceof Map);

        // make sure the sample maven project was cleaned
        assertFalse(new File("src/test/resources/simple/target").exists());
        assertFalse(new File("src/test/resources/simple/target/classes").exists());
        assertFalse(new File("src/test/resources/simple/target/test-classes").exists());
    }
}
