package com.openbiz.mes.web;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.openbiz.mes.api.MesService;
import com.openbiz.mes.api.MesStartResult;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * Phase 1.8 verification probe. Uses current login user only.
 */
@RestController
@RequestMapping("/openbiz/test/mes")
public class MesProbeController
{
    private final MesService mesService;

    public MesProbeController(MesService mesService)
    {
        this.mesService = mesService;
    }

    @PostMapping("/start-production")
    public AjaxResult startProduction(@RequestBody Map<String, Object> body)
    {
        if (body == null || body.get("workOrderId") == null)
        {
            return AjaxResult.error("workOrderId is required");
        }
        Long workOrderId = Long.valueOf(String.valueOf(body.get("workOrderId")));
        MesStartResult result = mesService.startProduction(workOrderId);
        Map<String, Object> data = new HashMap<>(8);
        data.put("success", result.isSuccess());
        data.put("workOrderId", result.getWorkOrderId());
        data.put("deviceId", result.getDeviceId());
        data.put("commandId", result.getCommandId());
        data.put("workOrderStatus", result.getWorkOrderStatus());
        data.put("result", result.getResult());
        return AjaxResult.success(data);
    }
}
