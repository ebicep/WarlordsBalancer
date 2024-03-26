package com.ebicep.warlordsbalancer;

import org.junit.Test;

public class SwapSpecTypesTest {

    @Test
    public void noFeatures() {
        Balancer.balance(
                BalanceMethod.V3_1,
                WeightGenerationMethod.DEFAULT_CUSTOM_NORMAL_DISTRIBUTION,
                ExtraBalanceFeature.SWAP_UNEVEN_TEAMS,
                ExtraBalanceFeature.SWAP_SPEC_TYPES
        );
    }

    @Test
    public void noFeatures2() {
        Balancer.balance(
                BalanceMethod.V3_1,
                WeightGenerationMethod.DEFAULT_CUSTOM_NORMAL_DISTRIBUTION,
                ExtraBalanceFeature.SWAP_UNEVEN_TEAMS,
                ExtraBalanceFeature.SWAP_SPEC_TYPES
        );
    }

    @Test
    public void onlyCompensate() {
        Balancer.balance(
                BalanceMethod.V3_1,
                WeightGenerationMethod.DEFAULT_CUSTOM_NORMAL_DISTRIBUTION,
                ExtraBalanceFeature.SWAP_UNEVEN_TEAMS,
                ExtraBalanceFeature.SWAP_SPEC_TYPES,
                ExtraBalanceFeature.COMPENSATE
        );
    }

    @Test
    public void onlyCompensate2() {
        Balancer.balance(
                BalanceMethod.V3_1,
                WeightGenerationMethod.DEFAULT_CUSTOM_NORMAL_DISTRIBUTION,
                ExtraBalanceFeature.SWAP_UNEVEN_TEAMS,
                ExtraBalanceFeature.SWAP_SPEC_TYPES,
                ExtraBalanceFeature.COMPENSATE
        );
    }

    @Test
    public void onlyHardSwap() {
        Balancer.balance(
                BalanceMethod.V3_1,
                WeightGenerationMethod.DEFAULT_CUSTOM_NORMAL_DISTRIBUTION,
                ExtraBalanceFeature.SWAP_UNEVEN_TEAMS,
                ExtraBalanceFeature.SWAP_SPEC_TYPES,
                ExtraBalanceFeature.HARD_SWAP
        );
    }

    @Test
    public void onlyHardSwap2() {
        Balancer.balance(
                BalanceMethod.V3_1,
                WeightGenerationMethod.DEFAULT_CUSTOM_NORMAL_DISTRIBUTION,
                ExtraBalanceFeature.SWAP_UNEVEN_TEAMS,
                ExtraBalanceFeature.SWAP_SPEC_TYPES,
                ExtraBalanceFeature.HARD_SWAP
        );
    }

    @Test
    public void both() {
        Balancer.balance(
                BalanceMethod.V3_1,
                WeightGenerationMethod.DEFAULT_CUSTOM_NORMAL_DISTRIBUTION,
                ExtraBalanceFeature.SWAP_UNEVEN_TEAMS,
                ExtraBalanceFeature.SWAP_SPEC_TYPES,
                ExtraBalanceFeature.COMPENSATE,
                ExtraBalanceFeature.HARD_SWAP
        );
    }

    @Test
    public void both2() {
        Balancer.balance(
                BalanceMethod.V3_1,
                WeightGenerationMethod.DEFAULT_CUSTOM_NORMAL_DISTRIBUTION,
                ExtraBalanceFeature.SWAP_UNEVEN_TEAMS,
                ExtraBalanceFeature.SWAP_SPEC_TYPES,
                ExtraBalanceFeature.COMPENSATE,
                ExtraBalanceFeature.HARD_SWAP
        );
    }

}
