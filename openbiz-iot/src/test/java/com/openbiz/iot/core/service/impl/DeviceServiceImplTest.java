package com.openbiz.iot.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.openbiz.iot.core.domain.OpenbizDevice;
import com.openbiz.iot.core.domain.OpenbizProduct;
import com.openbiz.iot.core.mapper.OpenbizDeviceMapper;
import com.openbiz.iot.core.mapper.OpenbizProductMapper;
import com.openbiz.saas.context.TenantContext;
import com.ruoyi.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest
{
    @Mock
    private OpenbizDeviceMapper deviceMapper;
    @Mock
    private OpenbizProductMapper productMapper;
    @InjectMocks
    private DeviceServiceImpl deviceService;

    @AfterEach
    void tearDown()
    {
        TenantContext.clear();
    }

    @Test
    void rejectsWhenNoTenant()
    {
        assertThrows(ServiceException.class, () -> deviceService.list());
    }

    @Test
    void listOnlyCurrentTenant()
    {
        TenantContext.setTenantId(1L);
        OpenbizDevice d = new OpenbizDevice();
        d.setId(1L);
        d.setTenantId(1L);
        d.setDeviceCode("DOOR-001");
        when(deviceMapper.selectByTenant(1L)).thenReturn(Collections.singletonList(d));

        List<OpenbizDevice> list = deviceService.list();
        assertEquals(1, list.size());
        assertEquals("DOOR-001", list.get(0).getDeviceCode());
        verify(deviceMapper).selectByTenant(1L);
    }

    @Test
    void getOtherTenantDeviceReturnsNull()
    {
        TenantContext.setTenantId(1L);
        when(deviceMapper.selectByIdAndTenant(2L, 1L)).thenReturn(null);
        assertNull(deviceService.get(2L));
    }

    @Test
    void createRequiresProductInSameTenant()
    {
        TenantContext.setTenantId(1L);
        OpenbizDevice device = new OpenbizDevice();
        device.setProductId(99L);
        device.setDeviceCode("X");
        device.setDeviceName("X");
        when(productMapper.selectByIdAndTenant(99L, 1L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> deviceService.create(device));
    }

    @Test
    void createOk()
    {
        TenantContext.setTenantId(1L);
        OpenbizProduct product = new OpenbizProduct();
        product.setId(1L);
        when(productMapper.selectByIdAndTenant(1L, 1L)).thenReturn(product);
        when(deviceMapper.insert(any())).thenAnswer(inv -> {
            OpenbizDevice d = inv.getArgument(0);
            d.setId(10L);
            return 1;
        });

        OpenbizDevice device = new OpenbizDevice();
        device.setProductId(1L);
        device.setDeviceCode("DOOR-010");
        device.setDeviceName("Door 010");
        Long id = deviceService.create(device);
        assertEquals(10L, id);

        ArgumentCaptor<OpenbizDevice> captor = ArgumentCaptor.forClass(OpenbizDevice.class);
        verify(deviceMapper).insert(captor.capture());
        assertEquals(1L, captor.getValue().getTenantId());
        assertEquals("OFFLINE", captor.getValue().getOnlineStatus());
    }
}
