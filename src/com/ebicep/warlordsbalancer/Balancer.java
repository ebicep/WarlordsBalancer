package com.ebicep.warlordsbalancer;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class Balancer {

    private static final DecimalFormat WEIGHT_FORMAT = new DecimalFormat("#.##");
    private static final double MAX_WEIGHT = 4;
    private static final double MIN_WEIGHT = .43;
    private static final List<Predicate<Player>> FILTERS = List.of(
            player -> player.spec == Specialization.DEFENDER,
            player -> player.spec == Specialization.CRYOMANCER,
            player -> player.spec.specType == SpecType.TANK,
            player -> player.spec.specType == SpecType.DAMAGE,
            player -> player.spec.specType == SpecType.HEALER
    );

    public static void balance(BalanceMethod balanceMethod, RandomWeightMethod randomWeightMethod, ExtraBalanceFeature... extraBalanceFeatures) {
        System.out.println("Balance Method: " + balanceMethod);
        System.out.println("Random Weight Method: " + randomWeightMethod);
        System.out.println("Extra Balance Features: " + Arrays.toString(extraBalanceFeatures));
        double maxWeightDiff = 0;
        Map<Team, TeamBalanceInfo> mostUnbalancedTeam = new HashMap<>();
        for (int i = 0; i < 1_000_000; i++) {
            Set<Player> players = new HashSet<>();
            for (int j = 0; j < 22; j++) {
                players.add(new Player(Specialization.getRandomSpec(), randomWeightMethod.generateRandomWeight()));
            }
            Map<Team, TeamBalanceInfo> teams = getBalancedTeams(players, balanceMethod);
            double weightDiff = Math.abs(teams.get(Team.BLUE).totalWeight - teams.get(Team.RED).totalWeight);
            if (weightDiff > maxWeightDiff) {
                maxWeightDiff = weightDiff;
                mostUnbalancedTeam = teams;
            }
        }
        System.out.println("Max Weight Diff: " + maxWeightDiff);
        printBalanceInfo(mostUnbalancedTeam);

        System.out.println("---------------------------------------------------------");
        for (ExtraBalanceFeature extraBalanceFeature : extraBalanceFeatures) {
            System.out.println("Extra Balance Feature: " + extraBalanceFeature);
            extraBalanceFeature.apply(mostUnbalancedTeam);
            TeamBalanceInfo blueBalanceInfo = mostUnbalancedTeam.get(Team.BLUE);
            TeamBalanceInfo redBalanceInfo = mostUnbalancedTeam.get(Team.RED);
            blueBalanceInfo.recalculateTotalWeight();
            redBalanceInfo.recalculateTotalWeight();
            maxWeightDiff = Math.abs(blueBalanceInfo.totalWeight - redBalanceInfo.totalWeight);
            System.out.println("Max Weight Diff: " + maxWeightDiff);
            printBalanceInfo(mostUnbalancedTeam);
            System.out.println("--------------------------------");
        }
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

    private static void printBalanceInfo(Map<Team, TeamBalanceInfo> teams) {
        teams.forEach((team, teamBalanceInfo) -> {
            System.out.println("-----------------------");
            System.out.println(team + " - " + WEIGHT_FORMAT.format(teamBalanceInfo.totalWeight));
            teamBalanceInfo.specTypeCount
                    .entrySet()
                    .stream()
                    .sorted(Comparator.comparingInt(o -> o.getKey().ordinal()))
                    .map(entry -> {
                        double totalSpecTypeWeight = teamBalanceInfo.players.stream()
                                                                            .filter(player -> player.spec.specType == entry.getKey())
                                                                            .mapToDouble(player -> player.weight)
                                                                            .sum();
                        return entry.getKey() + ": " + entry.getValue() + " (" + WEIGHT_FORMAT.format(totalSpecTypeWeight) + ") (" + WEIGHT_FORMAT.format(totalSpecTypeWeight / teamBalanceInfo.totalWeight * 100) + "%)";
                    })
                    .forEachOrdered(s -> System.out.println("  " + s));
            SpecType previousType = null;
            for (Player teamPlayer : teamBalanceInfo.players) {
                if (previousType != teamPlayer.spec.specType) {
                    previousType = teamPlayer.spec.specType;
                    System.out.println("  -----------");
                }
                System.out.println("  " + teamPlayer);
            }
        });
    }


    enum ExtraBalanceFeature {
        SWAP {
            @Override
            public void apply(Map<Team, TeamBalanceInfo> teamBalanceInfos) {
                if (teamBalanceInfos.keySet().size() != 2) {
                    throw new IllegalArgumentException("Can only swap between 2 teams (not gonna try to swap more)");
                }
                TeamBalanceInfo blueBalanceInfo = teamBalanceInfos.get(Team.BLUE);
                TeamBalanceInfo redBalanceInfo = teamBalanceInfos.get(Team.RED);
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
                    Player bluePlayerToSwap = null;
                    Player redPlayerToSwap = null;
                    List<Player> bluePlayers = blueBalanceInfo.players;
                    List<Player> redPlayers = redBalanceInfo.players;
                    List<Player> swappableBluePlayers = bluePlayers
                            .stream()
                            .filter(player -> player.spec.specType == specTypeToSwap)
                            .toList();
                    List<Player> swappableRedPlayers = redPlayers
                            .stream()
                            .filter(player -> player.spec.specType == specTypeToSwap)
                            .toList();
                    double weightDiff = blueBalanceInfo.totalWeight - redBalanceInfo.totalWeight;
                    double newTotalBlueWeight = 0;
                    double newTotalRedWeight = 0;
                    for (Player swappableBluePlayer : swappableBluePlayers) {
                        for (Player swappableRedPlayer : swappableRedPlayers) {
                            newTotalBlueWeight = blueBalanceInfo.totalWeight - swappableBluePlayer.weight + swappableRedPlayer.weight;
                            newTotalRedWeight = redBalanceInfo.totalWeight - swappableRedPlayer.weight + swappableBluePlayer.weight;
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
                    System.out.println("Swapping BLUE(" + bluePlayerToSwap + ") RED(" + redPlayerToSwap + ") = (" + WEIGHT_FORMAT.format(Math.abs(bluePlayerToSwap.weight - redPlayerToSwap.weight)));
                    bluePlayers.add(bluePlayers.indexOf(bluePlayerToSwap), redPlayerToSwap);
                    bluePlayers.remove(bluePlayerToSwap);
                    blueBalanceInfo.totalWeight = newTotalBlueWeight;
                    redPlayers.add(redPlayers.indexOf(redPlayerToSwap), bluePlayerToSwap);
                    redPlayers.remove(redPlayerToSwap);
                    redBalanceInfo.totalWeight = newTotalRedWeight;
                }
            }
        };

        public abstract void apply(Map<Team, TeamBalanceInfo> teamBalanceInfos);
    }

    enum BalanceMethod {
        V1 {
            @Override
            public Team getTeam(int amountOfPlayers, Map<Team, TeamBalanceInfo> teams) {
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
            public Team getTeam(int amountOfPlayers, Map<Team, TeamBalanceInfo> teams) {
                return teams.entrySet()
                            .stream()
                            .min(Comparator.comparingInt(entry -> entry.getValue().players.size()))
                            .map(Map.Entry::getKey)
                            .orElse(Team.RED);
            }
        };

        public abstract Team getTeam(int amountOfPlayers, Map<Team, TeamBalanceInfo> teams);
    }

    enum RandomWeightMethod {
        RANDOM {
            @Override
            public double generateRandomWeight() {
                return ThreadLocalRandom.current().nextDouble(MIN_WEIGHT, MAX_WEIGHT);
            }
        },
        NORMAL_DISTRIBUTION {
            @Override
            public double generateRandomWeight() {
                return clamp(ThreadLocalRandom.current().nextGaussian() * STANDARD_DEVIATION + MEAN);
            }
        },

        ;

        private static final double MEAN = (MAX_WEIGHT + MIN_WEIGHT) / 2;
        private static final double STANDARD_DEVIATION = 1.2;

        private static double clamp(double value) {
            return Math.max(MIN_WEIGHT, Math.min(MAX_WEIGHT, value));
        }

        public abstract double generateRandomWeight();
    }

    enum Team {
        RED, BLUE;
        public static final Team[] VALUES = values();
    }

    enum Specialization {
        PYROMANCER(SpecType.DAMAGE), CRYOMANCER(SpecType.TANK), AQUAMANCER(SpecType.HEALER),
        BERSERKER(SpecType.DAMAGE), DEFENDER(SpecType.TANK), REVENANT(SpecType.HEALER),
        AVENGER(SpecType.DAMAGE), CRUSADER(SpecType.TANK), PROTECTOR(SpecType.HEALER),
        THUNDERLORD(SpecType.DAMAGE), SPIRITGUARD(SpecType.TANK), EARTHWARDEN(SpecType.HEALER);
        public static final Specialization[] VALUES = values();

        public static Specialization getRandomSpec() {
            return VALUES[ThreadLocalRandom.current().nextInt(VALUES.length)];
        }

        public final SpecType specType;

        Specialization(SpecType specType) {
            this.specType = specType;
        }
    }

    enum SpecType {
        DAMAGE, TANK, HEALER;
        public static final SpecType[] VALUES = values();
    }

    static class TeamBalanceInfo {
        private final List<Player> players = new ArrayList<>();
        private final Map<SpecType, Integer> specTypeCount = new HashMap<>();
        private double totalWeight = 0;

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
        @Override
        public String toString() {
            return spec + " - " + WEIGHT_FORMAT.format(weight);
        }
    }

}