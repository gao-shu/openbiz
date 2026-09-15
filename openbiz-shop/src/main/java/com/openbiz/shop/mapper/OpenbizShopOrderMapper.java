package com.openbiz.shop.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.shop.domain.OpenbizShopOrder;

public interface OpenbizShopOrderMapper
{
    int insert(OpenbizShopOrder row);

    OpenbizShopOrder selectByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    OpenbizShopOrder selectByIdempotent(@Param("tenantId") Long tenantId, @Param("idempotentKey") String idempotentKey);
}
