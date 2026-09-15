package com.openbiz.shop.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.openbiz.shop.domain.OpenbizShopOrderItem;

public interface OpenbizShopOrderItemMapper
{
    int insert(OpenbizShopOrderItem row);

    List<OpenbizShopOrderItem> selectByOrderAndTenant(@Param("orderId") Long orderId, @Param("tenantId") Long tenantId);
}
