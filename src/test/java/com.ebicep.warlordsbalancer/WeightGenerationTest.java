package com.ebicep.warlordsbalancer;

import org.junit.Test;

import static com.ebicep.warlordsbalancer.Balancer.format;


public class WeightGenerationTest {

    @Test
    public void testMethods() {
        printStats(WeightGenerationMethod.RANDOM);
        printStats(WeightGenerationMethod.NORMAL_DISTRIBUTION);
        printStats(WeightGenerationMethod.CUSTOM);
    }

    @Test
    public void testRandomWeight() {
        for (int i = 0; i < 100; i++) {
            double randomWeight = WeightGenerationMethod.NORMAL_DISTRIBUTION.generateRandomWeight();
            System.out.println(randomWeight);
        }
    }

    private static void printStats(WeightGenerationMethod method) {
        int iterations = 10_000;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        double sum = 0;
        for (int i = 0; i < iterations; i++) {
            double customWeight = method.generateRandomWeight();
            if (customWeight > max) {
                max = customWeight;
            }
            if (customWeight < min) {
                min = customWeight;
            }
            sum += customWeight;
//            System.out.println(customWeight);
        }
        System.out.println("Method: " + method);
        System.out.println("Max: " + format(max));
        System.out.println("Min: " + format(min));
        System.out.println("Average: " + format(sum / iterations));
    }

}
