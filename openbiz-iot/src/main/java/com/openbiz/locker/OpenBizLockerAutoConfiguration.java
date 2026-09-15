package com.openbiz.locker;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.openbiz.locker.config.OpenBizLockerMybatisConfig;

@Configuration
@ComponentScan("com.openbiz.locker")
@Import(OpenBizLockerMybatisConfig.class)
public class OpenBizLockerAutoConfiguration
{
}
