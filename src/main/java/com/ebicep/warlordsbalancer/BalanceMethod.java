package com.ebicep.warlordsbalancer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

enum BalanceMethod {
    V1 {
        @Override
        public void balance(Set<Balancer.Player> players, List<Balancer.Filter> filters, Map<Team, Balancer.TeamBalanceInfo> teams) {
            int amountOfPlayers = players.size();
            int maxPerTeam = amountOfPlayers / 2 + (amountOfPlayers % 2);

            for (Balancer.Filter filter : filters) {
                List<Balancer.Player> playerList = players.stream()
                                                          .filter(filter::test)
                                                          .sorted(Comparator.comparingDouble(player -> -player.weight()))
                                                          .toList();
                for (Balancer.Player player : playerList) {
                    players.remove(player);
                    Team team;
                    if (teams.values().stream().anyMatch(teamBalanceInfo -> teamBalanceInfo.players.size() == maxPerTeam)) {
                        team = teams.entrySet()
                                    .stream()
                                    .filter(entry -> entry.getValue().players.size() < maxPerTeam)
                                    .map(Map.Entry::getKey)
                                    .findAny()
                                    .get();
                    } else { //put on team with lowest weight
                        team = teams.entrySet().stream()
                                    .min(Comparator.comparingDouble(entry -> entry.getValue().totalWeight))
                                    .map(Map.Entry::getKey)
                                    .orElse(Team.RED);
                    }
                    int index = amountOfPlayers - players.size();
                    teams.get(team).addPlayer(new Balancer.DebuggedPlayer(player, colors -> colors.aqua() + index));
                }
            }
        }

    },
    V2 {
        @Override
        public void balance(Set<Balancer.Player> players, List<Balancer.Filter> filters, Map<Team, Balancer.TeamBalanceInfo> teams) {
            int amountOfPlayers = players.size();
            for (Balancer.Filter filter : filters) {
                List<Balancer.Player> playerList = players.stream()
                                                          .filter(filter::test)
                                                          .sorted(Comparator.comparingDouble(player -> -player.weight()))
                                                          .toList();
                for (Balancer.Player player : playerList) {
                    players.remove(player);
                    Team team = teams.entrySet()
                                     .stream()
                                     .min(Comparator.comparingInt(entry -> entry.getValue().players.size()))
                                     .map(Map.Entry::getKey)
                                     .orElse(Team.RED);
                    int index = amountOfPlayers - players.size();
                    teams.get(team).addPlayer(new Balancer.DebuggedPlayer(player, colors -> colors.aqua() + index));
                }
            }
        }
    },
    V2_1 {
        @Override
        public void balance(Set<Balancer.Player> players, List<Balancer.Filter> filters, Map<Team, Balancer.TeamBalanceInfo> teams) {
            int amountOfPlayers = players.size();
            for (Balancer.Filter filter : filters) {
                List<Balancer.Player> playerList = players.stream()
                                                          .filter(filter::test)
                                                          .sorted(Comparator.comparingDouble(player -> -player.weight()))
                                                          .toList();
                for (int i = 0; i < playerList.size(); i++) {
                    Balancer.Player player = playerList.get(i);
                    players.remove(player);
                    boolean firstOfCategory = i == 0;// && filter instanceof Balancer.Filter.SpecTypeFilter;
                    Comparator<Map.Entry<Team, Balancer.TeamBalanceInfo>> comparator =
                            firstOfCategory ?
                            Comparator.comparingDouble(entry -> entry.getValue().totalWeight) :
                            Comparator.comparingInt(entry -> entry.getValue().players.size());
                    Team team = teams.entrySet()
                                     .stream()
                                     .min(comparator)
                                     .map(Map.Entry::getKey)
                                     .orElse(Team.BLUE);
                    int index = amountOfPlayers - players.size();
                    Balancer.DebuggedPlayer debuggedPlayer = new Balancer.DebuggedPlayer(player, colors -> colors.aqua() + index);
                    if (firstOfCategory) {
                        Map<Team, Double> teamsWeights = teams.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().totalWeight));
                        double newTeamWeight = teamsWeights.get(team) + player.weight();
                        debuggedPlayer.debuggedMessages().add(colors -> colors.darkPurple() +
                                teamsWeights.entrySet()
                                            .stream()
                                            .map(entry -> {
                                                Team key = entry.getKey();
                                                return key + ": " + Balancer.WEIGHT_FORMAT.format(entry.getValue()) +
                                                        (key == team ? "->" + Balancer.WEIGHT_FORMAT.format(newTeamWeight) : "");
                                            })
                                            .collect(Collectors.joining(" | "))
                        );
                    }
                    teams.get(team).addPlayer(debuggedPlayer);
                }
            }
        }
    },

    ;

    public abstract void balance(Set<Balancer.Player> players, List<Balancer.Filter> filters, Map<Team, Balancer.TeamBalanceInfo> teams);
}
