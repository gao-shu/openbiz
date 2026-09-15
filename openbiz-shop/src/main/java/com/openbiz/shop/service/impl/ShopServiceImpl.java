package com.openbiz.shop.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.openbiz.shop.api.OrderLineRequest;
import com.openbiz.shop.api.ShopService;
import com.openbiz.shop.domain.OpenbizShopInventory;
import com.openbiz.shop.domain.OpenbizShopOrder;
import com.openbiz.shop.domain.OpenbizShopOrderItem;
import com.openbiz.shop.domain.OpenbizShopProduct;
import com.openbiz.shop.mapper.OpenbizShopInventoryMapper;
import com.openbiz.shop.mapper.OpenbizShopOrderItemMapper;
import com.openbiz.shop.mapper.OpenbizShopOrderMapper;
import com.openbiz.shop.mapper.OpenbizShopProductMapper;
import com.openbiz.shop.money.ShopMoney;
import com.openbiz.shop.port.CurrentUserPort;
import com.openbiz.shop.support.ShopTenantGuard;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;

@Service
public class ShopServiceImpl implements ShopService
{
    private static final int CAS_RETRIES = 16;

    private final OpenbizShopProductMapper productMapper;
    private final OpenbizShopInventoryMapper inventoryMapper;
    private final OpenbizShopOrderMapper orderMapper;
    private final OpenbizShopOrderItemMapper orderItemMapper;
    private final CurrentUserPort currentUserPort;

    public ShopServiceImpl(OpenbizShopProductMapper productMapper,
                           OpenbizShopInventoryMapper inventoryMapper,
                           OpenbizShopOrderMapper orderMapper,
                           OpenbizShopOrderItemMapper orderItemMapper,
                           CurrentUserPort currentUserPort)
    {
        this.productMapper = productMapper;
        this.inventoryMapper = inventoryMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.currentUserPort = currentUserPort;
    }

    @Override
    @Transactional
    public OpenbizShopProduct createProduct(String productCode, String productName, BigDecimal price, int initialQuantity)
    {
        Long tenantId = ShopTenantGuard.requireTenantId();
        currentUserPort.requireUserId();
        requireText(productCode, "productCode");
        requireText(productName, "productName");
        BigDecimal money = ShopMoney.of(price);
        if (!ShopMoney.isPositive(money))
        {
            throw new ServiceException("price must be positive", HttpStatus.BAD_REQUEST);
        }
        if (initialQuantity < 0)
        {
            throw new ServiceException("initialQuantity must be >= 0", HttpStatus.BAD_REQUEST);
        }
        if (productMapper.selectByCodeAndTenant(productCode.trim(), tenantId) != null)
        {
            throw new ServiceException("product_code already exists in tenant", HttpStatus.BAD_REQUEST);
        }

        Date now = new Date();
        OpenbizShopProduct product = new OpenbizShopProduct();
        product.setTenantId(tenantId);
        product.setProductCode(productCode.trim());
        product.setProductName(productName.trim());
        product.setPrice(money);
        product.setStatus(OpenbizShopProduct.STATUS_ACTIVE);
        product.setCreateTime(now);
        product.setUpdateTime(now);
        productMapper.insert(product);

        OpenbizShopInventory inv = new OpenbizShopInventory();
        inv.setTenantId(tenantId);
        inv.setProductId(product.getId());
        inv.setQuantity(initialQuantity);
        inv.setVersion(0);
        inv.setUpdateTime(now);
        inventoryMapper.insert(inv);
        return productMapper.selectByIdAndTenant(product.getId(), tenantId);
    }

    @Override
    public OpenbizShopProduct getProduct(Long productId)
    {
        Long tenantId = ShopTenantGuard.requireTenantId();
        currentUserPort.requireUserId();
        return requireProduct(productId, tenantId);
    }

    @Override
    @Transactional
    public OpenbizShopInventory setInventory(Long productId, int quantity)
    {
        Long tenantId = ShopTenantGuard.requireTenantId();
        currentUserPort.requireUserId();
        if (quantity < 0)
        {
            throw new ServiceException("quantity must be >= 0", HttpStatus.BAD_REQUEST);
        }
        requireProduct(productId, tenantId);
        for (int i = 0; i < CAS_RETRIES; i++)
        {
            OpenbizShopInventory current = requireInventory(productId, tenantId);
            int rows = inventoryMapper.setQuantity(productId, tenantId, quantity, current.getVersion());
            if (rows == 1)
            {
                return inventoryMapper.selectByProductAndTenant(productId, tenantId);
            }
        }
        throw new ServiceException("inventory set conflict, retry later", HttpStatus.CONFLICT);
    }

    @Override
    public OpenbizShopInventory getInventory(Long productId)
    {
        Long tenantId = ShopTenantGuard.requireTenantId();
        currentUserPort.requireUserId();
        return requireInventory(productId, tenantId);
    }

