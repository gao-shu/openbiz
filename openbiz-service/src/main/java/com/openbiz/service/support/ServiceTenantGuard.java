package com.openbiz.service.support;

import com.openbiz.saas.context.TenantContext;
import com.ruoyi.common.exception.ServiceException;

/**
 * Local tenant gate for Service domain.
 */
public final class ServiceTenantGuard
{
    private ServiceTenantGuard()
    {
    }

    public static Long requireTenantId()
    {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null)
        {
            throw new ServiceException("NO_TENANT_CONTEXT: service operation requires tenant");
        }
        return tenantId;
    }
}
