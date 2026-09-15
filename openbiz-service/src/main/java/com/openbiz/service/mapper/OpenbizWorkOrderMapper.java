package com.openbiz.service.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.service.domain.OpenbizWorkOrder;

public interface OpenbizWorkOrderMapper
{
    int insert(OpenbizWorkOrder row);

    OpenbizWorkOrder selectByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    OpenbizWorkOrder selectByIdempotent(@Param("tenantId") Long tenantId, @Param("idempotentKey") String idempotentKey);

    int updateAssign(@Param("id") Long id, @Param("tenantId") Long tenantId,
                     @Param("fromStatus") String fromStatus, @Param("assigneeUserId") Long assigneeUserId,
                     @Param("toStatus") String toStatus);

    int updateStatus(@Param("id") Long id, @Param("tenantId") Long tenantId,
                     @Param("fromStatus") String fromStatus, @Param("toStatus") String toStatus,
                     @Param("completeNote") String completeNote);
}
