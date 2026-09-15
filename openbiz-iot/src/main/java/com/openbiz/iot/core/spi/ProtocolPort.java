package com.openbiz.iot.core.spi;

/**
 * Protocol adapter SPI. Implementations live in openbiz-iot-mqtt (and future adapters).
 * Industry templates must never depend on MQTT (or other) adapter modules.
 */
public interface ProtocolPort
{
    /**
     * Publish a downlink command to the physical / simulated device.
     *
     * @return true if accepted by the transport layer
     */
    boolean sendCommand(Long deviceId, String productKey, String deviceSn,
                        String serviceId, String paramsJson, Long commandId, String idempotentKey);

    /**
     * Protocol identifier, e.g. MQTT.
     */
    String protocol();
}
