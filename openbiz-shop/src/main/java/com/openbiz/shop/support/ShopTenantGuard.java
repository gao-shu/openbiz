package com.openbiz.shop.support;

import com.openbiz.saas.context.TenantContext;
import com.ruoyi.common.exception.ServiceException;

public final class ShopTenantGuard
{
    private ShopTenantGuard()
    {
    }

    public static Long requireTenantId()
    {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null)
        {
            throw new ServiceException("NO_TENANT_CONTEXT: shop operation requires tenant");
        }
        return tenantId;
    }
}
