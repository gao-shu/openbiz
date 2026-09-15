package com.openbiz.agent.model;

/**
 * Minimal model invoke response.
 */
public final class ModelResponse
{
    private final String content;
    private final String model;
    private final ModelUsage usage;

    public ModelResponse(String content, String model, ModelUsage usage)
    {
        this.content = content;
        this.model = model;
        this.usage = usage;
    }

    public String getContent()
    {
        return content;
    }

    public String getModel()
    {
        return model;
    }

    public ModelUsage getUsage()
    {
        return usage;
    }
}
