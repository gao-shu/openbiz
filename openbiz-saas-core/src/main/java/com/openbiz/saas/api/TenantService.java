package com.openbiz.saas.api;

/**
 * Tenant domain API.
 */
public interface TenantService
{
    /**
     * @return tenant id from TenantContext, or null when unset
     */
    Long currentTenantId();
}
