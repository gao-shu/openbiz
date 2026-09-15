package com.openbiz.demo.iot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.openbiz.iot.core.api.DeviceCommandService;
import com.openbiz.iot.core.api.DeviceService;
import com.openbiz.iot.core.domain.OpenbizDevice;
import com.openbiz.iot.core.domain.OpenbizDeviceCommand;
import com.openbiz.saas.api.TenantService;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * Light industrial equipment monitoring Demo.
 * Consumes OpenBiz IoT + SaaS tenant; does not reimplement Core.
 */
@RestController
@RequestMapping("/openbiz/demo/iot")
public class IotMonitorDemoController
{
    private static final String SERVICE_OPEN_DOOR = "open_door";

    private final DeviceService deviceService;
    private final DeviceCommandService deviceCommandService;
    private final TenantService tenantService;

    public IotMonitorDemoController(DeviceService deviceService,
                                    DeviceCommandService deviceCommandService,
                                    TenantService tenantService)
    {
        this.deviceService = deviceService;
        this.deviceCommandService = deviceCommandService;
        this.tenantService = tenantService;
    }

    @GetMapping("/devices")
    public AjaxResult listDevices()
    {
        List<OpenbizDevice> devices = deviceService.list();
        Map<String, Object> data = new HashMap<>(4);
        data.put("tenantId", tenantService.currentTenantId());
        data.put("devices", devices.stream().map(this::toDeviceView).collect(Collectors.toList()));
        return AjaxResult.success(data);
    }

    @GetMapping("/devices/{id}")
    public AjaxResult getDevice(@PathVariable("id") Long id)
    {
        OpenbizDevice device = deviceService.get(id);
        if (device == null)
        {
            return AjaxResult.error("device not found in current tenant");
        }
        Map<String, Object> data = new HashMap<>(4);
        data.put("tenantId", tenantService.currentTenantId());
        data.put("device", toDeviceView(device));
        return AjaxResult.success(data);
    }

    /**
     * Send one demo command via DeviceCommandService (open_door).
     */
    @PostMapping("/devices/{id}/commands")
    public AjaxResult sendCommand(@PathVariable("id") Long id,
                                  @RequestParam(value = "serviceId", defaultValue = SERVICE_OPEN_DOOR) String serviceId,
                                  @RequestParam(value = "idempotentKey", required = false) String idempotentKey)
    {
        String key = idempotentKey != null ? idempotentKey : ("demo-" + System.currentTimeMillis());
        String sid = (serviceId == null || serviceId.isBlank()) ? SERVICE_OPEN_DOOR : serviceId;
        Long commandPk = deviceCommandService.invoke(id, sid, "{}", key);
        OpenbizDeviceCommand command = deviceCommandService.get(commandPk);
        return AjaxResult.success(command);
    }

    private Map<String, Object> toDeviceView(OpenbizDevice d)
    {
        Map<String, Object> m = new HashMap<>(12);
        m.put("id", d.getId());
        m.put("tenantId", d.getTenantId());
        m.put("productId", d.getProductId());
        m.put("deviceCode", d.getDeviceCode());
        m.put("deviceName", d.getDeviceName());
        m.put("status", d.getStatus());
        m.put("onlineStatus", d.getOnlineStatus());
        m.put("lastOnlineTime", d.getLastOnlineTime());
        m.put("lastOfflineTime", d.getLastOfflineTime());
        return m;
    }
}
