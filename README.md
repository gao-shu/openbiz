# OpenBiz

**面向 AI Coding 的 Java 业务开发基础设施**  
**AI Coding × Reusable Java Business Capabilities**

Java 21 · Spring Boot 3 · MyBatis · MySQL · Maven

> OpenBiz 将**可复用的 Java 业务能力**与**工程化 AI Coding 流程**结合：  
> AI 负责高效实现，人负责需求、业务、架构、复用边界、风险与最终验收——  
> 让 AI 不只是生成代码，而是基于标准化能力与流程，更快交付**真实业务系统**。

**业务能力 × AI Coding × Real Consumer**

| Pillar | What you get |
| --- | --- |
| **业务能力** | SaaS 多租户 · 工单 · 客户与账务 · IoT 设备与命令 · 库存 · AI 模型调用（按需组合，不必全用） |
| **AI Coding** | 冻结流程 Understand → Plan → Implement → Verify → Close（见 [docs/ai-coding](docs/ai-coding/README.md)） |
| **Real Consumer** | 独立仓库用 Maven 消费能力，而不是复制源码——见下方 WorkOrder 证据 |

[Quick Start](docs/getting-started.md) · [Documentation](docs/README.md) · [Examples](docs/examples/workorder-demo.md) · [AI Coding](docs/ai-coding/README.md)

---

## Why OpenBiz?

Most business systems keep re-implementing the same cores: tenants, work orders, customers & balances, device commands, stock deduction, and (sometimes) a controlled model call. AI coding alone does not fix that—without reusable capabilities and engineering constraints, you still regenerate the same business code.

OpenBiz packages **proven** capabilities as Maven modules you can compose into a real app, plus a disciplined AI Coding workflow so generation stays inside reuse boundaries—not another admin template, and not an “AI builds the whole enterprise system” promise.

**OpenBiz is not:**

| Not this | Why |
| --- | --- |
| An AI coding product / agent platform | Workflow + constraints only; model invoke is optional and minimal |
| A RuoYi replacement | Optional auth/admin shell only—OpenBiz is the business-capability layer |
| An admin UI template | Frontend is out of scope |
| A microservice scaffold | One monorepo of capabilities, not a deploy topology |
| A general “SDK platform” | Local Maven artifacts today; not Maven Central productization |

---

## What can I reuse?

| Capability | Module | You get |
| --- | --- | --- |
| **Multi-tenant** | `openbiz-saas-core` | `TenantContext`, member→tenant resolve, interceptor |
| **Work Order** | `openbiz-service` | Create / assign / accept / complete / cancel + transitions + idempotency |
| **Customer / Account** | `openbiz-business` | Customer, balance account, ledger, paid order (CAS + idempotency) |
| **IoT Device + Command** | `openbiz-iot` | Product / Device / Command → `ProtocolPort` (MockDoor today) |
| **Inventory (optional)** | `openbiz-shop` | Product + inventory CAS + shop order |
| **Model invoke (optional)** | `openbiz-agent` | PromptTemplate → OpenAI-compatible `ModelPort` |

Pick only what you need. You do **not** have to use every domain.

---

## Real Consumer

The strongest proof that OpenBiz is **not a PPT project**—capabilities can leave the mother repo:

`openbiz-workorder-demo` is an **independent** GitHub consumer. It depends on `com.openbiz:openbiz-service` via **Maven** (no source copy, no submodule), runs its own process on **:18081**, and exercises WorkOrder against **real HTTP + real MySQL**.

```text
openbiz-service
        ↓
Maven Artifact  com.openbiz:openbiz-service:3.9.2  (local ~/.m2)
        ↓
openbiz-workorder-demo   ← independent GitHub repo
        ↓
Independent process  :18081
        ↓
Real HTTP + Real MySQL
        ↓
WorkOrder lifecycle (list / get / create / assign / accept / complete / cancel)
+ tenant isolation + create idempotency
```

