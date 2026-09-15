package com.openbiz.iot.core.spi;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.ruoyi.common.exception.ServiceException;

/**
 * Minimal protocol router: product.protocol -&gt; ProtocolPort.
 */
@Component
public class ProtocolPortRegistry
{
    private final Map<String, ProtocolPort> ports = new HashMap<>();

    public ProtocolPortRegistry(List<ProtocolPort> protocolPorts)
    {
        for (ProtocolPort port : protocolPorts)
        {
            if (port == null || !StringUtils.hasText(port.protocol()))
            {
                continue;
            }
            String key = normalize(port.protocol());
            if (ports.containsKey(key))
            {
                throw new IllegalStateException("Duplicate ProtocolPort for protocol: " + key);
            }
            ports.put(key, port);
        }
    }

    public ProtocolPort require(String protocol)
    {
        if (!StringUtils.hasText(protocol))
        {
            throw new ServiceException("product protocol is required");
        }
        ProtocolPort port = ports.get(normalize(protocol));
        if (port == null)
        {
            throw new ServiceException("no ProtocolPort registered for protocol: " + protocol);
        }
        return port;
    }

    private static String normalize(String protocol)
    {
        return protocol.trim().toUpperCase(Locale.ROOT);
    }
}
