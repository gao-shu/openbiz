package com.openbiz.access.service.impl;

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
import com.openbiz.access.api.AccessOpenResult;
import com.openbiz.access.domain.OpenbizAccessPermission;
import com.openbiz.access.domain.OpenbizAccessRecord;
import com.openbiz.access.mapper.OpenbizAccessPermissionMapper;
import com.openbiz.access.mapper.OpenbizAccessRecordMapper;
import com.openbiz.iot.core.api.DeviceCommandService;
import com.openbiz.iot.core.api.DeviceService;
import com.openbiz.iot.core.domain.OpenbizDevice;
import com.openbiz.iot.core.domain.OpenbizDeviceCommand;
import com.openbiz.saas.context.TenantContext;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class AccessServiceImplTest
{
    @Mock
    private OpenbizAccessPermissionMapper permissionMapper;
    @Mock
    private OpenbizAccessRecordMapper recordMapper;
    @Mock
    private DeviceService deviceService;
    @Mock
    private DeviceCommandService deviceCommandService;
    @InjectMocks
    private AccessServiceImpl accessService;

    @AfterEach
    void tearDown()
    {
        TenantContext.clear();
    }

    @Test
    void rejectsWithoutTenant()
    {
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            ServiceException ex = assertThrows(ServiceException.class, () -> accessService.openDoor(1L));
            assertTrue(ex.getMessage().contains("NO_TENANT"));
        }
    }

    @Test
    void deviceMissingNotFound()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(deviceService.get(999L)).thenReturn(null);
            ServiceException ex = assertThrows(ServiceException.class, () -> accessService.openDoor(999L));
            assertEquals(HttpStatus.NOT_FOUND, ex.getCode());
            verify(deviceCommandService, never()).invoke(any(), anyString(), anyString(), anyString());
        }
    }

    @Test
    void unauthorizedForbidden()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            OpenbizDevice device = new OpenbizDevice();
            device.setId(2L);
            when(deviceService.get(2L)).thenReturn(device);
            when(permissionMapper.selectActive(1L, 1L, 2L)).thenReturn(null);
            ServiceException ex = assertThrows(ServiceException.class, () -> accessService.openDoor(2L));
            assertEquals(HttpStatus.FORBIDDEN, ex.getCode());
            verify(deviceCommandService, never()).invoke(any(), anyString(), anyString(), anyString());
            verify(recordMapper, never()).insert(any());
        }
    }

    @Test
    void authorizedSuccessRecordsAccess()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            OpenbizDevice device = new OpenbizDevice();
            device.setId(1L);
            when(deviceService.get(1L)).thenReturn(device);
            when(permissionMapper.selectActive(1L, 1L, 1L)).thenReturn(new OpenbizAccessPermission());
            when(deviceCommandService.invoke(eq(1L), eq("open_door"), eq("{}"), anyString())).thenReturn(10L);
            OpenbizDeviceCommand cmd = new OpenbizDeviceCommand();
            cmd.setCommandId("abc123");
            cmd.setStatus("SUCCESS");
            when(deviceCommandService.get(10L)).thenReturn(cmd);

            AccessOpenResult result = accessService.openDoor(1L);
            assertTrue(result.isSuccess());
            assertEquals("abc123", result.getCommandId());

            ArgumentCaptor<OpenbizAccessRecord> captor = ArgumentCaptor.forClass(OpenbizAccessRecord.class);
            verify(recordMapper).insert(captor.capture());
            assertEquals("abc123", captor.getValue().getCommandId());
            assertEquals("SUCCESS", captor.getValue().getResult());
            assertEquals(1L, captor.getValue().getTenantId());
            assertEquals(1L, captor.getValue().getUserId());
        }
    }

    @Test
    void inactivePermissionDenied()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            OpenbizDevice device = new OpenbizDevice();
            device.setId(1L);
            when(deviceService.get(1L)).thenReturn(device);
            // selectActive only returns status=0 rows; inactive => null
            when(permissionMapper.selectActive(1L, 1L, 1L)).thenReturn(null);
            ServiceException ex = assertThrows(ServiceException.class, () -> accessService.openDoor(1L));
            assertEquals(HttpStatus.FORBIDDEN, ex.getCode());
            verify(deviceCommandService, never()).invoke(any(), anyString(), anyString(), anyString());
            verify(recordMapper, never()).insert(any());
        }
    }
}
