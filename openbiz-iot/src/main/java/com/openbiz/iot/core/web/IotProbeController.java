package com.openbiz.iot.core.web;

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
import com.openbiz.iot.core.api.ProductService;
import com.openbiz.iot.core.domain.OpenbizDevice;
import com.openbiz.iot.core.domain.OpenbizDeviceCommand;
import com.openbiz.iot.core.domain.OpenbizProduct;
import com.openbiz.iot.core.mock.MockDoorDevice;
import com.openbiz.iot.core.mock.MockDoorDeviceStore;
import com.openbiz.saas.api.TenantService;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * Phase 1.3 / 1.4 verification endpoints only (not product APIs).
 */
@RestController
@RequestMapping("/openbiz/test/iot")
public class IotProbeController
{
    private final TenantService tenantService;
    private final ProductService productService;
    private final DeviceService deviceService;
    private final DeviceCommandService deviceCommandService;
    private final MockDoorDeviceStore mockDoorDeviceStore;

    public IotProbeController(TenantService tenantService,
                              ProductService productService,
                              DeviceService deviceService,
                              DeviceCommandService deviceCommandService,
                              MockDoorDeviceStore mockDoorDeviceStore)
    {
        this.tenantService = tenantService;
        this.productService = productService;
        this.deviceService = deviceService;
        this.deviceCommandService = deviceCommandService;
        this.mockDoorDeviceStore = mockDoorDeviceStore;
    }

    @GetMapping("/devices")
    public AjaxResult listDevices()
    {
        List<OpenbizDevice> devices = deviceService.list();
        Map<String, Object> data = new HashMap<>(4);
        data.put("tenantId", tenantService.currentTenantId());
        data.put("devices", devices.stream().map(d -> {
            Map<String, Object> m = new HashMap<>(8);
            m.put("id", d.getId());
            m.put("deviceCode", d.getDeviceCode());
            m.put("productId", d.getProductId());
            m.put("tenantId", d.getTenantId());
            return m;
        }).collect(Collectors.toList()));
        return AjaxResult.success(data);
    }

    @GetMapping("/products")
    public AjaxResult listProducts()
    {
        List<OpenbizProduct> products = productService.list();
        Map<String, Object> data = new HashMap<>(4);
        data.put("tenantId", tenantService.currentTenantId());
        data.put("products", products);
        return AjaxResult.success(data);
    }

    @PostMapping("/devices/{deviceId}/invoke")
    public AjaxResult invoke(@PathVariable Long deviceId,
                             @RequestParam(defaultValue = "open_door") String serviceId,
                             @RequestParam(required = false) String idempotentKey)
    {
        String key = idempotentKey != null ? idempotentKey : ("probe-" + System.currentTimeMillis());
        Long id = deviceCommandService.invoke(deviceId, serviceId, "{}", key);
        OpenbizDeviceCommand cmd = deviceCommandService.get(id);
        return AjaxResult.success(cmd);
    }

    @GetMapping("/commands/{id}")
    public AjaxResult getCommand(@PathVariable Long id)
    {
        OpenbizDeviceCommand cmd = deviceCommandService.get(id);
        if (cmd == null)
        {
            return AjaxResult.error("command not found in current tenant");
        }
        return AjaxResult.success(cmd);
    }

    /**
     * Observe in-memory mock door state. Tenant-scoped via DeviceService.
     */
    @GetMapping("/mock-devices/{deviceId}")
    public AjaxResult mockDeviceState(@PathVariable Long deviceId)
    {
        OpenbizDevice device = deviceService.get(deviceId);
        if (device == null)
        {
            return AjaxResult.error("device not found in current tenant");
        }
        MockDoorDevice mock = mockDoorDeviceStore.get(deviceId);
        Map<String, Object> data = new HashMap<>(8);
        data.put("deviceId", device.getId());
        data.put("deviceCode", device.getDeviceCode());
        data.put("tenantId", device.getTenantId());
        data.put("state", mock == null ? MockDoorDevice.STATE_CLOSED : mock.getState());
        return AjaxResult.success(data);
    }
}
