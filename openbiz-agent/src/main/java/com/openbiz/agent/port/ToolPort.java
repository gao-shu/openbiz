package com.openbiz.agent.port;

/**
 * Future tool boundary only. No implementations in Phase 1.
 */
public interface ToolPort
{
    /**
     * Execute a named tool. Phase 1 has no default implementation.
     */
    String execute(String toolName, String input);
}
