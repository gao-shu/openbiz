package com.openbiz.biz.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.biz.domain.OpenbizOrder;

public interface OpenbizOrderMapper
{
    int insert(OpenbizOrder order);

    OpenbizOrder selectByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    OpenbizOrder selectByIdempotent(@Param("tenantId") Long tenantId, @Param("idempotentKey") String idempotentKey);
}
