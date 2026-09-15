package com.openbiz.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * openbiz.agent.model.* - secrets via env, never hardcode keys.
 */
@ConfigurationProperties(prefix = "openbiz.agent.model")
public class AgentModelProperties
{
    /** OpenAI-compatible base, e.g. https://api.example.com/v1 */
    private String baseUrl = "";
    private String apiKey = "";
    private String model = "";
    private int timeoutSec = 60;
    private Double temperature;

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public int getTimeoutSec()
    {
        return timeoutSec;
    }

    public void setTimeoutSec(int timeoutSec)
    {
        this.timeoutSec = timeoutSec;
    }

    public Double getTemperature()
    {
        return temperature;
    }

    public void setTemperature(Double temperature)
    {
        this.temperature = temperature;
    }
}
