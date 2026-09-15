package com.openbiz.locker.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.locker.domain.OpenbizLocker;

public interface OpenbizLockerMapper
{
    OpenbizLocker selectByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
