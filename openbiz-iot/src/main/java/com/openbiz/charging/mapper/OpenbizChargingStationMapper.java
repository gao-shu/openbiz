package com.openbiz.charging.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.charging.domain.OpenbizChargingStation;

public interface OpenbizChargingStationMapper
{
    OpenbizChargingStation selectByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
