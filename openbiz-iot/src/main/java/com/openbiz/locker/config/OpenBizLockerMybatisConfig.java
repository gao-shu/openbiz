package com.openbiz.locker.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.openbiz.locker.mapper")
public class OpenBizLockerMybatisConfig
{
}
