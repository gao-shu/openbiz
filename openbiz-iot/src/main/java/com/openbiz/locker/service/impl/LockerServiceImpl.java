package com.openbiz.locker.service.impl;

import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.openbiz.iot.core.api.DeviceCommandService;
import com.openbiz.iot.core.api.DeviceService;
import com.openbiz.iot.core.domain.OpenbizDevice;
import com.openbiz.iot.core.domain.OpenbizDeviceCommand;
import com.openbiz.iot.core.support.TenantGuard;
import com.openbiz.locker.api.LockerOpenResult;
import com.openbiz.locker.api.LockerService;
import com.openbiz.locker.domain.OpenbizLocker;
import com.openbiz.locker.domain.OpenbizLockerRecord;
import com.openbiz.locker.domain.OpenbizLockerSlot;
import com.openbiz.locker.mapper.OpenbizLockerMapper;
import com.openbiz.locker.mapper.OpenbizLockerRecordMapper;
import com.openbiz.locker.mapper.OpenbizLockerSlotMapper;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * Industry locker layer: tenant-scoped Locker/Slot then DeviceCommandService only.
 * Does not touch ProtocolPort / MQTT / MockDoor.
 * Reuses open_door to validate DeviceCommand reuse across industries.
 */
@Service
public class LockerServiceImpl implements LockerService
{
    private final OpenbizLockerMapper lockerMapper;
    private final OpenbizLockerSlotMapper slotMapper;
    private final OpenbizLockerRecordMapper recordMapper;
    private final DeviceService deviceService;
    private final DeviceCommandService deviceCommandService;

    public LockerServiceImpl(OpenbizLockerMapper lockerMapper,
                             OpenbizLockerSlotMapper slotMapper,
                             OpenbizLockerRecordMapper recordMapper,
                             DeviceService deviceService,
                             DeviceCommandService deviceCommandService)
    {
        this.lockerMapper = lockerMapper;
        this.slotMapper = slotMapper;
        this.recordMapper = recordMapper;
        this.deviceService = deviceService;
        this.deviceCommandService = deviceCommandService;
    }

    @Override
    public LockerOpenResult openLocker(Long lockerId, Long slotId)
    {
        Long tenantId = TenantGuard.requireTenantId();
        Long userId = SecurityUtils.getUserId();
        if (lockerId == null || slotId == null)
        {
            throw new ServiceException("lockerId and slotId are required", HttpStatus.BAD_REQUEST);
        }

        OpenbizLocker locker = lockerMapper.selectByIdAndTenant(lockerId, tenantId);
        if (locker == null)
        {
            throw new ServiceException("locker not found in current tenant", HttpStatus.NOT_FOUND);
        }

        OpenbizLockerSlot slot = slotMapper.selectByIdAndTenant(slotId, tenantId);
        if (slot == null)
        {
            throw new ServiceException("slot not found in current tenant", HttpStatus.NOT_FOUND);
        }
        if (!lockerId.equals(slot.getLockerId()))
        {
            throw new ServiceException("slot does not belong to locker", HttpStatus.BAD_REQUEST);
        }

        OpenbizDevice device = deviceService.get(locker.getDeviceId());
        if (device == null)
        {
            throw new ServiceException("device not found in current tenant", HttpStatus.NOT_FOUND);
        }

        Long deviceId = locker.getDeviceId();
        Long cmdPk = deviceCommandService.invoke(
                deviceId,
                "open_door",
                "{}",
                "locker-" + UUID.randomUUID().toString().replace("-", ""));
        OpenbizDeviceCommand command = deviceCommandService.get(cmdPk);
        if (command == null)
        {
            throw new ServiceException("command missing after invoke", HttpStatus.ERROR);
        }

        String result = "SUCCESS".equals(command.getStatus()) ? "SUCCESS" : "FAILED";

        OpenbizLockerRecord record = new OpenbizLockerRecord();
        record.setTenantId(tenantId);
        record.setUserId(userId);
        record.setLockerId(lockerId);
        record.setSlotId(slotId);
        record.setDeviceId(deviceId);
        record.setCommandId(command.getCommandId());
        record.setResult(result);
        record.setCreateTime(new Date());
        recordMapper.insert(record);

        if ("SUCCESS".equals(result))
        {
            slotMapper.updateStatus(slotId, tenantId, "OCCUPIED");
        }

        if (!"SUCCESS".equals(result))
        {
            throw new ServiceException("device command failed: " + command.getErrorMessage(), HttpStatus.ERROR);
        }
        return LockerOpenResult.of(true, lockerId, slotId, deviceId, command.getCommandId(), result);
    }
}
