package com.openbiz.iot.core.support;

import com.openbiz.saas.context.TenantContext;
import com.ruoyi.common.exception.ServiceException;

/**
 * Requires authenticated tenant context for IoT operations.
 */
public final class TenantGuard
{
    private TenantGuard()
    {
    }

    public static Long requireTenantId()
    {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null)
        {
            throw new ServiceException("NO_TENANT_CONTEXT: IoT operation requires tenant");
        }
        return tenantId;
    }
}
