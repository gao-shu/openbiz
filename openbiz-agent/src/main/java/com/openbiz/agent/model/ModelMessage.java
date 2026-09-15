package com.openbiz.agent.model;

/**
 * One chat message.
 */
public final class ModelMessage
{
    private final String role;
    private final String content;

    public ModelMessage(String role, String content)
    {
        this.role = role;
        this.content = content;
    }

    public String getRole()
    {
        return role;
    }

    public String getContent()
    {
        return content;
    }
}
