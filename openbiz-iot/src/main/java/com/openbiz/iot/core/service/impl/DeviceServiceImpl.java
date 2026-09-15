package com.openbiz.iot.core.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.openbiz.iot.core.api.DeviceService;
import com.openbiz.iot.core.domain.OpenbizDevice;
import com.openbiz.iot.core.domain.OpenbizProduct;
import com.openbiz.iot.core.mapper.OpenbizDeviceMapper;
import com.openbiz.iot.core.mapper.OpenbizProductMapper;
import com.openbiz.iot.core.support.TenantGuard;
import com.ruoyi.common.exception.ServiceException;

@Service
public class DeviceServiceImpl implements DeviceService
{
    private final OpenbizDeviceMapper deviceMapper;
    private final OpenbizProductMapper productMapper;

    public DeviceServiceImpl(OpenbizDeviceMapper deviceMapper, OpenbizProductMapper productMapper)
    {
        this.deviceMapper = deviceMapper;
        this.productMapper = productMapper;
    }

    @Override
    public Long create(OpenbizDevice device)
    {
        Long tenantId = TenantGuard.requireTenantId();
        if (device == null || device.getProductId() == null
                || !StringUtils.hasText(device.getDeviceCode())
                || !StringUtils.hasText(device.getDeviceName()))
        {
            throw new ServiceException("product_id, device_code and device_name are required");
        }
        OpenbizProduct product = productMapper.selectByIdAndTenant(device.getProductId(), tenantId);
        if (product == null)
        {
            throw new ServiceException("product not found in current tenant");
        }
        device.setTenantId(tenantId);
        if (!StringUtils.hasText(device.getStatus()))
        {
            device.setStatus("0");
        }
        if (!StringUtils.hasText(device.getOnlineStatus()))
        {
            device.setOnlineStatus("OFFLINE");
        }
        Date now = new Date();
        device.setCreateTime(now);
        device.setUpdateTime(now);
        deviceMapper.insert(device);
        return device.getId();
    }

    @Override
    public OpenbizDevice get(Long id)
    {
        Long tenantId = TenantGuard.requireTenantId();
        if (id == null)
        {
            return null;
        }
        return deviceMapper.selectByIdAndTenant(id, tenantId);
    }

    @Override
    public List<OpenbizDevice> list()
    {
        Long tenantId = TenantGuard.requireTenantId();
        return deviceMapper.selectByTenant(tenantId);
    }
}
