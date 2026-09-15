package com.openbiz.access;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.openbiz.access.config.OpenBizAccessMybatisConfig;

@Configuration
@ComponentScan("com.openbiz.access")
@Import(OpenBizAccessMybatisConfig.class)
public class OpenBizAccessAutoConfiguration
{
}
