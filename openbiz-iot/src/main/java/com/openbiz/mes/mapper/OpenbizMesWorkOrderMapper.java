package com.openbiz.mes.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.mes.domain.OpenbizMesWorkOrder;

public interface OpenbizMesWorkOrderMapper
{
    OpenbizMesWorkOrder selectByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    int updateStatus(@Param("id") Long id, @Param("tenantId") Long tenantId, @Param("status") String status);
}
