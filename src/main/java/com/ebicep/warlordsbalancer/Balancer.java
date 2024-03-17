package com.ebicep.warlordsbalancer;


import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;

public class Balancer {

    public static final DecimalFormat WEIGHT_FORMAT = new DecimalFormat("#.##");
    public static final double MAX_WEIGHT = 4;
    public static final double MIN_WEIGHT = .43;
    private static final List<Filter> FILTERS = List.of(
            (Filter.SpecificationFilter) () -> Specialization.DEFENDER,
            (Filter.SpecificationFilter) () -> Specialization.CRYOMANCER,
            (Filter.SpecTypeFilter) () -> SpecType.TANK,
            (Filter.SpecTypeFilter) () -> SpecType.DAMAGE,
            (Filter.SpecTypeFilter) () -> SpecType.HEALER
    );


    public static void balance(
            BalanceMethod balanceMethod,
            RandomWeightMethod randomWeightMethod,
            ExtraBalanceFeature... extraBalanceFeatures
    ) {
        List<ExtraBalanceFeature> features = List.of(extraBalanceFeatures);
        balance(new Printer(System.out::println, new Color() {}),
                1_000_000,
                22,
                balanceMethod,
                randomWeightMethod,
                features.isEmpty() ? EnumSet.noneOf(ExtraBalanceFeature.class) : EnumSet.copyOf(features)
        );
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
            // printing list of players to be balanced in order
//            players.stream()
//                   .sorted(Comparator.<Player>comparingInt(player -> {
//                       int index = 0;
//                       for (Predicate<Player> filter : FILTERS) {
//                           if (filter.test(player)) {
//                               return index;
//                           }
//                           index++;
//                       }
//                       return index;
//                   }).thenComparingDouble(player -> -player.weight))
//                   .forEachOrdered(player -> sendMessage.accept("  " + player.getInfo(colors)));
//            sendMessage.accept(colors.white() + "-------------------------------------------------");
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
        Map<Team, TeamBalanceInfo> teams = new LinkedHashMap<>();
        for (Team team : Team.VALUES) {
            teams.put(team, new TeamBalanceInfo());
        }

        balanceMethod.balance(players, FILTERS, teams);

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
                                                                            .filter(debuggedPlayer -> debuggedPlayer.player.spec.specType == specType)
                                                                            .mapToDouble(debuggedPlayer -> debuggedPlayer.player.weight)
                                                                            .sum();
                        return specType.getColor.apply(colors) + specType +
                                colors.gray() + ": " +
                                colors.green() + entry.getValue() +
                                colors.gray() + " (" + colors.lightPurple() + WEIGHT_FORMAT.format(totalSpecTypeWeight) + colors.gray() + ")" +
                                colors.gray() + " (" + colors.darkPurple() + WEIGHT_FORMAT.format(totalSpecTypeWeight / teamBalanceInfo.totalWeight * 100) + "%" + colors.gray() + ")";
                    })
                    .forEachOrdered(s -> sendMessage.accept("  " + s));
            sendMessage.accept(colors.gray() + "  -----------------------");
            List<DebuggedPlayer> players = teamBalanceInfo.players;
            for (DebuggedPlayer debuggedPlayer : players) {
                sendMessage.accept("  " + debuggedPlayer.getInfo(colors));
            }
        });
    }


    interface DebuggedMessage {
        String getMessage(Color colors);
    }

    interface Filter {
        boolean test(Player player);

        interface SpecTypeFilter extends Filter {
            @Override
            default boolean test(Player player) {
                return player.spec().specType == specType();
            }

            SpecType specType();
        }

        interface SpecificationFilter extends Filter {
            @Override
            default boolean test(Player player) {
                return player.spec() == spec();
            }

            Specialization spec();
        }

    }

    static class TeamBalanceInfo {
        final List<DebuggedPlayer> players = new ArrayList<>();
        final Map<SpecType, Integer> specTypeCount = new HashMap<>();
        double totalWeight = 0;

        public void addPlayer(DebuggedPlayer debuggedPlayer) {
            players.add(debuggedPlayer);
            specTypeCount.merge(debuggedPlayer.player.spec.specType, 1, Integer::sum);
            totalWeight += debuggedPlayer.player.weight;
        }

        public double getSpecTypeWeight(SpecType specType) {
            return players.stream()
                          .filter(debuggedPlayer -> debuggedPlayer.player.spec.specType == specType)
                          .mapToDouble(debuggedPlayer -> debuggedPlayer.player.weight)
                          .sum();
        }

        private void recalculateTotalWeight() {
            totalWeight = players.stream().mapToDouble(debuggedPlayer -> debuggedPlayer.player.weight).sum();
        }
    }

    record DebuggedPlayer(Player player, List<DebuggedMessage> debuggedMessages) {
        public DebuggedPlayer(Player player, DebuggedMessage... messages) {
            this(player, new ArrayList<>(List.of(messages)));
        }

        public String getInfo(Color colors) {
            StringBuilder info = new StringBuilder(player.getInfo(colors));
            for (DebuggedMessage debuggedMessage : debuggedMessages) {
                info.append(colors.gray()).append(" (");
                info.append(debuggedMessage.getMessage(colors));
                info.append(colors.gray()).append(")");
            }
            return info.toString();
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