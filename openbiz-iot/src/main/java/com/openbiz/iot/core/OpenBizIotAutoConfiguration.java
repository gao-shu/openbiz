package com.openbiz.iot.core;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.openbiz.iot.core.config.OpenBizIotMybatisConfig;

@Configuration
@ComponentScan("com.openbiz.iot.core")
@Import(OpenBizIotMybatisConfig.class)
public class OpenBizIotAutoConfiguration
{
}
