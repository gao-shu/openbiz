package com.openbiz.saas.mapper;

import org.apache.ibatis.annotations.Param;
import com.openbiz.saas.domain.OpenbizTenant;

/**
 * Tenant mapper.
 */
public interface OpenbizTenantMapper
{
    OpenbizTenant selectById(@Param("id") Long id);

    OpenbizTenant selectByCode(@Param("tenantCode") String tenantCode);
}
