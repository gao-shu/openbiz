package com.openbiz.access.service.impl;

import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.openbiz.access.api.AccessOpenResult;
import com.openbiz.access.api.AccessService;
import com.openbiz.access.domain.OpenbizAccessPermission;
import com.openbiz.access.domain.OpenbizAccessRecord;
import com.openbiz.access.mapper.OpenbizAccessPermissionMapper;
import com.openbiz.access.mapper.OpenbizAccessRecordMapper;
import com.openbiz.iot.core.api.DeviceCommandService;
import com.openbiz.iot.core.api.DeviceService;
import com.openbiz.iot.core.domain.OpenbizDevice;
import com.openbiz.iot.core.domain.OpenbizDeviceCommand;
import com.openbiz.iot.core.support.TenantGuard;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * Industry access layer: permission check then DeviceCommandService only.
 * Does not touch ProtocolPort / MQTT / MockDoor.
 */
@Service
public class AccessServiceImpl implements AccessService
{
    private final OpenbizAccessPermissionMapper permissionMapper;
    private final OpenbizAccessRecordMapper recordMapper;
    private final DeviceService deviceService;
    private final DeviceCommandService deviceCommandService;

    public AccessServiceImpl(OpenbizAccessPermissionMapper permissionMapper,
                             OpenbizAccessRecordMapper recordMapper,
                             DeviceService deviceService,
                             DeviceCommandService deviceCommandService)
    {
        this.permissionMapper = permissionMapper;
        this.recordMapper = recordMapper;
        this.deviceService = deviceService;
        this.deviceCommandService = deviceCommandService;
    }

    @Override
    public AccessOpenResult openDoor(Long deviceId)
    {
        Long tenantId = TenantGuard.requireTenantId();
        Long userId = SecurityUtils.getUserId();
        if (deviceId == null)
        {
            throw new ServiceException("deviceId is required", HttpStatus.BAD_REQUEST);
        }

        OpenbizDevice device = deviceService.get(deviceId);
        if (device == null)
        {
            throw new ServiceException("device not found in current tenant", HttpStatus.NOT_FOUND);
        }

        OpenbizAccessPermission permission = permissionMapper.selectActive(tenantId, userId, deviceId);
        if (permission == null)
        {
            throw new ServiceException("ACCESS_DENIED: no permission for this device", HttpStatus.FORBIDDEN);
        }

        Long cmdPk = deviceCommandService.invoke(
                deviceId,
                "open_door",
                "{}",
                "access-" + UUID.randomUUID().toString().replace("-", ""));
        OpenbizDeviceCommand command = deviceCommandService.get(cmdPk);
        if (command == null)
        {
            throw new ServiceException("command missing after invoke", HttpStatus.ERROR);
        }

        String result = "SUCCESS".equals(command.getStatus()) ? "SUCCESS" : "FAILED";

        OpenbizAccessRecord record = new OpenbizAccessRecord();
        record.setTenantId(tenantId);
        record.setUserId(userId);
        record.setDeviceId(deviceId);
        record.setCommandId(command.getCommandId());
        record.setResult(result);
        record.setCreateTime(new Date());
        recordMapper.insert(record);

        if (!"SUCCESS".equals(result))
        {
            throw new ServiceException("device command failed: " + command.getErrorMessage(), HttpStatus.ERROR);
        }
        return AccessOpenResult.of(true, deviceId, command.getCommandId(), result);
    }
}
