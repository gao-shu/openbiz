package com.openbiz.iot.core.mock;

/**
 * In-memory mock door. Phase 1.4 only: OPEN / CLOSED.
 */
public class MockDoorDevice
{
    public static final String STATE_CLOSED = "CLOSED";
    public static final String STATE_OPEN = "OPEN";

    public static final String CMD_OPEN = "open_door";
    public static final String CMD_CLOSE = "close_door";

    private final Long deviceId;
    private volatile String state = STATE_CLOSED;

    public MockDoorDevice(Long deviceId)
    {
        this.deviceId = deviceId;
    }

    public Long getDeviceId()
    {
        return deviceId;
    }

    public String getState()
    {
        return state;
    }

    /**
     * @return true if command applied, false if unknown command
     */
    public synchronized boolean apply(String serviceId)
    {
        if (CMD_OPEN.equals(serviceId))
        {
            state = STATE_OPEN;
            return true;
        }
        if (CMD_CLOSE.equals(serviceId))
        {
            state = STATE_CLOSED;
            return true;
        }
        return false;
    }
}
