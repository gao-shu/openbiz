package com.openbiz.mes.service.impl;

import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.openbiz.iot.core.api.DeviceCommandService;
import com.openbiz.iot.core.api.DeviceService;
import com.openbiz.iot.core.domain.OpenbizDevice;
import com.openbiz.iot.core.domain.OpenbizDeviceCommand;
import com.openbiz.iot.core.support.TenantGuard;
import com.openbiz.mes.api.MesService;
import com.openbiz.mes.api.MesStartResult;
import com.openbiz.mes.domain.OpenbizMesProductionRecord;
import com.openbiz.mes.domain.OpenbizMesWorkOrder;
import com.openbiz.mes.mapper.OpenbizMesProductionRecordMapper;
import com.openbiz.mes.mapper.OpenbizMesWorkOrderMapper;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * Industry MES layer: tenant-scoped WorkOrder then DeviceCommandService only.
 * Does not touch ProtocolPort / MQTT / MockDoor.
 * Reuses open_door as Phase 1.8 mock command to validate MES-IoT-Protocol-Device chain.
 */
@Service
public class MesServiceImpl implements MesService
{
    private final OpenbizMesWorkOrderMapper workOrderMapper;
    private final OpenbizMesProductionRecordMapper productionRecordMapper;
    private final DeviceService deviceService;
    private final DeviceCommandService deviceCommandService;

    public MesServiceImpl(OpenbizMesWorkOrderMapper workOrderMapper,
                          OpenbizMesProductionRecordMapper productionRecordMapper,
                          DeviceService deviceService,
                          DeviceCommandService deviceCommandService)
    {
        this.workOrderMapper = workOrderMapper;
        this.productionRecordMapper = productionRecordMapper;
        this.deviceService = deviceService;
        this.deviceCommandService = deviceCommandService;
    }

    @Override
    public MesStartResult startProduction(Long workOrderId)
    {
        Long tenantId = TenantGuard.requireTenantId();
        SecurityUtils.getUserId();
        if (workOrderId == null)
        {
            throw new ServiceException("workOrderId is required", HttpStatus.BAD_REQUEST);
        }

        OpenbizMesWorkOrder workOrder = workOrderMapper.selectByIdAndTenant(workOrderId, tenantId);
        if (workOrder == null)
        {
            throw new ServiceException("work order not found in current tenant", HttpStatus.NOT_FOUND);
        }

        OpenbizDevice device = deviceService.get(workOrder.getDeviceId());
        if (device == null)
        {
            throw new ServiceException("device not found in current tenant", HttpStatus.NOT_FOUND);
        }

        Long deviceId = workOrder.getDeviceId();
        Long cmdPk = deviceCommandService.invoke(
                deviceId,
                "open_door",
                "{}",
                "mes-" + UUID.randomUUID().toString().replace("-", ""));
        OpenbizDeviceCommand command = deviceCommandService.get(cmdPk);
        if (command == null)
        {
            throw new ServiceException("command missing after invoke", HttpStatus.ERROR);
        }

        String result = "SUCCESS".equals(command.getStatus()) ? "SUCCESS" : "FAILED";

        OpenbizMesProductionRecord record = new OpenbizMesProductionRecord();
        record.setTenantId(tenantId);
        record.setWorkOrderId(workOrderId);
        record.setDeviceId(deviceId);
        record.setQuantity(workOrder.getQuantity() == null ? 0 : workOrder.getQuantity());
        record.setResult(result);
        record.setCommandId(command.getCommandId());
        record.setCreateTime(new Date());
        productionRecordMapper.insert(record);

        if ("SUCCESS".equals(result))
        {
            workOrderMapper.updateStatus(workOrderId, tenantId, "RUNNING");
            return MesStartResult.of(true, workOrderId, deviceId, command.getCommandId(), "RUNNING", result);
        }

        throw new ServiceException("device command failed: " + command.getErrorMessage(), HttpStatus.ERROR);
    }
}
