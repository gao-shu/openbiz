package com.openbiz.iot.core.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.openbiz.iot.core.spi.ProtocolPort;

/**
 * In-memory mock door protocol. No network. Phase 1.4.
 */
@Component
public class MockDoorProtocolPort implements ProtocolPort
{
    private static final Logger log = LoggerFactory.getLogger(MockDoorProtocolPort.class);

    private final MockDoorDeviceStore store;

    public MockDoorProtocolPort(MockDoorDeviceStore store)
    {
        this.store = store;
    }

    @Override
    public boolean sendCommand(Long deviceId, String productKey, String deviceSn,
                               String serviceId, String paramsJson, Long commandId, String idempotentKey)
    {
        if (deviceId == null)
        {
            return false;
        }
        MockDoorDevice device = store.getOrCreate(deviceId);
        boolean ok = device.apply(serviceId);
        log.info("[Phase1.4 mock-door] deviceId={}, sn={}, service={}, ok={}, state={}",
                deviceId, deviceSn, serviceId, ok, device.getState());
        return ok;
    }

    @Override
    public String protocol()
    {
        return "MOCK";
    }
}
