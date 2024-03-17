package com.ebicep.warlordsbalancer;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class SetTest {

    @Test
    public void setTest() {
        RandomWeightMethod randomWeightMethod = RandomWeightMethod.NORMAL_DISTRIBUTION;
        for (int i = 0; i < 100; i++) {
            Set<Balancer.Player> players = new HashSet<>();
            for (int j = 0; j < 22; j++) {
                players.add(new Balancer.Player(Specialization.getRandomSpec(), randomWeightMethod.generateRandomWeight()));
            }
            if (players.size() != 22) {
                throw new IllegalStateException("Set size is not 22");
            }
        }
    }

}
