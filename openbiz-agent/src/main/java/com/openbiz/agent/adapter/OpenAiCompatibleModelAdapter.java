package com.openbiz.agent.adapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openbiz.agent.config.AgentModelProperties;
import com.openbiz.agent.exception.AgentConfigException;
import com.openbiz.agent.exception.ModelInvokeException;
import com.openbiz.agent.model.ModelMessage;
import com.openbiz.agent.model.ModelRequest;
import com.openbiz.agent.model.ModelResponse;
import com.openbiz.agent.model.ModelUsage;
import com.openbiz.agent.port.ModelPort;

/**
 * OpenAI-compatible chat/completions adapter. Not an OpenAI SDK wrapper.
 */
public final class OpenAiCompatibleModelAdapter implements ModelPort
{
    private final String baseUrl;
    private final String apiKey;
    private final String defaultModel;
    private final Double defaultTemperature;
    private final Duration timeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleModelAdapter(AgentModelProperties properties)
    {
        this(properties, new ObjectMapper());
    }

    public OpenAiCompatibleModelAdapter(AgentModelProperties properties, ObjectMapper objectMapper)
    {
        Objects.requireNonNull(properties, "properties");
        this.baseUrl = trimSlash(properties.getBaseUrl());
        this.apiKey = properties.getApiKey() == null ? "" : properties.getApiKey().trim();
        this.defaultModel = properties.getModel() == null ? "" : properties.getModel().trim();
        this.defaultTemperature = properties.getTemperature();
        int timeoutSec = properties.getTimeoutSec() <= 0 ? 60 : properties.getTimeoutSec();
        this.timeout = Duration.ofSeconds(timeoutSec);
        this.httpClient = HttpClient.newBuilder().connectTimeout(this.timeout).build();
        this.objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;
    }

    @Override
    public ModelResponse invoke(ModelRequest request)
    {
        Objects.requireNonNull(request, "request");
        if (baseUrl == null || baseUrl.isBlank())
        {
            throw new AgentConfigException("AGENT_CONFIG_ERROR: openbiz.agent.model.base-url is blank");
        }
        if (apiKey.isBlank())
        {
            throw new AgentConfigException("AGENT_CONFIG_ERROR: openbiz.agent.model.api-key is blank");
        }
        String model = request.getModel() == null || request.getModel().isBlank() ? defaultModel : request.getModel();
        if (model == null || model.isBlank())
        {
            throw new AgentConfigException("AGENT_CONFIG_ERROR: model name is blank");
        }
        if (request.getMessages() == null || request.getMessages().isEmpty())
        {
            throw new ModelInvokeException("MODEL_INVOKE_ERROR: messages empty");
        }

        try
        {
            String body = objectMapper.writeValueAsString(buildBody(model, request));
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(chatCompletionsUrl()))
                    .timeout(timeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int status = httpResponse.statusCode();
            String responseBody = httpResponse.body() == null ? "" : httpResponse.body();
            if (status < 200 || status >= 300)
            {
                throw new ModelInvokeException(
                        "MODEL_HTTP_ERROR: status=" + status + " body=" + truncate(responseBody, 300));
            }
            return parseResponse(responseBody, model);
        }
        catch (AgentConfigException | ModelInvokeException ex)
        {
            throw ex;
        }
        catch (IOException ex)
        {
            throw new ModelInvokeException("MODEL_IO_ERROR: " + ex.getMessage(), ex);
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            throw new ModelInvokeException("MODEL_TIMEOUT_OR_INTERRUPT: " + ex.getMessage(), ex);
        }
        catch (Exception ex)
        {
            throw new ModelInvokeException("MODEL_INVOKE_ERROR: " + ex.getMessage(), ex);
        }
    }

    private Map<String, Object> buildBody(String model, ModelRequest request)
    {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        List<Map<String, String>> messages = new ArrayList<>();
        for (ModelMessage message : request.getMessages())
        {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("role", message.getRole());
            row.put("content", message.getContent());
            messages.add(row);
        }
        body.put("messages", messages);
        Double temperature = request.getTemperature() != null ? request.getTemperature() : defaultTemperature;
        if (temperature != null)
        {
            body.put("temperature", temperature);
        }
        return body;
    }

    private ModelResponse parseResponse(String responseBody, String fallbackModel)
    {
        final JsonNode root;
        try
        {
            root = objectMapper.readTree(responseBody);
        }
        catch (IOException ex)
        {
            throw new ModelInvokeException("MODEL_JSON_PARSE_ERROR: " + ex.getMessage(), ex);
        }
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (contentNode.isMissingNode() || contentNode.isNull())
        {
            throw new ModelInvokeException("MODEL_EMPTY_RESPONSE: choices[0].message.content missing");
        }
        String content = contentNode.asText();
        if (content == null || content.isBlank())
        {
            throw new ModelInvokeException("MODEL_EMPTY_RESPONSE: content blank");
        }
        String model = root.path("model").asText(null);
        if (model == null || model.isBlank())
        {
            model = fallbackModel;
        }
        ModelUsage usage = null;
        JsonNode usageNode = root.get("usage");
        if (usageNode != null && usageNode.isObject())
        {
            usage = new ModelUsage(
                    usageNode.path("prompt_tokens").isMissingNode() ? null : usageNode.path("prompt_tokens").asInt(),
                    usageNode.path("completion_tokens").isMissingNode() ? null : usageNode.path("completion_tokens").asInt(),
                    usageNode.path("total_tokens").isMissingNode() ? null : usageNode.path("total_tokens").asInt());
        }
        return new ModelResponse(content, model, usage);
    }

    private String chatCompletionsUrl()
    {
        if (baseUrl.endsWith("/chat/completions"))
        {
            return baseUrl;
        }
        return baseUrl + "/chat/completions";
    }

    private static String trimSlash(String url)
    {
        if (url == null)
        {
            return "";
        }
        String trimmed = url.trim();
        while (trimmed.endsWith("/"))
        {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String truncate(String text, int max)
    {
        if (text == null)
        {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max);
    }
}
