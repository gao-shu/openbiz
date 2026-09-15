package com.openbiz.locker.api;

/**
 * Minimal locker business API for cross-industry reuse validation.
 */
public interface LockerService
{
    /**
     * Open a locker slot for the current login user in the current tenant.
     * Reuses DeviceCommandService with existing open_door (no new Core service ids).
     */
    LockerOpenResult openLocker(Long lockerId, Long slotId);
}
