package com.openbiz.biz.api;

import java.math.BigDecimal;
import java.util.List;
import com.openbiz.biz.domain.OpenbizAccount;
import com.openbiz.biz.domain.OpenbizAccountLedger;
import com.openbiz.biz.domain.OpenbizCustomer;
import com.openbiz.biz.domain.OpenbizItem;
import com.openbiz.biz.domain.OpenbizOrder;
import com.openbiz.biz.domain.OpenbizOrderItem;

/**
 * Minimal business core: customer, recharge, paid order.
 */
public interface BizService
{
    OpenbizCustomer createCustomer(String name, String phone);

    OpenbizCustomer getCustomer(Long customerId);

    OpenbizAccount getAccount(Long customerId);

    OpenbizItem getItemByCode(String itemCode);

    OpenbizAccount recharge(Long customerId, BigDecimal amount, String idempotentKey);

    OpenbizOrder placeOrder(Long customerId, List<OrderLineRequest> lines, String idempotentKey);

    OpenbizOrder getOrder(Long orderId);

    List<OpenbizOrderItem> listOrderItems(Long orderId);

    List<OpenbizAccountLedger> listLedgers(Long customerId);
}
