package com.openbiz.iot.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.openbiz.iot.core.domain.OpenbizDevice;
import com.openbiz.iot.core.domain.OpenbizDeviceCommand;
import com.openbiz.iot.core.domain.OpenbizProduct;
import com.openbiz.iot.core.mapper.OpenbizDeviceCommandMapper;
import com.openbiz.iot.core.mapper.OpenbizDeviceMapper;
import com.openbiz.iot.core.mapper.OpenbizProductMapper;
import com.openbiz.iot.core.spi.ProtocolPort;
import com.openbiz.iot.core.spi.ProtocolPortRegistry;
import com.openbiz.saas.context.TenantContext;
import com.ruoyi.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class DeviceCommandServiceImplTest
{
    @Mock
    private OpenbizDeviceCommandMapper commandMapper;
    @Mock
    private OpenbizDeviceMapper deviceMapper;
    @Mock
    private OpenbizProductMapper productMapper;
    @Mock
    private ProtocolPortRegistry protocolPortRegistry;
    @Mock
    private ProtocolPort protocolPort;
    @InjectMocks
    private DeviceCommandServiceImpl commandService;

    @AfterEach
    void tearDown()
    {
        TenantContext.clear();
    }

    @Test
    void rejectsWhenNoTenant()
    {
        assertThrows(ServiceException.class, () -> commandService.invoke(1L, "open_door", "{}", "k1"));
    }

    @Test
    void cannotInvokeOtherTenantDevice()
    {
        TenantContext.setTenantId(1L);
        when(deviceMapper.selectByIdAndTenant(2L, 1L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> commandService.invoke(2L, "open_door", "{}", "k1"));
    }

    @Test
    void sendRecordsCommandAndCallsProtocol()
    {
        TenantContext.setTenantId(1L);
        OpenbizDevice device = new OpenbizDevice();
        device.setId(1L);
        device.setProductId(1L);
        device.setDeviceCode("DOOR-001");
        OpenbizProduct product = new OpenbizProduct();
        product.setId(1L);
        product.setProductCode("DEMO-DOOR");
        product.setProtocol("MOCK");
        when(deviceMapper.selectByIdAndTenant(1L, 1L)).thenReturn(device);
        when(productMapper.selectByIdAndTenant(1L, 1L)).thenReturn(product);
        when(protocolPortRegistry.require("MOCK")).thenReturn(protocolPort);
        when(commandMapper.insert(any())).thenAnswer(inv -> {
            OpenbizDeviceCommand c = inv.getArgument(0);
            c.setId(100L);
            return 1;
        });
        when(protocolPort.sendCommand(eq(1L), eq("DEMO-DOOR"), eq("DOOR-001"),
                eq("open_door"), eq("{}"), eq(100L), eq("k-1"))).thenReturn(true);

        Long id = commandService.invoke(1L, "open_door", "{}", "k-1");
        assertEquals(100L, id);

        ArgumentCaptor<OpenbizDeviceCommand> captor = ArgumentCaptor.forClass(OpenbizDeviceCommand.class);
        verify(commandMapper).updateStatus(captor.capture());
        assertEquals(DeviceCommandServiceImpl.STATUS_SUCCESS, captor.getValue().getStatus());
        assertEquals(1L, captor.getValue().getTenantId());
        assertEquals(1L, captor.getValue().getDeviceId());
    }

    @Test
    void getOtherTenantCommandReturnsNull()
    {
        TenantContext.setTenantId(1L);
        when(commandMapper.selectByIdAndTenant(99L, 1L)).thenReturn(null);
        assertNull(commandService.get(99L));
    }

    @Test
    void protocolRejectMarksFailed()
    {
        TenantContext.setTenantId(1L);
        OpenbizDevice device = new OpenbizDevice();
        device.setId(1L);
        device.setProductId(1L);
        device.setDeviceCode("DOOR-001");
        OpenbizProduct product = new OpenbizProduct();
        product.setId(1L);
        product.setProductCode("DEMO-DOOR");
        product.setProtocol("MOCK");
        when(deviceMapper.selectByIdAndTenant(1L, 1L)).thenReturn(device);
        when(productMapper.selectByIdAndTenant(1L, 1L)).thenReturn(product);
        when(protocolPortRegistry.require("MOCK")).thenReturn(protocolPort);
        when(commandMapper.insert(any())).thenAnswer(inv -> {
            OpenbizDeviceCommand c = inv.getArgument(0);
            c.setId(101L);
            return 1;
        });
        when(protocolPort.sendCommand(any(), any(), any(), any(), any(), any(), any())).thenReturn(false);

        Long id = commandService.invoke(1L, "open_door", "{}", "k-2");
        assertEquals(101L, id);
        ArgumentCaptor<OpenbizDeviceCommand> captor = ArgumentCaptor.forClass(OpenbizDeviceCommand.class);
        verify(commandMapper).updateStatus(captor.capture());
        assertEquals(DeviceCommandServiceImpl.STATUS_FAILED, captor.getValue().getStatus());
    }
}
