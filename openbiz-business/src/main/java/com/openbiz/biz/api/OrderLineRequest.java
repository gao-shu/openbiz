package com.openbiz.biz.api;

public class OrderLineRequest
{
    private Long itemId;
    private Integer quantity;

    public OrderLineRequest()
    {
    }

    public OrderLineRequest(Long itemId, Integer quantity)
    {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
