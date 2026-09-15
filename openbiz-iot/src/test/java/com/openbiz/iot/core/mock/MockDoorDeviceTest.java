package com.openbiz.iot.core.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class MockDoorDeviceTest
{
    @Test
    void openThenClose()
    {
        MockDoorDevice door = new MockDoorDevice(1L);
        assertEquals(MockDoorDevice.STATE_CLOSED, door.getState());
        assertTrue(door.apply(MockDoorDevice.CMD_OPEN));
        assertEquals(MockDoorDevice.STATE_OPEN, door.getState());
        assertTrue(door.apply(MockDoorDevice.CMD_CLOSE));
        assertEquals(MockDoorDevice.STATE_CLOSED, door.getState());
    }

    @Test
    void invalidCommandFailsAndKeepsState()
    {
        MockDoorDevice door = new MockDoorDevice(1L);
        assertTrue(door.apply(MockDoorDevice.CMD_OPEN));
        assertFalse(door.apply("xxx"));
        assertEquals(MockDoorDevice.STATE_OPEN, door.getState());
    }
}