    @Override
    @Transactional
    public OpenbizShopOrder placeOrder(String buyerName, String buyerPhone, List<OrderLineRequest> lines, String idempotentKey)
    {
        Long tenantId = ShopTenantGuard.requireTenantId();
        currentUserPort.requireUserId();
        requireText(buyerName, "buyerName");
        requireText(buyerPhone, "buyerPhone");
        requireText(idempotentKey, "idempotentKey");
        if (lines == null || lines.isEmpty())
        {
            throw new ServiceException("order lines required", HttpStatus.BAD_REQUEST);
        }

        OpenbizShopOrder existing = orderMapper.selectByIdempotent(tenantId, idempotentKey.trim());
        if (existing != null)
        {
            return existing;
        }

        Map<Long, Integer> qtyByProduct = mergeLines(lines);
        List<Long> productIds = new ArrayList<>(qtyByProduct.keySet());
        productIds.sort(Comparator.naturalOrder());

        List<PreparedLine> prepared = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(ShopMoney.SCALE);
        for (Long productId : productIds)
        {
            int qty = qtyByProduct.get(productId);
            OpenbizShopProduct product = requireProduct(productId, tenantId);
            if (!OpenbizShopProduct.STATUS_ACTIVE.equals(product.getStatus()))
            {
                throw new ServiceException("product not active: " + productId, HttpStatus.BAD_REQUEST);
            }
            BigDecimal unit = ShopMoney.of(product.getPrice());
            BigDecimal lineAmt = ShopMoney.of(unit.multiply(BigDecimal.valueOf(qty)));
            total = ShopMoney.of(total.add(lineAmt));
            prepared.add(new PreparedLine(productId, qty, unit, lineAmt));
        }
        if (!ShopMoney.isPositive(total))
        {
            throw new ServiceException("totalAmount must be positive", HttpStatus.BAD_REQUEST);
        }

        for (PreparedLine line : prepared)
        {
            deductWithRetry(tenantId, line.productId, line.quantity);
        }

        Date now = new Date();
        OpenbizShopOrder order = new OpenbizShopOrder();
        order.setTenantId(tenantId);
        order.setBuyerName(buyerName.trim());
        order.setBuyerPhone(buyerPhone.trim());
        order.setTotalAmount(total);
        order.setStatus(OpenbizShopOrder.STATUS_SUCCESS);
        order.setIdempotentKey(idempotentKey.trim());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        try
        {
            orderMapper.insert(order);
        }
        catch (DuplicateKeyException ex)
        {
            // Rollback deducted stock via TX; client may retry and load winner order by idempotent key.
            throw new ServiceException("IDEMPOTENT_RACE: retry same idempotentKey", HttpStatus.CONFLICT);
        }

        for (PreparedLine line : prepared)
        {
            OpenbizShopOrderItem item = new OpenbizShopOrderItem();
            item.setTenantId(tenantId);
            item.setOrderId(order.getId());
            item.setProductId(line.productId);
            item.setQuantity(line.quantity);
            item.setUnitPrice(line.unitPrice);
            item.setLineAmount(line.lineAmount);
            orderItemMapper.insert(item);
        }
        return orderMapper.selectByIdAndTenant(order.getId(), tenantId);
    }

    @Override
    public OpenbizShopOrder getOrder(Long orderId)
    {
        Long tenantId = ShopTenantGuard.requireTenantId();
        currentUserPort.requireUserId();
        OpenbizShopOrder order = orderMapper.selectByIdAndTenant(orderId, tenantId);
        if (order == null)
        {
            throw new ServiceException("order not found in current tenant", HttpStatus.NOT_FOUND);
        }
        return order;
    }

    @Override
    public List<OpenbizShopOrderItem> listOrderItems(Long orderId)
    {
        Long tenantId = ShopTenantGuard.requireTenantId();
        currentUserPort.requireUserId();
        getOrder(orderId);
        return orderItemMapper.selectByOrderAndTenant(orderId, tenantId);
    }

    private void deductWithRetry(Long tenantId, Long productId, int qty)
    {
        for (int i = 0; i < CAS_RETRIES; i++)
        {
            OpenbizShopInventory inv = requireInventory(productId, tenantId);
            if (inv.getQuantity() < qty)
            {
                throw new ServiceException("INSUFFICIENT_STOCK: productId=" + productId, HttpStatus.BAD_REQUEST);
            }
            int rows = inventoryMapper.casDeduct(productId, tenantId, qty, inv.getVersion());
            if (rows == 1)
            {
                return;
            }
        }
        throw new ServiceException("INSUFFICIENT_STOCK_OR_CONFLICT: productId=" + productId, HttpStatus.CONFLICT);
    }

    private static Map<Long, Integer> mergeLines(List<OrderLineRequest> lines)
    {
        Map<Long, Integer> qtyByProduct = new HashMap<>();
        for (OrderLineRequest line : lines)
        {
            if (line == null || line.getProductId() == null)
            {
                throw new ServiceException("productId is required", HttpStatus.BAD_REQUEST);
            }
            if (line.getQuantity() <= 0)
            {
                throw new ServiceException("quantity must be > 0", HttpStatus.BAD_REQUEST);
            }
            qtyByProduct.merge(line.getProductId(), line.getQuantity(), Integer::sum);
        }
        return qtyByProduct;
    }

    private OpenbizShopProduct requireProduct(Long productId, Long tenantId)
    {
        if (productId == null)
        {
            throw new ServiceException("productId is required", HttpStatus.BAD_REQUEST);
        }
        OpenbizShopProduct product = productMapper.selectByIdAndTenant(productId, tenantId);
        if (product == null)
        {
            throw new ServiceException("product not found in current tenant", HttpStatus.NOT_FOUND);
        }
        return product;
    }

    private OpenbizShopInventory requireInventory(Long productId, Long tenantId)
    {
        OpenbizShopInventory inv = inventoryMapper.selectByProductAndTenant(productId, tenantId);
        if (inv == null)
        {
            throw new ServiceException("inventory not found in current tenant", HttpStatus.NOT_FOUND);
        }
        return inv;
    }

    private static void requireText(String value, String field)
    {
        if (!StringUtils.hasText(value))
        {
            throw new ServiceException(field + " is required", HttpStatus.BAD_REQUEST);
        }
    }

    private static final class PreparedLine
    {
        private final Long productId;
        private final int quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal lineAmount;

        private PreparedLine(Long productId, int quantity, BigDecimal unitPrice, BigDecimal lineAmount)
        {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.lineAmount = lineAmount;
        }
    }
}
