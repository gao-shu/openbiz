package com.openbiz.shop.api;

public class OrderLineRequest
{
    private final Long productId;
    private final int quantity;

    public OrderLineRequest(Long productId, int quantity)
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
