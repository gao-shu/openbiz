package com.openbiz.locker.api;

public class LockerOpenResult
{
    private boolean success;
    private Long lockerId;
    private Long slotId;
    private Long deviceId;
    private String commandId;
    private String result;

    public static LockerOpenResult of(boolean success, Long lockerId, Long slotId,
                                      Long deviceId, String commandId, String result)
    {
        LockerOpenResult r = new LockerOpenResult();
        r.success = success;
        r.lockerId = lockerId;
        r.slotId = slotId;
        r.deviceId = deviceId;
        r.commandId = commandId;
        r.result = result;
        return r;
    }

    public boolean isSuccess() { return success; }
    public Long getLockerId() { return lockerId; }
    public Long getSlotId() { return slotId; }
    public Long getDeviceId() { return deviceId; }
    public String getCommandId() { return commandId; }
    public String getResult() { return result; }
}
