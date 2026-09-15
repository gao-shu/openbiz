package com.openbiz.access.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.openbiz.access.mapper")
public class OpenBizAccessMybatisConfig
{
}
