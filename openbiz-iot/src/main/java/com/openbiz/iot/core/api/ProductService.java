package com.openbiz.iot.core.api;

import java.util.List;
import com.openbiz.iot.core.domain.OpenbizProduct;

public interface ProductService
{
    Long create(OpenbizProduct product);

    OpenbizProduct get(Long id);

    List<OpenbizProduct> list();
}
