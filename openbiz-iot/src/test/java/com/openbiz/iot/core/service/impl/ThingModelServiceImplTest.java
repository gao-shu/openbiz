package com.openbiz.iot.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.openbiz.iot.core.domain.OpenbizProduct;
import com.openbiz.iot.core.domain.OpenbizThingModel;
import com.openbiz.iot.core.mapper.OpenbizProductMapper;
import com.openbiz.iot.core.mapper.OpenbizThingModelMapper;
import com.openbiz.saas.context.TenantContext;
import com.ruoyi.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class ThingModelServiceImplTest
{
    @Mock
    private OpenbizThingModelMapper thingModelMapper;
    @Mock
    private OpenbizProductMapper productMapper;
    @InjectMocks
    private ThingModelServiceImpl thingModelService;

    @AfterEach
    void tearDown()
    {
        TenantContext.clear();
    }

    @Test
    void rejectsWhenNoTenant()
    {
        assertThrows(ServiceException.class, () -> thingModelService.getByProductId(1L));
    }

    @Test
    void getScopedToTenant()
    {
        TenantContext.setTenantId(1L);
        OpenbizThingModel model = new OpenbizThingModel();
        model.setId(1L);
        when(thingModelMapper.selectByProductAndTenant(1L, 1L)).thenReturn(model);
        assertEquals(1L, thingModelService.getByProductId(1L).getId());
        when(thingModelMapper.selectByProductAndTenant(2L, 1L)).thenReturn(null);
        assertNull(thingModelService.getByProductId(2L));
    }

    @Test
    void createRequiresProductInTenant()
    {
        TenantContext.setTenantId(1L);
        OpenbizThingModel model = new OpenbizThingModel();
        model.setProductId(99L);
        model.setModelJson("{}");
        when(productMapper.selectByIdAndTenant(99L, 1L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> thingModelService.create(model));
    }

    @Test
    void createOk()
    {
        TenantContext.setTenantId(1L);
        OpenbizProduct product = new OpenbizProduct();
        product.setId(1L);
        when(productMapper.selectByIdAndTenant(1L, 1L)).thenReturn(product);
        when(thingModelMapper.selectByProductAndTenant(1L, 1L)).thenReturn(null);
        when(thingModelMapper.insert(any())).thenAnswer(inv -> {
            OpenbizThingModel m = inv.getArgument(0);
            m.setId(5L);
            return 1;
        });
        OpenbizThingModel model = new OpenbizThingModel();
        model.setProductId(1L);
        model.setModelJson("{\"services\":[]}");
        assertEquals(5L, thingModelService.create(model));
        verify(thingModelMapper).insert(any());
    }
}
