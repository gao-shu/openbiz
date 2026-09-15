package com.openbiz.access.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.access.domain.OpenbizAccessPermission;

public interface OpenbizAccessPermissionMapper
{
    OpenbizAccessPermission selectActive(@Param("tenantId") Long tenantId,
                                         @Param("userId") Long userId,
                                         @Param("deviceId") Long deviceId);
}
