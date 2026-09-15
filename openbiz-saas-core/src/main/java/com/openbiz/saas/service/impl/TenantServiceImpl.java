package com.openbiz.saas.service.impl;

import org.springframework.stereotype.Service;
import com.openbiz.saas.api.TenantService;
import com.openbiz.saas.context.TenantContext;

@Service
public class TenantServiceImpl implements TenantService
{
    @Override
    public Long currentTenantId()
    {
        return TenantContext.getTenantId();
    }
}
