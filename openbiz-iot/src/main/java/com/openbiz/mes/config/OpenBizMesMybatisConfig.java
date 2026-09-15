package com.openbiz.mes.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.openbiz.mes.mapper")
public class OpenBizMesMybatisConfig
{
}
