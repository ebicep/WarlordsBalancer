package com.ebicep.warlordsbalancer;

import org.junit.Test;

import java.util.Collections;


public class BalancerTest {

    @Test
    public void testBalancer() {
        Balancer.balance(BalanceMethod.V2_1,
                WeightGenerationMethod.DEFAULT_CUSTOM_NORMAL_DISTRIBUTION,
                ExtraBalanceFeature.SWAP_UNEVEN_TEAMS,
//                ExtraBalanceFeature.SWAP_SPEC_TYPES,
//                ExtraBalanceFeature.SWAP_TEAM_SPEC_TYPES,
                ExtraBalanceFeature.COMPENSATE,
                ExtraBalanceFeature.HARD_SWAP
        );
    }

    @Test
    public void testBalancer2() {
        Balancer.balance(new Balancer.Printer(System.out::println, new Color() {}),
                1,
                24,
                BalanceMethod.V3_1,
                WeightGenerationMethod.DEFAULT_CUSTOM_NORMAL_DISTRIBUTION,
                Collections.emptyList()
        );
    }

    @Test
    public void v1Random() {
        Balancer.balance(BalanceMethod.V1, WeightGenerationMethod.DEFAULT_RANDOM);
    }

    @Test
    public void v1RandomSwapSpecTypes() {
        Balancer.balance(BalanceMethod.V1, WeightGenerationMethod.DEFAULT_RANDOM, ExtraBalanceFeature.SWAP_SPEC_TYPES);
    }

    @Test
    public void v1NormalDistribution() {
        Balancer.balance(BalanceMethod.V1, WeightGenerationMethod.DEFAULT_NORMAL_DISTRIBUTION);
    }

    @Test
    public void v1NormalDistributionSwapSpecTypes() {
        Balancer.balance(BalanceMethod.V1, WeightGenerationMethod.DEFAULT_NORMAL_DISTRIBUTION, ExtraBalanceFeature.SWAP_SPEC_TYPES);
    }

    @Test
    public void v2Random() {
        Balancer.balance(BalanceMethod.V2, WeightGenerationMethod.DEFAULT_RANDOM);
    }

    @Test
    public void v2RandomSwapSpecTypes() {
        Balancer.balance(BalanceMethod.V2, WeightGenerationMethod.DEFAULT_RANDOM, ExtraBalanceFeature.SWAP_SPEC_TYPES);
    }

    @Test
    public void v2NormalDistribution() {
        Balancer.balance(BalanceMethod.V2, WeightGenerationMethod.DEFAULT_NORMAL_DISTRIBUTION);
    }

    @Test
    public void v2NormalDistributionSwapSpecTypes() {
        Balancer.balance(BalanceMethod.V2, WeightGenerationMethod.DEFAULT_NORMAL_DISTRIBUTION, ExtraBalanceFeature.SWAP_SPEC_TYPES);
    }

    @Test
    public void v2_1Random() {
        Balancer.balance(BalanceMethod.V2_1, WeightGenerationMethod.DEFAULT_RANDOM);
    }

    @Test
    public void v2_1RandomSwapSpecTypes() {
        Balancer.balance(BalanceMethod.V2_1, WeightGenerationMethod.DEFAULT_RANDOM, ExtraBalanceFeature.SWAP_SPEC_TYPES);
    }

    @Test
    public void v2_1RandomExtra() {
        Balancer.balance(BalanceMethod.V2_1,
                WeightGenerationMethod.DEFAULT_RANDOM,
                ExtraBalanceFeature.SWAP_UNEVEN_TEAMS,
                ExtraBalanceFeature.SWAP_SPEC_TYPES,
                ExtraBalanceFeature.SWAP_TEAM_SPEC_TYPES,
                ExtraBalanceFeature.HARD_SWAP
        );
    }

    @Test
    public void v2_1NormalDistribution() {
        Balancer.balance(BalanceMethod.V2_1, WeightGenerationMethod.DEFAULT_NORMAL_DISTRIBUTION);
    }

    @Test
    public void v2_1NormalDistributionSwapUneven() {
        Balancer.balance(BalanceMethod.V2_1, WeightGenerationMethod.DEFAULT_NORMAL_DISTRIBUTION, ExtraBalanceFeature.SWAP_UNEVEN_TEAMS);
    }

    @Test
    public void v2_1NormalDistributionSwapSpecTypes() {
        Balancer.balance(BalanceMethod.V2_1, WeightGenerationMethod.DEFAULT_NORMAL_DISTRIBUTION, ExtraBalanceFeature.SWAP_SPEC_TYPES);
    }

    @Test
    public void v2_1NormalDistributionExtra() {
        Balancer.balance(BalanceMethod.V2_1,
                WeightGenerationMethod.DEFAULT_NORMAL_DISTRIBUTION,
                ExtraBalanceFeature.SWAP_UNEVEN_TEAMS,
                ExtraBalanceFeature.SWAP_SPEC_TYPES,
                ExtraBalanceFeature.SWAP_TEAM_SPEC_TYPES,
                ExtraBalanceFeature.COMPENSATE
        );
    }


}
