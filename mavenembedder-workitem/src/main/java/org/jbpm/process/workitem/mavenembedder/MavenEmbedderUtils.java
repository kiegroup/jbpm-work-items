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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.cli.MavenCli;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenEmbedderUtils {

    private static final Logger logger = LoggerFactory.getLogger(MavenEmbedderUtils.class);

    public static enum MavenEmbedderMode {
        SYNC,
        ASYNC;
    }

    public static Map<String, Object> executeMavenGoals(MavenCli cli,
                                                        String resultsKey,
                                                        String projectRoot,
                                                        String commandLineOptions,
                                                        String goals,
                                                        String workDir
    ) throws Exception {
        Map<String, Object> results = new HashMap<>();

        ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
        ByteArrayOutputStream baosErr = new ByteArrayOutputStream();

        PrintStream cliOut = new PrintStream(baosOut,
                                             true);
        PrintStream cliErr = new PrintStream(baosErr,
                                             true);

        String[] allCommandLineOptions = new String[0];
        if (commandLineOptions != null && commandLineOptions.length() > 0) {
            allCommandLineOptions = commandLineOptions.split("\\s+");
        }

        String[] allGoals = goals.split("\\s+");

        String[] allOptions = ArrayUtils.addAll(allGoals,
                                                allCommandLineOptions);
        int result = cli.doMain(allOptions,
                                workDir,
                                cliOut,
                                cliErr);

        String stdout = baosOut.toString("UTF-8");
        String stderr = baosErr.toString("UTF-8");

        if (result != 0) {
            logger.error("Maven build finished with unexpected result = {}", result);
            if (StringUtils.isNotEmpty(stdout)) {
                logger.error("Standard output of the Maven command was :\n{}", stdout);
            }
            if (StringUtils.isNotEmpty(stderr)) {
                logger.error("Standard error output of the Maven command was:\n{}", stderr);
            }

            throw new RuntimeException(String.format("Maven build finished with unexpected result = %s. See the error log for more information", result));
        }

        Map<String, String> mavenResults = new HashMap<>();
        mavenResults.put("stdout",
                         stdout);
        mavenResults.put("stderr",
                         stderr);

        results.put(resultsKey,
                    mavenResults);

        return results;
    }
}
