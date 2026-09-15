package com.openbiz.saas.resolve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.openbiz.saas.domain.OpenbizMember;
import com.openbiz.saas.domain.OpenbizTenant;
import com.openbiz.saas.mapper.OpenbizMemberMapper;
import com.openbiz.saas.mapper.OpenbizTenantMapper;

@ExtendWith(MockitoExtension.class)
class MemberTenantResolverTest
{
    @Mock
    private OpenbizMemberMapper memberMapper;

    @Mock
    private OpenbizTenantMapper tenantMapper;

    @InjectMocks
    private MemberTenantResolver resolver;

    @Test
    void userAMapsToTenantA()
    {
        OpenbizMember member = new OpenbizMember();
        member.setTenantId(1L);
        member.setUserId(1L);
        member.setStatus("0");
        OpenbizTenant tenant = new OpenbizTenant();
        tenant.setId(1L);
        tenant.setStatus("0");
        when(memberMapper.selectActiveByUserId(1L)).thenReturn(member);
        when(tenantMapper.selectById(1L)).thenReturn(tenant);

        assertEquals(1L, resolver.resolveTenantId(1L));
    }

    @Test
    void userBMapsToTenantB()
    {
        OpenbizMember member = new OpenbizMember();
        member.setTenantId(2L);
        member.setUserId(2L);
        member.setStatus("0");
        OpenbizTenant tenant = new OpenbizTenant();
        tenant.setId(2L);
        tenant.setStatus("0");
        when(memberMapper.selectActiveByUserId(2L)).thenReturn(member);
        when(tenantMapper.selectById(2L)).thenReturn(tenant);

        assertEquals(2L, resolver.resolveTenantId(2L));
    }

    @Test
    void unknownUserHasNoTenant()
    {
        when(memberMapper.selectActiveByUserId(99L)).thenReturn(null);
        assertNull(resolver.resolveTenantId(99L));
    }

    @Test
    void disabledTenantRejected()
    {
        OpenbizMember member = new OpenbizMember();
        member.setTenantId(1L);
        member.setUserId(1L);
        OpenbizTenant tenant = new OpenbizTenant();
        tenant.setId(1L);
        tenant.setStatus("1");
        when(memberMapper.selectActiveByUserId(1L)).thenReturn(member);
        when(tenantMapper.selectById(1L)).thenReturn(tenant);
        assertNull(resolver.resolveTenantId(1L));
    }

    @Test
    void nullUserId()
    {
        assertNull(resolver.resolveTenantId(null));
    }
}
