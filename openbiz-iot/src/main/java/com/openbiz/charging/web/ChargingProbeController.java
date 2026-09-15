package com.openbiz.charging.web;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.openbiz.charging.api.ChargingService;
import com.openbiz.charging.api.ChargingStartResult;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * Phase 1.7 verification probe. Uses current login user only.
 */
@RestController
@RequestMapping("/openbiz/test/charging")
public class ChargingProbeController
{
    private final ChargingService chargingService;

    public ChargingProbeController(ChargingService chargingService)
    {
        this.chargingService = chargingService;
    }

    @PostMapping("/start")
    public AjaxResult start(@RequestBody Map<String, Object> body)
    {
        if (body == null || body.get("stationId") == null || body.get("connectorId") == null)
        {
            return AjaxResult.error("stationId and connectorId are required");
        }
        Long stationId = Long.valueOf(String.valueOf(body.get("stationId")));
        Long connectorId = Long.valueOf(String.valueOf(body.get("connectorId")));
        ChargingStartResult result = chargingService.startCharging(stationId, connectorId);
        Map<String, Object> data = new HashMap<>(8);
        data.put("success", result.isSuccess());
        data.put("stationId", result.getStationId());
        data.put("connectorId", result.getConnectorId());
        data.put("deviceId", result.getDeviceId());
        data.put("commandId", result.getCommandId());
        data.put("result", result.getResult());
        return AjaxResult.success(data);
    }
}
