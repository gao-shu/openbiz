package com.openbiz.shop.api;

public final class ShopOrderLineRequest
{
    private final Long productId;
    private final int quantity;

    public ShopOrderLineRequest(Long productId, int quantity)
    {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId()
    {
        return productId;
    }

    public int getQuantity()
    {
        return quantity;
    }
}
