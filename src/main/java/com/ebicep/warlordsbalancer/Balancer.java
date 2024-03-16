package com.ebicep.warlordsbalancer;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Balancer {

    public static final DecimalFormat WEIGHT_FORMAT = new DecimalFormat("#.##");
    public static final double MAX_WEIGHT = 4;
    public static final double MIN_WEIGHT = .43;
    public static final List<Predicate<Player>> FILTERS = List.of(
            player -> player.spec == Specialization.DEFENDER,
            player -> player.spec == Specialization.CRYOMANCER,
            player -> player.spec.specType == SpecType.TANK,
            player -> player.spec.specType == SpecType.DAMAGE,
            player -> player.spec.specType == SpecType.HEALER
    );

    public static void balance(
            BalanceMethod balanceMethod,
            RandomWeightMethod randomWeightMethod,
            ExtraBalanceFeature... extraBalanceFeatures
    ) {
        balance(new Printer(s -> {}, new Color() {}), 1_000_000, 22, balanceMethod, randomWeightMethod, EnumSet.copyOf(List.of(extraBalanceFeatures)));
    }

    public static void balance(
            Printer printer,
            int iterations,
            int playerCount,
            BalanceMethod balanceMethod,
            RandomWeightMethod randomWeightMethod,
            EnumSet<ExtraBalanceFeature> extraBalanceFeatures
    ) {
        Consumer<String> sendMessage = printer.sendMessage;
        Color colors = printer.colors;
        sendMessage.accept(colors.white() + "-------------------------------------------------");
        sendMessage.accept(colors.white() + "-------------------------------------------------");
        sendMessage.accept(colors.gray() + "Balance Method: " + colors.green() + balanceMethod);
        sendMessage.accept(colors.gray() + "Random Weight Method: " + colors.green() + randomWeightMethod);
        sendMessage.accept(colors.gray() + "Extra Balance Features: " + colors.green() + extraBalanceFeatures);
        double maxWeightDiff = 0;
        Map<Team, TeamBalanceInfo> mostUnbalancedTeam = new HashMap<>();
        for (int i = 0; i < iterations; i++) {
            Set<Player> players = new HashSet<>();
            for (int j = 0; j < playerCount; j++) {
                players.add(new Player(Specialization.getRandomSpec(), randomWeightMethod.generateRandomWeight()));
            }
            Map<Team, TeamBalanceInfo> teams = getBalancedTeams(players, balanceMethod);
            double weightDiff = Math.abs(teams.get(Team.BLUE).totalWeight - teams.get(Team.RED).totalWeight);
            if (weightDiff > maxWeightDiff) {
                maxWeightDiff = weightDiff;
                mostUnbalancedTeam = teams;
            }
        }
        sendMessage.accept(colors.gray() + "Max Weight Diff: " + colors.darkPurple() + WEIGHT_FORMAT.format(maxWeightDiff));
        printBalanceInfo(printer, mostUnbalancedTeam);

        if (!extraBalanceFeatures.isEmpty()) {
            sendMessage.accept(colors.white() + "-------------------------------------------------");
        }
        for (ExtraBalanceFeature extraBalanceFeature : extraBalanceFeatures) {
            sendMessage.accept(colors.yellow() + "Extra Balance Feature: " + extraBalanceFeature);
            extraBalanceFeature.apply(printer, mostUnbalancedTeam);
            TeamBalanceInfo blueBalanceInfo = mostUnbalancedTeam.get(Team.BLUE);
            TeamBalanceInfo redBalanceInfo = mostUnbalancedTeam.get(Team.RED);
            blueBalanceInfo.recalculateTotalWeight();
            redBalanceInfo.recalculateTotalWeight();
            maxWeightDiff = Math.abs(blueBalanceInfo.totalWeight - redBalanceInfo.totalWeight);
            sendMessage.accept(colors.gray() + "Max Weight Diff: " + colors.darkPurple() + WEIGHT_FORMAT.format(maxWeightDiff));
            printBalanceInfo(printer, mostUnbalancedTeam);
            sendMessage.accept(colors.gray() + "--------------------------------");
        }
        sendMessage.accept(colors.white() + "-------------------------------------------------");
        sendMessage.accept(colors.white() + "-------------------------------------------------");
    }

    private static Map<Team, TeamBalanceInfo> getBalancedTeams(Set<Player> players, BalanceMethod balanceMethod) {
        int amountOfPlayers = players.size();

        Map<Team, TeamBalanceInfo> teams = new HashMap<>();
        for (Team team : Team.VALUES) {
            teams.put(team, new TeamBalanceInfo());
        }

        for (Predicate<Player> filter : FILTERS) {
            players.stream()
                   .filter(filter)
                   .sorted(Comparator.comparingDouble(player -> -player.weight))
                   .forEachOrdered(player -> {
                       players.remove(player);
                       Team team = balanceMethod.getTeam(amountOfPlayers, teams);
                       TeamBalanceInfo teamBalanceInfo = teams.get(team);
                       teamBalanceInfo.players.add(player);
                       teamBalanceInfo.totalWeight += player.weight;
                       teamBalanceInfo.specTypeCount.merge(player.spec.specType, 1, Integer::sum);
                   });
        }

        return teams;
    }

    private static void printBalanceInfo(Printer printer, Map<Team, TeamBalanceInfo> teams) {
        Consumer<String> sendMessage = printer.sendMessage;
        Color colors = printer.colors;
        teams.forEach((team, teamBalanceInfo) -> {
            sendMessage.accept(colors.gray() + "-----------------------");
            sendMessage.accept(team.getColor.apply(colors) + team + colors.gray() + " - " + colors.darkPurple() + WEIGHT_FORMAT.format(teamBalanceInfo.totalWeight));
            teamBalanceInfo.specTypeCount
                    .entrySet()
                    .stream()
                    .sorted(Comparator.comparingInt(o -> o.getKey().ordinal()))
                    .map(entry -> {
                        SpecType specType = entry.getKey();
                        double totalSpecTypeWeight = teamBalanceInfo.players.stream()
                                                                            .filter(player -> player.spec.specType == specType)
                                                                            .mapToDouble(player -> player.weight)
                                                                            .sum();
                        return specType.getColor.apply(colors) + specType +
                                colors.gray() + ": " +
                                colors.green() + entry.getValue() +
                                colors.gray() + " (" + colors.lightPurple() + WEIGHT_FORMAT.format(totalSpecTypeWeight) + colors.gray() + ")" +
                                colors.gray() + " (" + colors.darkPurple() + WEIGHT_FORMAT.format(totalSpecTypeWeight / teamBalanceInfo.totalWeight * 100) + "%" + colors.gray() + ")";
                    })
                    .forEachOrdered(s -> sendMessage.accept("  " + s));
            for (Player teamPlayer : teamBalanceInfo.players) {
                sendMessage.accept("  " + teamPlayer.getInfo(colors));
            }
        });
    }


    static class TeamBalanceInfo {
        final List<Player> players = new ArrayList<>();
        final Map<SpecType, Integer> specTypeCount = new HashMap<>();
        double totalWeight = 0;

        public double getSpecTypeWeight(SpecType specType) {
            return players.stream()
                          .filter(player -> player.spec.specType == specType)
                          .mapToDouble(player -> player.weight)
                          .sum();
        }

        private void recalculateTotalWeight() {
            totalWeight = players.stream().mapToDouble(player -> player.weight).sum();
        }
    }

    record Player(Specialization spec, double weight) {
        public String getInfo(Color colors) {
            return spec.specType.getColor.apply(colors) + spec + colors.gray() + " - " + colors.lightPurple() + WEIGHT_FORMAT.format(weight);
        }
    }

    record Printer(Consumer<String> sendMessage, Color colors) {

    }

}