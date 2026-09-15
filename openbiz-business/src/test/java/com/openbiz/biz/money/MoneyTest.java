package com.openbiz.biz.money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest
{
    @Test
    void scalesHalfUpToTwoDecimals()
    {
        assertEquals(new BigDecimal("1.01"), Money.of(new BigDecimal("1.005")));
        assertEquals(new BigDecimal("0.00"), Money.of(null));
        assertTrue(Money.isPositive(new BigDecimal("0.01")));
        assertFalse(Money.isPositive(BigDecimal.ZERO));
        assertFalse(Money.isPositive(new BigDecimal("-1.00")));
    }
}
