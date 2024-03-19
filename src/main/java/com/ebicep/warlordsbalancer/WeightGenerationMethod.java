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
            int winsLast100 = ThreadLocalRandom.current().nextInt(0, 101);
            int winsLast10 = (int) (winsLast100 * .1);
            double last100WinLoss = winsLast100 / (100.0 - winsLast100);
            double last10WinLoss = winsLast10 / 10.0;
//            System.out.println(last100WinLoss + " - " + last10WinLoss);
            return clamp(last100WinLoss * (1 + Math.sqrt(last10WinLoss)));
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
