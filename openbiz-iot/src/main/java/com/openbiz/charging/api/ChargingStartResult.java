package com.openbiz.charging.api;

public class ChargingStartResult
{
    private boolean success;
    private Long stationId;
    private Long connectorId;
    private Long deviceId;
    private String commandId;
    private String result;

    public static ChargingStartResult of(boolean success, Long stationId, Long connectorId,
                                         Long deviceId, String commandId, String result)
    {
        ChargingStartResult r = new ChargingStartResult();
        r.success = success;
        r.stationId = stationId;
        r.connectorId = connectorId;
        r.deviceId = deviceId;
        r.commandId = commandId;
        r.result = result;
        return r;
    }

    public boolean isSuccess() { return success; }
    public Long getStationId() { return stationId; }
    public Long getConnectorId() { return connectorId; }
    public Long getDeviceId() { return deviceId; }
    public String getCommandId() { return commandId; }
    public String getResult() { return result; }
}
