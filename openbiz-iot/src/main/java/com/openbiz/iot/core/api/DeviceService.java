package com.openbiz.iot.core.api;

import java.util.List;
import com.openbiz.iot.core.domain.OpenbizDevice;

public interface DeviceService
{
    Long create(OpenbizDevice device);

    OpenbizDevice get(Long id);

    List<OpenbizDevice> list();
}
