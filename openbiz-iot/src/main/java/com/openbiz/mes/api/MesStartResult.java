package com.openbiz.mes.api;

public class MesStartResult
{
    private boolean success;
    private Long workOrderId;
    private Long deviceId;
    private String commandId;
    private String workOrderStatus;
    private String result;

    public static MesStartResult of(boolean success, Long workOrderId, Long deviceId,
                                    String commandId, String workOrderStatus, String result)
    {
        MesStartResult r = new MesStartResult();
        r.success = success;
        r.workOrderId = workOrderId;
        r.deviceId = deviceId;
        r.commandId = commandId;
        r.workOrderStatus = workOrderStatus;
        r.result = result;
        return r;
    }

    public boolean isSuccess() { return success; }
    public Long getWorkOrderId() { return workOrderId; }
    public Long getDeviceId() { return deviceId; }
    public String getCommandId() { return commandId; }
    public String getWorkOrderStatus() { return workOrderStatus; }
    public String getResult() { return result; }
}
