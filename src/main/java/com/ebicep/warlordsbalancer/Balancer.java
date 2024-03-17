package com.ebicep.warlordsbalancer;


import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
        Color colors = printer.colors;
        printer.sendMessage(colors.white() + "-------------------------------------------------");
        printer.sendMessage(colors.white() + "-------------------------------------------------");
        printer.sendMessage(colors.gray() + "Iterations: " + colors.green() + iterations);
        printer.sendMessage(colors.gray() + "Player Count: " + colors.green() + playerCount);
        printer.sendMessage(colors.gray() + "Balance Method: " + colors.green() + balanceMethod);
        printer.sendMessage(colors.gray() + "Random Weight Method: " + colors.green() + randomWeightMethod);
        printer.sendMessage(colors.gray() + "Extra Balance Features: " + colors.green() + extraBalanceFeatures);
        double totalWeightDiff = 0;
        double maxWeightDiff = 0;
        Map<Team, TeamBalanceInfo> mostUnbalancedTeam = new HashMap<>();
        for (int i = 0; i < iterations; i++) {
            Set<Player> players = new HashSet<>();
            for (int j = 0; j < playerCount; j++) {
                players.add(new Player(Specialization.getRandomSpec(), randomWeightMethod.generateRandomWeight()));
            }
            if (iterations == 1) {
                // printing list of players to be balanced in order
                players.stream()
                       .sorted(Comparator.<Player>comparingInt(player -> {
                           int index = 0;
                           for (Filter filter : FILTERS) {
                               if (filter.test(player)) {
                                   return index;
                               }
                               index++;
                           }
                           return index;
                       }).thenComparingDouble(player -> -player.weight))
                       .forEachOrdered(player -> printer.sendMessage("  " + player.getInfo(colors)));
                printer.sendMessage(colors.white() + "-------------------------------------------------");
            }
            Map<Team, TeamBalanceInfo> teams = getBalancedTeams(players, balanceMethod);
            if (iterations == 1) {
                printBalanceInfo(printer, teams);
            }
            printer.setEnabled(iterations == 1);
            printer.sendMessage(colors.white() + "-------------------------------------------------");
            double weightDiff = getMaxWeightDiff(teams);
            printer.sendMessage(colors.gray() + "Weight Diff: " + colors.darkPurple() + WEIGHT_FORMAT.format(weightDiff));
            printer.sendMessage(colors.white() + "-------------------------------------------------");
            for (ExtraBalanceFeature extraBalanceFeature : extraBalanceFeatures) {
                printer.sendMessage(colors.yellow() + "Extra Balance Feature: " + extraBalanceFeature);
                boolean applied = extraBalanceFeature.apply(printer, teams);
                if (!applied) {
                    printer.sendMessage(colors.yellow() + "No changes applied");
                    printer.sendMessage(colors.gray() + "--------------------------------");
                    continue;
                }
                weightDiff = getMaxWeightDiff(teams);
                printer.sendMessage(colors.gray() + "Max Weight Diff: " + colors.darkPurple() + WEIGHT_FORMAT.format(weightDiff));
                printBalanceInfo(printer, teams);
                printer.sendMessage(colors.gray() + "--------------------------------");
            }
            totalWeightDiff += weightDiff;
            if (weightDiff > maxWeightDiff) {
                maxWeightDiff = weightDiff;
                mostUnbalancedTeam = teams;
            }
        }
        printer.setEnabled(true);
        printer.sendMessage(colors.gray() + "Average Weight Diff: " + colors.darkPurple() + WEIGHT_FORMAT.format(totalWeightDiff / iterations));
        printer.sendMessage(colors.gray() + "Max Weight Diff: " + colors.darkPurple() + WEIGHT_FORMAT.format(maxWeightDiff));
        printBalanceInfo(printer, mostUnbalancedTeam);

        printer.sendMessage(colors.white() + "-------------------------------------------------");
        printer.sendMessage(colors.white() + "-------------------------------------------------");

        mostUnbalancedTeam.forEach((key, value) -> {
            for (DebuggedPlayer player : value.players) {
                printer.sendMessage("new Player(Specialization." + player.player.spec + ", " + WEIGHT_FORMAT.format(player.player.weight) + ")");
            }
        });
    }

    private static double getMaxWeightDiff(Map<Team, TeamBalanceInfo> teams) {
        double maxWeight = 0;
        double minWeight = Double.MAX_VALUE;
        for (TeamBalanceInfo teamBalanceInfo : teams.values()) {
            maxWeight = Math.max(maxWeight, teamBalanceInfo.totalWeight);
            minWeight = Math.min(minWeight, teamBalanceInfo.totalWeight);
        }
        return maxWeight - minWeight;
    }

    private static Map<Team, TeamBalanceInfo> getBalancedTeams(Set<Player> players, BalanceMethod balanceMethod) {
        Map<Team, TeamBalanceInfo> teams = new LinkedHashMap<>();
        for (Team team : Team.VALUES) {
            teams.put(team, new TeamBalanceInfo());
        }

        balanceMethod.balance(players, FILTERS, teams);

        return teams;
    }

    public static void printBalanceInfo(Printer printer, Map<Team, TeamBalanceInfo> teams) {
        Color colors = printer.colors;
        teams.forEach((team, teamBalanceInfo) -> {
            printer.sendMessage(colors.gray() + "-----------------------");
            printer.sendMessage(team.getColor.apply(colors) + team + colors.gray() + " - " + colors.darkPurple() + WEIGHT_FORMAT.format(teamBalanceInfo.totalWeight));
            teamBalanceInfo.specTypeCount
                    .entrySet()
                    .stream()
                    .sorted(Comparator.comparingInt(o -> o.getKey().ordinal()))
                    .map(entry -> {
                        SpecType specType = entry.getKey();
                        double totalSpecTypeWeight = teamBalanceInfo.getSpecTypeWeight(specType);
                        return specType.getColor.apply(colors) + specType +
                                colors.gray() + ": " +
                                colors.green() + entry.getValue() +
                                colors.gray() + " (" + colors.lightPurple() + WEIGHT_FORMAT.format(totalSpecTypeWeight) + colors.gray() + ")" +
                                colors.gray() + " (" + colors.darkPurple() + WEIGHT_FORMAT.format(totalSpecTypeWeight / teamBalanceInfo.totalWeight * 100) + "%" + colors.gray() + ")";
                    })
                    .forEachOrdered(s -> printer.sendMessage("  " + s));
            printer.sendMessage(colors.gray() + "  -----------------------");
            List<DebuggedPlayer> players = teamBalanceInfo.players;
            for (DebuggedPlayer debuggedPlayer : players) {
                printer.sendMessage("  " + debuggedPlayer.getInfo(colors));
            }
        });
    }


    /**
     * Debug message supplier
     */
    interface DebuggedMessage {
        String getMessage(Color colors);
    }

    /**
     * Balance filters
     */
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

    /**
     * Balance information for an entire team
     */
    static class TeamBalanceInfo {
        final List<DebuggedPlayer> players = new ArrayList<>();
        final Map<SpecType, Integer> specTypeCount = new HashMap<>();
        double totalWeight = 0;

        public void addPlayer(DebuggedPlayer debuggedPlayer) {
            players.add(debuggedPlayer);
            specTypeCount.merge(debuggedPlayer.player.spec.specType, 1, Integer::sum);
            totalWeight += debuggedPlayer.player.weight;
        }

        public void removePlayer(DebuggedPlayer debuggedPlayer) {
            players.remove(debuggedPlayer);
            specTypeCount.merge(debuggedPlayer.player.spec.specType, -1, Integer::sum);
            totalWeight -= debuggedPlayer.player.weight;
        }

        public double getSpecTypeWeight(SpecType specType) {
            return players.stream()
                          .filter(debuggedPlayer -> debuggedPlayer.player.spec.specType == specType)
                          .mapToDouble(debuggedPlayer -> debuggedPlayer.player.weight)
                          .sum();
        }

        public List<DebuggedPlayer> getPlayersMatching(Predicate<Player> filter) {
            return players.stream()
                          .filter(debuggedPlayer -> filter.test(debuggedPlayer.player))
                          .toList();
        }

        public List<DebuggedPlayer> getPlayersMatching(SpecType specType) {
            return getPlayersMatching(player -> player.spec.specType == specType);
        }
    }

    /**
     * Player wrapper class with debug messages
     *
     * @param player           the player
     * @param debuggedMessages debug messages
     */
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

    /**
     * @param uuid   the uuid of the player - needed for equals and hashcode
     * @param spec   the specialization of the player
     * @param weight the weight of the player
     */
    record Player(UUID uuid, Specialization spec, double weight) {

        public Player(Specialization spec, double weight) {
            this(UUID.randomUUID(), spec, weight);
        }

        public String getInfo(Color colors) {
            return spec.specType.getColor.apply(colors) + spec + colors.gray() + " - " + colors.lightPurple() + WEIGHT_FORMAT.format(weight);
        }

        @Override
        public String toString() {
            return "Player{" +
                    "" + spec +
                    ", " + WEIGHT_FORMAT.format(weight) +
                    '}';
        }
    }

    /**
     * Class used as an interface to print debug messages
     */
    static final class Printer {
        private final Consumer<String> sendMessage;
        private final Color colors;
        private boolean enabled = true;

        /**
         * @param sendMessage the consumer to send messages to
         * @param colors      color interface to use for coloring messages
         */
        Printer(Consumer<String> sendMessage, Color colors) {
            this.sendMessage = sendMessage;
            this.colors = colors;
        }

        public void sendMessage(String message) {
            if (enabled) {
                sendMessage.accept(message);
            }
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Color colors() {
            return colors;
        }

    }

}