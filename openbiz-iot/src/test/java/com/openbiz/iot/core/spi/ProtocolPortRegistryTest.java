package com.openbiz.iot.core.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import com.ruoyi.common.exception.ServiceException;

class ProtocolPortRegistryTest
{
    @Test
    void resolvesByProtocol()
    {
        ProtocolPort mockPort = mock(ProtocolPort.class);
        when(mockPort.protocol()).thenReturn("MOCK");
        ProtocolPort mqtt = mock(ProtocolPort.class);
        when(mqtt.protocol()).thenReturn("MQTT");
        ProtocolPortRegistry registry = new ProtocolPortRegistry(Arrays.asList(mockPort, mqtt));
        assertEquals(mockPort, registry.require("mock"));
        assertEquals(mqtt, registry.require("MQTT"));
    }

    @Test
    void missingProtocolFails()
    {
        ProtocolPortRegistry registry = new ProtocolPortRegistry(Collections.emptyList());
        assertThrows(ServiceException.class, () -> registry.require("MOCK"));
    }
}
