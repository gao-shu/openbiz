package com.openbiz.agent.exception;

/**
 * Model HTTP / parse / empty-response failures.
 */
public class ModelInvokeException extends AgentException
{
    public ModelInvokeException(String message)
    {
        super(message);
    }

    public ModelInvokeException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
