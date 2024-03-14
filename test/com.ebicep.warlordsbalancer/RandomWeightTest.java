package com.ebicep.warlordsbalancer;

import org.junit.Test;

public class RandomWeightTest {

    @Test
    public void testRandomWeight() {
        for (int i = 0; i < 100; i++) {
            double randomWeight = Balancer.RandomWeightMethod.NORMAL_DISTRIBUTION.generateRandomWeight();
            System.out.println(randomWeight);
        }
    }

}
