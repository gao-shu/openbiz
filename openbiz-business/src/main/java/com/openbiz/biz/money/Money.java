package com.openbiz.biz.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money
{
    public static final int SCALE = 2;

    private Money()
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
