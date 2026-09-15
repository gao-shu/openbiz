package com.openbiz.shop.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.shop.domain.OpenbizShopProduct;

public interface OpenbizShopProductMapper
{
    int insert(OpenbizShopProduct row);

    OpenbizShopProduct selectByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    OpenbizShopProduct selectByCodeAndTenant(@Param("productCode") String productCode, @Param("tenantId") Long tenantId);
}
