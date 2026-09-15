package com.openbiz.saas.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.openbiz.saas.web.TenantInterceptor;

/**
 * Registers OpenBiz SaaS MVC pieces without modifying RuoYi ResourcesConfig heavily.
 */
@Configuration
@MapperScan("com.openbiz.saas.mapper")
public class OpenBizSaasWebConfig implements WebMvcConfigurer
{
    private final TenantInterceptor tenantInterceptor;

    public OpenBizSaasWebConfig(TenantInterceptor tenantInterceptor)
    {
        this.tenantInterceptor = tenantInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/register",
                        "/captchaImage",
                        "/logout",
                        "/error",
                        "/favicon.ico",
                        "/*.html",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/druid/**"
                );
    }
}
