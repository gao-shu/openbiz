package com.openbiz.agent.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.openbiz.agent.adapter.OpenAiCompatibleModelAdapter;
import com.openbiz.agent.config.AgentModelProperties;
import com.openbiz.agent.invocation.AgentInvocation;
import com.openbiz.agent.invocation.AgentInvoker;
import com.openbiz.agent.invocation.InvocationStatus;
import com.openbiz.agent.prompt.PromptTemplate;

/**
 * Real OpenAI-compatible call. Skips when credentials are absent - never fake PASS.
 */
class RealModelE2ETest
{
    @Test
    void realOptimisticLockQuestion()
    {
        String baseUrl = env("OPENBIZ_AGENT_BASE_URL");
        String apiKey = env("OPENBIZ_AGENT_API_KEY");
        String model = env("OPENBIZ_AGENT_MODEL");
        assumeTrue(notBlank(baseUrl) && notBlank(apiKey) && notBlank(model),
                "Real Model E2E: NOT RUN - set OPENBIZ_AGENT_BASE_URL / API_KEY / MODEL");

        AgentModelProperties properties = new AgentModelProperties();
        properties.setBaseUrl(baseUrl);
        properties.setApiKey(apiKey);
        properties.setModel(model);
        properties.setTimeoutSec(60);

        AgentInvoker invoker = new AgentInvoker(new OpenAiCompatibleModelAdapter(properties));
        PromptTemplate template = new PromptTemplate(
                "java-help",
                "You are a professional Java technical assistant. Answer in one short Chinese sentence.",
                "Please explain in one sentence: {{question}}");
        AgentInvocation invocation = invoker.invoke(
                template,
                Map.of("question", "What is Java optimistic locking?"),
                model);

        assertEquals(InvocationStatus.SUCCESS, invocation.getStatus(), invocation.getError());
        assertNotNull(invocation.getResponse());
        assertFalse(invocation.getResponse().getContent().isBlank());
        assertFalse(invocation.getResponse().getModel() == null || invocation.getResponse().getModel().isBlank());
    }

    private static String env(String key)
    {
        String value = System.getenv(key);
        return value == null ? "" : value.trim();
    }

    private static boolean notBlank(String value)
    {
        return value != null && !value.isBlank();
    }
}
