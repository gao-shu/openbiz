package com.openbiz.mes.service.impl;

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
import com.openbiz.mes.api.MesStartResult;
import com.openbiz.mes.domain.OpenbizMesProductionRecord;
import com.openbiz.mes.domain.OpenbizMesWorkOrder;
import com.openbiz.mes.mapper.OpenbizMesProductionRecordMapper;
import com.openbiz.mes.mapper.OpenbizMesWorkOrderMapper;
import com.openbiz.saas.context.TenantContext;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class MesServiceImplTest
{
    @Mock
    private OpenbizMesWorkOrderMapper workOrderMapper;
    @Mock
    private OpenbizMesProductionRecordMapper productionRecordMapper;
    @Mock
    private DeviceService deviceService;
    @Mock
    private DeviceCommandService deviceCommandService;
    @InjectMocks
    private MesServiceImpl mesService;

    @AfterEach
    void tearDown()
    {
        TenantContext.clear();
    }

    private OpenbizMesWorkOrder wo(Long id, Long deviceId)
    {
        OpenbizMesWorkOrder w = new OpenbizMesWorkOrder();
        w.setId(id);
        w.setDeviceId(deviceId);
        w.setQuantity(10);
        w.setStatus("CREATED");
        return w;
    }

    @Test
    void case1_startSuccessSetsRunningAndRecords()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(workOrderMapper.selectByIdAndTenant(1L, 1L)).thenReturn(wo(1L, 1L));
            OpenbizDevice device = new OpenbizDevice();
            device.setId(1L);
            when(deviceService.get(1L)).thenReturn(device);
            when(deviceCommandService.invoke(eq(1L), eq("open_door"), eq("{}"), anyString())).thenReturn(10L);
            OpenbizDeviceCommand cmd = new OpenbizDeviceCommand();
            cmd.setCommandId("cmd-mes-1");
            cmd.setStatus("SUCCESS");
            when(deviceCommandService.get(10L)).thenReturn(cmd);

            MesStartResult result = mesService.startProduction(1L);
            assertTrue(result.isSuccess());
            assertEquals(1L, result.getWorkOrderId());
            assertEquals(1L, result.getDeviceId());
            assertEquals("cmd-mes-1", result.getCommandId());
            assertEquals("RUNNING", result.getWorkOrderStatus());

            ArgumentCaptor<OpenbizMesProductionRecord> captor = ArgumentCaptor.forClass(OpenbizMesProductionRecord.class);
            verify(productionRecordMapper).insert(captor.capture());
            assertEquals("SUCCESS", captor.getValue().getResult());
            assertEquals("cmd-mes-1", captor.getValue().getCommandId());
            verify(workOrderMapper).updateStatus(1L, 1L, "RUNNING");
        }
    }

    @Test
    void case2_crossTenantWorkOrderNotFound()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(workOrderMapper.selectByIdAndTenant(2L, 1L)).thenReturn(null);
            ServiceException ex = assertThrows(ServiceException.class, () -> mesService.startProduction(2L));
            assertEquals(HttpStatus.NOT_FOUND, ex.getCode());
            verify(deviceCommandService, never()).invoke(any(), anyString(), anyString(), anyString());
        }
    }

    @Test
    void case3_workOrderMissing()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(workOrderMapper.selectByIdAndTenant(99L, 1L)).thenReturn(null);
            ServiceException ex = assertThrows(ServiceException.class, () -> mesService.startProduction(99L));
            assertEquals(HttpStatus.NOT_FOUND, ex.getCode());
            assertTrue(ex.getMessage().contains("work order"));
        }
    }

    @Test
    void case4_deviceMissing()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(workOrderMapper.selectByIdAndTenant(1L, 1L)).thenReturn(wo(1L, 999L));
            when(deviceService.get(999L)).thenReturn(null);
            ServiceException ex = assertThrows(ServiceException.class, () -> mesService.startProduction(1L));
            assertEquals(HttpStatus.NOT_FOUND, ex.getCode());
            assertTrue(ex.getMessage().contains("device"));
            verify(deviceCommandService, never()).invoke(any(), anyString(), anyString(), anyString());
        }
    }

    @Test
    void case6_commandFailedKeepsCreated()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(workOrderMapper.selectByIdAndTenant(1L, 1L)).thenReturn(wo(1L, 1L));
            OpenbizDevice device = new OpenbizDevice();
            device.setId(1L);
            when(deviceService.get(1L)).thenReturn(device);
            when(deviceCommandService.invoke(eq(1L), eq("open_door"), eq("{}"), anyString())).thenReturn(11L);
            OpenbizDeviceCommand cmd = new OpenbizDeviceCommand();
            cmd.setCommandId("cmd-fail");
            cmd.setStatus("FAILED");
            cmd.setErrorMessage("protocol rejected command");
            when(deviceCommandService.get(11L)).thenReturn(cmd);

            ServiceException ex = assertThrows(ServiceException.class, () -> mesService.startProduction(1L));
            assertEquals(HttpStatus.ERROR, ex.getCode());

            ArgumentCaptor<OpenbizMesProductionRecord> captor = ArgumentCaptor.forClass(OpenbizMesProductionRecord.class);
            verify(productionRecordMapper).insert(captor.capture());
            assertEquals("FAILED", captor.getValue().getResult());
            verify(workOrderMapper, never()).updateStatus(any(), any(), anyString());
        }
    }

    @Test
    void rejectsWithoutTenant()
    {
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            ServiceException ex = assertThrows(ServiceException.class, () -> mesService.startProduction(1L));
            assertTrue(ex.getMessage().contains("NO_TENANT"));
        }
    }
}
