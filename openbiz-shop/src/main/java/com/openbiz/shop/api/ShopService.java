package com.openbiz.shop.api;

import java.math.BigDecimal;
import java.util.List;
import com.openbiz.shop.domain.OpenbizShopInventory;
import com.openbiz.shop.domain.OpenbizShopOrder;
import com.openbiz.shop.domain.OpenbizShopOrderItem;
import com.openbiz.shop.domain.OpenbizShopProduct;

public interface ShopService
{
    OpenbizShopProduct createProduct(String productCode, String productName, BigDecimal price, int initialQuantity);

    OpenbizShopProduct getProduct(Long productId);

    OpenbizShopInventory setInventory(Long productId, int quantity);

    OpenbizShopInventory getInventory(Long productId);

    OpenbizShopOrder placeOrder(String buyerName, String buyerPhone, List<OrderLineRequest> lines, String idempotentKey);

    OpenbizShopOrder getOrder(Long orderId);

    List<OpenbizShopOrderItem> listOrderItems(Long orderId);
}
