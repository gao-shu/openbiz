# OpenBiz Agent �� Phase 0 Audit

> Date: 2026-09-14  
> Type: **read-only** (no Agent runtime, no Service/Shop, no architecture edits)  
> Evidence repo: `e:\work\project\cangku\ai-manju-platform`  
> OpenBiz module today: `openbiz-agent` skeleton only

```text
Phase 0 Status: COMPLETE
Code Modification: 0
Database Modification: 0
IoT / Business / Foundation Modification: 0
```

---

## 1. Goal of this phase

Answer three questions **before** writing Java:

1. What AI capability is **already proven** in the ���� platform?
2. What must **not** be copied into OpenBiz (industry-specific)?
3. What is the **minimum OpenBiz Agent Core** that can later host: �ͷ� / ֪ʶ�� / CRM Agent / MES Agent / ���� Agent?

Not in this phase: LangChain, vector DB, MCP, Redis Memory, multi-agent, tool-calling runtime, Business/IoT adapters.

---

## 2. What the ���� platform actually is

`ai-manju-platform` is a **TypeScript production pipeline**, not an enterprise Agent OS.

```text
�籾 / �ڲ� / ����Ƶ idea
    �� Compiler (LLM JSON steps)
    �� Prompt templates
    �� Media Providers (image / video / TTS)
    �� FFmpeg assemble
```

Stack (proven): Node + Hono/Vite + SQLite file store + OpenAI-compatible HTTP + Agnes/MiniMax media + TOS.

It is **evidence of AI application engineering**, not a library OpenBiz can Maven-depend on (different language, different product).

---

## 3. Capability inventory (manju �� Agent vocabulary)

| Agent-word | Exists in ����? | What it really is | Reuse into OpenBiz Core? |
| ----------- | ---------------- | ----------------- | ------------------------ |
| Model | **YES** | `LlmProvider.completeJson` �� `POST {base}/chat/completions` (`src/providers/llm.ts`) | **YES �� pattern** (Java port of the port, not the TS file) |
| Prompt | **YES** | Named templates, system vs user, `{{placeholders}}`, settings JSON (`prompt-templates.ts`) | **YES �� pattern** (generic store, **without** �־�/��ɫ fields) |
| Agent | **partial** | Folders named `short-video/agents/{idea,first-frame,video,audio}` | **NO as-is** �� these are **pipeline steps**, not a tool-using agent |
| Tool | **NO (LLM tools)** | Media `ImageProvider` / `VideoProvider` / `TtsProvider` | **NO in v1 Core** �� those are ���� industry tools |
| Workflow | **YES** | In-process worker + status machine (`pipeline/worker.ts`, factory, short-video pipeline) | **NO in v1 Core** �� too heavy; belongs to industry later |
| Knowledge / RAG | **NO** | Docs explicitly exclude RAG (`docs/12-v01a-factory-contract.md`) | **defer** |
| Memory | **NO** | Project JSON / SQLite job rows, not conversational memory | **defer** |
| MCP / LangChain | **NO** | Direct `fetch` to OpenAI-compatible APIs | **keep out** |
| Function/tool calling | **NO** | JSON-in / JSON-out prompts only | **defer** |
| Multi-model | **thin YES** | `LLM_MODEL` vs `LLM_MODEL_STRONG` via env | **YES �� two named models max in v1, still env/config** |
| Mock | **YES** | `providers/mock.ts` | **YES �� pattern** (test without keys) |
| Fallback | **YES** | schema repair, idea fallback (`strategies/fallback.ts`) | **YES �� pattern** (fail closed / fallback string, not a framework) |

### The ��Agent�� naming trap

`generateIdeaPlans` is: system prompt + user prompt + `completeJson` + Zod schema + fallback.

That is **Prompt + Model + Schema**, not:

```text
Agent �� Tool �� Business API
```

OpenBiz must not import this naming into Core as ��we already have Agents.��

---

