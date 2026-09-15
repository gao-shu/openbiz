package com.openbiz.saas.resolve;

/**
 * Resolves tenant id for the current authenticated user.
 * Phase 1.2 strategy: LoginUser -&gt; Member -&gt; tenantId.
 */
public interface TenantResolver
{
    /**
     * @param userId RuoYi sys_user.user_id
     * @return tenant id, or null when user has no active membership
     */
    Long resolveTenantId(Long userId);
}
