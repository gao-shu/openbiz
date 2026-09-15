package com.openbiz.iot.core.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.openbiz.iot.core.domain.OpenbizProduct;

public interface OpenbizProductMapper
{
    int insert(OpenbizProduct product);

    OpenbizProduct selectByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    List<OpenbizProduct> selectByTenant(@Param("tenantId") Long tenantId);
}
