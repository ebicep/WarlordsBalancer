package com.ebicep.warlordsbalancer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ebicep.warlordsbalancer.Balancer.format;

enum BalanceMethod {
    /**
     *
     */
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
                    if (player.preassignedTeam() != null) {
                        team = player.preassignedTeam();
                    } else if (teams.values().stream().anyMatch(teamBalanceInfo -> teamBalanceInfo.players.size() == maxPerTeam)) {
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
                    Team team;
                    if (player.preassignedTeam() != null) {
                        team = player.preassignedTeam();
                    } else {
                        team = teams.entrySet()
                                    .stream()
                                    .min(Comparator.comparingInt(entry -> entry.getValue().players.size()))
                                    .map(Map.Entry::getKey)
                                    .orElse(Team.RED);
                    }
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
//            Team lastTeam = Team.RED;
            for (Balancer.Filter filter : filters) {
                List<Balancer.Player> playerList = players.stream()
                                                          .filter(filter::test)
                                                          .sorted(Comparator.comparingDouble(player -> -player.weight()))
                                                          .toList();
                for (int i = 0; i < playerList.size(); i++) {
                    Balancer.Player player = playerList.get(i);
                    players.remove(player);
                    boolean firstOfCategory = i == 0;// && filter instanceof Balancer.Filter.SpecTypeFilter;
                    Team team;
                    if (player.preassignedTeam() != null) {
                        team = player.preassignedTeam();
                    } else if (firstOfCategory) {
                        team = teams.entrySet()
                                    .stream()
                                    .min(Comparator.comparingDouble(entry -> entry.getValue().totalWeight))
                                    .map(Map.Entry::getKey)
                                    .orElse(Team.BLUE);
                    } else {
                        team = teams.entrySet()
                                    .stream()
                                    .min(Comparator.comparingDouble(entry -> entry.getValue().players.size()))
                                    .map(Map.Entry::getKey)
                                    .orElse(Team.BLUE);
                    }
//                    lastTeam = team;
                    int index = amountOfPlayers - players.size();
                    Balancer.DebuggedPlayer debuggedPlayer = new Balancer.DebuggedPlayer(player, colors -> colors.aqua() + index);
                    if (player.preassignedTeam() == null && firstOfCategory) {
                        Map<Team, Double> teamsWeights = teams.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().totalWeight));
                        double newTeamWeight = teamsWeights.get(team) + player.weight();
                        debuggedPlayer.debuggedMessages().add(colors -> colors.darkPurple() +
                                teamsWeights.entrySet()
                                            .stream()
                                            .map(entry -> {
                                                Team key = entry.getKey();
                                                return key + ": " + format(entry.getValue()) +
                                                        (key == team ? "->" + format(newTeamWeight) : "");
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
