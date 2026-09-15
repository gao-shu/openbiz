package com.openbiz.charging.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.charging.domain.OpenbizChargingConnector;

public interface OpenbizChargingConnectorMapper
{
    OpenbizChargingConnector selectByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    int updateStatus(@Param("id") Long id, @Param("tenantId") Long tenantId, @Param("status") String status);
}
