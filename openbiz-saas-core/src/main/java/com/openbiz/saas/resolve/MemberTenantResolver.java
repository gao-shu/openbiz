package com.openbiz.saas.resolve;

import org.springframework.stereotype.Component;
import com.openbiz.saas.domain.OpenbizMember;
import com.openbiz.saas.domain.OpenbizTenant;
import com.openbiz.saas.mapper.OpenbizMemberMapper;
import com.openbiz.saas.mapper.OpenbizTenantMapper;

/**
 * Default resolver: active member of a normal tenant.
 */
@Component
public class MemberTenantResolver implements TenantResolver
{
    private final OpenbizMemberMapper memberMapper;
    private final OpenbizTenantMapper tenantMapper;

    public MemberTenantResolver(OpenbizMemberMapper memberMapper, OpenbizTenantMapper tenantMapper)
    {
        this.memberMapper = memberMapper;
        this.tenantMapper = tenantMapper;
    }

    @Override
    public Long resolveTenantId(Long userId)
    {
        if (userId == null)
        {
            return null;
        }
        OpenbizMember member = memberMapper.selectActiveByUserId(userId);
        if (member == null || member.getTenantId() == null)
        {
            return null;
        }
        OpenbizTenant tenant = tenantMapper.selectById(member.getTenantId());
        if (tenant == null || !"0".equals(tenant.getStatus()))
        {
            return null;
        }
        return tenant.getId();
    }
}
