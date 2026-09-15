package com.openbiz.iot.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.openbiz.iot.core.domain.OpenbizDevice;
import com.openbiz.iot.core.domain.OpenbizDeviceCommand;
import com.openbiz.iot.core.domain.OpenbizProduct;
import com.openbiz.iot.core.mapper.OpenbizDeviceCommandMapper;
import com.openbiz.iot.core.mapper.OpenbizDeviceMapper;
import com.openbiz.iot.core.mapper.OpenbizProductMapper;
import com.openbiz.iot.core.mock.MockDoorDevice;
import com.openbiz.iot.core.mock.MockDoorDeviceStore;
import com.openbiz.iot.core.mock.MockDoorProtocolPort;
import com.openbiz.iot.core.spi.ProtocolPortRegistry;
import com.openbiz.saas.context.TenantContext;
import com.ruoyi.common.exception.ServiceException;

/**
 * End-to-end within IoT core: real Mock protocol + registry, mocked persistence.
 */
@ExtendWith(MockitoExtension.class)
class DeviceCommandMockDoorFlowTest
{
    @Mock
    private OpenbizDeviceCommandMapper commandMapper;
    @Mock
    private OpenbizDeviceMapper deviceMapper;
    @Mock
    private OpenbizProductMapper productMapper;

    private MockDoorDeviceStore store;
    private DeviceCommandServiceImpl commandService;

    @BeforeEach
    void setUp()
    {
        store = new MockDoorDeviceStore();
        MockDoorProtocolPort mockPort = new MockDoorProtocolPort(store);
        ProtocolPortRegistry registry = new ProtocolPortRegistry(Collections.singletonList(mockPort));
        commandService = new DeviceCommandServiceImpl(commandMapper, deviceMapper, productMapper, registry);
    }

    @AfterEach
    void tearDown()
    {
        TenantContext.clear();
        store.clear();
    }

    @Test
    void openThenCloseChangesState()
    {
        TenantContext.setTenantId(1L);
        stubDevice(1L, 1L, "DOOR-001");
        when(commandMapper.insert(any())).thenAnswer(inv -> {
            OpenbizDeviceCommand c = inv.getArgument(0);
            c.setId(c.getCommandName().equals("open_door") ? 1L : 2L);
            return 1;
        });

        commandService.invoke(1L, "open_door", "{}", "k-open");
        assertEquals(MockDoorDevice.STATE_OPEN, store.get(1L).getState());

        commandService.invoke(1L, "close_door", "{}", "k-close");
        assertEquals(MockDoorDevice.STATE_CLOSED, store.get(1L).getState());
    }

    @Test
    void invalidCommandFailsAndKeepsOpen()
    {
        TenantContext.setTenantId(1L);
        stubDevice(1L, 1L, "DOOR-001");
        when(commandMapper.insert(any())).thenAnswer(inv -> {
            OpenbizDeviceCommand c = inv.getArgument(0);
            c.setId(10L);
            return 1;
        });
        commandService.invoke(1L, "open_door", "{}", "k1");
        Long failedId = commandService.invoke(1L, "xxx", "{}", "k2");
        assertEquals(10L, failedId);
        assertEquals(MockDoorDevice.STATE_OPEN, store.get(1L).getState());
    }

    @Test
    void crossTenantStillRejected()
    {
        TenantContext.setTenantId(1L);
        when(deviceMapper.selectByIdAndTenant(2L, 1L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> commandService.invoke(2L, "open_door", "{}", "k"));
    }

    private void stubDevice(Long deviceId, Long productId, String code)
    {
        OpenbizDevice device = new OpenbizDevice();
        device.setId(deviceId);
        device.setProductId(productId);
        device.setDeviceCode(code);
        OpenbizProduct product = new OpenbizProduct();
        product.setId(productId);
        product.setProductCode("DEMO-DOOR");
        product.setProtocol("MOCK");
        when(deviceMapper.selectByIdAndTenant(deviceId, 1L)).thenReturn(device);
        when(productMapper.selectByIdAndTenant(productId, 1L)).thenReturn(product);
    }
}
