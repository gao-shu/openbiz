package com.openbiz.access.api;

/**
 * Minimal smart-access business API.
 */
public interface AccessService
{
    /**
     * Open door for the current login user against a device in current tenant.
     *
     * @return result with commandId / deviceId / success
     */
    AccessOpenResult openDoor(Long deviceId);
}
