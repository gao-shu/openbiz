package com.openbiz.agent.exception;

/**
 * Prompt template render failures (e.g. missing variable).
 */
public class PromptRenderException extends AgentException
{
    public PromptRenderException(String message)
    {
        super(message);
    }
}
