package com.openbiz.saas;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.openbiz.saas.config.OpenBizSaasWebConfig;

/**
 * SaaS Core Spring entry.
 */
@Configuration
@ComponentScan("com.openbiz.saas")
@Import(OpenBizSaasWebConfig.class)
public class OpenBizSaasAutoConfiguration
{
}
