# OpenBiz

**Reusable Java Business Capabilities**

Java 21 · Spring Boot 3 · MyBatis · MySQL · Maven

OpenBiz provides reusable Java business capabilities that can be composed into real projects instead of rebuilding common business primitives from scratch.

Built with an AI-Coding-oriented engineering workflow ([docs/ai-coding](docs/ai-coding/README.md)) — a development approach, not an AI product or agent platform.

[Quick Start](docs/getting-started.md) · [Documentation](docs/README.md) · [Examples](docs/examples/workorder-demo.md) · [Evidence](docs/evidence.md)

---

## 30-second proof

OpenBiz capabilities are meant to **leave the mother repository** and run inside other projects.

```text
OpenBiz Service
        ↓
mvn install  (local ~/.m2)
        ↓
com.openbiz:openbiz-service:3.9.2
        ↓
openbiz-workorder-demo   ← independent GitHub repo
        ↓
Independent Spring Boot process  :18081
        ↓
Real HTTP + Real MySQL
        ↓
WorkOrder lifecycle + tenant isolation + create idempotency
```

Example: [gao-shu/openbiz-workorder-demo](https://github.com/gao-shu/openbiz-workorder-demo)

**The WorkOrder example is not OpenBiz itself.**

It demonstrates that an OpenBiz business capability can leave the mother repository and be consumed by an independent project via **Maven** (no source copy, no submodule).

> Local Maven Artifact **≠** Maven Central.

IoT Monitor Demo inside `ruoyi-admin` is an **in-repo** evidence path—not the same as this independent Maven consumer.

---

## Why OpenBiz?

Most business systems keep re-implementing the same cores: tenants, work orders, customers & balances, device commands, stock deduction, and (sometimes) a controlled model call.

OpenBiz packages those capabilities as Maven modules you can compose into a real app—not another admin template, and not an “AI builds the whole enterprise system” promise.

**OpenBiz is not:**

| Not this | Why |
| --- | --- |
| An AI coding product / agent platform | Workflow + constraints only; model invoke is optional and minimal |
| A RuoYi replacement | RuoYi remains a thin runtime/auth/configuration shell; OpenBiz is the business-capability layer |
| An admin UI template | Frontend is out of scope |
| A microservice scaffold | One monorepo of capabilities, not a deploy topology |
| A general “SDK platform” | Local Maven artifacts today; not Maven Central productization |
| A WorkOrder / IoT / shop product | Those are capabilities (and one Hero Example)—not the product identity |

---

## What can I reuse?

### Hero Example

| Capability | Module | You get | Consumer evidence |
| --- | --- | --- | --- |
| **Work Order** | `openbiz-service` | Create / assign / accept / complete / cancel + transitions + idempotency | **Independent** [openbiz-workorder-demo](https://github.com/gao-shu/openbiz-workorder-demo) (Maven + HTTP + MySQL) |

### Additional capabilities

| Capability | Module | You get | Consumer evidence |
| --- | --- | --- | --- |
| **Multi-tenant** | `openbiz-saas-core` | `TenantContext`, member→tenant resolve, interceptor | Used by other modules / consumers |
| **Customer / Account** | `openbiz-business` | Customer, balance account, ledger, paid order (CAS + idempotency) | Mother MySQL tests + in-repo probes |
| **IoT Device + Command** | `openbiz-iot` | Product / Device / Command → `ProtocolPort` (MockDoor today) | In-repo Demo; protocol = **mock** |
| **Inventory (optional)** | `openbiz-shop` | Product + inventory CAS + shop order | Mother MySQL tests |
| **Model invoke (optional)** | `openbiz-agent` | PromptTemplate → OpenAI-compatible `ModelPort` | Unit / optional real-model E2E |

Pick only what you need. You do **not** have to use every domain.

**Only WorkOrder currently has an independent Maven consumer**—other rows are capabilities with mother-repo evidence, not the same proof level.

---

## Quick Start

**Default path = WorkOrder Example (not the full admin app).**

When **JDK 21, Maven, MySQL, and Redis are already installed**, you can typically reach first API success in about **5–10 minutes**.

1. Install artifacts from this repo into your **local** Maven repository (`openbiz-service` + `ruoyi-framework` thin shell)
2. Clone & configure [openbiz-workorder-demo](https://github.com/gao-shu/openbiz-workorder-demo)  
3. Point it at MySQL (`ry-vue` by default) + Redis  
4. `mvn spring-boot:run` → port **18081**  
5. `POST /login` → `POST /api/work-orders` → `GET /api/work-orders/{id}`

Full steps, SQL filenames, and failure notes: **[docs/getting-started.md](docs/getting-started.md)**

> Optional (not default): run `ruoyi-admin` on **:18080** for in-repo probes. That is for maintainers—not the stranger’s first path.

---

## Evidence (summary)

| Area | Evidence level |
| --- | --- |
| WorkOrder + **independent** HTTP consumer | **L3** (MySQL + process `:18081`) |
| Business account CAS / idempotency | **L3** (mother MySQL tests + probes) |
| IoT device command (MockDoor) | **L3** MySQL / HTTP in-repo Demo; protocol = **mock** |
| Shop inventory CAS | **L3** mother MySQL tests |
| Agent model call | **L2** (+ optional real model E2E when API key set) |

**Read this carefully:**

| Claim | Reality |
| --- | --- |
| Local Maven Artifact | **≠** Maven Central |
| Independent Consumer | **≠** SDK productization |
| Real MySQL | **≠** production |
| Mock protocol / MQTT stub | **≠** real devices / real MQTT |

Details & honest limitations: **[docs/evidence.md](docs/evidence.md)**

---

## Architecture (short)

```text
RuoYi (auth / RBAC)     thin runtime / auth / configuration shell
        │
openbiz-saas-core       TenantContext
        │
 ┌──────┼──────────────┬─────────────┬──────────┐
iot  business  service  shop      agent
```

RuoYi is retained as a thin runtime/auth/configuration shell in the current repository setup; OpenBiz business capabilities are designed to be consumed independently.

All mother domains **stay in this monorepo** (`KEEP IN open-biz-platform`).  
Artifact consumption ≠ “every domain must be its own Git repository”.

Decision record: **[docs/architecture-decision-mother-boundary.md](docs/architecture-decision-mother-boundary.md)**

---

## Examples

| Example | What it proves |
| --- | --- |
| **[openbiz-workorder-demo](https://github.com/gao-shu/openbiz-workorder-demo)** | Independent Maven consumer of `openbiz-service` (Hero Example) |
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
| [AI Coding workflow](docs/ai-coding/README.md) | Engineering method for evolving this repo (secondary) |
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

## Versioning

The first public OpenBiz release is **`v0.1.0`**.

Current Maven capability artifacts, such as `com.openbiz:openbiz-service:3.9.2`, retain the existing reactor/upstream-aligned version for now. **`3.9.2` is an artifact/reactor alignment version, not an OpenBiz product release number** (and not "OpenBiz v3.9.2").

The independent example project `openbiz-workorder-demo` uses its own project version (`0.1.0-SNAPSHOT`) while depending on the Maven coordinate above.

`v0.1.0` does **not** imply Maven Central publication, production readiness, or SDK productization.

---

## License / About

OpenBiz domain modules live in this repository as reusable business capabilities.

The repository retains a RuoYi-based thin shell for auth/admin demos; see `LICENSE` and `NOTICE`.

- GitHub: https://github.com/gao-shu/openbiz  
- Example: https://github.com/gao-shu/openbiz-workorder-demo  
- Author site: https://gao-shu.github.io/my-vitepress-site  
