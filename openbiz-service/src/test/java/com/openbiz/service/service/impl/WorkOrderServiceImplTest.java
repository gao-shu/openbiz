package com.openbiz.service.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.openbiz.saas.context.TenantContext;
import com.openbiz.service.domain.OpenbizWorkOrder;
import com.openbiz.service.domain.WorkOrderStatus;
import com.openbiz.service.mapper.OpenbizWorkOrderMapper;
import com.openbiz.service.port.CurrentUserPort;
import com.openbiz.service.port.StaffUserPort;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceImplTest
{
    @Mock
    private OpenbizWorkOrderMapper workOrderMapper;
    @Mock
    private StaffUserPort staffUserPort;
    @Mock
    private CurrentUserPort currentUserPort;
    @InjectMocks
    private WorkOrderServiceImpl workOrderService;

    @BeforeEach
    void setTenant()
    {
        TenantContext.setTenantId(1L);
        when(currentUserPort.requireUserId()).thenReturn(100L);
    }

    @AfterEach
    void clearTenant()
    {
        TenantContext.clear();
    }

    @Test
    void create_startsAsCreated()
    {
        when(workOrderMapper.selectByIdempotent(1L, "k1")).thenReturn(null);
        when(workOrderMapper.insert(any())).thenAnswer(inv -> {
            ((OpenbizWorkOrder) inv.getArgument(0)).setId(9L);
            return 1;
        });
        when(workOrderMapper.selectByIdAndTenant(9L, 1L)).thenAnswer(inv -> sample(9L, WorkOrderStatus.CREATED, null));

        OpenbizWorkOrder wo = workOrderService.create("Fix AC", "noise", "Bob", "13800000001", "k1");
        assertEquals(WorkOrderStatus.CREATED.name(), wo.getStatus());
        ArgumentCaptor<OpenbizWorkOrder> cap = ArgumentCaptor.forClass(OpenbizWorkOrder.class);
        verify(workOrderMapper).insert(cap.capture());
        assertEquals("Bob", cap.getValue().getContactName());
        assertEquals("13800000001", cap.getValue().getContactPhone());
        assertEquals(1L, cap.getValue().getTenantId());
    }

    @Test
    void assign_accept_complete_happyPath()
    {
        doNothing().when(staffUserPort).requireActiveUser(200L);
        when(workOrderMapper.selectByIdAndTenant(eq(1L), eq(1L)))
                .thenReturn(sample(1L, WorkOrderStatus.CREATED, null))
                .thenReturn(sample(1L, WorkOrderStatus.ASSIGNED, 200L))
                .thenReturn(sample(1L, WorkOrderStatus.ASSIGNED, 200L))
                .thenReturn(sample(1L, WorkOrderStatus.ACCEPTED, 200L))
                .thenReturn(sample(1L, WorkOrderStatus.ACCEPTED, 200L))
                .thenReturn(sample(1L, WorkOrderStatus.COMPLETED, 200L));
        when(workOrderMapper.updateAssign(1L, 1L, "CREATED", 200L, "ASSIGNED")).thenReturn(1);
        when(workOrderMapper.updateStatus(eq(1L), eq(1L), eq("ASSIGNED"), eq("ACCEPTED"), any())).thenReturn(1);
        when(workOrderMapper.updateStatus(eq(1L), eq(1L), eq("ACCEPTED"), eq("COMPLETED"), eq("done"))).thenReturn(1);

        assertEquals("ASSIGNED", workOrderService.assign(1L, 200L).getStatus());
        when(currentUserPort.requireUserId()).thenReturn(200L);
        assertEquals("ACCEPTED", workOrderService.accept(1L).getStatus());
        assertEquals("COMPLETED", workOrderService.complete(1L, "done").getStatus());
    }

    @Test
    void accept_rejectsNonAssignee()
    {
        when(workOrderMapper.selectByIdAndTenant(1L, 1L))
                .thenReturn(sample(1L, WorkOrderStatus.ASSIGNED, 200L));
        when(currentUserPort.requireUserId()).thenReturn(100L);
        ServiceException ex = assertThrows(ServiceException.class, () -> workOrderService.accept(1L));
        assertEquals(Integer.valueOf(HttpStatus.FORBIDDEN), ex.getCode());
        verify(workOrderMapper, never()).updateStatus(any(), any(), any(), any(), any());
    }

    @Test
    void create_to_completed_rejected()
    {
        when(workOrderMapper.selectByIdAndTenant(1L, 1L))
                .thenReturn(sample(1L, WorkOrderStatus.CREATED, null));
        ServiceException ex = assertThrows(ServiceException.class, () -> workOrderService.complete(1L, "x"));
        assertTrue(ex.getMessage().contains("ILLEGAL_TRANSITION"));
    }

    @Test
    void get_crossTenant_notFound()
    {
        when(workOrderMapper.selectByIdAndTenant(1L, 1L)).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> workOrderService.get(1L));
        assertEquals(Integer.valueOf(HttpStatus.NOT_FOUND), ex.getCode());
    }

    @Test
    void noTenant_rejected()
    {
        TenantContext.clear();
        org.mockito.Mockito.clearInvocations(currentUserPort);
        org.mockito.Mockito.reset(currentUserPort);
        ServiceException ex = assertThrows(ServiceException.class,
                () -> workOrderService.create("t", "c", "n", "p", "k"));
        assertTrue(ex.getMessage().contains("NO_TENANT_CONTEXT"));
    }

    @Test
    void cancel_fromCreated()
    {
        when(workOrderMapper.selectByIdAndTenant(1L, 1L))
                .thenReturn(sample(1L, WorkOrderStatus.CREATED, null))
                .thenReturn(sample(1L, WorkOrderStatus.CANCELLED, null));
        when(workOrderMapper.updateStatus(1L, 1L, "CREATED", "CANCELLED", null)).thenReturn(1);
        assertEquals("CANCELLED", workOrderService.cancel(1L).getStatus());
    }

    @Test
    void cancel_fromAccepted_rejected()
    {
        when(workOrderMapper.selectByIdAndTenant(1L, 1L))
                .thenReturn(sample(1L, WorkOrderStatus.ACCEPTED, 200L));
        assertThrows(ServiceException.class, () -> workOrderService.cancel(1L));
    }

    private static OpenbizWorkOrder sample(Long id, WorkOrderStatus status, Long assignee)
    {
        OpenbizWorkOrder wo = new OpenbizWorkOrder();
        wo.setId(id);
        wo.setTenantId(1L);
        wo.setTitle("t");
        wo.setContent("c");
        wo.setContactName("n");
        wo.setContactPhone("p");
        wo.setStatus(status.name());
        wo.setAssigneeUserId(assignee);
        wo.setIdempotentKey("k");
        return wo;
    }
}
