package com.ebicep.warlordsbalancer;

import org.junit.Test;


public class BalancerTest {

    @Test
    public void v1Random() {
        Balancer.balance(BalanceMethod.V1, RandomWeightMethod.RANDOM);
    }

    @Test
    public void v1RandomSwapSpecTypes() {
        Balancer.balance(BalanceMethod.V1, RandomWeightMethod.RANDOM, ExtraBalanceFeature.SWAP_SPEC_TYPES);
    }

    @Test
    public void v1NormalDistribution() {
        Balancer.balance(BalanceMethod.V1, RandomWeightMethod.NORMAL_DISTRIBUTION);
    }

    @Test
    public void v1NormalDistributionSwapSpecTypes() {
        Balancer.balance(BalanceMethod.V1, RandomWeightMethod.NORMAL_DISTRIBUTION, ExtraBalanceFeature.SWAP_SPEC_TYPES);
    }

    @Test
    public void v2Random() {
        Balancer.balance(BalanceMethod.V2, RandomWeightMethod.RANDOM);
    }

    @Test
    public void v2RandomSwapSpecTypes() {
        Balancer.balance(BalanceMethod.V2, RandomWeightMethod.RANDOM, ExtraBalanceFeature.SWAP_SPEC_TYPES);
    }

    @Test
    public void v2NormalDistribution() {
        Balancer.balance(BalanceMethod.V2, RandomWeightMethod.NORMAL_DISTRIBUTION);
    }

    @Test
    public void v2NormalDistributionSwapSpecTypes() {
        Balancer.balance(BalanceMethod.V2, RandomWeightMethod.NORMAL_DISTRIBUTION, ExtraBalanceFeature.SWAP_SPEC_TYPES);
    }

    @Test
    public void v2_1Random() {
        Balancer.balance(BalanceMethod.V2_1, RandomWeightMethod.RANDOM);
    }

    @Test
    public void v2_1RandomSwapSpecTypes() {
        Balancer.balance(BalanceMethod.V2_1, RandomWeightMethod.RANDOM, ExtraBalanceFeature.SWAP_SPEC_TYPES);
    }

    @Test
    public void v2_1RandomExtra() {
        Balancer.balance(BalanceMethod.V2_1,
                RandomWeightMethod.RANDOM,
                ExtraBalanceFeature.SWAP_UNEVEN_TEAMS,
//                ExtraBalanceFeature.SWAP_SPEC_TYPES,
//                ExtraBalanceFeature.SWAP_TEAM_SPEC_TYPES,
                ExtraBalanceFeature.HARD_SWAP
        );
    }


    @Test
    public void v2_1NormalDistribution() {
        Balancer.balance(BalanceMethod.V2_1, RandomWeightMethod.NORMAL_DISTRIBUTION);
    }

    @Test
    public void v2_1NormalDistributionSwapUneven() {
        Balancer.balance(BalanceMethod.V2_1, RandomWeightMethod.NORMAL_DISTRIBUTION, ExtraBalanceFeature.SWAP_UNEVEN_TEAMS);
    }

    @Test
    public void v2_1NormalDistributionSwapSpecTypes() {
        Balancer.balance(BalanceMethod.V2_1, RandomWeightMethod.NORMAL_DISTRIBUTION, ExtraBalanceFeature.SWAP_SPEC_TYPES);
    }

    @Test
    public void v2_1NormalDistributionExtra() {
        Balancer.balance(BalanceMethod.V2_1,
                RandomWeightMethod.NORMAL_DISTRIBUTION,
                ExtraBalanceFeature.SWAP_UNEVEN_TEAMS,
                ExtraBalanceFeature.SWAP_SPEC_TYPES,
                ExtraBalanceFeature.SWAP_TEAM_SPEC_TYPES,
                ExtraBalanceFeature.COMPENSATE
        );
    }


}
