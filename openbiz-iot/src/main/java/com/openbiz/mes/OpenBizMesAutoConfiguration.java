package com.openbiz.mes;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.openbiz.mes.config.OpenBizMesMybatisConfig;

@Configuration
@ComponentScan("com.openbiz.mes")
@Import(OpenBizMesMybatisConfig.class)
public class OpenBizMesAutoConfiguration
{
}
