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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class RepoServiceIntegrationTest {

    private static final String VIMEO_NEW = "Vimeo-new";
    private static String jsonInput;
    private static RepoService repoService;

    // update this when new workitem is added
    private static int TOTAL_MODULES = Integer.parseInt(System.getProperty("total.mod.count", "41"));

    // update this when new workitem handlers are added
    private static int TOTAL_SERVICES = Integer.parseInt(System.getProperty("total.wi.count", "95"));

    @BeforeClass
    public static void init() throws Exception {
        jsonInput = IOUtils.toString(RepoServiceIntegrationTest.class.getResourceAsStream("/serviceinfo.js"),
                                     StandardCharsets.UTF_8);
        repoService = new RepoService(jsonInput);
    }

    private List<RepoData> getInitRepoDataList(){
        return repoService.getServices();
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

    @Test
    public void testAddServiceTask(){
        RepoData addService = initRepoData(VIMEO_NEW, "7.54.0.Final");
        HashMap<String, List<String>> resultMap = new HashMap<>();
        repoService.addService(addService, resultMap);
        addService.setId(UUID.randomUUID().toString());
        assertNull(resultMap.get(RepoService.SKIPPED));
        assertEquals(VIMEO_NEW, resultMap.get(RepoService.CREATED).get(0));


        RepoData updateService = initRepoData(VIMEO_NEW, "7.54.0.Final");
        resultMap.clear();
        updateService.setId(UUID.randomUUID().toString());
        repoService.addService(updateService, resultMap);

        assertNull(resultMap.get(RepoService.CREATED));
        assertEquals(VIMEO_NEW, resultMap.get(RepoService.SKIPPED).get(0));
    }

    @Test
    public void testRemoveServiceTask() {
        assertTrue(getInitRepoDataList().size() > 2);
        String serviceTaskNameOne = getInitRepoDataList().get(0).getName();
        String serviceTaskNameTwo = getInitRepoDataList().get(1).getName();
        Optional<RepoData> beforeRemoveOne = repoService.getServices().stream().filter(s -> s.getName().equals(serviceTaskNameOne)).findFirst();
        assertTrue(beforeRemoveOne.isPresent());
        String serviceTaskOneId = beforeRemoveOne.get().getId();

        repoService.removeServiceTask(serviceTaskOneId);
        Optional<RepoData> afterRemoveOne = repoService.getServices().stream().filter(s -> s.getName().equals(serviceTaskNameOne)).findFirst();
        assertFalse(afterRemoveOne.isPresent());

        Optional<RepoData> beforeRemoveTwo = repoService.getServices().stream().filter(s -> s.getName().equals(serviceTaskNameTwo)).findFirst();
        assertTrue(beforeRemoveTwo.isPresent());
        String serviceTaskTwoId = beforeRemoveTwo.get().getId();
        beforeRemoveTwo.get().setEnabled(true);

        repoService.removeServiceTask(serviceTaskTwoId);
        Optional<RepoData> afterRemoveTwo = repoService.getServices().stream().filter(s -> s.getName().equals(serviceTaskNameTwo)).findFirst();
        assertFalse(afterRemoveTwo.isPresent());
    }

    private RepoData initRepoData(String name, String version) {
        RepoData service = new RepoData();
        service.setGav(null);
        service.setActiontitle("Delete an existing video");
        service.setAuthparams(null);
        service.setAuthreferencesite("https://developer.vimeo.com/api/authentication");
        service.setCategory("Vimeo");
        service.setDescription("Interact with videos on Vimeo");
        service.setDisplayName(name);
        service.setDefaultHandler("mvel: new org.jbpm.process.workitem.vimeo.DeleteVideoWorkitemHandler(\\\"accessToken\\\")");
        service.setDocumentation("vimeo-workitem/index.html");
        service.setEnabled(false);
        service.setIcon("DeleteVimeo.png");
        service.setIsaction(null);
        service.setIstrigger(null);
        service.setKeywords(null);
        RepoMavenDepend repoMavenDepend = new RepoMavenDepend();
        repoMavenDepend.setGroupId("org.jbpm.contrib");
        repoMavenDepend.setArtifactId("org.jbpm.contrib");
        repoMavenDepend.setVersion(version);
        service.setMavenDependencies(Collections.singletonList(repoMavenDepend));

        service.setModule(repoMavenDepend.getArtifactId());
        service.setName(name);
        service.setParameters(null);
        service.setRequiresauth("true");
        service.setResults(null);

        return service;
    }
}
