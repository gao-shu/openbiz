package com.openbiz.iot.core.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MockDoorProtocolPortTest
{
    private MockDoorDeviceStore store;
    private MockDoorProtocolPort port;

    @BeforeEach
    void setUp()
    {
        store = new MockDoorDeviceStore();
        port = new MockDoorProtocolPort(store);
    }

    @Test
    void openAndClose()
    {
        assertTrue(port.sendCommand(1L, "DEMO-DOOR", "DOOR-001", "open_door", "{}", 1L, "k1"));
        assertEquals(MockDoorDevice.STATE_OPEN, store.get(1L).getState());
        assertTrue(port.sendCommand(1L, "DEMO-DOOR", "DOOR-001", "close_door", "{}", 2L, "k2"));
        assertEquals(MockDoorDevice.STATE_CLOSED, store.get(1L).getState());
    }

    @Test
    void unknownCommandFails()
    {
        assertTrue(port.sendCommand(1L, "DEMO-DOOR", "DOOR-001", "open_door", "{}", 1L, "k1"));
        assertFalse(port.sendCommand(1L, "DEMO-DOOR", "DOOR-001", "xxx", "{}", 2L, "k2"));
        assertEquals(MockDoorDevice.STATE_OPEN, store.get(1L).getState());
    }

    @Test
    void protocolIsMock()
    {
        assertEquals("MOCK", port.protocol());
    }
}
