package com.openbiz.service.service.impl;

import java.util.Date;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.openbiz.service.api.WorkOrderService;
import com.openbiz.service.domain.OpenbizWorkOrder;
import com.openbiz.service.domain.WorkOrderStatus;
import com.openbiz.service.domain.WorkOrderTransitions;
import com.openbiz.service.mapper.OpenbizWorkOrderMapper;
import com.openbiz.service.port.CurrentUserPort;
import com.openbiz.service.port.StaffUserPort;
import com.openbiz.service.support.ServiceTenantGuard;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;

@Service
public class WorkOrderServiceImpl implements WorkOrderService
{
    private final OpenbizWorkOrderMapper workOrderMapper;
    private final StaffUserPort staffUserPort;
    private final CurrentUserPort currentUserPort;

    public WorkOrderServiceImpl(OpenbizWorkOrderMapper workOrderMapper,
                                StaffUserPort staffUserPort,
                                CurrentUserPort currentUserPort)
    {
        this.workOrderMapper = workOrderMapper;
        this.staffUserPort = staffUserPort;
        this.currentUserPort = currentUserPort;
    }

    @Override
    @Transactional
    public OpenbizWorkOrder create(String title, String content, String contactName, String contactPhone,
                                   String idempotentKey)
    {
        Long tenantId = ServiceTenantGuard.requireTenantId();
        currentUserPort.requireUserId();
        requireText(title, "title");
        requireText(contactName, "contactName");
        requireText(contactPhone, "contactPhone");
        requireText(idempotentKey, "idempotentKey");

        OpenbizWorkOrder existing = workOrderMapper.selectByIdempotent(tenantId, idempotentKey);
        if (existing != null)
        {
            return existing;
        }

        Date now = new Date();
        OpenbizWorkOrder row = new OpenbizWorkOrder();
        row.setTenantId(tenantId);
        row.setTitle(title.trim());
        row.setContent(content == null ? null : content.trim());
        row.setContactName(contactName.trim());
        row.setContactPhone(contactPhone.trim());
        row.setStatus(WorkOrderStatus.CREATED.name());
        row.setAssigneeUserId(null);
        row.setCompleteNote(null);
        row.setIdempotentKey(idempotentKey.trim());
        row.setCreateTime(now);
        row.setUpdateTime(now);
        try
        {
            workOrderMapper.insert(row);
        }
        catch (DuplicateKeyException ex)
        {
            OpenbizWorkOrder replay = workOrderMapper.selectByIdempotent(tenantId, idempotentKey);
            if (replay != null)
            {
                return replay;
            }
            throw ex;
        }
        return requireOwned(row.getId(), tenantId);
    }

    @Override
    @Transactional
    public OpenbizWorkOrder assign(Long workOrderId, Long assigneeUserId)
    {
        Long tenantId = ServiceTenantGuard.requireTenantId();
        currentUserPort.requireUserId();
        staffUserPort.requireActiveUser(assigneeUserId);
        OpenbizWorkOrder wo = requireOwned(workOrderId, tenantId);
        WorkOrderTransitions.requireTransition(wo.statusEnum(), WorkOrderStatus.ASSIGNED);
        int updated = workOrderMapper.updateAssign(
                workOrderId, tenantId, WorkOrderStatus.CREATED.name(), assigneeUserId, WorkOrderStatus.ASSIGNED.name());
        if (updated != 1)
        {
            throw new ServiceException("ILLEGAL_TRANSITION or concurrent update on assign", HttpStatus.BAD_REQUEST);
        }
        return requireOwned(workOrderId, tenantId);
    }

    @Override
    @Transactional
    public OpenbizWorkOrder accept(Long workOrderId)
    {
        Long tenantId = ServiceTenantGuard.requireTenantId();
        Long userId = currentUserPort.requireUserId();
        OpenbizWorkOrder wo = requireOwned(workOrderId, tenantId);
        WorkOrderTransitions.requireTransition(wo.statusEnum(), WorkOrderStatus.ACCEPTED);
        requireAssignee(wo, userId);
        int updated = workOrderMapper.updateStatus(
                workOrderId, tenantId, WorkOrderStatus.ASSIGNED.name(), WorkOrderStatus.ACCEPTED.name(), wo.getCompleteNote());
        if (updated != 1)
        {
            throw new ServiceException("ILLEGAL_TRANSITION or concurrent update on accept", HttpStatus.BAD_REQUEST);
        }
        return requireOwned(workOrderId, tenantId);
    }

    @Override
    @Transactional
    public OpenbizWorkOrder complete(Long workOrderId, String completeNote)
    {
        Long tenantId = ServiceTenantGuard.requireTenantId();
        Long userId = currentUserPort.requireUserId();
        OpenbizWorkOrder wo = requireOwned(workOrderId, tenantId);
        WorkOrderTransitions.requireTransition(wo.statusEnum(), WorkOrderStatus.COMPLETED);
        requireAssignee(wo, userId);
        String note = completeNote == null ? "" : completeNote.trim();
        int updated = workOrderMapper.updateStatus(
                workOrderId, tenantId, WorkOrderStatus.ACCEPTED.name(), WorkOrderStatus.COMPLETED.name(), note);
        if (updated != 1)
        {
            throw new ServiceException("ILLEGAL_TRANSITION or concurrent update on complete", HttpStatus.BAD_REQUEST);
        }
        return requireOwned(workOrderId, tenantId);
    }

    @Override
    @Transactional
    public OpenbizWorkOrder cancel(Long workOrderId)
    {
        Long tenantId = ServiceTenantGuard.requireTenantId();
        currentUserPort.requireUserId();
        OpenbizWorkOrder wo = requireOwned(workOrderId, tenantId);
        WorkOrderTransitions.requireTransition(wo.statusEnum(), WorkOrderStatus.CANCELLED);
        int updated = workOrderMapper.updateStatus(
                workOrderId, tenantId, wo.getStatus(), WorkOrderStatus.CANCELLED.name(), wo.getCompleteNote());
        if (updated != 1)
        {
            throw new ServiceException("ILLEGAL_TRANSITION or concurrent update on cancel", HttpStatus.BAD_REQUEST);
        }
        return requireOwned(workOrderId, tenantId);
    }

    @Override
    public OpenbizWorkOrder get(Long workOrderId)
    {
        Long tenantId = ServiceTenantGuard.requireTenantId();
        currentUserPort.requireUserId();
        return requireOwned(workOrderId, tenantId);
    }

    private OpenbizWorkOrder requireOwned(Long workOrderId, Long tenantId)
    {
        if (workOrderId == null)
        {
            throw new ServiceException("workOrderId is required", HttpStatus.BAD_REQUEST);
        }
        OpenbizWorkOrder wo = workOrderMapper.selectByIdAndTenant(workOrderId, tenantId);
        if (wo == null)
        {
            throw new ServiceException("work order not found in current tenant", HttpStatus.NOT_FOUND);
        }
        return wo;
    }

    private static void requireAssignee(OpenbizWorkOrder wo, Long userId)
    {
        if (wo.getAssigneeUserId() == null || !wo.getAssigneeUserId().equals(userId))
        {
            throw new ServiceException("ONLY_ASSIGNEE_ALLOWED", HttpStatus.FORBIDDEN);
        }
    }

    private static void requireText(String value, String field)
    {
        if (!StringUtils.hasText(value))
        {
            throw new ServiceException(field + " is required", HttpStatus.BAD_REQUEST);
        }
    }
}
