package com.openbiz.saas.service.impl;

import org.springframework.stereotype.Service;
import com.openbiz.saas.api.MemberService;
import com.openbiz.saas.domain.OpenbizMember;
import com.openbiz.saas.mapper.OpenbizMemberMapper;

@Service
public class MemberServiceImpl implements MemberService
{
    private final OpenbizMemberMapper memberMapper;

    public MemberServiceImpl(OpenbizMemberMapper memberMapper)
    {
        this.memberMapper = memberMapper;
    }

    @Override
    public OpenbizMember findActiveByUserId(Long userId)
    {
        if (userId == null)
        {
            return null;
        }
        return memberMapper.selectActiveByUserId(userId);
    }
}
