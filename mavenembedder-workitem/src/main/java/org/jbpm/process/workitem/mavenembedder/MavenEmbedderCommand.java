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

import org.apache.maven.cli.MavenCli;
import org.kie.api.executor.Command;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenEmbedderCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(MavenEmbedderCommand.class);
    private static final String RESULTS_VALUES = "MavenResults";

    @Override
    public ExecutionResults execute(CommandContext ctx) throws Exception {
        try {
            String goals = (String) ctx.getData("Goals");
            String commandLineOptions = (String) ctx.getData("CLOptions");
            String workDir = (String) ctx.getData("WorkDirectory");
            String projectRoot = (String) ctx.getData("ProjectRoot");

            if (goals == null || workDir == null || projectRoot == null) {
                throw new IllegalArgumentException("Invalid command inputs.");
            }

            ExecutionResults results = new ExecutionResults();

            results.setData(MavenEmbedderUtils.executeMavenGoals(new MavenCli(),
                                                                 RESULTS_VALUES,
                                                                 projectRoot,
                                                                 commandLineOptions,
                                                                 goals,
                                                                 workDir));
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
