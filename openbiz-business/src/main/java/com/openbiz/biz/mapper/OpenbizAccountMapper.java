package com.openbiz.biz.mapper;

import java.math.BigDecimal;
import org.apache.ibatis.annotations.Param;
import com.openbiz.biz.domain.OpenbizAccount;

public interface OpenbizAccountMapper
{
    int insert(OpenbizAccount account);

    OpenbizAccount selectByCustomerAndTenant(@Param("customerId") Long customerId, @Param("tenantId") Long tenantId);

    OpenbizAccount selectByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    int casUpdateBalance(@Param("id") Long id,
                         @Param("tenantId") Long tenantId,
                         @Param("newBalance") BigDecimal newBalance,
                         @Param("version") Integer version);
}
