package com.openbiz.shop.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.shop.domain.OpenbizShopInventory;

public interface OpenbizShopInventoryMapper
{
    int insert(OpenbizShopInventory row);

    OpenbizShopInventory selectByProductAndTenant(@Param("productId") Long productId, @Param("tenantId") Long tenantId);

    int setQuantity(@Param("productId") Long productId, @Param("tenantId") Long tenantId,
                    @Param("quantity") Integer quantity, @Param("version") Integer version);

    int casDeduct(@Param("productId") Long productId, @Param("tenantId") Long tenantId,
                  @Param("qty") Integer qty, @Param("version") Integer version);
}
