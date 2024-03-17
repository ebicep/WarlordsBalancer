package com.ebicep.warlordsbalancer;

import java.util.*;

enum ExtraBalanceFeature {

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
                for (Balancer.DebuggedPlayer player : maxPlayersTeamInfo.players) {
                    Balancer.Player p = player.player();
                    if (p.spec().specType != specType) {
                        continue;
                    }
                    double weight = p.weight();
                    double newLowestWeightDiff = Math.abs(weightDiff - weight * 2);
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
            maxPlayersTeamInfo.removePlayer(playerToMove);
            minPlayersTeamInfo.addPlayer(playerToMove);
            return true;
        }
    },
    SWAP_SPEC_TYPES {
        @Override
        public boolean apply(Balancer.Printer printer, Map<Team, Balancer.TeamBalanceInfo> teamBalanceInfos) {
            Color colors = printer.colors();
            if (teamBalanceInfos.keySet().size() != 2) {
                printer.sendMessage(colors.darkRed() + "Can only swap between 2 teams (not gonna try to swap more)");
                return false;
            }
            Balancer.TeamBalanceInfo blueBalanceInfo = teamBalanceInfos.get(Team.BLUE);
            Balancer.TeamBalanceInfo redBalanceInfo = teamBalanceInfos.get(Team.RED);
            Map<SpecType, Double> specTypeWeightDiffs = new HashMap<>();
            List<SpecType> specTypes = new ArrayList<>(List.of(SpecType.VALUES));
            double previousTeamWeightDiff = Double.MAX_VALUE;
            for (int i = 0; i < 10; i++) {
                specTypeWeightDiffs.clear();
                for (SpecType specType : specTypes) {
                    specTypeWeightDiffs.put(specType, Math.abs(blueBalanceInfo.getSpecTypeWeight(specType) - redBalanceInfo.getSpecTypeWeight(specType)));
                }
                // try to swap spec type with highest diff
                SpecType specTypeToSwap = specTypeWeightDiffs
                        .entrySet()
                        .stream()
                        .max(Comparator.comparingDouble(Map.Entry::getValue))
                        .map(Map.Entry::getKey)
                        .orElseThrow();
                // find player on blue/red to swap that has the spec type and would even out the weights the most
                Balancer.DebuggedPlayer bluePlayerToSwap = null;
                Balancer.DebuggedPlayer redPlayerToSwap = null;
                List<Balancer.DebuggedPlayer> bluePlayers = blueBalanceInfo.players;
                List<Balancer.DebuggedPlayer> redPlayers = redBalanceInfo.players;
                List<Balancer.DebuggedPlayer> swappableBluePlayers = bluePlayers
                        .stream()
                        .filter(debuggedPlayer -> debuggedPlayer.player().spec().specType == specTypeToSwap)
                        .toList();
                List<Balancer.DebuggedPlayer> swappableRedPlayers = redPlayers
                        .stream()
                        .filter(debuggedPlayer -> debuggedPlayer.player().spec().specType == specTypeToSwap)
                        .toList();
                double weightDiff = blueBalanceInfo.totalWeight - redBalanceInfo.totalWeight;
                double newTotalBlueWeight = 0;
                double newTotalRedWeight = 0;
                for (Balancer.DebuggedPlayer swappableBluePlayer : swappableBluePlayers) {
                    for (Balancer.DebuggedPlayer swappableRedPlayer : swappableRedPlayers) {
                        newTotalBlueWeight = blueBalanceInfo.totalWeight - swappableBluePlayer.player().weight() + swappableRedPlayer.player().weight();
                        newTotalRedWeight = redBalanceInfo.totalWeight - swappableRedPlayer.player().weight() + swappableBluePlayer.player().weight();
                        if (Math.abs(newTotalBlueWeight - newTotalRedWeight) < weightDiff) {
                            weightDiff = Math.abs(newTotalBlueWeight - newTotalRedWeight);
                            bluePlayerToSwap = swappableBluePlayer;
                            redPlayerToSwap = swappableRedPlayer;
                        }
                    }
                }
                if (bluePlayerToSwap == null) {
                    specTypes.remove(specTypeToSwap);
                    if (specTypes.isEmpty()) {
                        break;
                    }
                    continue;
                }
                double newTotalWeightDiff = Math.abs(newTotalBlueWeight - newTotalRedWeight);
                if (newTotalWeightDiff >= previousTeamWeightDiff) {
                    break;
                }
                previousTeamWeightDiff = newTotalWeightDiff;
                printer.sendMessage(colors.yellow() + "Swapping " +
                        colors.blue() + "BLUE" +
                        colors.gray() + "(" + bluePlayerToSwap.player().getInfo(colors) +
                        colors.gray() + ") " +
                        colors.red() + "RED" +
                        colors.gray() + "(" + redPlayerToSwap.player().getInfo(colors) +
                        colors.gray() + ") = (" +
                        colors.lightPurple() + Balancer.WEIGHT_FORMAT.format(Math.abs(bluePlayerToSwap.player().weight() - redPlayerToSwap.player().weight())) +
                        colors.gray() + ")");
                bluePlayers.add(bluePlayers.indexOf(bluePlayerToSwap), redPlayerToSwap);
                bluePlayers.remove(bluePlayerToSwap);
                blueBalanceInfo.totalWeight = newTotalBlueWeight;
                redPlayers.add(redPlayers.indexOf(redPlayerToSwap), bluePlayerToSwap);
                redPlayers.remove(redPlayerToSwap);
                redBalanceInfo.totalWeight = newTotalRedWeight;
            }
            return true;
        }
    };

    /**
     * @param printer          the printer to send messages to
     * @param teamBalanceInfos the team balance infos
     * @return true if the feature was applied
     */
    public abstract boolean apply(Balancer.Printer printer, Map<Team, Balancer.TeamBalanceInfo> teamBalanceInfos);
}
