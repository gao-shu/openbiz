# openbiz-agent

**Agent** domain �� Phase 1 minimal Core.

```text
PromptTemplate �� AgentInvoker �� ModelPort �� OpenAI-compatible Adapter �� LLM
```

## Status

**Phase 1 COMPLETE** (when tests pass). ToolPort is an empty boundary only.

## Config (env)

```text
OPENBIZ_AGENT_BASE_URL   # e.g. https://api.example.com/v1
OPENBIZ_AGENT_API_KEY
OPENBIZ_AGENT_MODEL
```

See `src/main/resources/application-agent-example.yml`. Never commit real keys.

## Out of scope

RAG, Memory, MCP, Workflow, multi-agent, Tool implementations, Web API, DB tables, manju media providers.
