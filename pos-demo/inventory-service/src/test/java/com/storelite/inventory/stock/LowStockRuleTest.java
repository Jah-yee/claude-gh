package com.storelite.inventory.stock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LowStockRuleTest {

    @ParameterizedTest(name = "{0} -> {1} with threshold {2} => {3}")
    @CsvSource({
            "8, 6, 5, false",   // still above
            "8, 5, 5, true",    // lands exactly on threshold
            "8, 2, 5, true",    // jumps below
            "5, 4, 5, false",   // already low: no repeat alert
            "3, -1, 5, false",  // already low, oversold
            "6, -2, 5, true",   // crosses straight into negative
    })
    void crossedThreshold(int before, int after, int threshold, boolean expected) {
        assertThat(LowStockRule.crossedThreshold(before, after, threshold)).isEqualTo(expected);
    }
}
