package com.openbiz.locker.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.locker.domain.OpenbizLockerSlot;

public interface OpenbizLockerSlotMapper
{
    OpenbizLockerSlot selectByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    int updateStatus(@Param("id") Long id, @Param("tenantId") Long tenantId, @Param("status") String status);
}
