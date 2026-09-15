package com.openbiz.biz.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.openbiz.biz.api.BizService;
import com.openbiz.biz.api.OrderLineRequest;
import com.openbiz.biz.domain.OpenbizAccount;
import com.openbiz.biz.domain.OpenbizAccountLedger;
import com.openbiz.biz.domain.OpenbizCustomer;
import com.openbiz.biz.domain.OpenbizItem;
import com.openbiz.biz.domain.OpenbizOrder;
import com.openbiz.biz.domain.OpenbizOrderItem;
import com.openbiz.biz.mapper.OpenbizAccountLedgerMapper;
import com.openbiz.biz.mapper.OpenbizAccountMapper;
import com.openbiz.biz.mapper.OpenbizCustomerMapper;
import com.openbiz.biz.mapper.OpenbizItemMapper;
import com.openbiz.biz.mapper.OpenbizOrderItemMapper;
import com.openbiz.biz.mapper.OpenbizOrderMapper;
import com.openbiz.biz.money.Money;
import com.openbiz.biz.support.BizTenantGuard;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;

@Service
public class BizServiceImpl implements BizService
{
    private static final int CAS_RETRY = 16;

    private final OpenbizCustomerMapper customerMapper;
    private final OpenbizAccountMapper accountMapper;
    private final OpenbizAccountLedgerMapper ledgerMapper;
    private final OpenbizItemMapper itemMapper;
    private final OpenbizOrderMapper orderMapper;
    private final OpenbizOrderItemMapper orderItemMapper;

