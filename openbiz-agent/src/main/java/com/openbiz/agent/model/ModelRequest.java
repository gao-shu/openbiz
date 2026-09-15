package com.openbiz.agent.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Minimal model invoke request.
 */
public final class ModelRequest
{
    private final String model;
    private final List<ModelMessage> messages;
    private final Double temperature;

    public ModelRequest(String model, List<ModelMessage> messages, Double temperature)
    {
        this.model = model;
        this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
        this.temperature = temperature;
    }

    public static ModelRequest of(String model, List<ModelMessage> messages)
    {
        return new ModelRequest(model, messages, null);
    }

    public String getModel()
    {
        return model;
    }

    public List<ModelMessage> getMessages()
    {
        return messages;
    }

    public Double getTemperature()
    {
        return temperature;
    }
}
