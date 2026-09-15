package com.openbiz.biz.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.openbiz.biz.mapper")
public class OpenBizBizMybatisConfig
{
}
