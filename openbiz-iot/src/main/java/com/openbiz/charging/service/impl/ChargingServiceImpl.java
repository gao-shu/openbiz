package com.openbiz.charging.service.impl;

import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.openbiz.charging.api.ChargingService;
import com.openbiz.charging.api.ChargingStartResult;
import com.openbiz.charging.domain.OpenbizChargingConnector;
import com.openbiz.charging.domain.OpenbizChargingRecord;
import com.openbiz.charging.domain.OpenbizChargingStation;
import com.openbiz.charging.mapper.OpenbizChargingConnectorMapper;
import com.openbiz.charging.mapper.OpenbizChargingRecordMapper;
import com.openbiz.charging.mapper.OpenbizChargingStationMapper;
import com.openbiz.iot.core.api.DeviceCommandService;
import com.openbiz.iot.core.api.DeviceService;
import com.openbiz.iot.core.domain.OpenbizDevice;
import com.openbiz.iot.core.domain.OpenbizDeviceCommand;
import com.openbiz.iot.core.support.TenantGuard;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * Industry charging layer: tenant-scoped Station/Connector then DeviceCommandService only.
 * Does not touch ProtocolPort / MQTT / MockDoor.
 * Reuses open_door to validate DeviceCommand across Access / Locker / Charging.
 */
@Service
public class ChargingServiceImpl implements ChargingService
{
    private final OpenbizChargingStationMapper stationMapper;
    private final OpenbizChargingConnectorMapper connectorMapper;
    private final OpenbizChargingRecordMapper recordMapper;
    private final DeviceService deviceService;
    private final DeviceCommandService deviceCommandService;

    public ChargingServiceImpl(OpenbizChargingStationMapper stationMapper,
                               OpenbizChargingConnectorMapper connectorMapper,
                               OpenbizChargingRecordMapper recordMapper,
                               DeviceService deviceService,
                               DeviceCommandService deviceCommandService)
    {
        this.stationMapper = stationMapper;
        this.connectorMapper = connectorMapper;
        this.recordMapper = recordMapper;
        this.deviceService = deviceService;
        this.deviceCommandService = deviceCommandService;
    }

    @Override
    public ChargingStartResult startCharging(Long stationId, Long connectorId)
    {
        Long tenantId = TenantGuard.requireTenantId();
        Long userId = SecurityUtils.getUserId();
        if (stationId == null || connectorId == null)
        {
            throw new ServiceException("stationId and connectorId are required", HttpStatus.BAD_REQUEST);
        }

        OpenbizChargingStation station = stationMapper.selectByIdAndTenant(stationId, tenantId);
        if (station == null)
        {
            throw new ServiceException("station not found in current tenant", HttpStatus.NOT_FOUND);
        }

        OpenbizChargingConnector connector = connectorMapper.selectByIdAndTenant(connectorId, tenantId);
        if (connector == null)
        {
            throw new ServiceException("connector not found in current tenant", HttpStatus.NOT_FOUND);
        }
        if (!stationId.equals(connector.getStationId()))
        {
            throw new ServiceException("connector does not belong to station", HttpStatus.BAD_REQUEST);
        }

        OpenbizDevice device = deviceService.get(station.getDeviceId());
        if (device == null)
        {
            throw new ServiceException("device not found in current tenant", HttpStatus.NOT_FOUND);
        }

        Long deviceId = station.getDeviceId();
        Long cmdPk = deviceCommandService.invoke(
                deviceId,
                "open_door",
                "{}",
                "charging-" + UUID.randomUUID().toString().replace("-", ""));
        OpenbizDeviceCommand command = deviceCommandService.get(cmdPk);
        if (command == null)
        {
            throw new ServiceException("command missing after invoke", HttpStatus.ERROR);
        }

        String result = "SUCCESS".equals(command.getStatus()) ? "SUCCESS" : "FAILED";

        OpenbizChargingRecord record = new OpenbizChargingRecord();
        record.setTenantId(tenantId);
        record.setUserId(userId);
        record.setStationId(stationId);
        record.setConnectorId(connectorId);
        record.setDeviceId(deviceId);
        record.setCommandId(command.getCommandId());
        record.setResult(result);
        record.setCreateTime(new Date());
        recordMapper.insert(record);

        if ("SUCCESS".equals(result))
        {
            connectorMapper.updateStatus(connectorId, tenantId, "CHARGING");
        }

        if (!"SUCCESS".equals(result))
        {
            throw new ServiceException("device command failed: " + command.getErrorMessage(), HttpStatus.ERROR);
        }
        return ChargingStartResult.of(true, stationId, connectorId, deviceId, command.getCommandId(), result);
    }
}
