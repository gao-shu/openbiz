package com.openbiz.service.api;

import com.openbiz.service.domain.OpenbizWorkOrder;

/**
 * Minimal multi-tenant WorkOrder API.
 */
public interface WorkOrderService
{
    OpenbizWorkOrder create(String title, String content, String contactName, String contactPhone, String idempotentKey);

    OpenbizWorkOrder assign(Long workOrderId, Long assigneeUserId);

    OpenbizWorkOrder accept(Long workOrderId);

    OpenbizWorkOrder complete(Long workOrderId, String completeNote);

    OpenbizWorkOrder cancel(Long workOrderId);

    OpenbizWorkOrder get(Long workOrderId);
}
