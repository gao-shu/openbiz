package com.openbiz.iot.core.mock;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * In-memory store for mock door devices (not persisted).
 */
@Component
public class MockDoorDeviceStore
{
    private final ConcurrentHashMap<Long, MockDoorDevice> devices = new ConcurrentHashMap<>();

    public MockDoorDevice getOrCreate(Long deviceId)
    {
        return devices.computeIfAbsent(deviceId, MockDoorDevice::new);
    }

    public MockDoorDevice get(Long deviceId)
    {
        if (deviceId == null)
        {
            return null;
        }
        return devices.get(deviceId);
    }

    /** Test helper */
    public void clear()
    {
        devices.clear();
    }
}
