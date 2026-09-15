package com.openbiz.service.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.openbiz.service.mapper")
public class OpenBizServiceMybatisConfig
{
}
