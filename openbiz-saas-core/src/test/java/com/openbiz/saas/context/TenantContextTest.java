package com.openbiz.saas.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantContextTest
{
    @AfterEach
    void tearDown()
    {
        TenantContext.clear();
    }

    @Test
    void setGetClear()
    {
        assertNull(TenantContext.getTenantId());
        TenantContext.setTenantId(10L);
        assertEquals(10L, TenantContext.getTenantId());
        TenantContext.clear();
        assertNull(TenantContext.getTenantId());
    }

    @Test
    void clearRemovesValue()
    {
        TenantContext.setTenantId(1L);
        TenantContext.clear();
        assertNull(TenantContext.getTenantId());
    }
}
