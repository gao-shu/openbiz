package com.openbiz.iot.core.service.impl;

import java.util.Date;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.openbiz.iot.core.api.ThingModelService;
import com.openbiz.iot.core.domain.OpenbizProduct;
import com.openbiz.iot.core.domain.OpenbizThingModel;
import com.openbiz.iot.core.mapper.OpenbizProductMapper;
import com.openbiz.iot.core.mapper.OpenbizThingModelMapper;
import com.openbiz.iot.core.support.TenantGuard;
import com.ruoyi.common.exception.ServiceException;

@Service
public class ThingModelServiceImpl implements ThingModelService
{
    private final OpenbizThingModelMapper thingModelMapper;
    private final OpenbizProductMapper productMapper;

    public ThingModelServiceImpl(OpenbizThingModelMapper thingModelMapper, OpenbizProductMapper productMapper)
    {
        this.thingModelMapper = thingModelMapper;
        this.productMapper = productMapper;
    }

    @Override
    public Long create(OpenbizThingModel model)
    {
        Long tenantId = TenantGuard.requireTenantId();
        if (model == null || model.getProductId() == null || !StringUtils.hasText(model.getModelJson()))
        {
            throw new ServiceException("product_id and model_json are required");
        }
        OpenbizProduct product = productMapper.selectByIdAndTenant(model.getProductId(), tenantId);
        if (product == null)
        {
            throw new ServiceException("product not found in current tenant");
        }
        if (thingModelMapper.selectByProductAndTenant(model.getProductId(), tenantId) != null)
        {
            throw new ServiceException("thing model already exists for product");
        }
        model.setTenantId(tenantId);
        if (!StringUtils.hasText(model.getVersion()))
        {
            model.setVersion("1.0");
        }
        if (!StringUtils.hasText(model.getStatus()))
        {
            model.setStatus("0");
        }
        Date now = new Date();
        model.setCreateTime(now);
        model.setUpdateTime(now);
        thingModelMapper.insert(model);
        return model.getId();
    }

    @Override
    public OpenbizThingModel getByProductId(Long productId)
    {
        Long tenantId = TenantGuard.requireTenantId();
        if (productId == null)
        {
            return null;
        }
        return thingModelMapper.selectByProductAndTenant(productId, tenantId);
    }
}
