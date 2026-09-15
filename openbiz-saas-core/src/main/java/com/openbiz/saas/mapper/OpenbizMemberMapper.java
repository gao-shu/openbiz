package com.openbiz.saas.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.saas.domain.OpenbizMember;

/**
 * Member mapper.
 */
public interface OpenbizMemberMapper
{
    /**
     * Find active member for a RuoYi user. Phase 1.2 assumes at most one active membership.
     */
    OpenbizMember selectActiveByUserId(@Param("userId") Long userId);
}
