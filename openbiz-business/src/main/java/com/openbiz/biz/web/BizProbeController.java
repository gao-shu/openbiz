package com.openbiz.biz.web;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.openbiz.biz.api.BizService;
import com.openbiz.biz.api.OrderLineRequest;
import com.openbiz.biz.domain.OpenbizAccount;
import com.openbiz.biz.domain.OpenbizCustomer;
import com.openbiz.biz.domain.OpenbizOrder;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * Phase 1 business probe. Login + TenantContext required.
 */
@RestController
@RequestMapping("/openbiz/test/biz")
public class BizProbeController
{
    private final BizService bizService;

    public BizProbeController(BizService bizService)
    {
        this.bizService = bizService;
    }

    @PostMapping("/customers")
    public AjaxResult createCustomer(@RequestBody Map<String, Object> body)
    {
        String name = body == null || body.get("name") == null ? null : String.valueOf(body.get("name"));
        String phone = body == null || body.get("phone") == null ? null : String.valueOf(body.get("phone"));
        OpenbizCustomer c = bizService.createCustomer(name, phone);
        Map<String, Object> data = new HashMap<>();
        data.put("customerId", c.getId());
        data.put("name", c.getName());
        data.put("phone", c.getPhone());
        return AjaxResult.success(data);
    }

    @GetMapping("/customers/{customerId}")
    public AjaxResult customer(@PathVariable Long customerId)
    {
        OpenbizCustomer c = bizService.getCustomer(customerId);
        Map<String, Object> data = new HashMap<>();
        data.put("customerId", c.getId());
        data.put("name", c.getName());
        data.put("phone", c.getPhone());
        data.put("status", c.getStatus());
        return AjaxResult.success(data);
    }

    @GetMapping("/items/{itemCode}")
    public AjaxResult item(@PathVariable String itemCode)
    {
        return AjaxResult.success(bizService.getItemByCode(itemCode));
    }

    @PostMapping("/recharge")
    public AjaxResult recharge(@RequestBody Map<String, Object> body)
    {
        Long customerId = Long.valueOf(String.valueOf(body.get("customerId")));
        BigDecimal amount = new BigDecimal(String.valueOf(body.get("amount")));
        String key = String.valueOf(body.get("idempotentKey"));
        OpenbizAccount acc = bizService.recharge(customerId, amount, key);
        Map<String, Object> data = new HashMap<>();
        data.put("customerId", customerId);
        data.put("balance", acc.getBalance());
        data.put("version", acc.getVersion());
        return AjaxResult.success(data);
    }

    @PostMapping("/orders")
    public AjaxResult placeOrder(@RequestBody Map<String, Object> body)
    {
        Long customerId = Long.valueOf(String.valueOf(body.get("customerId")));
        String key = String.valueOf(body.get("idempotentKey"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> raw = (List<Map<String, Object>>) body.get("items");
        java.util.ArrayList<OrderLineRequest> lines = new java.util.ArrayList<>();
        if (raw != null)
        {
            for (Map<String, Object> row : raw)
            {
                lines.add(new OrderLineRequest(
                        Long.valueOf(String.valueOf(row.get("itemId"))),
                        Integer.valueOf(String.valueOf(row.get("quantity")))));
            }
        }
        OpenbizOrder order = bizService.placeOrder(customerId, lines, key);
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("totalAmount", order.getTotalAmount());
        data.put("status", order.getStatus());
        data.put("customerId", order.getCustomerId());
        return AjaxResult.success(data);
    }

    @GetMapping("/accounts/{customerId}")
    public AjaxResult account(@PathVariable Long customerId)
    {
        OpenbizAccount acc = bizService.getAccount(customerId);
        Map<String, Object> data = new HashMap<>();
        data.put("customerId", acc.getCustomerId());
        data.put("balance", acc.getBalance());
        data.put("version", acc.getVersion());
        return AjaxResult.success(data);
    }

    @GetMapping("/ledgers/{customerId}")
    public AjaxResult ledgers(@PathVariable Long customerId)
    {
        return AjaxResult.success(bizService.listLedgers(customerId));
    }

    @GetMapping("/orders/{orderId}")
    public AjaxResult order(@PathVariable Long orderId)
    {
        OpenbizOrder order = bizService.getOrder(orderId);
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("customerId", order.getCustomerId());
        data.put("totalAmount", order.getTotalAmount());
        data.put("status", order.getStatus());
        data.put("items", bizService.listOrderItems(orderId));
        return AjaxResult.success(data);
    }
}
