package com.openbiz.iot.core.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.openbiz.iot.core.mapper")
public class OpenBizIotMybatisConfig
{
}
