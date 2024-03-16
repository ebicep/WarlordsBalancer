package com.ebicep.warlordsbalancer;

import java.util.concurrent.ThreadLocalRandom;

enum RandomWeightMethod {
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

    ;

    private static final double MEAN = (Balancer.MAX_WEIGHT + Balancer.MIN_WEIGHT) / 2;
    private static final double STANDARD_DEVIATION = 1.2;

    private static double clamp(double value) {
        return Math.max(Balancer.MIN_WEIGHT, Math.min(Balancer.MAX_WEIGHT, value));
    }

    public abstract double generateRandomWeight();
}
