package com.openbiz.iot.core.api;

import com.openbiz.iot.core.domain.OpenbizDeviceCommand;

/**
 * Downlink command API used by industry templates.
 * Business must not call MQTT directly.
 */
public interface DeviceCommandService
{
    /**
     * Invoke a thing-model service on a device.
     *
     * @return primary key of openbiz_device_command
     */
    Long invoke(Long deviceId, String serviceId, String paramsJson, String idempotentKey);

    OpenbizDeviceCommand get(Long id);
}
