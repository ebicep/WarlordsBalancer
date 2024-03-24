package com.ebicep.warlordsbalancer;

import java.util.*;
import java.util.stream.Collectors;

import static com.ebicep.warlordsbalancer.Balancer.format;

public interface BalanceMethod {

    BalanceMethod V1 = new V1();
    BalanceMethod V2 = new V2();
    BalanceMethod V2_1 = new V2_1();
    BalanceMethod V3 = new V3();
    BalanceMethod V3_1 = new V3_1();

    void balance(Set<Balancer.Player> players, List<Balancer.Filter> filters, Map<Team, Balancer.TeamBalanceInfo> teams);

    class V1 implements BalanceMethod {
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
                    } else if (teams.values().stream().anyMatch(teamBalanceInfo -> teamBalanceInfo.getPlayers().size() == maxPerTeam)) {
                        team = teams.entrySet()
                                    .stream()
                                    .filter(entry -> entry.getValue().getPlayers().size() < maxPerTeam)
                                    .map(Map.Entry::getKey)
                                    .findAny()
                                    .get();
                    } else { //put on team with lowest weight
                        team = teams.entrySet().stream()
                                    .min(Comparator.comparingDouble(entry -> entry.getValue().getTotalWeight()))
                                    .map(Map.Entry::getKey)
                                    .orElse(Team.RED);
                    }
                    int index = amountOfPlayers - players.size();
                    teams.get(team).addPlayer(new Balancer.DebuggedPlayer(player, colors -> colors.aqua() + index));
                }
            }
        }
    }

    class V2 implements BalanceMethod {
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
                                    .min(Comparator.comparingInt(entry -> entry.getValue().getPlayers().size()))
                                    .map(Map.Entry::getKey)
                                    .orElse(Team.RED);
                    }
                    int index = amountOfPlayers - players.size();
                    teams.get(team).addPlayer(new Balancer.DebuggedPlayer(player, colors -> colors.aqua() + index));
                }
            }
        }
    }

    class V2_1 implements BalanceMethod {
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
                                    .min(Comparator.comparingDouble(entry -> entry.getValue().getTotalWeight()))
                                    .map(Map.Entry::getKey)
                                    .orElse(Team.BLUE);
                    } else {
                        team = teams.entrySet()
                                    .stream()
                                    .min(Comparator.comparingDouble(entry -> entry.getValue().getPlayers().size()))
                                    .map(Map.Entry::getKey)
                                    .orElse(Team.BLUE);
                    }
//                    lastTeam = team;
                    int index = amountOfPlayers - players.size();
                    Balancer.DebuggedPlayer debuggedPlayer = new Balancer.DebuggedPlayer(player, colors -> colors.aqua() + index);
                    if (player.preassignedTeam() == null && firstOfCategory) {
                        Map<Team, Double> teamsWeights = teams.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getTotalWeight()));
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
    }

    class V3 implements BalanceMethod {
        @Override
        public void balance(Set<Balancer.Player> players, List<Balancer.Filter> filters, Map<Team, Balancer.TeamBalanceInfo> teams) {
            int amountOfPlayers = players.size();
            int counter = 0;
            Team lastTeam = Team.RED;
            List<Balancer.Player> playerList = new ArrayList<>();
            for (Balancer.Filter filter : filters) {
                playerList.addAll(players.stream()
                                         .filter(filter::test)
                                         .sorted(Comparator.comparingDouble(player -> -player.weight()))
                                         .toList());
            }
            for (int i = 0; i < playerList.size(); i++) {
                Balancer.Player player = playerList.get(i);
                players.remove(player);
                Team team;
                if (player.preassignedTeam() != null) {
                    team = player.preassignedTeam();
                } else {
                    int pairCount = counter % 2;
                    if (pairCount == 0) {
                        team = teams.entrySet()
                                    .stream()
                                    .min(Comparator.comparingDouble(entry -> entry.getValue().getTotalWeight()))
                                    .map(Map.Entry::getKey)
                                    .orElse(Team.RED);
                    } else {
                        if (lastTeam == Team.BLUE) {
                            team = Team.RED;
                        } else if (lastTeam == Team.RED) {
                            team = Team.BLUE;
                        } else {
                            throw new IllegalStateException("V3 does not support more than 2 teams");
                        }
                    }
                }
                lastTeam = team;
                int index = amountOfPlayers - players.size();
                teams.get(team).addPlayer(new Balancer.DebuggedPlayer(player, colors -> colors.aqua() + index));
                counter++;
            }
        }
    }

    class V3_1 implements BalanceMethod {
        @Override
        public void balance(Set<Balancer.Player> players, List<Balancer.Filter> filters, Map<Team, Balancer.TeamBalanceInfo> teams) {
            int amountOfPlayers = players.size();
            List<Balancer.Player> playerList = new ArrayList<>();
            for (Balancer.Filter filter : filters) {
                playerList.addAll(players.stream()
                                         .filter(filter::test)
                                         .sorted(Comparator.comparingDouble(player -> -player.weight()))
                                         .toList());
                playerList.forEach(players::remove);
            }
            List<Balancer.Player> preassignedPlayersList = playerList
                    .stream()
                    .filter(player -> player.preassignedTeam() != null)
                    .toList();
            int preassignedPlayers = preassignedPlayersList.size();
            for (int i = 0; i < preassignedPlayersList.size(); i++) {
                Balancer.Player player = preassignedPlayersList.get(i);
                int playerIndex = i + 1;
                teams.get(player.preassignedTeam()).addPlayer(new Balancer.DebuggedPlayer(player, colors -> colors.aqua() + playerIndex));
            }
            playerList.removeAll(preassignedPlayersList);
            for (int i = 0; i < playerList.size(); i += 2) {
                Balancer.Player player1 = playerList.get(i);
                Balancer.Player player2;
                Team team1;
                Team team2;
                int player1Index = preassignedPlayers + i + 1;
                int player2Index = preassignedPlayers + i + 2;
                Team lowestWeightTeam = teams.entrySet()
                                             .stream()
                                             .min(Comparator.comparingDouble(entry -> entry.getValue().getTotalWeight()))
                                             .map(Map.Entry::getKey)
                                             .orElse(Team.RED);
                Team otherTeam = lowestWeightTeam == Team.RED ? Team.BLUE : Team.RED;
                if (i + 1 >= playerList.size()) {
                    teams.get(lowestWeightTeam).addPlayer(new Balancer.DebuggedPlayer(player1, colors -> colors.aqua() + player1Index));
                    continue;
                }
                player2 = playerList.get(i + 1);
                if (player1.weight() > player2.weight()) {
                    team1 = lowestWeightTeam;
                    team2 = otherTeam;
                } else {
                    team2 = lowestWeightTeam;
                    team1 = otherTeam;
                }

                double blueWeight = teams.get(Team.BLUE).getTotalWeight();
                double redWeight = teams.get(Team.RED).getTotalWeight();
                teams.get(team1).addPlayer(new Balancer.DebuggedPlayer(player1, colors -> colors.aqua() + player1Index));
                teams.get(team2)
                     .addPlayer(new Balancer.DebuggedPlayer(player2, colors -> colors.aqua() + player2Index + "-[" + format(blueWeight) + "|" + format(redWeight) + "]"));
            }
        }
    }

}
