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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import net.rithms.riot.api.endpoints.match.dto.Match;
import net.rithms.riot.api.endpoints.match.dto.MatchReference;
import net.rithms.riot.constant.Platform;

public class RiotUtils {

    private static Comparator<MatchReference> matchTimestampComparator
            = Comparator.comparing(MatchReference::getTimestamp);

    private static Comparator<Match> matchCreationpComparator
            = Comparator.comparing(Match::getGameCreation);

    public static Platform getPlatform(String summonerPlatform) throws IllegalArgumentException {
        if (summonerPlatform != null && summonerPlatform.length() > 0) {
            try {
                return Platform.getPlatformByName(summonerPlatform);
            } catch (NoSuchElementException e) {
                throw new IllegalArgumentException("Unable to create platform from: " + summonerPlatform);
            }
        }

        // default to euw
        return Platform.EUW;
    }

    public static MatchReference getLatestPlayedMatch(List<MatchReference> playedMatchesList) {

        Collections.sort(playedMatchesList,
                         matchTimestampComparator);

        return playedMatchesList.get(0);
    }

    public static List<Match> getPlayedMatches(List<Match> playedMatchesList,
                                               int numOfMatches) {
        Collections.sort(playedMatchesList,
                         matchCreationpComparator);

        if (numOfMatches >= playedMatchesList.size()) {
            return playedMatchesList;
        } else {
            return playedMatchesList.subList(0,
                                             numOfMatches);
        }
    }
}
