package com.openbiz.biz.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.biz.domain.OpenbizCustomer;

public interface OpenbizCustomerMapper
{
    int insert(OpenbizCustomer customer);

    OpenbizCustomer selectByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