Example repo: [gao-shu/openbiz-workorder-demo](https://github.com/gao-shu/openbiz-workorder-demo)

> IoT Monitor Demo inside `ruoyi-admin` is an **in-repo** evidence path—not the same as this independent Maven consumer.

**Read this carefully:**

| Claim | Reality |
| --- | --- |
| Local Maven Artifact | **≠** Maven Central |
| Independent Consumer | **≠** SDK productization |
| Real MySQL | **≠** production |
| Mock protocol / MQTT stub | **≠** real devices / real MQTT |

---

## Quick Start

**Default path = WorkOrder Example (not the full admin app).**

When **JDK 21, Maven, MySQL, and Redis are already installed**, you can typically reach first API success in about **5–10 minutes**.

1. Install the Service artifact from this repo into your **local** Maven repository  
2. Clone & configure [openbiz-workorder-demo](https://github.com/gao-shu/openbiz-workorder-demo)  
3. Point it at MySQL (`ry-vue` by default) + Redis  
4. `mvn spring-boot:run` → port **18081**  
5. `POST /login` → `POST /api/work-orders` → `GET /api/work-orders/{id}`

Full steps, SQL minimum set, and failure notes: **[docs/getting-started.md](docs/getting-started.md)**

> Optional (not default): run `ruoyi-admin` on **:18080** to explore in-repo probes. That is for maintainers—not the stranger’s first path.

---

## Evidence (summary)

| Area | Evidence level |
| --- | --- |
| WorkOrder + **independent** HTTP consumer | **L3** (MySQL + process `:18081`) |
| Business account CAS / idempotency | **L3** (mother MySQL tests + probes) |
| IoT device command (MockDoor) | **L3** MySQL / HTTP in-repo Demo; protocol = **mock** |
| Shop inventory CAS | **L3** mother MySQL tests |
| Agent model call | **L2** (+ optional real model E2E when API key set) |

Details & honest limitations: **[docs/evidence.md](docs/evidence.md)**

---

## Architecture (short)

```text
RuoYi (auth / RBAC)     optional thin shell for demos & admin
        │
openbiz-saas-core       TenantContext
        │
 ┌──────┼──────────────┬─────────────┬──────────┐
iot  business  service  shop      agent
```

All mother domains **stay in this monorepo** (`KEEP IN open-biz-platform`).  
Artifact consumption ≠ “every domain must be its own Git repository”.

Decision record: **[docs/architecture-decision-mother-boundary.md](docs/architecture-decision-mother-boundary.md)**

---

## Examples

| Example | What it proves |
| --- | --- |
| **[openbiz-workorder-demo](https://github.com/gao-shu/openbiz-workorder-demo)** | Independent Maven consumer of `openbiz-service` |
| IoT Monitor Demo (in `ruoyi-admin`) | In-repo reuse of `DeviceService` / `DeviceCommandService` (not a separate repo) |

Guide: **[docs/examples/workorder-demo.md](docs/examples/workorder-demo.md)**

---

## Documentation

Start here: **[docs/README.md](docs/README.md)**

| Doc | Purpose |
| --- | --- |
| [Getting Started](docs/getting-started.md) | Consumer-first runbook |
| [Evidence](docs/evidence.md) | L1–L4 matrix & red lines |
| [Mother-boundary ADR](docs/architecture-decision-mother-boundary.md) | No premature Git split |
| [AI Coding workflow](docs/ai-coding/README.md) | How this repo is evolved with AI (secondary) |
| [Archive index](docs/archive/README.md) | Historical phase/audit reports |

---

## Roadmap

| Version | Focus |
| --- | --- |
| **V0.1** | Open-source entry: README, Quick Start, Example, Evidence *(this phase)* |
| **V0.2** | More reuse proof (e.g. additional consumers)—only with clear need |
| **V1.0** | Community packaging: contributing guide, changelog, releases |

**Not on the roadmap:** physical domain split “for architecture beauty”, industry spam modules, MQTT-as-marketing, RAG/multi-agent platforms, or chasing Stars by adding features.

---

## License / About

Based on [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) (see `LICENSE`). OpenBiz domain modules live in this repository as reusable business capabilities.

- GitHub: https://github.com/gao-shu/openbiz  
- Example: https://github.com/gao-shu/openbiz-workorder-demo  
- Author site: https://gao-shu.github.io/my-vitepress-site  
