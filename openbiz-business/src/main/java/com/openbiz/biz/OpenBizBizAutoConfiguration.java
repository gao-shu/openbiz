package com.openbiz.biz;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.openbiz.biz.config.OpenBizBizMybatisConfig;

@Configuration
@ComponentScan("com.openbiz.biz")
@Import(OpenBizBizMybatisConfig.class)
public class OpenBizBizAutoConfiguration
{
}
