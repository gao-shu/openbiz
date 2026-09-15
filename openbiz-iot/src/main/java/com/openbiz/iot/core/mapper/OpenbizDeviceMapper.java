package com.openbiz.iot.core.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.openbiz.iot.core.domain.OpenbizDevice;

public interface OpenbizDeviceMapper
{
    int insert(OpenbizDevice device);

    OpenbizDevice selectByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    List<OpenbizDevice> selectByTenant(@Param("tenantId") Long tenantId);
}
