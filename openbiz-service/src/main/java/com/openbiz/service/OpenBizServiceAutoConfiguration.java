package com.openbiz.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.openbiz.service.config.OpenBizServiceMybatisConfig;

@Configuration
@ComponentScan("com.openbiz.service")
@Import(OpenBizServiceMybatisConfig.class)
public class OpenBizServiceAutoConfiguration
{
}
