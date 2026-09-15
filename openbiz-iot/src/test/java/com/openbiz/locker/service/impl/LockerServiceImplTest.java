package com.openbiz.locker.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import com.openbiz.iot.core.api.DeviceCommandService;
import com.openbiz.iot.core.api.DeviceService;
import com.openbiz.iot.core.domain.OpenbizDevice;
import com.openbiz.iot.core.domain.OpenbizDeviceCommand;
import com.openbiz.locker.api.LockerOpenResult;
import com.openbiz.locker.domain.OpenbizLocker;
import com.openbiz.locker.domain.OpenbizLockerRecord;
import com.openbiz.locker.domain.OpenbizLockerSlot;
import com.openbiz.locker.mapper.OpenbizLockerMapper;
import com.openbiz.locker.mapper.OpenbizLockerRecordMapper;
import com.openbiz.locker.mapper.OpenbizLockerSlotMapper;
import com.openbiz.saas.context.TenantContext;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class LockerServiceImplTest
{
    @Mock
    private OpenbizLockerMapper lockerMapper;
    @Mock
    private OpenbizLockerSlotMapper slotMapper;
    @Mock
    private OpenbizLockerRecordMapper recordMapper;
    @Mock
    private DeviceService deviceService;
    @Mock
    private DeviceCommandService deviceCommandService;
    @InjectMocks
    private LockerServiceImpl lockerService;

    @AfterEach
    void tearDown()
    {
        TenantContext.clear();
    }

    private OpenbizLocker locker(Long id, Long deviceId)
    {
        OpenbizLocker l = new OpenbizLocker();
        l.setId(id);
        l.setDeviceId(deviceId);
        l.setStatus("ACTIVE");
        return l;
    }

    private OpenbizLockerSlot slot(Long id, Long lockerId)
    {
        OpenbizLockerSlot s = new OpenbizLockerSlot();
        s.setId(id);
        s.setLockerId(lockerId);
        s.setSlotNo(1);
        s.setStatus("AVAILABLE");
        return s;
    }

    @Test
    void case1_openSuccessRecordsAndOccupies()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(lockerMapper.selectByIdAndTenant(1L, 1L)).thenReturn(locker(1L, 1L));
            when(slotMapper.selectByIdAndTenant(1L, 1L)).thenReturn(slot(1L, 1L));
            OpenbizDevice device = new OpenbizDevice();
            device.setId(1L);
            when(deviceService.get(1L)).thenReturn(device);
            when(deviceCommandService.invoke(eq(1L), eq("open_door"), eq("{}"), anyString())).thenReturn(10L);
            OpenbizDeviceCommand cmd = new OpenbizDeviceCommand();
            cmd.setCommandId("cmd-locker-1");
            cmd.setStatus("SUCCESS");
            when(deviceCommandService.get(10L)).thenReturn(cmd);

            LockerOpenResult result = lockerService.openLocker(1L, 1L);
            assertTrue(result.isSuccess());
            assertEquals(1L, result.getLockerId());
            assertEquals(1L, result.getSlotId());
            assertEquals(1L, result.getDeviceId());
            assertEquals("cmd-locker-1", result.getCommandId());

            ArgumentCaptor<OpenbizLockerRecord> captor = ArgumentCaptor.forClass(OpenbizLockerRecord.class);
            verify(recordMapper).insert(captor.capture());
            assertEquals("SUCCESS", captor.getValue().getResult());
            assertEquals("cmd-locker-1", captor.getValue().getCommandId());
            verify(slotMapper).updateStatus(1L, 1L, "OCCUPIED");
        }
    }

    @Test
    void case2_crossTenantLockerNotFound()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(lockerMapper.selectByIdAndTenant(2L, 1L)).thenReturn(null);
            ServiceException ex = assertThrows(ServiceException.class, () -> lockerService.openLocker(2L, 2L));
            assertEquals(HttpStatus.NOT_FOUND, ex.getCode());
            verify(deviceCommandService, never()).invoke(any(), anyString(), anyString(), anyString());
        }
    }

    @Test
    void case3_lockerMissing()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(lockerMapper.selectByIdAndTenant(99L, 1L)).thenReturn(null);
            ServiceException ex = assertThrows(ServiceException.class, () -> lockerService.openLocker(99L, 1L));
            assertEquals(HttpStatus.NOT_FOUND, ex.getCode());
            assertTrue(ex.getMessage().contains("locker"));
        }
    }

    @Test
    void case4_slotMissing()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(lockerMapper.selectByIdAndTenant(1L, 1L)).thenReturn(locker(1L, 1L));
            when(slotMapper.selectByIdAndTenant(99L, 1L)).thenReturn(null);
            ServiceException ex = assertThrows(ServiceException.class, () -> lockerService.openLocker(1L, 99L));
            assertEquals(HttpStatus.NOT_FOUND, ex.getCode());
            assertTrue(ex.getMessage().contains("slot"));
            verify(deviceCommandService, never()).invoke(any(), anyString(), anyString(), anyString());
        }
    }

    @Test
    void case5_deviceMissing()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(lockerMapper.selectByIdAndTenant(1L, 1L)).thenReturn(locker(1L, 999L));
            when(slotMapper.selectByIdAndTenant(1L, 1L)).thenReturn(slot(1L, 1L));
            when(deviceService.get(999L)).thenReturn(null);
            ServiceException ex = assertThrows(ServiceException.class, () -> lockerService.openLocker(1L, 1L));
            assertEquals(HttpStatus.NOT_FOUND, ex.getCode());
            assertTrue(ex.getMessage().contains("device"));
            verify(deviceCommandService, never()).invoke(any(), anyString(), anyString(), anyString());
        }
    }

    @Test
    void case6_commandFailedRecordsFailed()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(lockerMapper.selectByIdAndTenant(1L, 1L)).thenReturn(locker(1L, 1L));
            when(slotMapper.selectByIdAndTenant(1L, 1L)).thenReturn(slot(1L, 1L));
            OpenbizDevice device = new OpenbizDevice();
            device.setId(1L);
            when(deviceService.get(1L)).thenReturn(device);
            when(deviceCommandService.invoke(eq(1L), eq("open_door"), eq("{}"), anyString())).thenReturn(11L);
            OpenbizDeviceCommand cmd = new OpenbizDeviceCommand();
            cmd.setCommandId("cmd-fail");
            cmd.setStatus("FAILED");
            cmd.setErrorMessage("protocol rejected command");
            when(deviceCommandService.get(11L)).thenReturn(cmd);

            ServiceException ex = assertThrows(ServiceException.class, () -> lockerService.openLocker(1L, 1L));
            assertEquals(HttpStatus.ERROR, ex.getCode());

            ArgumentCaptor<OpenbizLockerRecord> captor = ArgumentCaptor.forClass(OpenbizLockerRecord.class);
            verify(recordMapper).insert(captor.capture());
            assertEquals("FAILED", captor.getValue().getResult());
            assertEquals("cmd-fail", captor.getValue().getCommandId());
            verify(slotMapper, never()).updateStatus(any(), any(), anyString());
        }
    }

    @Test
    void rejectsWithoutTenant()
    {
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            ServiceException ex = assertThrows(ServiceException.class, () -> lockerService.openLocker(1L, 1L));
            assertTrue(ex.getMessage().contains("NO_TENANT"));
        }
    }
}
