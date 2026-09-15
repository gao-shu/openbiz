package com.openbiz.iot.core.api;

import com.openbiz.iot.core.domain.OpenbizThingModel;

public interface ThingModelService
{
    Long create(OpenbizThingModel model);

    OpenbizThingModel getByProductId(Long productId);
}
