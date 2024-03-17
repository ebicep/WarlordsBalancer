package com.ebicep.warlordsbalancer;

import java.util.*;
import java.util.function.Consumer;

enum ExtraBalanceFeature {
    SWAP {
        @Override
        public void apply(Balancer.Printer printer, Map<Team, Balancer.TeamBalanceInfo> teamBalanceInfos) {
            Consumer<String> sendMessage = printer.sendMessage();
            Color colors = printer.colors();
            if (teamBalanceInfos.keySet().size() != 2) {
                sendMessage.accept(colors.darkRed() + "Can only swap between 2 teams (not gonna try to swap more)");
                return;
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
                sendMessage.accept(colors.yellow() + "Swapping " +
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
        }
    };

    public abstract void apply(Balancer.Printer printer, Map<Team, Balancer.TeamBalanceInfo> teamBalanceInfos);
}
