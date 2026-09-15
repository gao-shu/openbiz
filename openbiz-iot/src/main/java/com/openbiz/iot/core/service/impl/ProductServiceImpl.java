package com.openbiz.iot.core.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.openbiz.iot.core.api.ProductService;
import com.openbiz.iot.core.domain.OpenbizProduct;
import com.openbiz.iot.core.mapper.OpenbizProductMapper;
import com.openbiz.iot.core.support.TenantGuard;
import com.ruoyi.common.exception.ServiceException;

@Service
public class ProductServiceImpl implements ProductService
{
    private final OpenbizProductMapper productMapper;

    public ProductServiceImpl(OpenbizProductMapper productMapper)
    {
        this.productMapper = productMapper;
    }

    @Override
    public Long create(OpenbizProduct product)
    {
        Long tenantId = TenantGuard.requireTenantId();
        if (product == null || !StringUtils.hasText(product.getProductCode()) || !StringUtils.hasText(product.getProductName()))
        {
            throw new ServiceException("product_code and product_name are required");
        }
        product.setTenantId(tenantId);
        if (!StringUtils.hasText(product.getProtocol()))
        {
            product.setProtocol("MQTT");
        }
        if (!StringUtils.hasText(product.getStatus()))
        {
            product.setStatus("0");
        }
        Date now = new Date();
        product.setCreateTime(now);
        product.setUpdateTime(now);
        productMapper.insert(product);
        return product.getId();
    }

    @Override
    public OpenbizProduct get(Long id)
    {
        Long tenantId = TenantGuard.requireTenantId();
        if (id == null)
        {
            return null;
        }
        return productMapper.selectByIdAndTenant(id, tenantId);
    }

    @Override
    public List<OpenbizProduct> list()
    {
        Long tenantId = TenantGuard.requireTenantId();
        return productMapper.selectByTenant(tenantId);
    }
}
