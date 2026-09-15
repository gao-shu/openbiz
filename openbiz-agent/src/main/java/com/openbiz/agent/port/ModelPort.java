package com.openbiz.agent.port;

import com.openbiz.agent.model.ModelRequest;
import com.openbiz.agent.model.ModelResponse;

/**
 * Vendor-neutral LLM call port.
 */
public interface ModelPort
{
    ModelResponse invoke(ModelRequest request);
}
