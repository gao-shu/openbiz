package com.openbiz.service.web;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.openbiz.service.api.WorkOrderService;
import com.openbiz.service.domain.OpenbizWorkOrder;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * Phase 1 Service probe. Login + TenantContext required.
 */
@RestController
@RequestMapping("/openbiz/test/service")
public class ServiceProbeController
{
    private final WorkOrderService workOrderService;

    public ServiceProbeController(WorkOrderService workOrderService)
    {
        this.workOrderService = workOrderService;
    }

    @PostMapping("/work-orders")
    public AjaxResult create(@RequestBody Map<String, Object> body)
    {
        OpenbizWorkOrder wo = workOrderService.create(
                str(body, "title"),
                str(body, "content"),
                str(body, "contactName"),
                str(body, "contactPhone"),
                str(body, "idempotentKey"));
        return AjaxResult.success(toMap(wo));
    }

    @PostMapping("/work-orders/{id}/assign")
    public AjaxResult assign(@PathVariable Long id, @RequestBody Map<String, Object> body)
    {
        Long assigneeUserId = Long.valueOf(String.valueOf(body.get("assigneeUserId")));
        return AjaxResult.success(toMap(workOrderService.assign(id, assigneeUserId)));
    }

    @PostMapping("/work-orders/{id}/accept")
    public AjaxResult accept(@PathVariable Long id)
    {
        return AjaxResult.success(toMap(workOrderService.accept(id)));
    }

    @PostMapping("/work-orders/{id}/complete")
    public AjaxResult complete(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body)
    {
        String note = body == null ? null : str(body, "completeNote");
        return AjaxResult.success(toMap(workOrderService.complete(id, note)));
    }

    @PostMapping("/work-orders/{id}/cancel")
    public AjaxResult cancel(@PathVariable Long id)
    {
        return AjaxResult.success(toMap(workOrderService.cancel(id)));
    }

    @GetMapping("/work-orders/{id}")
    public AjaxResult get(@PathVariable Long id)
    {
        return AjaxResult.success(toMap(workOrderService.get(id)));
    }

    private static Map<String, Object> toMap(OpenbizWorkOrder wo)
    {
        Map<String, Object> data = new HashMap<>();
        data.put("id", wo.getId());
        data.put("tenantId", wo.getTenantId());
        data.put("title", wo.getTitle());
        data.put("content", wo.getContent());
        data.put("contactName", wo.getContactName());
        data.put("contactPhone", wo.getContactPhone());
        data.put("status", wo.getStatus());
        data.put("assigneeUserId", wo.getAssigneeUserId());
        data.put("completeNote", wo.getCompleteNote());
        data.put("idempotentKey", wo.getIdempotentKey());
        return data;
    }

    private static String str(Map<String, Object> body, String key)
    {
        if (body == null || body.get(key) == null)
        {
            return null;
        }
        return String.valueOf(body.get(key));
    }
}
