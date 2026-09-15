package com.openbiz.charging.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.openbiz.charging.mapper")
public class OpenBizChargingMybatisConfig
{
}
