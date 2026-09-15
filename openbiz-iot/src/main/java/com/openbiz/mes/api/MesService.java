package com.openbiz.mes.api;

/**
 * Minimal MES business API for cross-industry reuse validation.
 */
public interface MesService
{
    /**
     * Start production for a work order in the current tenant.
     * Reuses DeviceCommandService with existing open_door (Phase 1.8 mock command for chain validation).
     */
    MesStartResult startProduction(Long workOrderId);
}
