package com.openbiz.iot.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.openbiz.iot.core.domain.OpenbizProduct;
import com.openbiz.iot.core.mapper.OpenbizProductMapper;
import com.openbiz.saas.context.TenantContext;
import com.ruoyi.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest
{
    @Mock
    private OpenbizProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @AfterEach
    void tearDown()
    {
        TenantContext.clear();
    }

    @Test
    void rejectsWhenNoTenant()
    {
        assertThrows(ServiceException.class, () -> productService.list());
    }

    @Test
    void listScopedToTenant()
    {
        TenantContext.setTenantId(1L);
        OpenbizProduct p = new OpenbizProduct();
        p.setProductCode("DEMO-DOOR");
        when(productMapper.selectByTenant(1L)).thenReturn(Collections.singletonList(p));
        List<OpenbizProduct> list = productService.list();
        assertEquals(1, list.size());
        verify(productMapper).selectByTenant(1L);
    }

    @Test
    void createSetsTenantId()
    {
        TenantContext.setTenantId(2L);
        when(productMapper.insert(any())).thenAnswer(inv -> {
            OpenbizProduct p = inv.getArgument(0);
            p.setId(20L);
            return 1;
        });
        OpenbizProduct product = new OpenbizProduct();
        product.setProductCode("DEMO-DOOR");
        product.setProductName("Door");
        Long id = productService.create(product);
        assertEquals(20L, id);
        ArgumentCaptor<OpenbizProduct> captor = ArgumentCaptor.forClass(OpenbizProduct.class);
        verify(productMapper).insert(captor.capture());
        assertEquals(2L, captor.getValue().getTenantId());
        assertEquals("MQTT", captor.getValue().getProtocol());
    }
}
