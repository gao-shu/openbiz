package com.openbiz.access.web;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.openbiz.access.api.AccessOpenResult;
import com.openbiz.access.api.AccessService;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * Phase 1.5 verification probe. Uses current login user only.
 */
@RestController
@RequestMapping("/openbiz/test/access")
public class AccessProbeController
{
    private final AccessService accessService;

    public AccessProbeController(AccessService accessService)
    {
        this.accessService = accessService;
    }

    @PostMapping("/open")
    public AjaxResult open(@RequestBody Map<String, Object> body)
    {
        Object raw = body == null ? null : body.get("deviceId");
        if (raw == null)
        {
            return AjaxResult.error("deviceId is required");
        }
        Long deviceId = Long.valueOf(String.valueOf(raw));
        AccessOpenResult result = accessService.openDoor(deviceId);
        Map<String, Object> data = new HashMap<>(8);
        data.put("success", result.isSuccess());
        data.put("deviceId", result.getDeviceId());
        data.put("commandId", result.getCommandId());
        data.put("result", result.getResult());
        return AjaxResult.success(data);
    }
}
