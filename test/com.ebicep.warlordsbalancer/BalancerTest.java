package com.ebicep.warlordsbalancer;

import org.junit.Test;

public class BalancerTest {

    @Test
    public void v1Random() {
        Balancer.balance(Balancer.BalanceMethod.V1, Balancer.RandomWeightMethod.RANDOM);
    }

    @Test
    public void v1NormalDistribution() {
        Balancer.balance(Balancer.BalanceMethod.V1, Balancer.RandomWeightMethod.NORMAL_DISTRIBUTION);
    }

    @Test
    public void v2Random() {
        Balancer.balance(Balancer.BalanceMethod.V2, Balancer.RandomWeightMethod.RANDOM);
    }

    @Test
    public void v2NormalDistribution() {
        Balancer.balance(Balancer.BalanceMethod.V2, Balancer.RandomWeightMethod.NORMAL_DISTRIBUTION);
    }

}
