package com.ebicep.warlordsbalancer;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class SetTest {

    @Test
    public void setTest() {
        WeightGenerationMethod weightGenerationMethod = WeightGenerationMethod.NORMAL_DISTRIBUTION;
        for (int i = 0; i < 100; i++) {
            Set<Balancer.Player> players = new HashSet<>();
            for (int j = 0; j < 22; j++) {
                players.add(new Balancer.Player(Specialization.getRandomSpec(), weightGenerationMethod.generateRandomWeight(), null));
            }
            if (players.size() != 22) {
                throw new IllegalStateException("Set size is not 22");
            }
        }
    }

}
