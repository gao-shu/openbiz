package com.openbiz.service.port.impl;

import org.springframework.stereotype.Component;
import com.openbiz.service.port.CurrentUserPort;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

@Component
public class SecurityCurrentUserPort implements CurrentUserPort
{
    @Override
    public Long requireUserId()
    {
        try
        {
            Long userId = SecurityUtils.getUserId();
            if (userId == null)
            {
                throw new ServiceException("UNAUTHENTICATED: login required");
            }
            return userId;
        }
        catch (ServiceException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new ServiceException("UNAUTHENTICATED: login required");
        }
    }
}
