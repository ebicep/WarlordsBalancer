package com.ebicep.warlordsbalancer;

import java.util.concurrent.ThreadLocalRandom;

enum WeightGenerationMethod {
    RANDOM {
        @Override
        public double generateRandomWeight() {
            return ThreadLocalRandom.current().nextDouble(Balancer.MIN_WEIGHT, Balancer.MAX_WEIGHT);
        }
    },
    NORMAL_DISTRIBUTION {
        @Override
        public double generateRandomWeight() {
            return clamp(ThreadLocalRandom.current().nextGaussian() * STANDARD_DEVIATION + MEAN);
        }
    },
    CUSTOM {
        @Override
        public double generateRandomWeight() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int last100Wins = random.nextInt(0, 101);
            int last10Wins = clamp(random.nextInt(0, 11), 0, last100Wins);
            double weight = clamp(last100Wins / (100.0 - last100Wins));
            double last10WinLoss = last10Wins / 10.0;
//            System.out.println(last100Wins + " - " + weight + " - " + last10WinLoss);
            return weight * (1 + Math.sqrt(last10WinLoss));
        }
    },
    CUSTOM_NORMAL_DISTRIBUTION {
        @Override
        public double generateRandomWeight() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int last100Wins = (int) clamp(random.nextGaussian() * 25 + 50, 0, 101);
            int last10Wins = clamp(random.nextInt(0, 11), 0, last100Wins);
            double weight = clamp(last100Wins / (100.0 - last100Wins));
            double last10WinLoss = last10Wins / 10.0;
//            System.out.println(last100Wins + " - " + weight + " - " + last10WinLoss);
            return weight * (1 + Math.sqrt(last10WinLoss));
        }
    };

    private static final double MEAN = (Balancer.MAX_WEIGHT + Balancer.MIN_WEIGHT) / 2;
    private static final double STANDARD_DEVIATION = 1.2;

    private static double clamp(double value) {
        return clamp(value, Balancer.MIN_WEIGHT, Balancer.MAX_WEIGHT);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double lerp(double min, double max, double ratio) {
        return min + (max - min) * ratio;
    }

    public abstract double generateRandomWeight();
}
