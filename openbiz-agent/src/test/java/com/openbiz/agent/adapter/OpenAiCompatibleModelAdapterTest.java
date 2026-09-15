package com.openbiz.agent.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import com.sun.net.httpserver.HttpServer;
import com.openbiz.agent.config.AgentModelProperties;
import com.openbiz.agent.exception.AgentConfigException;
import com.openbiz.agent.exception.ModelInvokeException;
import com.openbiz.agent.model.ModelMessage;
import com.openbiz.agent.model.ModelRequest;
import com.openbiz.agent.model.ModelResponse;

class OpenAiCompatibleModelAdapterTest
{
    private HttpServer server;

    @AfterEach
    void tearDown()
    {
        if (server != null)
        {
            server.stop(0);
            server = null;
        }
    }

    @Test
    void invoke_success() throws Exception
    {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            byte[] body = """
                    {"id":"x","model":"demo-model","choices":[{"message":{"role":"assistant","content":"optimistic lock uses version"}}],"usage":{"prompt_tokens":1,"completion_tokens":2,"total_tokens":3}}
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();

        AgentModelProperties props = props("http://127.0.0.1:" + server.getAddress().getPort() + "/v1", "test-key", "demo-model");
        OpenAiCompatibleModelAdapter adapter = new OpenAiCompatibleModelAdapter(props);
        ModelResponse response = adapter.invoke(ModelRequest.of("demo-model",
                List.of(new ModelMessage("user", "what is optimistic lock?"))));
        assertEquals("optimistic lock uses version", response.getContent());
        assertEquals("demo-model", response.getModel());
        assertEquals(3, response.getUsage().getTotalTokens());
    }

    @Test
    void invoke_httpFailure() throws Exception
    {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            byte[] body = "{\"error\":\"nope\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();

        OpenAiCompatibleModelAdapter adapter = new OpenAiCompatibleModelAdapter(
                props("http://127.0.0.1:" + server.getAddress().getPort() + "/v1", "test-key", "demo-model"));
        ModelInvokeException ex = assertThrows(ModelInvokeException.class,
                () -> adapter.invoke(ModelRequest.of("demo-model", List.of(new ModelMessage("user", "x")))));
        assertTrue(ex.getMessage().contains("MODEL_HTTP_ERROR"));
    }

    @Test
    void invoke_emptyContent() throws Exception
    {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            byte[] body = "{\"choices\":[{\"message\":{\"content\":\"\"}}]}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();

        OpenAiCompatibleModelAdapter adapter = new OpenAiCompatibleModelAdapter(
                props("http://127.0.0.1:" + server.getAddress().getPort() + "/v1", "test-key", "demo-model"));
        ModelInvokeException ex = assertThrows(ModelInvokeException.class,
                () -> adapter.invoke(ModelRequest.of("demo-model", List.of(new ModelMessage("user", "x")))));
        assertTrue(ex.getMessage().contains("MODEL_EMPTY_RESPONSE"));
    }

    @Test
    void invoke_jsonParseFailure() throws Exception
    {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            byte[] body = "not-json".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();

        OpenAiCompatibleModelAdapter adapter = new OpenAiCompatibleModelAdapter(
                props("http://127.0.0.1:" + server.getAddress().getPort() + "/v1", "test-key", "demo-model"));
        ModelInvokeException ex = assertThrows(ModelInvokeException.class,
                () -> adapter.invoke(ModelRequest.of("demo-model", List.of(new ModelMessage("user", "x")))));
        assertTrue(ex.getMessage().contains("MODEL_JSON_PARSE_ERROR"));
    }

    @Test
    void invoke_blankApiKey_failsBeforeHttp() throws Exception
    {
        AtomicInteger hits = new AtomicInteger();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            hits.incrementAndGet();
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();

        OpenAiCompatibleModelAdapter adapter = new OpenAiCompatibleModelAdapter(
                props("http://127.0.0.1:" + server.getAddress().getPort() + "/v1", "  ", "demo-model"));
        assertThrows(AgentConfigException.class,
                () -> adapter.invoke(ModelRequest.of("demo-model", List.of(new ModelMessage("user", "x")))));
        assertEquals(0, hits.get());
    }

    private static AgentModelProperties props(String baseUrl, String apiKey, String model)
    {
        AgentModelProperties properties = new AgentModelProperties();
        properties.setBaseUrl(baseUrl);
        properties.setApiKey(apiKey);
        properties.setModel(model);
        properties.setTimeoutSec(5);
        return properties;
    }
}
