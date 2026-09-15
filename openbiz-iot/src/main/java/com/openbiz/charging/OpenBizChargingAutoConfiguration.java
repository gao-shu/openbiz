package com.openbiz.charging;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.openbiz.charging.config.OpenBizChargingMybatisConfig;

@Configuration
@ComponentScan("com.openbiz.charging")
@Import(OpenBizChargingMybatisConfig.class)
public class OpenBizChargingAutoConfiguration
{
}
