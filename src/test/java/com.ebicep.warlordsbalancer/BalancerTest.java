package com.ebicep.warlordsbalancer;

import org.junit.Test;


public class BalancerTest {

    @Test
    public void v1Random() {
        Balancer.balance(BalanceMethod.V1, RandomWeightMethod.RANDOM);
    }

    @Test
    public void v1RandomSwap() {
        Balancer.balance(BalanceMethod.V1, RandomWeightMethod.RANDOM, ExtraBalanceFeature.SWAP);
    }

    @Test
    public void v1NormalDistribution() {
        Balancer.balance(BalanceMethod.V1, RandomWeightMethod.NORMAL_DISTRIBUTION);
    }

    @Test
    public void v1NormalDistributionSwap() {
        Balancer.balance(BalanceMethod.V1, RandomWeightMethod.NORMAL_DISTRIBUTION, ExtraBalanceFeature.SWAP);
    }

    @Test
    public void v2Random() {
        Balancer.balance(BalanceMethod.V2, RandomWeightMethod.RANDOM);
    }

    @Test
    public void v2RandomSwap() {
        Balancer.balance(BalanceMethod.V2, RandomWeightMethod.RANDOM, ExtraBalanceFeature.SWAP);
    }

    @Test
    public void v2NormalDistribution() {
        Balancer.balance(BalanceMethod.V2, RandomWeightMethod.NORMAL_DISTRIBUTION);
    }

    @Test
    public void v2NormalDistributionSwap() {
        Balancer.balance(BalanceMethod.V2, RandomWeightMethod.NORMAL_DISTRIBUTION, ExtraBalanceFeature.SWAP);
    }

}
