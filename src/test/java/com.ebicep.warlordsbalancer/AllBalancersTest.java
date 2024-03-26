package com.ebicep.warlordsbalancer;

import org.junit.Test;
import org.junit.experimental.ParallelComputer;
import org.junit.runner.JUnitCore;

public class AllBalancersTest {

    @Test
    public void testAll() {
        Class<?>[] classes = {
                SwapSpecTypesTest.class
        };
        JUnitCore.runClasses(new ParallelComputer(true, true), classes);
    }

}
