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

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class RepoServiceIntegrationTest {

    private static final String EMAIL = "Email";
    private static final String EMAIL_NEW = "Email-new";
    private static final String GET_INFO_VIMEO = "GetInfoVimeo";
    private static final String GET_INFO_VIMEO_NEW = "GetInfoVimeo-new";
    private static final String DELETE_VIMEO = "DeleteVimeo";
    private static final String DELETE_VIMEO_NEW = "DeleteVimeo-new";
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

    @Test
    public void testAddService() throws InvocationTargetException, IllegalAccessException {
        List<RepoData> beforeAddRepoDataList = repoService.getServices();
        assertEquals(93, beforeAddRepoDataList.size());
        assertThat(beforeAddRepoDataList.stream()).hasSize(93).extracting(repoData -> repoData.getName()).containsOnlyOnce(EMAIL).doesNotContain(EMAIL_NEW);
        Optional<RepoData> beforeEmailRepoData = beforeAddRepoDataList.stream().filter(repoData -> repoData.getName().equals(EMAIL)).findFirst();
        assertTrue(beforeEmailRepoData.isPresent());

        RepoData newRepDataHasSameGAV = new RepoData();
        BeanUtils.copyProperties(newRepDataHasSameGAV, beforeEmailRepoData.get());
        newRepDataHasSameGAV.setDisplayName(EMAIL_NEW);
        newRepDataHasSameGAV.setName(EMAIL_NEW);

        repoService.addService(newRepDataHasSameGAV);
        List<RepoData> afterAddNewRepDataHasSameGAV = repoService.getServices();
        assertThat(afterAddNewRepDataHasSameGAV.stream()).hasSize(93).extracting(repoData -> repoData.getName()).containsOnlyOnce(EMAIL_NEW).doesNotContain(EMAIL);

        Optional<RepoData> beforeGetInfoVimeoRepoData = beforeAddRepoDataList.stream().filter(repoData -> repoData.getName().equals(GET_INFO_VIMEO)).findFirst();
        assertTrue(beforeGetInfoVimeoRepoData.isPresent());

        RepoData newRepDataHasDifferentGAV = new RepoData();
        BeanUtils.copyProperties(newRepDataHasDifferentGAV, beforeGetInfoVimeoRepoData.get());
        newRepDataHasDifferentGAV.setDisplayName(GET_INFO_VIMEO_NEW);
        newRepDataHasDifferentGAV.setName(GET_INFO_VIMEO_NEW);
        newRepDataHasDifferentGAV.setGav("org.jbpm.contrib:vimeo-workitem:7.30.0-SNAPSHOT");

        repoService.addService(newRepDataHasDifferentGAV);
        List<RepoData> afterAddNewRepDataHasDifferentGA = repoService.getServices();
        assertThat(afterAddNewRepDataHasDifferentGA.stream()).hasSize(94).extracting(repoData -> repoData.getName()).containsOnlyOnce(GET_INFO_VIMEO_NEW).containsOnlyOnce(GET_INFO_VIMEO);

        Optional<RepoData> beforeNewRepDataHasDifferentGAVWithoutGAV = afterAddNewRepDataHasDifferentGA.stream().filter(repoData -> repoData.getName().equals(DELETE_VIMEO)).findFirst();
        assertTrue(beforeNewRepDataHasDifferentGAVWithoutGAV.isPresent());

        RepoData newRepDataHasDifferentGAVWithoutGAV = new RepoData();
        RepoData beforeNewRepDataHasDifferentGAVWithoutGAVRepoData = beforeNewRepDataHasDifferentGAVWithoutGAV.get();
        BeanUtils.copyProperties(newRepDataHasDifferentGAVWithoutGAV, beforeNewRepDataHasDifferentGAVWithoutGAVRepoData);
        RepoMavenDepend newRepoMavenDependDifferentGAVWithoutGAV = new RepoMavenDepend();
        BeanUtils.copyProperties(newRepoMavenDependDifferentGAVWithoutGAV, beforeNewRepDataHasDifferentGAVWithoutGAVRepoData.getMavenDependencies().get(0));
        newRepDataHasDifferentGAVWithoutGAV.setDisplayName(DELETE_VIMEO_NEW);
        newRepDataHasDifferentGAVWithoutGAV.setName(DELETE_VIMEO_NEW);
        newRepDataHasDifferentGAVWithoutGAV.setGav(null);
        newRepoMavenDependDifferentGAVWithoutGAV.setVersion("7.30.0-SNAPSHOT");
        newRepDataHasDifferentGAVWithoutGAV.setMavenDependencies(Collections.singletonList(newRepoMavenDependDifferentGAVWithoutGAV));

        repoService.addService(newRepDataHasDifferentGAVWithoutGAV);
        List<RepoData> afterNewRepDataHasDifferentGAVWithoutGAV = repoService.getServices();

        assertThat(afterNewRepDataHasDifferentGAVWithoutGAV.stream()).hasSize(95).extracting(repoData -> repoData.getName()).containsOnlyOnce(DELETE_VIMEO_NEW).containsOnlyOnce(DELETE_VIMEO);
    }
}
