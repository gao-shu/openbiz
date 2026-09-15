package com.openbiz.saas.context;

/**
 * Request-scoped tenant holder (ThreadLocal).
 * Must be cleared at the end of each HTTP request to avoid Tomcat thread reuse leaks.
 */
public final class TenantContext
{
    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    private TenantContext()
    {
    }

    public static void setTenantId(Long tenantId)
    {
        TENANT_ID.set(tenantId);
    }

    /**
     * @return current tenant id, or {@code null} when unset / anonymous / no membership
     */
    public static Long getTenantId()
    {
        return TENANT_ID.get();
    }

    public static void clear()
    {
        TENANT_ID.remove();
    }
}
