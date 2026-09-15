package com.openbiz.charging.api;

/**
 * Minimal charging business API for cross-industry reuse validation.
 */
public interface ChargingService
{
    /**
     * Start charging on a connector for the current login user in the current tenant.
     * Reuses DeviceCommandService with existing open_door (no new Core service ids).
     */
    ChargingStartResult startCharging(Long stationId, Long connectorId);
}
