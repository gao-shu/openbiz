package com.openbiz.service.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.ruoyi.common.exception.ServiceException;

class WorkOrderTransitionsTest
{
    @Test
    void happyPathAllowed()
    {
        assertTrue(WorkOrderTransitions.canTransition(WorkOrderStatus.CREATED, WorkOrderStatus.ASSIGNED));
        assertTrue(WorkOrderTransitions.canTransition(WorkOrderStatus.ASSIGNED, WorkOrderStatus.ACCEPTED));
        assertTrue(WorkOrderTransitions.canTransition(WorkOrderStatus.ACCEPTED, WorkOrderStatus.COMPLETED));
    }

    @Test
    void cancelAllowedFromCreatedAndAssigned()
    {
        assertTrue(WorkOrderTransitions.canTransition(WorkOrderStatus.CREATED, WorkOrderStatus.CANCELLED));
        assertTrue(WorkOrderTransitions.canTransition(WorkOrderStatus.ASSIGNED, WorkOrderStatus.CANCELLED));
        assertFalse(WorkOrderTransitions.canTransition(WorkOrderStatus.ACCEPTED, WorkOrderStatus.CANCELLED));
        assertFalse(WorkOrderTransitions.canTransition(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED));
    }

    @Test
    void illegalJumpsRejected()
    {
        assertFalse(WorkOrderTransitions.canTransition(WorkOrderStatus.CREATED, WorkOrderStatus.COMPLETED));
        assertFalse(WorkOrderTransitions.canTransition(WorkOrderStatus.CREATED, WorkOrderStatus.ACCEPTED));
        assertFalse(WorkOrderTransitions.canTransition(WorkOrderStatus.ASSIGNED, WorkOrderStatus.COMPLETED));
        assertFalse(WorkOrderTransitions.canTransition(WorkOrderStatus.ACCEPTED, WorkOrderStatus.CREATED));
        assertFalse(WorkOrderTransitions.canTransition(WorkOrderStatus.COMPLETED, WorkOrderStatus.CREATED));
        assertFalse(WorkOrderTransitions.canTransition(WorkOrderStatus.COMPLETED, WorkOrderStatus.ASSIGNED));
        assertThrows(ServiceException.class,
                () -> WorkOrderTransitions.requireTransition(WorkOrderStatus.CREATED, WorkOrderStatus.COMPLETED));
    }
}
