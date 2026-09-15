package com.openbiz.agent.invocation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.openbiz.agent.exception.AgentException;
import com.openbiz.agent.model.ModelMessage;
import com.openbiz.agent.model.ModelRequest;
import com.openbiz.agent.model.ModelResponse;
import com.openbiz.agent.port.ModelPort;
import com.openbiz.agent.prompt.PromptTemplate;

/**
 * Runs PromptTemplate -&gt; ModelPort once. Memory only.
 */
public final class AgentInvoker
{
    private final ModelPort modelPort;

    public AgentInvoker(ModelPort modelPort)
    {
        this.modelPort = Objects.requireNonNull(modelPort, "modelPort");
    }

    public AgentInvocation invoke(PromptTemplate template, Map<String, String> variables, String model)
    {
        Objects.requireNonNull(template, "template");
        String resolvedModel = Objects.requireNonNull(model, "model");
        AgentInvocation invocation = new AgentInvocation(template.getName(), resolvedModel, variables);
        try
        {
            List<ModelMessage> messages = template.render(variables);
            ModelRequest request = ModelRequest.of(resolvedModel, messages);
            invocation.setRequest(request);
            ModelResponse response = modelPort.invoke(request);
            invocation.markSuccess(response);
            return invocation;
        }
        catch (AgentException ex)
        {
            invocation.markFailed(ex.getMessage());
            return invocation;
        }
        catch (RuntimeException ex)
        {
            invocation.markFailed(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
            return invocation;
        }
    }
}
