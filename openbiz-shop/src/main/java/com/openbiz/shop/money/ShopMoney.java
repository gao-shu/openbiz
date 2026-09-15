package com.openbiz.shop.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Shop-local money helpers. Pattern copied from Business; no Maven dep on biz-core.
 */
public final class ShopMoney
{
    public static final int SCALE = 2;

    private ShopMoney()
    {
    }

    public static BigDecimal of(BigDecimal value)
    {
        if (value == null)
        {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.UNNECESSARY);
        }
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static boolean isPositive(BigDecimal value)
    {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }
}
