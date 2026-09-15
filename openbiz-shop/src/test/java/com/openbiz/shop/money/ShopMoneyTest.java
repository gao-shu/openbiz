package com.openbiz.shop.money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ShopMoneyTest
{
    @Test
    void scaleAndPositive()
    {
        assertEquals(new BigDecimal("10.50"), ShopMoney.of(new BigDecimal("10.5")));
        assertTrue(ShopMoney.isPositive(new BigDecimal("0.01")));
        assertFalse(ShopMoney.isPositive(BigDecimal.ZERO));
    }
}
