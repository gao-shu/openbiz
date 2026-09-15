package com.openbiz.agent.invocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import com.openbiz.agent.model.ModelRequest;
import com.openbiz.agent.model.ModelResponse;

/**
 * One in-memory AI call. No DB persistence in Phase 1.
 */
public final class AgentInvocation
{
    private final String invocationId;
    private final String templateName;
    private final String model;
    private final Map<String, String> variables;
    private ModelRequest request;
    private ModelResponse response;
    private InvocationStatus status;
    private String error;

    public AgentInvocation(String templateName, String model, Map<String, String> variables)
    {
        this.invocationId = UUID.randomUUID().toString().replace("-", "");
        this.templateName = templateName;
        this.model = model;
        this.variables = variables == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(variables));
        this.status = null;
    }

    public String getInvocationId()
    {
        return invocationId;
    }

    public String getTemplateName()
    {
        return templateName;
    }

    public String getModel()
    {
        return model;
    }

    public Map<String, String> getVariables()
    {
        return variables;
    }

    public ModelRequest getRequest()
    {
        return request;
    }

    public void setRequest(ModelRequest request)
    {
        this.request = request;
    }

    public ModelResponse getResponse()
    {
        return response;
    }

    public void setResponse(ModelResponse response)
    {
        this.response = response;
    }

    public InvocationStatus getStatus()
    {
        return status;
    }

    public void markSuccess(ModelResponse response)
    {
        this.response = response;
        this.status = InvocationStatus.SUCCESS;
        this.error = null;
    }

    public void markFailed(String error)
    {
        this.status = InvocationStatus.FAILED;
        this.error = error;
    }

    public String getError()
    {
        return error;
    }
}
