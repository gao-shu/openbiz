package com.openbiz.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.openbiz.biz.domain.OpenbizOrderItem;

public interface OpenbizOrderItemMapper
{
    int insert(OpenbizOrderItem item);

    List<OpenbizOrderItem> listByOrderAndTenant(@Param("orderId") Long orderId, @Param("tenantId") Long tenantId);
}
