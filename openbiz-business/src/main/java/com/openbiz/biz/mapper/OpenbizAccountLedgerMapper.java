package com.openbiz.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.openbiz.biz.domain.OpenbizAccountLedger;

public interface OpenbizAccountLedgerMapper
{
    int insert(OpenbizAccountLedger ledger);

    OpenbizAccountLedger selectByIdempotent(@Param("tenantId") Long tenantId, @Param("idempotentKey") String idempotentKey);

    List<OpenbizAccountLedger> listByCustomerAndTenant(@Param("customerId") Long customerId, @Param("tenantId") Long tenantId);
}
