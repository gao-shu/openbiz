package com.openbiz.iot.core.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.iot.core.domain.OpenbizDeviceCommand;

public interface OpenbizDeviceCommandMapper
{
    int insert(OpenbizDeviceCommand command);

    int updateStatus(OpenbizDeviceCommand command);

    OpenbizDeviceCommand selectByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    OpenbizDeviceCommand selectByCommandIdAndTenant(@Param("commandId") String commandId, @Param("tenantId") Long tenantId);
}
