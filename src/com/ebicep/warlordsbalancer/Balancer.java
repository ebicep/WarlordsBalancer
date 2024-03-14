package main.java;

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
    private static final BalanceMethod BALANCER_VERSION = BalanceMethod.V2;
    private static final RandomWeightMethod RANDOM_WEIGHT_METHOD = RandomWeightMethod.NORMAL_DISTRIBUTION;


    public static void main(String[] args) {

        double maxWeightDiff = 0;
        Map<Team, TeamBalanceInfo> mostUnbalancedTeam = new HashMap<>();
        for (int i = 0; i < 1_000_000; i++) {
            Set<Player> players = new HashSet<>();
            for (int j = 0; j < 22; j++) {
                players.add(new Player(Specialization.getRandomSpec(), ThreadLocalRandom.current().nextDouble(MIN_WEIGHT, MAX_WEIGHT)));
            }
            Map<Team, TeamBalanceInfo> teams = getBalancedTeams(players, BALANCER_VERSION);
            double weightDiff = teams.get(Team.RED).totalWeight - teams.get(Team.BLUE).totalWeight;
            if (weightDiff > maxWeightDiff) {
                maxWeightDiff = weightDiff;
                mostUnbalancedTeam = teams;
            }
        }
        System.out.println("Max Weight Diff: " + maxWeightDiff);
        printBalanceInfo(mostUnbalancedTeam);
    }

    private static Map<Team, TeamBalanceInfo> getBalancedTeams(Set<Player> players, BalanceMethod balanceMethod) {
        int amountOfPlayers = players.size();
        int maxPerTeam = amountOfPlayers / 2 + (amountOfPlayers % 2);

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
                       Team team;
                       // check if any team is amount of players / 2 if so then just assign to other team
                       if (balanceMethod == BalanceMethod.V1 && teams.values().stream().anyMatch(teamBalanceInfo -> teamBalanceInfo.players.size() == maxPerTeam)) {
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
                    .forEachOrdered(s -> {
                        System.out.println("  " + s);
                    });
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

    enum BalanceMethod {
        V1, V2
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
                return ThreadLocalRandom.current().nextGaussian() * (MAX_WEIGHT - MIN_WEIGHT) / 2 + (MAX_WEIGHT + MIN_WEIGHT) / 2;
            }
        },
        ;
        public abstract double generateRandomWeight();
    }

    enum Team {
        RED, BLUE,

        ;

        public static final Team[] VALUES = values();
    }

    enum Specialization {
        PYROMANCER(SpecType.DAMAGE), CRYOMANCER(SpecType.TANK), AQUAMANCER(SpecType.HEALER),
        BERSERKER(SpecType.DAMAGE), DEFENDER(SpecType.TANK), REVENANT(SpecType.HEALER),
        AVENGER(SpecType.DAMAGE), CRUSADER(SpecType.TANK), PROTECTOR(SpecType.HEALER),
        THUNDERLORD(SpecType.DAMAGE), SPIRITGUARD(SpecType.TANK), EARTHWARDEN(SpecType.HEALER),

        ;

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
        DAMAGE, TANK, HEALER,
    }

    static class TeamBalanceInfo {
        private List<Player> players = new ArrayList<>();
        private double totalWeight = 0;
        private Map<SpecType, Integer> specTypeCount = new HashMap<>();
    }

    record Player(Specialization spec, double weight) {
        @Override
        public String toString() {
            return spec + " - " + WEIGHT_FORMAT.format(weight);
        }
    }

}