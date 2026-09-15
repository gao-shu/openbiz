package com.openbiz.shop.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.openbiz.saas.context.TenantContext;
import com.openbiz.shop.api.OrderLineRequest;
import com.openbiz.shop.domain.OpenbizShopInventory;
import com.openbiz.shop.domain.OpenbizShopOrder;
import com.openbiz.shop.domain.OpenbizShopProduct;
import com.openbiz.shop.mapper.OpenbizShopInventoryMapper;
import com.openbiz.shop.mapper.OpenbizShopOrderItemMapper;
import com.openbiz.shop.mapper.OpenbizShopOrderMapper;
import com.openbiz.shop.mapper.OpenbizShopProductMapper;
import com.openbiz.shop.port.CurrentUserPort;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class ShopServiceImplTest
{
    @Mock
    private OpenbizShopProductMapper productMapper;
    @Mock
    private OpenbizShopInventoryMapper inventoryMapper;
    @Mock
    private OpenbizShopOrderMapper orderMapper;
    @Mock
    private OpenbizShopOrderItemMapper orderItemMapper;
    @Mock
    private CurrentUserPort currentUserPort;
    @InjectMocks
    private ShopServiceImpl shopService;

    @BeforeEach
    void setUp()
    {
        TenantContext.setTenantId(1L);
        when(currentUserPort.requireUserId()).thenReturn(10L);
    }

    @AfterEach
    void tearDown()
    {
        TenantContext.clear();
    }

    @Test
    void createProduct_withInventory()
    {
        when(productMapper.selectByCodeAndTenant("SKU1", 1L)).thenReturn(null);
        when(productMapper.insert(any())).thenAnswer(inv -> {
            ((OpenbizShopProduct) inv.getArgument(0)).setId(5L);
            return 1;
        });
        when(productMapper.selectByIdAndTenant(5L, 1L)).thenAnswer(inv -> product(5L, "SKU1", "10.00"));

        OpenbizShopProduct p = shopService.createProduct("SKU1", "Water", new BigDecimal("10"), 3);
        assertEquals(5L, p.getId());
        ArgumentCaptor<OpenbizShopInventory> inv = ArgumentCaptor.forClass(OpenbizShopInventory.class);
        verify(inventoryMapper).insert(inv.capture());
        assertEquals(3, inv.getValue().getQuantity());
        assertEquals(0, inv.getValue().getVersion());
    }

    @Test
    void placeOrder_success_casDeductAndSnapshot()
    {
        when(orderMapper.selectByIdempotent(1L, "k1")).thenReturn(null);
        when(productMapper.selectByIdAndTenant(5L, 1L)).thenReturn(product(5L, "SKU1", "10.00"));
        when(inventoryMapper.selectByProductAndTenant(5L, 1L)).thenReturn(inventory(5L, 2, 0));
        when(inventoryMapper.casDeduct(5L, 1L, 1, 0)).thenReturn(1);
        when(orderMapper.insert(any())).thenAnswer(inv -> {
            ((OpenbizShopOrder) inv.getArgument(0)).setId(99L);
            return 1;
        });
        when(orderMapper.selectByIdAndTenant(99L, 1L)).thenAnswer(inv -> {
            OpenbizShopOrder o = new OpenbizShopOrder();
            o.setId(99L);
            o.setTenantId(1L);
            o.setTotalAmount(new BigDecimal("10.00"));
            o.setStatus(OpenbizShopOrder.STATUS_SUCCESS);
            return o;
        });

        OpenbizShopOrder order = shopService.placeOrder("Ann", "13900000001",
                List.of(new OrderLineRequest(5L, 1)), "k1");
        assertEquals(OpenbizShopOrder.STATUS_SUCCESS, order.getStatus());
        assertEquals(new BigDecimal("10.00"), order.getTotalAmount());
        verify(inventoryMapper).casDeduct(5L, 1L, 1, 0);
        verify(orderItemMapper).insert(any());
    }

    @Test
    void placeOrder_idempotentReplay()
    {
        OpenbizShopOrder existing = new OpenbizShopOrder();
        existing.setId(7L);
        existing.setStatus(OpenbizShopOrder.STATUS_SUCCESS);
        when(orderMapper.selectByIdempotent(1L, "k1")).thenReturn(existing);

        OpenbizShopOrder order = shopService.placeOrder("Ann", "139", List.of(new OrderLineRequest(5L, 1)), "k1");
        assertEquals(7L, order.getId());
        verify(inventoryMapper, never()).casDeduct(any(), any(), any(), any());
        verify(orderMapper, never()).insert(any());
    }

    @Test
    void placeOrder_insufficientStock()
    {
        when(orderMapper.selectByIdempotent(1L, "k2")).thenReturn(null);
        when(productMapper.selectByIdAndTenant(5L, 1L)).thenReturn(product(5L, "SKU1", "10.00"));
        when(inventoryMapper.selectByProductAndTenant(5L, 1L)).thenReturn(inventory(5L, 0, 1));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> shopService.placeOrder("Ann", "139", List.of(new OrderLineRequest(5L, 1)), "k2"));
        assertTrue(ex.getMessage().contains("INSUFFICIENT_STOCK"));
        verify(orderMapper, never()).insert(any());
    }

    @Test
    void placeOrder_casRetryThenSuccess()
    {
        when(orderMapper.selectByIdempotent(1L, "k3")).thenReturn(null);
        when(productMapper.selectByIdAndTenant(5L, 1L)).thenReturn(product(5L, "SKU1", "10.00"));
        when(inventoryMapper.selectByProductAndTenant(5L, 1L))
                .thenReturn(inventory(5L, 1, 0))
                .thenReturn(inventory(5L, 1, 1));
        when(inventoryMapper.casDeduct(eq(5L), eq(1L), eq(1), eq(0))).thenReturn(0);
        when(inventoryMapper.casDeduct(eq(5L), eq(1L), eq(1), eq(1))).thenReturn(1);
        when(orderMapper.insert(any())).thenAnswer(inv -> {
            ((OpenbizShopOrder) inv.getArgument(0)).setId(8L);
            return 1;
        });
        when(orderMapper.selectByIdAndTenant(8L, 1L)).thenAnswer(inv -> {
            OpenbizShopOrder o = new OpenbizShopOrder();
            o.setId(8L);
            o.setStatus(OpenbizShopOrder.STATUS_SUCCESS);
            o.setTotalAmount(new BigDecimal("10.00"));
            return o;
        });

        OpenbizShopOrder order = shopService.placeOrder("Ann", "139", List.of(new OrderLineRequest(5L, 1)), "k3");
        assertEquals(8L, order.getId());
    }

    @Test
    void getOrder_crossTenant_notFound()
    {
        when(orderMapper.selectByIdAndTenant(1L, 1L)).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> shopService.getOrder(1L));
        assertEquals(Integer.valueOf(HttpStatus.NOT_FOUND), ex.getCode());
    }

    @Test
    void noTenant_rejected()
    {
        TenantContext.clear();
        org.mockito.Mockito.reset(currentUserPort);
        assertThrows(ServiceException.class,
                () -> shopService.createProduct("A", "B", new BigDecimal("1"), 1));
    }

    private static OpenbizShopProduct product(Long id, String code, String price)
    {
        OpenbizShopProduct p = new OpenbizShopProduct();
        p.setId(id);
        p.setTenantId(1L);
        p.setProductCode(code);
        p.setProductName(code);
        p.setPrice(new BigDecimal(price));
        p.setStatus(OpenbizShopProduct.STATUS_ACTIVE);
        return p;
    }

    private static OpenbizShopInventory inventory(Long productId, int qty, int version)
    {
        OpenbizShopInventory inv = new OpenbizShopInventory();
        inv.setId(productId);
        inv.setTenantId(1L);
        inv.setProductId(productId);
        inv.setQuantity(qty);
        inv.setVersion(version);
        return inv;
    }
}
