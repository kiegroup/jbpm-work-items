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

package org.apache.maven.cli;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.cli.Option;

/**
 * This is an extension of the default MavenCli implementation to allow maven invocation concurrently.
 * The main issues found are:
 * - OptionsBuilder class is not thread safe and thus multiple threads doing doMain will run into race conditions modifying different entries of the options
 * - projectRoot by default is allowed to be set via system properties which does not make sense in concurrent builds env
 * - -D options from arguments of doMain should not be set as system properties as that will corrupt different builds
 * <p>
 * This is considered as workaround for the time being ... though it's unclear if we can get these fixed in maven/cli itself.
 */
public class KieMavenCli extends MavenCli {

    private String projectRoot;

    public KieMavenCli(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    @Override
    public int doMain(CliRequest cliRequest) {
        cliRequest.multiModuleProjectDirectory = new File(projectRoot);
        return super.doMain(cliRequest);
    }

    @Override
    void cli(CliRequest cliRequest) throws Exception {
        synchronized (KieMavenCli.class) {

            super.cli(cliRequest);
            if (cliRequest.commandLine.hasOption(CLIManager.SET_SYSTEM_PROPERTY)) {
                String[] defStrs = cliRequest.commandLine.getOptionValues(CLIManager.SET_SYSTEM_PROPERTY);

                if (defStrs != null) {
                    for (String property : defStrs) {
                        String name;

                        String value;

                        int i = property.indexOf("=");

                        if (i <= 0) {
                            name = property.trim();

                            value = "true";
                        } else {
                            name = property.substring(0,
                                                      i).trim();

                            value = property.substring(i + 1);
                        }

                        cliRequest.userProperties.setProperty(name,
                                                              value);
                    }

                    Iterator<Option> it = cliRequest.commandLine.iterator();

                    while (it.hasNext()) {
                        Option option = (Option) it.next();

                        if (option.getOpt().equals(Character.toString(CLIManager.SET_SYSTEM_PROPERTY))) {
                            it.remove();
                        }
                    }
                }
            }
        }
    }
}