## 4. What is industry (stay in ����; do not become Core)

```text
Recipe / �ڲ� / �־� Shot
Character / Scene / Prop lock
Agnes image, MiniMax i2v, FFmpeg, TOS
Storyboard.md export
Factory video-clip / remix
```

These prove **one Agent industry** later (`openbiz-agent-manju` or keep the TS app). They must **not** sit in `openbiz-agent` Core.

Same rule as IoT Product �� Shop Product.

---

## 5. What OpenBiz Agent Core should be (minimum)

Recommend **four nouns only** for Phase 1 (when authorized �� not now):

```text
openbiz-agent (Core)
������ ModelPort          // OpenAI-compatible chat; completeText / completeJson
������ PromptTemplate     // name + system + user + placeholders
������ AgentInvocation    // one run: template + model + input �� output + status
������ ToolPort (SPI)     // interface only; zero implementations
```

Explicitly **out** of first Core:

| Item | Why wait |
| ---- | -------- |
| RAG / vector / Knowledge base | ���� never proved it; no evidence |
| Memory / Redis | no need |
| LangChain / Spring AI mega-graph | ���� succeeded with `fetch` |
| MCP | Cursor/dev concern, not Core |
| Multi-agent planner | ���� ��agents�� are sequential pipeline |
| Tools that call IoT/Business | no real Agent product yet; keep SPI empty |
| Copying Node code into Java | reuse **contract**, rewrite in RuoYi/MyBatis style |

### Invocation shape (design only)

```text
tenant_id
template_code
model_code
input_json
output_json | error
status = SUCCEEDED | FAILED
idempotent_key?   // optional later; not required for Phase 0
```

One round trip. No loop. No tool calls. That is enough to demo ��Java Agent Core�� next to IoT/Business.

---

## 6. Reuse map (honest)

```text
Reuse as idea (OpenAI-compatible Model + Prompt templates + Mock + JSON schema)
    �� ai-manju-platform

Do not reuse as library
    �� different runtime (Node vs Java 21 / RuoYi)

Later industry plugin (not Core)
    �� ���� compiler + media providers + FFmpeg
```

GitHub story later:

> OpenBiz Agent Core = Model + Prompt + Invocation  
> ����ƽ̨ = first **industry** that already exists in TS; Java Core does not swallow it in Phase 1.

---

## 7. Resume / 10�C15 min talk track (after Phase 1 exists)

Do **not** claim this until Java probe is green:

1. Why Agent is a **domain**, not a ChatGPT wrapper  
2. ModelPort vs ���� media providers  
3. Prompt as data, not hardcoded strings  
4. Tool SPI empty on purpose (same as MQTT stub)  
5. Will not call `BizService` directly  

Until then, resume still uses **���� TS + OpenBiz IoT/Business**.

---

## 8. Recommended Phase 1 (wait for explicit order)

When asked to implement, **only**:

1. Tables or in-memory first? Prefer **one** `openbiz_agent_invocation` + optional `openbiz_agent_prompt` �� still no RAG.  
2. Probe: `POST /openbiz/test/agent/invoke` with login + tenant (mirror Business probe).  
3. Unit test with Mock ModelPort (no live key required).  
4. Optional live call behind env flag.  
5. **Stop.** No �ͷ�, no ֪ʶ��, no MES Agent, no wrapping ����.

Forbidden in Phase 1: Service/Shop, IoT command tools, LangChain starters.

---

## 9. Verdict

```text
Manju is a production AI pipeline (Model + Prompt + media Tools + workflow).
OpenBiz Agent Core should extract Model + Prompt + Invocation only.
RAG / Memory / Tool-calling / multi-agent: Evidence = 0 �� do not build yet.
Copy-paste ���� into Java: NO.
Move saas-core / Foundation: NO.
```

```text
STOP. Wait for Agent Phase 1 implementation order.
Do not implement Service / Shop.
Architecture remains SEALED.
```
