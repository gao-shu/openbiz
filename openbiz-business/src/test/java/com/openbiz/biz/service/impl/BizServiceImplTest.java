package com.openbiz.biz.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import com.openbiz.biz.api.OrderLineRequest;
import com.openbiz.biz.domain.OpenbizAccount;
import com.openbiz.biz.domain.OpenbizAccountLedger;
import com.openbiz.biz.domain.OpenbizCustomer;
import com.openbiz.biz.domain.OpenbizItem;
import com.openbiz.biz.domain.OpenbizOrder;
import com.openbiz.biz.mapper.OpenbizAccountLedgerMapper;
import com.openbiz.biz.mapper.OpenbizAccountMapper;
import com.openbiz.biz.mapper.OpenbizCustomerMapper;
import com.openbiz.biz.mapper.OpenbizItemMapper;
import com.openbiz.biz.mapper.OpenbizOrderItemMapper;
import com.openbiz.biz.mapper.OpenbizOrderMapper;
import com.openbiz.saas.context.TenantContext;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class BizServiceImplTest
{
    @Mock
    private OpenbizCustomerMapper customerMapper;
    @Mock
    private OpenbizAccountMapper accountMapper;
    @Mock
    private OpenbizAccountLedgerMapper ledgerMapper;
    @Mock
    private OpenbizItemMapper itemMapper;
    @Mock
    private OpenbizOrderMapper orderMapper;
    @Mock
    private OpenbizOrderItemMapper orderItemMapper;
    @InjectMocks
    private BizServiceImpl bizService;

    @BeforeEach
    void setTenant()
    {
        TenantContext.setTenantId(1L);
    }

    @AfterEach
    void clearTenant()
    {
        TenantContext.clear();
    }

    @Test
    void customerAndAccountCreatedTogether()
    {
        when(customerMapper.insert(any())).thenAnswer(inv -> {
            ((OpenbizCustomer) inv.getArgument(0)).setId(10L);
            return 1;
        });
        OpenbizCustomer c = bizService.createCustomer("Alice", "13800000001");
        assertEquals(10L, c.getId());
        ArgumentCaptor<OpenbizAccount> acc = ArgumentCaptor.forClass(OpenbizAccount.class);
        verify(accountMapper).insert(acc.capture());
        assertEquals(10L, acc.getValue().getCustomerId());
        assertEquals(new BigDecimal("0.00"), acc.getValue().getBalance());
        assertEquals(0, acc.getValue().getVersion());
    }

    @Test
    void rechargeAddsBalanceAndLedger()
    {
        stubCustomerAndAccount(10L, 100L, "0.00", 0);
        when(ledgerMapper.selectByIdempotent(1L, "r1")).thenReturn(null);
        when(accountMapper.selectByIdAndTenant(100L, 1L)).thenReturn(account(100L, 10L, "0.00", 0));
        when(accountMapper.casUpdateBalance(eq(100L), eq(1L), eq(new BigDecimal("100.00")), eq(0))).thenReturn(1);
        when(accountMapper.selectByCustomerAndTenant(10L, 1L)).thenReturn(account(100L, 10L, "100.00", 1));

        OpenbizAccount after = bizService.recharge(10L, new BigDecimal("100"), "r1");
        assertEquals(new BigDecimal("100.00"), after.getBalance());
        ArgumentCaptor<OpenbizAccountLedger> cap = ArgumentCaptor.forClass(OpenbizAccountLedger.class);
        verify(ledgerMapper).insert(cap.capture());
        assertEquals(OpenbizAccountLedger.TYPE_RECHARGE, cap.getValue().getTxnType());
        assertEquals(new BigDecimal("100.00"), cap.getValue().getAmount());
        assertEquals(new BigDecimal("0.00"), cap.getValue().getBalanceBefore());
        assertEquals(new BigDecimal("100.00"), cap.getValue().getBalanceAfter());
    }

    @Test
    void consumeDebitsAndFreezesUnitPrice()
    {
        stubCustomerAndAccount(10L, 100L, "100.00", 1);
        when(orderMapper.selectByIdempotent(1L, "c1")).thenReturn(null);
        when(ledgerMapper.selectByIdempotent(1L, "c1")).thenReturn(null);
        when(itemMapper.selectByIdAndTenant(1L, 1L)).thenReturn(item(1L, "30.00"));
        when(orderMapper.insert(any())).thenAnswer(inv -> {
            ((OpenbizOrder) inv.getArgument(0)).setId(50L);
            return 1;
        });
        when(accountMapper.selectByIdAndTenant(100L, 1L)).thenReturn(account(100L, 10L, "100.00", 1));
        when(accountMapper.casUpdateBalance(eq(100L), eq(1L), eq(new BigDecimal("70.00")), eq(1))).thenReturn(1);
        when(orderMapper.selectByIdAndTenant(50L, 1L)).thenAnswer(inv -> {
            OpenbizOrder o = new OpenbizOrder();
            o.setId(50L);
            o.setTotalAmount(new BigDecimal("30.00"));
            o.setStatus(OpenbizOrder.STATUS_SUCCESS);
            o.setCustomerId(10L);
            return o;
        });

        OpenbizOrder order = bizService.placeOrder(10L, List.of(new OrderLineRequest(1L, 1)), "c1");
        assertEquals(new BigDecimal("30.00"), order.getTotalAmount());
        verify(orderItemMapper).insert(any());
        ArgumentCaptor<OpenbizAccountLedger> cap = ArgumentCaptor.forClass(OpenbizAccountLedger.class);
        verify(ledgerMapper).insert(cap.capture());
        assertEquals(OpenbizAccountLedger.TYPE_CONSUME, cap.getValue().getTxnType());
        assertEquals(new BigDecimal("30.00"), cap.getValue().getAmount());
        assertEquals(new BigDecimal("70.00"), cap.getValue().getBalanceAfter());
    }

    @Test
    void duplicateRechargeDoesNotChangeBalanceTwice()
    {
        OpenbizAccountLedger existing = new OpenbizAccountLedger();
        existing.setAccountId(100L);
        existing.setTxnType(OpenbizAccountLedger.TYPE_RECHARGE);
        when(ledgerMapper.selectByIdempotent(1L, "r1")).thenReturn(existing);
        when(accountMapper.selectByIdAndTenant(100L, 1L)).thenReturn(account(100L, 10L, "100.00", 1));

        OpenbizAccount after = bizService.recharge(10L, new BigDecimal("100"), "r1");
        assertEquals(new BigDecimal("100.00"), after.getBalance());
        verify(accountMapper, never()).casUpdateBalance(any(), any(), any(), any());
        verify(ledgerMapper, never()).insert(any());
    }

    @Test
    void duplicateConsumeReturnsExistingOrderWithoutDebit()
    {
        OpenbizOrder existing = new OpenbizOrder();
        existing.setId(50L);
        existing.setTotalAmount(new BigDecimal("30.00"));
        existing.setStatus(OpenbizOrder.STATUS_SUCCESS);
        when(orderMapper.selectByIdempotent(1L, "c1")).thenReturn(existing);

        OpenbizOrder again = bizService.placeOrder(10L, List.of(new OrderLineRequest(1L, 1)), "c1");
        assertEquals(50L, again.getId());
        verify(accountMapper, never()).casUpdateBalance(any(), any(), any(), any());
        verify(ledgerMapper, never()).insert(any());
        verify(orderMapper, never()).insert(any());
    }

    @Test
    void insufficientBalanceDoesNotInsertOrderOrLedger()
    {
        stubCustomerAndAccount(10L, 100L, "50.00", 1);
        when(orderMapper.selectByIdempotent(1L, "c-fail")).thenReturn(null);
        when(ledgerMapper.selectByIdempotent(1L, "c-fail")).thenReturn(null);
        when(itemMapper.selectByIdAndTenant(2L, 1L)).thenReturn(item(2L, "100.00"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> bizService.placeOrder(10L, List.of(new OrderLineRequest(2L, 1)), "c-fail"));
        assertTrue(ex.getMessage().contains("INSUFFICIENT"));
        verify(orderMapper, never()).insert(any());
        verify(ledgerMapper, never()).insert(any());
        verify(accountMapper, never()).casUpdateBalance(any(), any(), any(), any());
    }

    @Test
    void crossTenantCustomerNotFound()
    {
        when(customerMapper.selectByIdAndTenant(99L, 1L)).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> bizService.getCustomer(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getCode());
    }

    @Test
    void crossTenantAccountNotFound()
    {
        when(customerMapper.selectByIdAndTenant(99L, 1L)).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> bizService.getAccount(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getCode());
    }

    @Test
    void crossTenantOrderNotFound()
    {
        when(orderMapper.selectByIdAndTenant(77L, 1L)).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> bizService.getOrder(77L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getCode());
    }

    @Test
    void hairCutUsesServiceItemWithoutCoreChange()
    {
        stubCustomerAndAccount(10L, 100L, "500.00", 1);
        when(orderMapper.selectByIdempotent(1L, "hair-cut")).thenReturn(null);
        when(ledgerMapper.selectByIdempotent(1L, "hair-cut")).thenReturn(null);
        when(itemMapper.selectByIdAndTenant(1L, 1L)).thenReturn(serviceItem(1L, "CUT", "50.00"));
        when(orderMapper.insert(any())).thenAnswer(inv -> {
            OpenbizOrder o = inv.getArgument(0);
            assertEquals(new BigDecimal("50.00"), o.getTotalAmount());
            o.setId(80L);
            return 1;
        });
        when(accountMapper.selectByIdAndTenant(100L, 1L)).thenReturn(account(100L, 10L, "500.00", 1));
        when(accountMapper.casUpdateBalance(eq(100L), eq(1L), eq(new BigDecimal("450.00")), eq(1))).thenReturn(1);
        when(orderMapper.selectByIdAndTenant(80L, 1L)).thenAnswer(inv -> {
            OpenbizOrder o = new OpenbizOrder();
            o.setId(80L);
            o.setTotalAmount(new BigDecimal("50.00"));
            o.setStatus(OpenbizOrder.STATUS_SUCCESS);
            return o;
        });
        OpenbizOrder order = bizService.placeOrder(10L, List.of(new OrderLineRequest(1L, 1)), "hair-cut");
        assertEquals(new BigDecimal("50.00"), order.getTotalAmount());
    }

    @Test
    void waterStationQtyTwoUsesProductItemWithoutCoreChange()
    {
        TenantContext.setTenantId(2L);
        stubCustomerAndAccountForTenant(2L, 20L, 200L, "500.00", 1);
        when(orderMapper.selectByIdempotent(2L, "water-2")).thenReturn(null);
        when(ledgerMapper.selectByIdempotent(2L, "water-2")).thenReturn(null);
        when(itemMapper.selectByIdAndTenant(3L, 2L)).thenReturn(productItem(3L, "WATER", "20.00"));
        when(orderMapper.insert(any())).thenAnswer(inv -> {
            OpenbizOrder o = inv.getArgument(0);
            assertEquals(new BigDecimal("40.00"), o.getTotalAmount());
            o.setId(90L);
            return 1;
        });
        when(accountMapper.selectByIdAndTenant(200L, 2L)).thenReturn(account(200L, 20L, "500.00", 1));
        when(accountMapper.casUpdateBalance(eq(200L), eq(2L), eq(new BigDecimal("460.00")), eq(1))).thenReturn(1);
        when(orderMapper.selectByIdAndTenant(90L, 2L)).thenAnswer(inv -> {
            OpenbizOrder o = new OpenbizOrder();
            o.setId(90L);
            o.setTotalAmount(new BigDecimal("40.00"));
            o.setStatus(OpenbizOrder.STATUS_SUCCESS);
            return o;
        });
        OpenbizOrder order = bizService.placeOrder(20L, List.of(new OrderLineRequest(3L, 2)), "water-2");
        assertEquals(new BigDecimal("40.00"), order.getTotalAmount());
    }

    @Test
    void casRetryOnVersionConflictThenSucceeds()
    {
        stubCustomerAndAccount(10L, 100L, "0.00", 0);
        when(ledgerMapper.selectByIdempotent(1L, "r-cas")).thenReturn(null);
        when(accountMapper.selectByIdAndTenant(100L, 1L))
                .thenReturn(account(100L, 10L, "0.00", 0))
                .thenReturn(account(100L, 10L, "0.00", 1));
        when(accountMapper.casUpdateBalance(eq(100L), eq(1L), eq(new BigDecimal("100.00")), eq(0))).thenReturn(0);
        when(accountMapper.casUpdateBalance(eq(100L), eq(1L), eq(new BigDecimal("100.00")), eq(1))).thenReturn(1);
        when(accountMapper.selectByCustomerAndTenant(10L, 1L)).thenReturn(account(100L, 10L, "100.00", 2));

        OpenbizAccount after = bizService.recharge(10L, new BigDecimal("100"), "r-cas");
        assertEquals(new BigDecimal("100.00"), after.getBalance());
        verify(accountMapper, times(2)).casUpdateBalance(eq(100L), eq(1L), eq(new BigDecimal("100.00")), any());
        verify(ledgerMapper, times(1)).insert(any());
    }

    @Test
    void rejectsWithoutTenant()
    {
        TenantContext.clear();
        ServiceException ex = assertThrows(ServiceException.class, () -> bizService.createCustomer("x", "1"));
        assertTrue(ex.getMessage().contains("NO_TENANT"));
    }

    @Test
    void itemFromOtherTenantNotFound()
    {
        stubCustomerAndAccount(10L, 100L, "500.00", 1);
        when(orderMapper.selectByIdempotent(1L, "x")).thenReturn(null);
        when(ledgerMapper.selectByIdempotent(1L, "x")).thenReturn(null);
        when(itemMapper.selectByIdAndTenant(3L, 1L)).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class,
                () -> bizService.placeOrder(10L, List.of(new OrderLineRequest(3L, 1)), "x"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getCode());
    }

    private void stubCustomerAndAccount(Long customerId, Long accountId, String balance, int version)
    {
        stubCustomerAndAccountForTenant(1L, customerId, accountId, balance, version);
    }

    private void stubCustomerAndAccountForTenant(Long tenantId, Long customerId, Long accountId, String balance, int version)
    {
        OpenbizCustomer c = new OpenbizCustomer();
        c.setId(customerId);
        c.setTenantId(tenantId);
        when(customerMapper.selectByIdAndTenant(customerId, tenantId)).thenReturn(c);
        when(accountMapper.selectByCustomerAndTenant(customerId, tenantId))
                .thenReturn(account(accountId, customerId, balance, version));
    }

    private static OpenbizAccount account(Long id, Long customerId, String balance, int version)
    {
        OpenbizAccount a = new OpenbizAccount();
        a.setId(id);
        a.setTenantId(TenantContext.getTenantId());
        a.setCustomerId(customerId);
        a.setBalance(new BigDecimal(balance));
        a.setVersion(version);
        return a;
    }

    private static OpenbizItem item(Long id, String price)
    {
        OpenbizItem item = new OpenbizItem();
        item.setId(id);
        item.setPrice(new BigDecimal(price));
        item.setItemType(OpenbizItem.TYPE_SERVICE);
        return item;
    }

    private static OpenbizItem serviceItem(Long id, String code, String price)
    {
        OpenbizItem item = item(id, price);
        item.setItemCode(code);
        item.setItemType(OpenbizItem.TYPE_SERVICE);
        return item;
    }

    private static OpenbizItem productItem(Long id, String code, String price)
    {
        OpenbizItem item = item(id, price);
        item.setItemCode(code);
        item.setItemType(OpenbizItem.TYPE_PRODUCT);
        return item;
    }
}
