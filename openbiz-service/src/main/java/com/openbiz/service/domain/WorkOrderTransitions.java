package com.openbiz.service.domain;

import java.util.EnumSet;
import java.util.Set;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;

/**
 * Single source of WorkOrder transition rules.
 */
public final class WorkOrderTransitions
{
    private WorkOrderTransitions()
    {
    }

    public static boolean canTransition(WorkOrderStatus from, WorkOrderStatus to)
    {
        if (from == null || to == null || from == to)
        {
            return false;
        }
        return allowed(from).contains(to);
    }

    public static void requireTransition(WorkOrderStatus from, WorkOrderStatus to)
    {
        if (!canTransition(from, to))
        {
            throw new ServiceException(
                    "ILLEGAL_TRANSITION: " + from + " -> " + to,
                    HttpStatus.BAD_REQUEST);
        }
    }

    private static Set<WorkOrderStatus> allowed(WorkOrderStatus from)
    {
        return switch (from)
        {
            case CREATED -> EnumSet.of(WorkOrderStatus.ASSIGNED, WorkOrderStatus.CANCELLED);
            case ASSIGNED -> EnumSet.of(WorkOrderStatus.ACCEPTED, WorkOrderStatus.CANCELLED);
            case ACCEPTED -> EnumSet.of(WorkOrderStatus.COMPLETED);
            case COMPLETED, CANCELLED -> EnumSet.noneOf(WorkOrderStatus.class);
        };
    }
}
