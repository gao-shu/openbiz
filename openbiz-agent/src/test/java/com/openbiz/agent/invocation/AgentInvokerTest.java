package com.openbiz.agent.invocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.openbiz.agent.exception.ModelInvokeException;
import com.openbiz.agent.model.ModelResponse;
import com.openbiz.agent.port.ModelPort;
import com.openbiz.agent.prompt.PromptTemplate;

class AgentInvokerTest
{
    @Test
    void invoke_success()
    {
        ModelPort port = request -> {
            assertEquals(2, request.getMessages().size());
            assertTrue(request.getMessages().get(1).getContent().contains("optimistic lock"));
            return new ModelResponse("version field CAS", "mock-model", null);
        };
        AgentInvoker invoker = new AgentInvoker(port);
        PromptTemplate template = new PromptTemplate(
                "java-help",
                "You are a Java assistant.",
                "Please explain in one sentence: {{question}}");
        AgentInvocation invocation = invoker.invoke(template, Map.of("question", "what is Java optimistic lock?"), "mock-model");
        assertEquals(InvocationStatus.SUCCESS, invocation.getStatus());
        assertNotNull(invocation.getRequest());
        assertEquals("version field CAS", invocation.getResponse().getContent());
        assertEquals("mock-model", invocation.getResponse().getModel());
    }

    @Test
    void invoke_modelFailure_marksFailed()
    {
        ModelPort port = request -> {
            throw new ModelInvokeException("MODEL_HTTP_ERROR: status=500");
        };
        AgentInvoker invoker = new AgentInvoker(port);
        PromptTemplate template = new PromptTemplate("t", "", "{{question}}");
        AgentInvocation invocation = invoker.invoke(template, Map.of("question", "x"), "m");
        assertEquals(InvocationStatus.FAILED, invocation.getStatus());
        assertTrue(invocation.getError().contains("MODEL_HTTP_ERROR"));
    }

    @Test
    void invoke_missingPromptVar_failsWithoutModel()
    {
        ModelPort port = request -> {
            throw new IllegalStateException("model should not be called");
        };
        AgentInvoker invoker = new AgentInvoker(port);
        PromptTemplate template = new PromptTemplate("t", "", "{{question}}");
        AgentInvocation invocation = invoker.invoke(template, Map.of(), "m");
        assertEquals(InvocationStatus.FAILED, invocation.getStatus());
        assertTrue(invocation.getError().contains("PROMPT_VAR_MISSING"));
    }
}
