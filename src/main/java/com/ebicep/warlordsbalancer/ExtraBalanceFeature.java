package com.ebicep.warlordsbalancer;

import java.util.*;

enum ExtraBalanceFeature {

    /**
     * <p>Swaps players between the two teams to even out the player count.</p>
     * <p>- Only tries to swap players that have a spec type whose count isnt even between the two teams</p>
     * <p>- Only tries to swap player which would even out the weights the most</p>
     * <p>- Players swapped indicated by "MOVED" </p>
     */
    SWAP_UNEVEN_TEAMS {
        @Override
        public boolean apply(Balancer.Printer printer, Map<Team, Balancer.TeamBalanceInfo> teamBalanceInfos) {
            // check if there are any teams with at least 2 more players than the other team
            int minPlayers = 0;
            Balancer.TeamBalanceInfo minPlayersTeamInfo = null;
            int maxPlayers = 0;
            Balancer.TeamBalanceInfo maxPlayersTeamInfo = null;
            for (Map.Entry<Team, Balancer.TeamBalanceInfo> entry : teamBalanceInfos.entrySet()) {
                int size = entry.getValue().players.size();
                if (minPlayers == 0 || size < minPlayers) {
                    minPlayers = size;
                    minPlayersTeamInfo = entry.getValue();
                }
                if (size > maxPlayers) {
                    maxPlayers = size;
                    maxPlayersTeamInfo = entry.getValue();
                }
            }
            Color colors = printer.colors();
            int playerDifference = maxPlayers - minPlayers;
            if (playerDifference <= 1) {
                printer.sendMessage(colors.yellow() + "Teams are even");
                return false;
            }
            boolean applied = false;
            for (int i = 0; i < playerDifference - 1; i++) {
                if (trySwapping(minPlayersTeamInfo, maxPlayersTeamInfo, printer, colors)) {
                    applied = true;
                }
            }
            return applied;
        }

        private boolean trySwapping(
                Balancer.TeamBalanceInfo minPlayersTeamInfo,
                Balancer.TeamBalanceInfo maxPlayersTeamInfo,
                Balancer.Printer printer,
                Color colors
        ) {
            // check which spec type to swap
            EnumSet<SpecType> specTypes = EnumSet.allOf(SpecType.class);
            for (SpecType specType : SpecType.VALUES) {
                if (minPlayersTeamInfo.specTypeCount.getOrDefault(specType, 0).equals(maxPlayersTeamInfo.specTypeCount.getOrDefault(specType, 0))) {
                    specTypes.remove(specType);
                }
            }
            printer.sendMessage(colors.yellow() + "Swappable spec types: " + colors.darkAqua() + specTypes);
            // find any player on the team with the most players that has the spec type and would even out the weights the most
            double weightDiff = maxPlayersTeamInfo.totalWeight - minPlayersTeamInfo.totalWeight;
            Balancer.DebuggedPlayer playerToMove = null;
            double lowestWeightDiff = Double.MAX_VALUE;
            for (SpecType specType : specTypes) {
                for (Balancer.DebuggedPlayer player : maxPlayersTeamInfo.getPlayersMatching(specType)) {
                    Balancer.Player p = player.player();
                    double newLowestWeightDiff = Math.abs(weightDiff - p.weight() * 2);
                    if (newLowestWeightDiff < lowestWeightDiff) {
                        lowestWeightDiff = newLowestWeightDiff;
                        playerToMove = player;
                    }
                }
            }
            if (playerToMove == null) {
                printer.sendMessage(colors.darkRed() + "No player to move");
                return false;
            }
            printer.sendMessage(colors.yellow() + "Moving " + playerToMove.player().getInfo(colors));
            playerToMove.debuggedMessages().add(c -> colors.yellow() + "MOVED");
            maxPlayersTeamInfo.removePlayer(playerToMove);
            minPlayersTeamInfo.addPlayer(playerToMove);
            return true;
        }
    },
    /**
     * <p>Swaps players between the two teams to even out the spec type weights.</p>
     * <p>- First gets the spec type with the most difference in weight.</p>
     * <p>- Then gets the players with the spec type on the two teams.</p>
     * <p>- Then finds a swap that would even out the spec type weights the most.</p>
     * <p>- Players swapped indicated by "SWAPPED".</p>
     * <p>- This repeats 5 times or until no more swaps can be made.</p>
     */
    SWAP_SPEC_TYPES {
        @Override
        public boolean apply(Balancer.Printer printer, Map<Team, Balancer.TeamBalanceInfo> teamBalanceInfos) {
            Color colors = printer.colors();
            Set<Team> teams = teamBalanceInfos.keySet();
            if (teams.size() < 2) {
                printer.sendMessage(colors.darkRed() + "Not 2 teams");
                return false;
            }
            Team[] teamArray = teams.toArray(new Team[0]);
            Team team1 = teamArray[0];
            Team team2 = teamArray[1];
            boolean applied = false;
            for (int i = 0; i < 5; i++) {
                if (trySwapTeams(printer, teamBalanceInfos, team1, team2, i)) {
                    Balancer.printBalanceInfo(printer, teamBalanceInfos);
                    applied = true;
                } else {
                    break;
                }
            }
            return applied;
        }

        private boolean trySwapTeams(Balancer.Printer printer, Map<Team, Balancer.TeamBalanceInfo> teamBalanceInfos, Team team1, Team team2, int index) {
            Color colors = printer.colors();
            // find teams with equal amount of a spec type with the most difference in weight
            Balancer.TeamBalanceInfo teamBalanceInfo1 = teamBalanceInfos.get(team1);
            Balancer.TeamBalanceInfo teamBalanceInfo2 = teamBalanceInfos.get(team2);
            SpecType specType = getSpecTypeHighestWeightDiff(printer, colors, teamBalanceInfo1, teamBalanceInfo2);
            if (specType == null) {
                return false;
            }
            List<Balancer.DebuggedPlayer> team1Matching = teamBalanceInfo1.getPlayersMatching(specType);
            List<Balancer.DebuggedPlayer> team2Matching = teamBalanceInfo2.getPlayersMatching(specType);
            // find the players that would even out the spec type weights the most
            Balancer.DebuggedPlayer team1Swap = null;
            Balancer.DebuggedPlayer team2Swap = null;
            double lowestWeightDiff = Math.abs(teamBalanceInfo1.getSpecTypeWeight(specType) - teamBalanceInfo2.getSpecTypeWeight(specType));
            for (Balancer.DebuggedPlayer player1 : team1Matching) {
                for (Balancer.DebuggedPlayer player2 : team2Matching) {
                    double newLowestWeightDiff = Math.abs(
                            (teamBalanceInfo1.getSpecTypeWeight(specType) - player1.player().weight() + player2.player().weight()) -
                                    (teamBalanceInfo2.getSpecTypeWeight(specType) - player2.player().weight() + player1.player().weight())
                    );
                    if (newLowestWeightDiff < lowestWeightDiff) {
                        lowestWeightDiff = newLowestWeightDiff;
                        team1Swap = player1;
                        team2Swap = player2;
                    }
                }
            }
            if (team1Swap == null) {
                printer.sendMessage(colors.darkRed() + "No players to swap");
                return false;
            }
            printer.sendMessage(colors.yellow() + "Swapping " +
                    colors.blue() + "BLUE" +
                    colors.gray() + "(" + team1Swap.player().getInfo(colors) +
                    colors.gray() + ") " +
                    colors.red() + "RED" +
                    colors.gray() + "(" + team2Swap.player().getInfo(colors) +
                    colors.gray() + ") = (" +
                    colors.lightPurple() + Balancer.WEIGHT_FORMAT.format(Math.abs(team1Swap.player().weight() - team2Swap.player().weight())) +
                    colors.gray() + ")");
            team1Swap.debuggedMessages().add(c -> colors.yellow() + "SWAPPED #" + (index + 1));
            team2Swap.debuggedMessages().add(c -> colors.yellow() + "SWAPPED #" + (index + 1));
            teamBalanceInfo1.removePlayer(team1Swap);
            teamBalanceInfo2.removePlayer(team2Swap);
            teamBalanceInfo1.addPlayer(team2Swap);
            teamBalanceInfo2.addPlayer(team1Swap);
            return true;
        }

    },
    /**
     * Swaps groups of players with matching spec types between the two teams to even out the total weight between the teams.
     * <p>- First gets the spec type with the most difference in weight.</p>
     * <p>- Then ensures all players of the other spec types which combine to have the lowest spec type weight goes on that team.</p>
     * <p>Example: </p>
     * <p>BLUE = (0, 100, 50), RED = (50, 0, 0) | 100 is the highest diff</p>
     * <p>BLUE = (0, 100, 0), RED = (50, 0, 50)</p>
     */
    SWAP_TEAM_SPEC_TYPES {
        @Override
        public boolean apply(Balancer.Printer printer, Map<Team, Balancer.TeamBalanceInfo> teamBalanceInfos) {
            Color colors = printer.colors();
            Set<Team> teams = teamBalanceInfos.keySet();
            if (teams.size() < 2) {
                printer.sendMessage(colors.darkRed() + "Not 2 teams");
                return false;
            }
            Team[] teamArray = teams.toArray(new Team[0]);
            Team team1 = teamArray[0];
            Team team2 = teamArray[1];
            return trySwapSpecTypeGroup(printer, teamBalanceInfos, team1, team2);
        }

        private boolean trySwapSpecTypeGroup(Balancer.Printer printer, Map<Team, Balancer.TeamBalanceInfo> teamBalanceInfos, Team team1, Team team2) {
            Balancer.TeamBalanceInfo teamBalanceInfo1 = teamBalanceInfos.get(team1);
            Balancer.TeamBalanceInfo teamBalanceInfo2 = teamBalanceInfos.get(team2);
            SpecType specType = ExtraBalanceFeature.getSpecTypeHighestWeightDiff(printer, printer.colors(), teamBalanceInfo1, teamBalanceInfo2);
            EnumSet<SpecType> otherSpecTypes = EnumSet.allOf(SpecType.class);
            otherSpecTypes.remove(specType);
            // get team with the highest weight of spec type
            double specTypeWeight1 = teamBalanceInfo1.getSpecTypeWeight(specType);
            double specTypeWeight2 = teamBalanceInfo2.getSpecTypeWeight(specType);
            Balancer.TeamBalanceInfo highestSpecTypeWeightTeam = specTypeWeight1 > specTypeWeight2 ? teamBalanceInfo1 : teamBalanceInfo2;
            printer.sendMessage(printer.colors().yellow() + "Highest spec type weight team: " +
                    (highestSpecTypeWeightTeam == teamBalanceInfo1 ? team1 : team2) + " " +
                    (highestSpecTypeWeightTeam == teamBalanceInfo1 ? Balancer.WEIGHT_FORMAT.format(specTypeWeight1) : Balancer.WEIGHT_FORMAT.format(specTypeWeight2)) + ">" +
                    (highestSpecTypeWeightTeam == teamBalanceInfo1 ? Balancer.WEIGHT_FORMAT.format(specTypeWeight2) : Balancer.WEIGHT_FORMAT.format(specTypeWeight1))
            );
            // make team with lowest weight of other spec types on the team with highest weight of spec type
            boolean applied = false;
            for (SpecType otherSpecType : otherSpecTypes) {
                List<Balancer.DebuggedPlayer> otherSpecTypePlayers1 = teamBalanceInfo1.getPlayersMatching(otherSpecType);
                List<Balancer.DebuggedPlayer> otherSpecTypePlayers2 = teamBalanceInfo2.getPlayersMatching(otherSpecType);
                double otherSpecTypeWeight1 = teamBalanceInfo1.getSpecTypeWeight(otherSpecType);
                double otherSpecTypeWeight2 = teamBalanceInfo2.getSpecTypeWeight(otherSpecType);
                printer.sendMessage(printer.colors().yellow() + otherSpecType + " Weight team 1: " + printer.colors().lightPurple() + Balancer.WEIGHT_FORMAT.format(
                        otherSpecTypeWeight1));
                printer.sendMessage(printer.colors().yellow() + otherSpecType + " Weight team 2: " + printer.colors().lightPurple() + Balancer.WEIGHT_FORMAT.format(
                        otherSpecTypeWeight2));
                boolean swapGroupToTeam2 = highestSpecTypeWeightTeam == teamBalanceInfo1 && otherSpecTypeWeight1 > otherSpecTypeWeight2;
                boolean swapGroupToTeam1 = highestSpecTypeWeightTeam == teamBalanceInfo2 && otherSpecTypeWeight2 > otherSpecTypeWeight1;
                boolean shouldSwap = swapGroupToTeam2 || swapGroupToTeam1;
                if (!shouldSwap) {
                    printer.sendMessage(printer.colors().yellow() + "Not swapping " + otherSpecType + " group");
                    continue;
                }
                printer.sendMessage(printer.colors().yellow() + "Swapping " + otherSpecType + " group");
                for (Balancer.DebuggedPlayer debuggedPlayer : otherSpecTypePlayers1) {
                    teamBalanceInfo1.removePlayer(debuggedPlayer);
                    teamBalanceInfo2.addPlayer(debuggedPlayer);
                }
                for (Balancer.DebuggedPlayer debuggedPlayer : otherSpecTypePlayers2) {
                    teamBalanceInfo2.removePlayer(debuggedPlayer);
                    teamBalanceInfo1.addPlayer(debuggedPlayer);
                }
                applied = true;
            }
            return applied;
        }
    };

