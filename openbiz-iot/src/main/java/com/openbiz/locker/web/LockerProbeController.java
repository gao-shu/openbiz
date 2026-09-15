package com.openbiz.locker.web;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.openbiz.locker.api.LockerOpenResult;
import com.openbiz.locker.api.LockerService;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * Phase 1.6 verification probe. Uses current login user only.
 */
@RestController
@RequestMapping("/openbiz/test/locker")
public class LockerProbeController
{
    private final LockerService lockerService;

    public LockerProbeController(LockerService lockerService)
    {
        this.lockerService = lockerService;
    }

    @PostMapping("/open")
    public AjaxResult open(@RequestBody Map<String, Object> body)
    {
        if (body == null || body.get("lockerId") == null || body.get("slotId") == null)
        {
            return AjaxResult.error("lockerId and slotId are required");
        }
        Long lockerId = Long.valueOf(String.valueOf(body.get("lockerId")));
        Long slotId = Long.valueOf(String.valueOf(body.get("slotId")));
        LockerOpenResult result = lockerService.openLocker(lockerId, slotId);
        Map<String, Object> data = new HashMap<>(8);
        data.put("success", result.isSuccess());
        data.put("lockerId", result.getLockerId());
        data.put("slotId", result.getSlotId());
        data.put("deviceId", result.getDeviceId());
        data.put("commandId", result.getCommandId());
        data.put("result", result.getResult());
        return AjaxResult.success(data);
    }
}
