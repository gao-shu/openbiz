package com.openbiz.iot.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.openbiz.iot.core.spi.ProtocolPort;

/**
 * MQTT ProtocolPort stub. Still no broker. Registered as protocol=MQTT.
 */
@Component
public class MqttProtocolAdapter implements ProtocolPort
{
    private static final Logger log = LoggerFactory.getLogger(MqttProtocolAdapter.class);

    @Override
    public boolean sendCommand(Long deviceId, String productKey, String deviceSn,
                               String serviceId, String paramsJson, Long commandId, String idempotentKey)
    {
        log.info("[MQTT stub] accept (no broker). deviceId={}, product={}, sn={}, service={}, commandId={}",
                deviceId, productKey, deviceSn, serviceId, commandId);
        return true;
    }

    @Override
    public String protocol()
    {
        return "MQTT";
    }
}