    private static SpecType getSpecTypeHighestWeightDiff(
            Balancer.Printer printer,
            Color colors,
            Balancer.TeamBalanceInfo teamBalanceInfo1,
            Balancer.TeamBalanceInfo teamBalanceInfo2
    ) {
        if (teamBalanceInfo1 == null || teamBalanceInfo2 == null) {
            printer.sendMessage(colors.darkRed() + "One of the teams is null");
            return null;
        }
        Map<SpecType, Double> specTypeWeightDiff = new HashMap<>();
        for (SpecType value : SpecType.VALUES) {
            double weight1 = teamBalanceInfo1.getSpecTypeWeight(value);
            double weight2 = teamBalanceInfo2.getSpecTypeWeight(value);
            specTypeWeightDiff.put(value, Math.abs(weight1 - weight2));
        }
        printer.sendMessage(colors.gray() + "-----------");
        specTypeWeightDiff.forEach((specType, diff) -> printer.sendMessage(colors.yellow() + specType + " Weight Diff: " + colors.lightPurple() + Balancer.WEIGHT_FORMAT.format(diff)));
        printer.sendMessage(colors.gray() + "-----------");
        // get the spec type with the most difference in weight
        SpecType specType = null;
        double highestWeightDiff = 0;
        for (Map.Entry<SpecType, Double> entry : specTypeWeightDiff.entrySet()) {
            if (entry.getValue() > highestWeightDiff) {
                highestWeightDiff = entry.getValue();
                specType = entry.getKey();
            }
        }
        return specType;
    }

    /**
     * @param printer          the printer to send messages to
     * @param teamBalanceInfos the team balance infos
     * @return true if the feature was applied
     */
    public abstract boolean apply(Balancer.Printer printer, Map<Team, Balancer.TeamBalanceInfo> teamBalanceInfos);

}
