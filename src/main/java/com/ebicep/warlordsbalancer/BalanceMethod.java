package com.ebicep.warlordsbalancer;

import java.util.Comparator;
import java.util.Map;

enum BalanceMethod {
    V1 {
        @Override
        public Team getTeam(int amountOfPlayers, Map<Team, Balancer.TeamBalanceInfo> teams) {
            int maxPerTeam = amountOfPlayers / 2 + (amountOfPlayers % 2);
            if (teams.values().stream().anyMatch(teamBalanceInfo -> teamBalanceInfo.players.size() == maxPerTeam)) {
                return teams.entrySet()
                            .stream()
                            .filter(entry -> entry.getValue().players.size() < maxPerTeam)
                            .map(Map.Entry::getKey)
                            .findAny()
                            .get();
            } else { //put on team with lowest weight
                return teams.entrySet().stream()
                            .min(Comparator.comparingDouble(entry -> entry.getValue().totalWeight))
                            .map(Map.Entry::getKey)
                            .orElse(Team.RED);
            }
        }
    },
    V2 {
        @Override
        public Team getTeam(int amountOfPlayers, Map<Team, Balancer.TeamBalanceInfo> teams) {
            return teams.entrySet()
                        .stream()
                        .min(Comparator.comparingInt(entry -> entry.getValue().players.size()))
                        .map(Map.Entry::getKey)
                        .orElse(Team.RED);
        }
    };

    public abstract Team getTeam(int amountOfPlayers, Map<Team, Balancer.TeamBalanceInfo> teams);
}