    public BizServiceImpl(OpenbizCustomerMapper customerMapper,
                          OpenbizAccountMapper accountMapper,
                          OpenbizAccountLedgerMapper ledgerMapper,
                          OpenbizItemMapper itemMapper,
                          OpenbizOrderMapper orderMapper,
                          OpenbizOrderItemMapper orderItemMapper)
    {
        this.customerMapper = customerMapper;
        this.accountMapper = accountMapper;
        this.ledgerMapper = ledgerMapper;
        this.itemMapper = itemMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpenbizCustomer createCustomer(String name, String phone)
    {
        Long tenantId = BizTenantGuard.requireTenantId();
        if (!StringUtils.hasText(name) || !StringUtils.hasText(phone))
        {
            throw new ServiceException("name and phone are required", HttpStatus.BAD_REQUEST);
        }
        Date now = new Date();
        OpenbizCustomer customer = new OpenbizCustomer();
        customer.setTenantId(tenantId);
        customer.setName(name.trim());
        customer.setPhone(phone.trim());
        customer.setStatus("ACTIVE");
        customer.setCreateTime(now);
        customer.setUpdateTime(now);
        customerMapper.insert(customer);

        OpenbizAccount account = new OpenbizAccount();
        account.setTenantId(tenantId);
        account.setCustomerId(customer.getId());
        account.setBalance(Money.of(BigDecimal.ZERO));
        account.setVersion(0);
        account.setStatus("ACTIVE");
        account.setCreateTime(now);
        account.setUpdateTime(now);
        accountMapper.insert(account);
        return customer;
    }

    @Override
    public OpenbizCustomer getCustomer(Long customerId)
    {
        Long tenantId = BizTenantGuard.requireTenantId();
        OpenbizCustomer customer = customerMapper.selectByIdAndTenant(customerId, tenantId);
        if (customer == null)
        {
            throw new ServiceException("customer not found in current tenant", HttpStatus.NOT_FOUND);
        }
        return customer;
    }

    @Override
    public OpenbizAccount getAccount(Long customerId)
    {
        Long tenantId = BizTenantGuard.requireTenantId();
        getCustomer(customerId);
        OpenbizAccount account = accountMapper.selectByCustomerAndTenant(customerId, tenantId);
        if (account == null)
        {
            throw new ServiceException("account not found in current tenant", HttpStatus.NOT_FOUND);
        }
        return account;
    }

    @Override
    public OpenbizItem getItemByCode(String itemCode)
    {
        Long tenantId = BizTenantGuard.requireTenantId();
        if (!StringUtils.hasText(itemCode))
        {
            throw new ServiceException("itemCode is required", HttpStatus.BAD_REQUEST);
        }
        OpenbizItem item = itemMapper.selectByCodeAndTenant(itemCode.trim(), tenantId);
        if (item == null)
        {
            throw new ServiceException("item not found in current tenant", HttpStatus.NOT_FOUND);
        }
        return item;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpenbizAccount recharge(Long customerId, BigDecimal amount, String idempotentKey)
    {
        Long tenantId = BizTenantGuard.requireTenantId();
        BigDecimal money = Money.of(amount);
        if (!Money.isPositive(money))
        {
            throw new ServiceException("recharge amount must be positive", HttpStatus.BAD_REQUEST);
        }
        requireKey(idempotentKey);

        OpenbizAccountLedger existing = ledgerMapper.selectByIdempotent(tenantId, idempotentKey);
        if (existing != null)
        {
            if (!OpenbizAccountLedger.TYPE_RECHARGE.equals(existing.getTxnType()))
            {
                throw new ServiceException("idempotentKey already used", HttpStatus.BAD_REQUEST);
            }
            OpenbizAccount replay = accountMapper.selectByIdAndTenant(existing.getAccountId(), tenantId);
            if (replay == null)
            {
                throw new ServiceException("account not found in current tenant", HttpStatus.NOT_FOUND);
            }
            return replay;
        }

        OpenbizAccount account = requireAccount(customerId, tenantId);
        applyBalanceChange(account, money, OpenbizAccountLedger.TYPE_RECHARGE, "RECHARGE", null, idempotentKey);
        return accountMapper.selectByCustomerAndTenant(customerId, tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpenbizOrder placeOrder(Long customerId, List<OrderLineRequest> lines, String idempotentKey)
    {
        Long tenantId = BizTenantGuard.requireTenantId();
        requireKey(idempotentKey);
        OpenbizOrder existing = orderMapper.selectByIdempotent(tenantId, idempotentKey);
        if (existing != null)
        {
            return existing;
        }
        OpenbizAccountLedger existingLedger = ledgerMapper.selectByIdempotent(tenantId, idempotentKey);
        if (existingLedger != null)
        {
            throw new ServiceException("idempotentKey already used", HttpStatus.BAD_REQUEST);
        }

        if (lines == null || lines.isEmpty())
        {
            throw new ServiceException("order lines are required", HttpStatus.BAD_REQUEST);
        }

        OpenbizAccount snapshot = requireAccount(customerId, tenantId);
        BigDecimal total = BigDecimal.ZERO.setScale(Money.SCALE);
        java.util.ArrayList<OpenbizOrderItem> prepared = new java.util.ArrayList<>();
        for (OrderLineRequest line : lines)
        {
            if (line == null || line.getItemId() == null || line.getQuantity() == null || line.getQuantity() <= 0)
            {
                throw new ServiceException("itemId and positive quantity are required", HttpStatus.BAD_REQUEST);
            }
            OpenbizItem item = itemMapper.selectByIdAndTenant(line.getItemId(), tenantId);
            if (item == null)
            {
                throw new ServiceException("item not found in current tenant", HttpStatus.NOT_FOUND);
            }
            BigDecimal unit = Money.of(item.getPrice());
            BigDecimal lineAmt = Money.of(unit.multiply(BigDecimal.valueOf(line.getQuantity())));
            total = Money.of(total.add(lineAmt));
            OpenbizOrderItem oi = new OpenbizOrderItem();
            oi.setTenantId(tenantId);
            oi.setItemId(item.getId());
            oi.setQuantity(line.getQuantity());
            oi.setUnitPrice(unit);
            oi.setAmount(lineAmt);
            prepared.add(oi);
        }
        if (!Money.isPositive(total))
        {
            throw new ServiceException("order total must be positive", HttpStatus.BAD_REQUEST);
        }
        if (Money.of(snapshot.getBalance()).compareTo(total) < 0)
        {
            throw new ServiceException("INSUFFICIENT_BALANCE", HttpStatus.BAD_REQUEST);
        }

        Date now = new Date();
        OpenbizOrder order = new OpenbizOrder();
        order.setTenantId(tenantId);
        order.setCustomerId(customerId);
        order.setTotalAmount(total);
        order.setStatus(OpenbizOrder.STATUS_SUCCESS);
        order.setIdempotentKey(idempotentKey);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        try
        {
            orderMapper.insert(order);
        }
        catch (DuplicateKeyException ex)
        {
            OpenbizOrder raced = orderMapper.selectByIdempotent(tenantId, idempotentKey);
            if (raced != null)
            {
                return raced;
            }
            throw new ServiceException("duplicate order in progress", HttpStatus.ERROR);
        }

        for (OpenbizOrderItem oi : prepared)
        {
            oi.setOrderId(order.getId());
            orderItemMapper.insert(oi);
        }

        OpenbizAccount account = requireAccount(customerId, tenantId);
        applyBalanceChange(account, total.negate(), OpenbizAccountLedger.TYPE_CONSUME, "ORDER", order.getId(), idempotentKey);
        return orderMapper.selectByIdAndTenant(order.getId(), tenantId);
    }

    @Override
    public OpenbizOrder getOrder(Long orderId)
    {
        Long tenantId = BizTenantGuard.requireTenantId();
        OpenbizOrder order = orderMapper.selectByIdAndTenant(orderId, tenantId);
        if (order == null)
        {
            throw new ServiceException("order not found in current tenant", HttpStatus.NOT_FOUND);
        }
        return order;
    }

    @Override
    public List<OpenbizOrderItem> listOrderItems(Long orderId)
    {
        Long tenantId = BizTenantGuard.requireTenantId();
        getOrder(orderId);
        return orderItemMapper.listByOrderAndTenant(orderId, tenantId);
    }

    @Override
    public List<OpenbizAccountLedger> listLedgers(Long customerId)
    {
        Long tenantId = BizTenantGuard.requireTenantId();
        getCustomer(customerId);
        return ledgerMapper.listByCustomerAndTenant(customerId, tenantId);
    }

    /**
     * Optimistic lock on version. delta &gt; 0 credit, delta &lt; 0 debit.
     */
    private void applyBalanceChange(OpenbizAccount seed, BigDecimal delta, String txnType,
                                    String referenceType, Long referenceId, String idempotentKey)
    {
        Long tenantId = seed.getTenantId();
        Long accountId = seed.getId();
        for (int i = 0; i < CAS_RETRY; i++)
        {
            OpenbizAccount current = accountMapper.selectByIdAndTenant(accountId, tenantId);
            if (current == null)
            {
                throw new ServiceException("account not found in current tenant", HttpStatus.NOT_FOUND);
            }
            BigDecimal before = Money.of(current.getBalance());
            BigDecimal after = Money.of(before.add(delta));
            if (after.compareTo(BigDecimal.ZERO) < 0)
            {
                throw new ServiceException("INSUFFICIENT_BALANCE", HttpStatus.BAD_REQUEST);
            }
            int rows = accountMapper.casUpdateBalance(accountId, tenantId, after, current.getVersion());
            if (rows == 1)
            {
                OpenbizAccountLedger ledger = new OpenbizAccountLedger();
                ledger.setTenantId(tenantId);
                ledger.setAccountId(accountId);
                ledger.setCustomerId(current.getCustomerId());
                ledger.setTxnType(txnType);
                ledger.setAmount(Money.of(delta.abs()));
                ledger.setBalanceBefore(before);
                ledger.setBalanceAfter(after);
                ledger.setReferenceType(referenceType);
                ledger.setReferenceId(referenceId);
                ledger.setIdempotentKey(idempotentKey);
                ledger.setCreateTime(new Date());
                ledgerMapper.insert(ledger);
                return;
            }
        }
        throw new ServiceException("account concurrent update failed", HttpStatus.ERROR);
    }

    private OpenbizAccount requireAccount(Long customerId, Long tenantId)
    {
        OpenbizCustomer customer = customerMapper.selectByIdAndTenant(customerId, tenantId);
        if (customer == null)
        {
            throw new ServiceException("customer not found in current tenant", HttpStatus.NOT_FOUND);
        }
        OpenbizAccount account = accountMapper.selectByCustomerAndTenant(customerId, tenantId);
        if (account == null)
        {
            throw new ServiceException("account not found in current tenant", HttpStatus.NOT_FOUND);
        }
        return account;
    }

    private static void requireKey(String idempotentKey)
    {
        if (!StringUtils.hasText(idempotentKey))
        {
            throw new ServiceException("idempotentKey is required", HttpStatus.BAD_REQUEST);
        }
    }
}
