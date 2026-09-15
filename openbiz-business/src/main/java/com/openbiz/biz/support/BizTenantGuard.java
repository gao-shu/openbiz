package com.openbiz.biz.support;

import com.openbiz.saas.context.TenantContext;
import com.ruoyi.common.exception.ServiceException;

/**
 * Local tenant gate. Does not depend on IoT TenantGuard (IoT Core stays frozen).
 */
public final class BizTenantGuard
{
    private BizTenantGuard()
    {
    }

    public static Long requireTenantId()
    {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null)
        {
            throw new ServiceException("NO_TENANT_CONTEXT: business operation requires tenant");
        }
        return tenantId;
    }
}
