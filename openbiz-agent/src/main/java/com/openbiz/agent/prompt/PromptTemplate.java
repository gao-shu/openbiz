package com.openbiz.agent.prompt;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openbiz.agent.exception.PromptRenderException;
import com.openbiz.agent.model.ModelMessage;

/**
 * Minimal system/user prompt with {{variable}} substitution.
 * Missing variables fail before any model call.
 */
public final class PromptTemplate
{
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*\\}\\}");

    private final String name;
    private final String system;
    private final String user;

    public PromptTemplate(String name, String system, String user)
    {
        this.name = Objects.requireNonNull(name, "name");
        this.system = system == null ? "" : system;
        this.user = Objects.requireNonNull(user, "user");
    }

    public String getName()
    {
        return name;
    }

    public String getSystem()
    {
        return system;
    }

    public String getUser()
    {
        return user;
    }

    /**
     * Render to chat messages. Fails if any {{var}} is absent or null.
     */
    public List<ModelMessage> render(Map<String, String> variables)
    {
        Map<String, String> vars = variables == null ? Map.of() : variables;
        Set<String> required = new LinkedHashSet<>();
        required.addAll(findPlaceholders(system));
        required.addAll(findPlaceholders(user));
        List<String> missing = new ArrayList<>();
        for (String key : required)
        {
            if (!vars.containsKey(key) || vars.get(key) == null)
            {
                missing.add(key);
            }
        }
        if (!missing.isEmpty())
        {
            throw new PromptRenderException("PROMPT_VAR_MISSING: " + String.join(",", missing));
        }
        List<ModelMessage> messages = new ArrayList<>(2);
        String systemRendered = apply(system, vars);
        if (!systemRendered.isBlank())
        {
            messages.add(new ModelMessage("system", systemRendered));
        }
        messages.add(new ModelMessage("user", apply(user, vars)));
        return messages;
    }

    private static List<String> findPlaceholders(String text)
    {
        List<String> keys = new ArrayList<>();
        Matcher matcher = PLACEHOLDER.matcher(text == null ? "" : text);
        while (matcher.find())
        {
            keys.add(matcher.group(1));
        }
        return keys;
    }

    private static String apply(String text, Map<String, String> vars)
    {
        Matcher matcher = PLACEHOLDER.matcher(text == null ? "" : text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find())
        {
            String key = matcher.group(1);
            String value = vars.get(key);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
