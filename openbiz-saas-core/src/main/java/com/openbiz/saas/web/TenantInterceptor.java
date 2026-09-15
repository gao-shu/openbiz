package com.openbiz.saas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.openbiz.saas.context.TenantContext;
import com.openbiz.saas.resolve.TenantResolver;
import com.ruoyi.common.core.domain.model.LoginUser;

/**
 * Resolves tenant for authenticated requests and always clears ThreadLocal afterwards.
 */
@Component
public class TenantInterceptor implements HandlerInterceptor
{
    private static final Logger log = LoggerFactory.getLogger(TenantInterceptor.class);

    private final TenantResolver tenantResolver;

    public TenantInterceptor(TenantResolver tenantResolver)
    {
        this.tenantResolver = tenantResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    {
        // Never leave stale tenant from a previous request on this thread
        TenantContext.clear();

        Long userId = currentUserIdOrNull();
        if (userId == null)
        {
            // Anonymous / pre-auth: no default tenant
            return true;
        }

        Long tenantId = tenantResolver.resolveTenantId(userId);
        if (tenantId != null)
        {
            TenantContext.setTenantId(tenantId);
            if (log.isDebugEnabled())
            {
                log.debug("TenantContext set: userId={}, tenantId={}, uri={}", userId, tenantId, request.getRequestURI());
            }
        }
        else if (log.isDebugEnabled())
        {
            log.debug("No tenant membership for userId={}, uri={}", userId, request.getRequestURI());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
    {
        TenantContext.clear();
    }

    private Long currentUserIdOrNull()
    {
        try
        {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated())
            {
                return null;
            }
            Object principal = authentication.getPrincipal();
            if (principal instanceof LoginUser loginUser)
            {
                return loginUser.getUserId();
            }
            // anonymousUser string or other principals
            return null;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
