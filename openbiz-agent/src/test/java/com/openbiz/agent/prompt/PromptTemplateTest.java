package com.openbiz.agent.prompt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.openbiz.agent.exception.PromptRenderException;
import com.openbiz.agent.model.ModelMessage;

class PromptTemplateTest
{
    @Test
    void render_singleVariable()
    {
        PromptTemplate template = new PromptTemplate(
                "java-help",
                "You are a Java assistant.",
                "Explain: {{question}}");
        List<ModelMessage> messages = template.render(Map.of("question", "optimistic lock"));
        assertEquals(2, messages.size());
        assertEquals("system", messages.get(0).getRole());
        assertEquals("You are a Java assistant.", messages.get(0).getContent());
        assertEquals("user", messages.get(1).getRole());
        assertEquals("Explain: optimistic lock", messages.get(1).getContent());
    }

    @Test
    void render_multipleVariables()
    {
        PromptTemplate template = new PromptTemplate(
                "pair",
                "Lang={{lang}}",
                "Q={{question}} A={{answer}}");
        List<ModelMessage> messages = template.render(Map.of(
                "lang", "Java",
                "question", "lock",
                "answer", "version"));
        assertEquals("Lang=Java", messages.get(0).getContent());
        assertEquals("Q=lock A=version", messages.get(1).getContent());
    }

    @Test
    void render_missingVariable_fails()
    {
        PromptTemplate template = new PromptTemplate("t", "", "Hi {{name}}");
        PromptRenderException ex = assertThrows(PromptRenderException.class,
                () -> template.render(Map.of()));
        assertTrue(ex.getMessage().contains("PROMPT_VAR_MISSING"));
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    void render_repeatedVariable()
    {
        PromptTemplate template = new PromptTemplate("t", "", "{{x}} and {{x}}");
        List<ModelMessage> messages = template.render(Map.of("x", "ok"));
        assertEquals("ok and ok", messages.get(0).getContent());
    }

    @Test
    void render_nullVariableValue_fails()
    {
        PromptTemplate template = new PromptTemplate("t", "", "{{x}}");
        Map<String, String> vars = new java.util.HashMap<>();
        vars.put("x", null);
        assertThrows(PromptRenderException.class, () -> template.render(vars));
    }
}
