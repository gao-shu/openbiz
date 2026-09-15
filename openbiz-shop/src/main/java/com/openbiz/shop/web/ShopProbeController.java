package com.openbiz.shop.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.openbiz.shop.api.OrderLineRequest;
import com.openbiz.shop.api.ShopService;
import com.openbiz.shop.domain.OpenbizShopInventory;
import com.openbiz.shop.domain.OpenbizShopOrder;
import com.openbiz.shop.domain.OpenbizShopProduct;
import com.ruoyi.common.core.domain.AjaxResult;

@RestController
@RequestMapping("/openbiz/test/shop")
public class ShopProbeController
{
    private final ShopService shopService;

    public ShopProbeController(ShopService shopService)
    {
        this.shopService = shopService;
    }

    @PostMapping("/products")
    public AjaxResult createProduct(@RequestBody Map<String, Object> body)
    {
        String code = str(body, "productCode");
        String name = str(body, "productName");
        BigDecimal price = new BigDecimal(String.valueOf(body.get("price")));
        int qty = Integer.parseInt(String.valueOf(body.getOrDefault("initialQuantity", "0")));
        OpenbizShopProduct p = shopService.createProduct(code, name, price, qty);
        Map<String, Object> data = new HashMap<>();
        data.put("productId", p.getId());
        data.put("productCode", p.getProductCode());
        data.put("price", p.getPrice());
        return AjaxResult.success(data);
    }

    @GetMapping("/products/{id}")
    public AjaxResult product(@PathVariable Long id)
    {
        OpenbizShopProduct p = shopService.getProduct(id);
        OpenbizShopInventory inv = shopService.getInventory(id);
        Map<String, Object> data = new HashMap<>();
        data.put("productId", p.getId());
        data.put("productCode", p.getProductCode());
        data.put("productName", p.getProductName());
        data.put("price", p.getPrice());
        data.put("quantity", inv.getQuantity());
        data.put("version", inv.getVersion());
        return AjaxResult.success(data);
    }

    @PostMapping("/inventory/set")
    public AjaxResult setInventory(@RequestBody Map<String, Object> body)
    {
        Long productId = Long.valueOf(String.valueOf(body.get("productId")));
        int quantity = Integer.parseInt(String.valueOf(body.get("quantity")));
        OpenbizShopInventory inv = shopService.setInventory(productId, quantity);
        Map<String, Object> data = new HashMap<>();
        data.put("productId", inv.getProductId());
        data.put("quantity", inv.getQuantity());
        data.put("version", inv.getVersion());
        return AjaxResult.success(data);
    }

    @PostMapping("/orders")
    public AjaxResult placeOrder(@RequestBody Map<String, Object> body)
    {
        String buyerName = str(body, "buyerName");
        String buyerPhone = str(body, "buyerPhone");
        String key = str(body, "idempotentKey");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> raw = (List<Map<String, Object>>) body.get("items");
        List<OrderLineRequest> lines = new ArrayList<>();
        if (raw != null)
        {
            for (Map<String, Object> row : raw)
            {
                lines.add(new OrderLineRequest(
                        Long.valueOf(String.valueOf(row.get("productId"))),
                        Integer.parseInt(String.valueOf(row.get("quantity")))));
            }
        }
        OpenbizShopOrder order = shopService.placeOrder(buyerName, buyerPhone, lines, key);
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("totalAmount", order.getTotalAmount());
        data.put("status", order.getStatus());
        return AjaxResult.success(data);
    }

    @GetMapping("/orders/{id}")
    public AjaxResult order(@PathVariable Long id)
    {
        OpenbizShopOrder order = shopService.getOrder(id);
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("buyerName", order.getBuyerName());
        data.put("buyerPhone", order.getBuyerPhone());
        data.put("totalAmount", order.getTotalAmount());
        data.put("status", order.getStatus());
        data.put("items", shopService.listOrderItems(id));
        return AjaxResult.success(data);
    }

    private static String str(Map<String, Object> body, String key)
    {
        if (body == null || body.get(key) == null)
        {
            return null;
        }
        return String.valueOf(body.get(key));
    }
}
