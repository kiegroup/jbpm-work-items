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

import java.util.HashMap;
import java.util.Map;

import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.endpoints.match.dto.MatchList;
import net.rithms.riot.api.endpoints.match.dto.MatchReference;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;
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

@Wid(widfile = "RiotSummonerLastMatch.wid", name = "RiotSummonerLastMatch",
        displayName = "RiotSummonerLastMatch",
        defaultHandler = "mvel: new org.jbpm.process.workitem.riot.LastSummonerMatchWorkitemHandler(\"apiKey\")",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "icon.png",
        parameters = {
                @WidParameter(name = "SummonerName", required = true),
                @WidParameter(name = "SummonerPlatform")
        },
        results = {
                @WidResult(name = "LastMatch", runtimeType = "net.rithms.riot.api.endpoints.match.dto.MatchReference")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "riot,league,legends,summoner,match,get,info",
                action = @WidAction(title = "Get Summoner last match info"),
                authinfo = @WidAuth(required = true, params = {"apiKey"},
                        paramsdescription = {"Riot Games api key"},
                        referencesite = "https://developer.riotgames.com/api-keys.html")
        ))
public class LastSummonerMatchWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String apiKey;
    private RiotAuth riotAuth;
    private RiotApi riotApi;

    private static final Logger logger = LoggerFactory
            .getLogger(LastSummonerMatchWorkitemHandler.class);
    private static final String RESULTS_VALUE = "LastMatch";

    public LastSummonerMatchWorkitemHandler(String apiKey) {
        this.apiKey = apiKey;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            String summonerName = (String) workItem.getParameter("SummonerName");
            String summonerPlatform = (String) workItem.getParameter("SummonerPlatform");

            Map<String, Object> results = new HashMap<String, Object>();

            Platform platform = RiotUtils.getPlatform(summonerPlatform);

            if (riotAuth == null) {
                riotAuth = new RiotAuth();
            }

            riotApi = riotAuth.getRiotApi(apiKey);
            Summoner summoner = riotApi.getSummonerByName(platform,
                                                          summonerName);

            if (summoner != null) {

                MatchList matchList = riotApi.getMatchListByAccountId(platform,
                                                                      summoner.getAccountId());
                if (matchList != null && matchList.getTotalGames() > 0) {
                    MatchReference lastMatch = RiotUtils.getLatestPlayedMatch(matchList.getMatches());
                    results.put(RESULTS_VALUE,
                                lastMatch);

                    workItemManager.completeWorkItem(workItem.getId(),
                                                     results);
                } else {
                    throw new IllegalArgumentException("Could not find matches for: " + summonerName + " and platform: " + summonerPlatform);
                }
            } else {
                throw new IllegalArgumentException("Could not find summoner with name: " + summonerName + " and platform: " + summonerPlatform);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setRiotAuth(RiotAuth riotAuth) {
        this.riotAuth = riotAuth;
    }
}
