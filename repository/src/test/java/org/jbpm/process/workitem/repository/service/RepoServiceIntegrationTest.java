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

package org.jbpm.process.workitem.repository.service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class RepoServiceIntegrationTest {

    private static String jsonInput;
    private static RepoService repoService;

    // update this when new workitem is added
    private static int TOTAL_MODULES = Integer.parseInt(System.getProperty("total.mod.count", "40"));

    // update this when new workitem handlers are added
    private static int TOTAL_SERVICES = Integer.parseInt(System.getProperty("total.wi.count", "93"));

    @BeforeClass
    public static void init() throws Exception {
        jsonInput = IOUtils.toString(RepoServiceIntegrationTest.class.getResourceAsStream("/serviceinfo.js"),
                                     StandardCharsets.UTF_8);
        repoService = new RepoService(jsonInput);
    }

    @Test
    public void testRepoServiceLoad() throws Exception {
        // repo services are the workitem handlers
        List<RepoData> repoData = repoService.getServices();
        assertNotNull(repoData);
        assertEquals(TOTAL_SERVICES,
                     repoData.size());
    }

    @Test
    public void testRepoModules() throws Exception {
        // repo modules give the handler grouping info, such as "archive-workitem" or "twitter-workitem"
        List<RepoModule> repoModules = repoService.getModules();
        assertNotNull(repoModules);

        assertEquals(TOTAL_MODULES,
                     repoModules.size());
    }

    @Test
    public void testEnabledDisableModules() throws Exception {
        // by default all modules are enabled
        List<RepoModule> enabledRepoModules = repoService.getEnabledModules();
        assertNotNull(enabledRepoModules);
        assertEquals(TOTAL_MODULES,
                     enabledRepoModules.size());

        // by default no modules are disabled
        List<RepoModule> disabledModules = repoService.getDisabledModules();
        assertNotNull(disabledModules);
        assertEquals(0,
                     disabledModules.size());

        // disable a module
        String moduleName = enabledRepoModules.get(0).getName();
        repoService.disableModule(moduleName);
        enabledRepoModules = repoService.getEnabledModules();
        assertNotNull(enabledRepoModules);
        assertEquals(TOTAL_MODULES - 1,
                     enabledRepoModules.size());

        disabledModules = repoService.getDisabledModules();
        assertNotNull(disabledModules);
        assertEquals(1,
                     disabledModules.size());

        // not enable it again
        repoService.enableModule(moduleName);
        disabledModules = repoService.getDisabledModules();
        assertNotNull(disabledModules);
        assertEquals(0,
                     disabledModules.size());
    }

    @Test
    public void testInstalledUninstalledModules() throws Exception {
        // by default all modules are uninstalled
        List<RepoModule> installeddRepoModules = repoService.getInstalledModules();
        assertNotNull(installeddRepoModules);
        assertEquals(0,
                     installeddRepoModules.size());

        // install module
        String moduleName = repoService.getModules().get(0).getName();
        repoService.installModule(moduleName,
                                  "project A");
        installeddRepoModules = repoService.getInstalledModules();
        assertNotNull(installeddRepoModules);
        assertEquals(1,
                     installeddRepoModules.size());

        // uninstall it again
        repoService.uninstallModule(moduleName,
                                    "project A");
        installeddRepoModules = repoService.getInstalledModules();
        assertNotNull(installeddRepoModules);
        assertEquals(0,
                     installeddRepoModules.size());
    }
}
