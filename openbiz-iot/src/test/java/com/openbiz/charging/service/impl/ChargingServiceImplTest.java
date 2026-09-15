package com.openbiz.charging.service.impl;

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
import com.openbiz.saas.context.TenantContext;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class ChargingServiceImplTest
{
    @Mock
    private OpenbizChargingStationMapper stationMapper;
    @Mock
    private OpenbizChargingConnectorMapper connectorMapper;
    @Mock
    private OpenbizChargingRecordMapper recordMapper;
    @Mock
    private DeviceService deviceService;
    @Mock
    private DeviceCommandService deviceCommandService;
    @InjectMocks
    private ChargingServiceImpl chargingService;

    @AfterEach
    void tearDown()
    {
        TenantContext.clear();
    }

    private OpenbizChargingStation station(Long id, Long deviceId)
    {
        OpenbizChargingStation s = new OpenbizChargingStation();
        s.setId(id);
        s.setDeviceId(deviceId);
        s.setStatus("ACTIVE");
        return s;
    }

    private OpenbizChargingConnector connector(Long id, Long stationId)
    {
        OpenbizChargingConnector c = new OpenbizChargingConnector();
        c.setId(id);
        c.setStationId(stationId);
        c.setConnectorNo(1);
        c.setStatus("AVAILABLE");
        return c;
    }

    @Test
    void case1_startSuccessRecordsAndSetsCharging()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(stationMapper.selectByIdAndTenant(1L, 1L)).thenReturn(station(1L, 1L));
            when(connectorMapper.selectByIdAndTenant(1L, 1L)).thenReturn(connector(1L, 1L));
            OpenbizDevice device = new OpenbizDevice();
            device.setId(1L);
            when(deviceService.get(1L)).thenReturn(device);
            when(deviceCommandService.invoke(eq(1L), eq("open_door"), eq("{}"), anyString())).thenReturn(10L);
            OpenbizDeviceCommand cmd = new OpenbizDeviceCommand();
            cmd.setCommandId("cmd-chg-1");
            cmd.setStatus("SUCCESS");
            when(deviceCommandService.get(10L)).thenReturn(cmd);

            ChargingStartResult result = chargingService.startCharging(1L, 1L);
            assertTrue(result.isSuccess());
            assertEquals(1L, result.getStationId());
            assertEquals(1L, result.getConnectorId());
            assertEquals(1L, result.getDeviceId());
            assertEquals("cmd-chg-1", result.getCommandId());

            ArgumentCaptor<OpenbizChargingRecord> captor = ArgumentCaptor.forClass(OpenbizChargingRecord.class);
            verify(recordMapper).insert(captor.capture());
            assertEquals("SUCCESS", captor.getValue().getResult());
            assertEquals("cmd-chg-1", captor.getValue().getCommandId());
            verify(connectorMapper).updateStatus(1L, 1L, "CHARGING");
        }
    }

    @Test
    void case2_crossTenantStationNotFound()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(stationMapper.selectByIdAndTenant(2L, 1L)).thenReturn(null);
            ServiceException ex = assertThrows(ServiceException.class, () -> chargingService.startCharging(2L, 2L));
            assertEquals(HttpStatus.NOT_FOUND, ex.getCode());
            verify(deviceCommandService, never()).invoke(any(), anyString(), anyString(), anyString());
        }
    }

    @Test
    void case3_stationMissing()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(stationMapper.selectByIdAndTenant(99L, 1L)).thenReturn(null);
            ServiceException ex = assertThrows(ServiceException.class, () -> chargingService.startCharging(99L, 1L));
            assertEquals(HttpStatus.NOT_FOUND, ex.getCode());
            assertTrue(ex.getMessage().contains("station"));
        }
    }

    @Test
    void case4_connectorMissing()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(stationMapper.selectByIdAndTenant(1L, 1L)).thenReturn(station(1L, 1L));
            when(connectorMapper.selectByIdAndTenant(99L, 1L)).thenReturn(null);
            ServiceException ex = assertThrows(ServiceException.class, () -> chargingService.startCharging(1L, 99L));
            assertEquals(HttpStatus.NOT_FOUND, ex.getCode());
            assertTrue(ex.getMessage().contains("connector"));
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
            when(stationMapper.selectByIdAndTenant(1L, 1L)).thenReturn(station(1L, 999L));
            when(connectorMapper.selectByIdAndTenant(1L, 1L)).thenReturn(connector(1L, 1L));
            when(deviceService.get(999L)).thenReturn(null);
            ServiceException ex = assertThrows(ServiceException.class, () -> chargingService.startCharging(1L, 1L));
            assertEquals(HttpStatus.NOT_FOUND, ex.getCode());
            assertTrue(ex.getMessage().contains("device"));
            verify(deviceCommandService, never()).invoke(any(), anyString(), anyString(), anyString());
        }
    }

    @Test
    void case6_commandFailedKeepsConnectorAvailable()
    {
        TenantContext.setTenantId(1L);
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            when(stationMapper.selectByIdAndTenant(1L, 1L)).thenReturn(station(1L, 1L));
            when(connectorMapper.selectByIdAndTenant(1L, 1L)).thenReturn(connector(1L, 1L));
            OpenbizDevice device = new OpenbizDevice();
            device.setId(1L);
            when(deviceService.get(1L)).thenReturn(device);
            when(deviceCommandService.invoke(eq(1L), eq("open_door"), eq("{}"), anyString())).thenReturn(11L);
            OpenbizDeviceCommand cmd = new OpenbizDeviceCommand();
            cmd.setCommandId("cmd-fail");
            cmd.setStatus("FAILED");
            cmd.setErrorMessage("protocol rejected command");
            when(deviceCommandService.get(11L)).thenReturn(cmd);

            ServiceException ex = assertThrows(ServiceException.class, () -> chargingService.startCharging(1L, 1L));
            assertEquals(HttpStatus.ERROR, ex.getCode());

            ArgumentCaptor<OpenbizChargingRecord> captor = ArgumentCaptor.forClass(OpenbizChargingRecord.class);
            verify(recordMapper).insert(captor.capture());
            assertEquals("FAILED", captor.getValue().getResult());
            assertEquals("cmd-fail", captor.getValue().getCommandId());
            verify(connectorMapper, never()).updateStatus(any(), any(), anyString());
        }
    }

    @Test
    void rejectsWithoutTenant()
    {
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUserId).thenReturn(1L);
            ServiceException ex = assertThrows(ServiceException.class, () -> chargingService.startCharging(1L, 1L));
            assertTrue(ex.getMessage().contains("NO_TENANT"));
        }
    }
}
