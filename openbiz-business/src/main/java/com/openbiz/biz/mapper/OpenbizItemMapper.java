package com.openbiz.biz.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.biz.domain.OpenbizItem;

public interface OpenbizItemMapper
{
    OpenbizItem selectByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    OpenbizItem selectByCodeAndTenant(@Param("itemCode") String itemCode, @Param("tenantId") Long tenantId);
}
