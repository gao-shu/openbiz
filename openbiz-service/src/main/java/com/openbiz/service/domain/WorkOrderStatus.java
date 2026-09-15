package com.openbiz.service.domain;

/**
 * WorkOrder status. Transitions only via WorkOrderTransitions.
 */
public enum WorkOrderStatus
{
    CREATED,
    ASSIGNED,
    ACCEPTED,
    COMPLETED,
    CANCELLED;

    public static WorkOrderStatus fromCode(String code)
    {
        if (code == null || code.isBlank())
        {
            throw new IllegalArgumentException("status blank");
        }
        return WorkOrderStatus.valueOf(code.trim());
    }
}
