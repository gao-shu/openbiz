package com.openbiz.saas.web;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.openbiz.saas.context.TenantContext;
import com.openbiz.saas.resolve.TenantResolver;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;

@ExtendWith(MockitoExtension.class)
class TenantInterceptorTest
{
    @Mock
    private TenantResolver tenantResolver;

    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void setsAndClearsTenantForLoginUser() throws Exception
    {
        TenantInterceptor interceptor = new TenantInterceptor(tenantResolver);
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1L);
        SysUser user = new SysUser();
        user.setUserId(1L);
        loginUser.setUser(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, java.util.Collections.emptyList()));
        when(tenantResolver.resolveTenantId(1L)).thenReturn(1L);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openbiz/test/tenant");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
        org.junit.jupiter.api.Assertions.assertEquals(1L, TenantContext.getTenantId());

        interceptor.afterCompletion(request, response, new Object(), null);
        assertNull(TenantContext.getTenantId());
    }

    @Test
    void anonymousLeavesNoTenant() throws Exception
    {
        TenantInterceptor interceptor = new TenantInterceptor(tenantResolver);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openbiz/test/tenant");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertNull(TenantContext.getTenantId());
        interceptor.afterCompletion(request, response, new Object(), null);
        assertNull(TenantContext.getTenantId());
    }
}
