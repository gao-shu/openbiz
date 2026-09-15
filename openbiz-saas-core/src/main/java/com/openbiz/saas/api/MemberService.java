package com.openbiz.saas.api;

import com.openbiz.saas.domain.OpenbizMember;

/**
 * Member domain API.
 */
public interface MemberService
{
    /**
     * Active membership for a RuoYi user, or null.
     */
    OpenbizMember findActiveByUserId(Long userId);
}
