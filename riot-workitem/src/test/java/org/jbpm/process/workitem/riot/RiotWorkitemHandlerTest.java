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
package org.jbpm.process.workitem.riot;

import java.util.Arrays;
import java.util.List;

import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.endpoints.match.dto.Match;
import net.rithms.riot.api.endpoints.match.dto.MatchList;
import net.rithms.riot.api.endpoints.match.dto.MatchReference;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.test.AbstractBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RiotWorkitemHandlerTest extends AbstractBaseTest {

    @Mock
    RiotAuth auth;

    @Mock
    RiotApi riotApi;

    @Mock
    Summoner summoner;

    @Mock
    MatchList matchList;

    @Mock
    MatchReference matchReference;

    @Mock
    Match match;

    @Before
    public void setUp() {
        try {
            when(auth.getRiotApi(anyString())).thenReturn(riotApi);
            when(riotApi.getSummonerByName(any(Platform.class),
                                           anyString())).thenReturn(summoner);
            when(summoner.getName()).thenReturn("testSummonerName");
            when(summoner.getSummonerLevel()).thenReturn(10);
            when(summoner.getAccountId()).thenReturn(123L);

            when(riotApi.getMatchListByAccountId(any(Platform.class),
                                                 anyLong())).thenReturn(matchList);
            when(matchList.getTotalGames()).thenReturn(10);
            when(matchList.getMatches()).thenReturn(Arrays.asList(matchReference,
                                                                  matchReference,
                                                                  matchReference,
                                                                  matchReference,
                                                                  matchReference,
                                                                  matchReference,
                                                                  matchReference,
                                                                  matchReference,
                                                                  matchReference,
                                                                  matchReference));
            when(matchReference.getGameId()).thenReturn(11111L);

            when(riotApi.getMatch(any(Platform.class),
                                  anyLong())).thenReturn(match);
            when(match.getGameId()).thenReturn(12345L);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetSummonerInfo() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("SummonerName",
                              "testSummonerName");

        SummonerInfoWorkitemHandler handler = new SummonerInfoWorkitemHandler("testApiKey");

        handler.setRiotAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("SummonerInfo") instanceof Summoner);

        Summoner testSummoner = (Summoner) manager.getResults().get(workItem.getId()).get("SummonerInfo");
        assertNotNull(testSummoner);
        assertEquals("testSummonerName",
                     testSummoner.getName());
        assertEquals(10,
                     testSummoner.getSummonerLevel());
        assertEquals(123L,
                     testSummoner.getAccountId());
    }

    @Test
    public void testGetLastMatchInfo() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("SummonerName",
                              "testSummonerName");

        LastSummonerMatchWorkitemHandler handler = new LastSummonerMatchWorkitemHandler("testApiKey");

        handler.setRiotAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("LastMatch") instanceof MatchReference);

        MatchReference matchReference = (MatchReference) manager.getResults().get(workItem.getId()).get("LastMatch");
        assertNotNull(matchReference);
        assertEquals(11111L,
                     matchReference.getGameId());
    }

    @Test
    public void testGetSingleMatchInfo() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("SummonerName",
                              "testSummonerName");

        MatchesInfoWorkitemHandler handler = new MatchesInfoWorkitemHandler("testApiKey");

        handler.setRiotAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("MatchesInfo") instanceof List);

        List<Match> matches = (List<Match>) manager.getResults().get(workItem.getId()).get("MatchesInfo");
        assertNotNull(matches);
        assertEquals(1,
                     matches.size());
        assertEquals(12345L,
                     matches.get(0).getGameId());
    }

    @Test
    public void testGetLastTenMatchInfo() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("SummonerName",
                              "testSummonerName");
        workItem.setParameter("NumOfMatches",
                              "10");

        MatchesInfoWorkitemHandler handler = new MatchesInfoWorkitemHandler("testApiKey");

        handler.setRiotAuth(auth);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("MatchesInfo") instanceof List);

        List<Match> matches = (List<Match>) manager.getResults().get(workItem.getId()).get("MatchesInfo");
        assertNotNull(matches);
        assertEquals(10,
                     matches.size());
    }
}
