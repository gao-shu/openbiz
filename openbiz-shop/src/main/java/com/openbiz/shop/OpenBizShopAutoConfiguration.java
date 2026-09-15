package com.openbiz.shop;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.openbiz.shop.config.OpenBizShopMybatisConfig;

@Configuration
@ComponentScan("com.openbiz.shop")
@Import(OpenBizShopMybatisConfig.class)
public class OpenBizShopAutoConfiguration
{
}
