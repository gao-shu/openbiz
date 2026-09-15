package com.openbiz.iot.core.service.impl;

import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.openbiz.iot.core.api.DeviceCommandService;
import com.openbiz.iot.core.domain.OpenbizDevice;
import com.openbiz.iot.core.domain.OpenbizDeviceCommand;
import com.openbiz.iot.core.domain.OpenbizProduct;
import com.openbiz.iot.core.mapper.OpenbizDeviceCommandMapper;
import com.openbiz.iot.core.mapper.OpenbizDeviceMapper;
import com.openbiz.iot.core.mapper.OpenbizProductMapper;
import com.openbiz.iot.core.spi.ProtocolPort;
import com.openbiz.iot.core.spi.ProtocolPortRegistry;
import com.openbiz.iot.core.support.TenantGuard;
import com.ruoyi.common.exception.ServiceException;

@Service
public class DeviceCommandServiceImpl implements DeviceCommandService
{
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    private final OpenbizDeviceCommandMapper commandMapper;
    private final OpenbizDeviceMapper deviceMapper;
    private final OpenbizProductMapper productMapper;
    private final ProtocolPortRegistry protocolPortRegistry;

    public DeviceCommandServiceImpl(OpenbizDeviceCommandMapper commandMapper,
                                    OpenbizDeviceMapper deviceMapper,
                                    OpenbizProductMapper productMapper,
                                    ProtocolPortRegistry protocolPortRegistry)
    {
        this.commandMapper = commandMapper;
        this.deviceMapper = deviceMapper;
        this.productMapper = productMapper;
        this.protocolPortRegistry = protocolPortRegistry;
    }

    @Override
    public Long invoke(Long deviceId, String serviceId, String paramsJson, String idempotentKey)
    {
        Long tenantId = TenantGuard.requireTenantId();
        if (deviceId == null || !StringUtils.hasText(serviceId))
        {
            throw new ServiceException("deviceId and serviceId are required");
        }

        OpenbizDevice device = deviceMapper.selectByIdAndTenant(deviceId, tenantId);
        if (device == null)
        {
            throw new ServiceException("device not found in current tenant");
        }
        OpenbizProduct product = productMapper.selectByIdAndTenant(device.getProductId(), tenantId);
        if (product == null)
        {
            throw new ServiceException("product not found in current tenant");
        }

        ProtocolPort protocolPort = protocolPortRegistry.require(product.getProtocol());

        Date now = new Date();
        OpenbizDeviceCommand command = new OpenbizDeviceCommand();
        command.setTenantId(tenantId);
        command.setDeviceId(deviceId);
        command.setCommandId(UUID.randomUUID().toString().replace("-", ""));
        command.setCommandName(serviceId);
        command.setPayload(paramsJson);
        command.setStatus(STATUS_PENDING);
        command.setRequestTime(now);
        command.setIdempotentKey(idempotentKey);
        command.setCreateTime(now);
        commandMapper.insert(command);

        boolean accepted;
        try
        {
            accepted = protocolPort.sendCommand(
                    device.getId(),
                    product.getProductCode(),
                    device.getDeviceCode(),
                    serviceId,
                    paramsJson,
                    command.getId(),
                    idempotentKey);
        }
        catch (Exception ex)
        {
            command.setStatus(STATUS_FAILED);
            command.setResponseTime(new Date());
            command.setErrorMessage(ex.getMessage());
            commandMapper.updateStatus(command);
            throw new ServiceException("protocol send failed: " + ex.getMessage());
        }

        command.setResponseTime(new Date());
        if (accepted)
        {
            command.setStatus(STATUS_SUCCESS);
            command.setErrorMessage(null);
        }
        else
        {
            command.setStatus(STATUS_FAILED);
            command.setErrorMessage("protocol rejected command");
        }
        commandMapper.updateStatus(command);
        return command.getId();
    }

    @Override
    public OpenbizDeviceCommand get(Long id)
    {
        Long tenantId = TenantGuard.requireTenantId();
        if (id == null)
        {
            return null;
        }
        return commandMapper.selectByIdAndTenant(id, tenantId);
    }
}
