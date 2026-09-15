package com.openbiz.agent;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.openbiz.agent.adapter.OpenAiCompatibleModelAdapter;
import com.openbiz.agent.config.AgentModelProperties;
import com.openbiz.agent.invocation.AgentInvoker;
import com.openbiz.agent.port.ModelPort;
import com.openbiz.foundation.OpenBizFoundationAutoConfiguration;

/**
 * Agent Phase 1 wiring. ModelPort bean only when api-key is configured.
 */
@Configuration
@Import(OpenBizFoundationAutoConfiguration.class)
@EnableConfigurationProperties(AgentModelProperties.class)
public class OpenBizAgentAutoConfiguration
{
    @Bean
    @ConditionalOnProperty(prefix = "openbiz.agent.model", name = "api-key")
    public ModelPort openBizModelPort(AgentModelProperties properties)
    {
        return new OpenAiCompatibleModelAdapter(properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "openbiz.agent.model", name = "api-key")
    public AgentInvoker openBizAgentInvoker(ModelPort modelPort)
    {
        return new AgentInvoker(modelPort);
    }
}
