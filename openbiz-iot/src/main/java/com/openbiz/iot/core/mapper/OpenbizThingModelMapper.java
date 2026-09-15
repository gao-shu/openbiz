package com.openbiz.iot.core.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.iot.core.domain.OpenbizThingModel;

public interface OpenbizThingModelMapper
{
    int insert(OpenbizThingModel model);

    OpenbizThingModel selectByProductAndTenant(@Param("productId") Long productId, @Param("tenantId") Long tenantId);

    OpenbizThingModel selectByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
