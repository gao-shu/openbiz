package com.openbiz.shop.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.openbiz.shop.mapper")
public class OpenBizShopMybatisConfig
{
}
