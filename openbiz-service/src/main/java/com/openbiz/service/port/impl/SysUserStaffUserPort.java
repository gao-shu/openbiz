package com.openbiz.service.port.impl;

import org.springframework.stereotype.Component;
import com.openbiz.service.port.StaffUserPort;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.service.ISysUserService;

@Component
public class SysUserStaffUserPort implements StaffUserPort
{
    private final ISysUserService userService;

    public SysUserStaffUserPort(ISysUserService userService)
    {
        this.userService = userService;
    }

    @Override
    public void requireActiveUser(Long userId)
    {
        if (userId == null)
        {
            throw new ServiceException("assigneeUserId is required", HttpStatus.BAD_REQUEST);
        }
        SysUser user = userService.selectUserById(userId);
        if (user == null)
        {
            throw new ServiceException("assignee user not found", HttpStatus.BAD_REQUEST);
        }
        if (UserConstants.USER_DISABLE.equals(user.getStatus()))
        {
            throw new ServiceException("assignee user is disabled", HttpStatus.BAD_REQUEST);
        }
    }
}
