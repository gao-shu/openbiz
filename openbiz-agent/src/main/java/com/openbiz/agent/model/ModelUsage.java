package com.openbiz.agent.model;

/**
 * Optional token usage from provider.
 */
public final class ModelUsage
{
    private final Integer promptTokens;
    private final Integer completionTokens;
    private final Integer totalTokens;

    public ModelUsage(Integer promptTokens, Integer completionTokens, Integer totalTokens)
    {
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
    }

    public Integer getPromptTokens()
    {
        return promptTokens;
    }

    public Integer getCompletionTokens()
    {
        return completionTokens;
    }

    public Integer getTotalTokens()
    {
        return totalTokens;
    }
}
