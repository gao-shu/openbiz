package com.openbiz.access.api;

public class AccessOpenResult
{
    private boolean success;
    private Long deviceId;
    private String commandId;
    private String result;

    public static AccessOpenResult of(boolean success, Long deviceId, String commandId, String result)
    {
        AccessOpenResult r = new AccessOpenResult();
        r.success = success;
        r.deviceId = deviceId;
        r.commandId = commandId;
        r.result = result;
        return r;
    }

    public boolean isSuccess() { return success; }
    public Long getDeviceId() { return deviceId; }
    public String getCommandId() { return commandId; }
    public String getResult() { return result; }
}
