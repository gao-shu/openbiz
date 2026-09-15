package com.openbiz.saas.web;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.openbiz.saas.api.TenantService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * Phase 1.2 verification endpoint only (not a product API).
 */
@RestController
@RequestMapping("/openbiz/test")
public class TenantProbeController
{
    private final TenantService tenantService;

    public TenantProbeController(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    @GetMapping("/tenant")
    public AjaxResult currentTenant()
    {
        Long tenantId = tenantService.currentTenantId();
        if (tenantId == null)
        {
            return AjaxResult.error("NO_TENANT_CONTEXT: login user has no active OpenBiz membership");
        }
        Map<String, Object> data = new HashMap<>(4);
        data.put("tenantId", tenantId);
        data.put("userId", SecurityUtils.getUserId());
        return AjaxResult.success(data);
    }
}
