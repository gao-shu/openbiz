# OpenBiz

一个面向企业业务开发的通用应用母平台（Monorepo + 五大业务母域 MVP）。

```text
OPENBIZ MVP = FINAL
ARCHITECTURE = SEALED
FIVE MOTHER DOMAINS = FROZEN
CODE DEVELOPMENT = STOP
```

```text
OpenBiz Maven modules (7)
│
├── openbiz-foundation   极薄技术基础（无业务）
├── openbiz-saas-core    Tenant / Member（真实共享）
├── openbiz-iot          IoT 母域（core/mqtt/access/locker/charging/mes）
├── openbiz-business     Business 母域（package 仍为 com.openbiz.biz.*）
├── openbiz-service      Service 母域（WorkOrder）
├── openbiz-agent        Agent 母域（Model + Prompt + Invocation）
└── openbiz-shop         Shop 母域（Inventory CAS）
```

## Modules（当前真实能力）

| Maven module | Implemented scope | Status |
|--------------|-------------------|--------|
| **openbiz-iot** | Device / Product / ThingModel / DeviceCommand / Protocol / Access / Locker / Charging / MES | **v1.0 FROZEN** |
| **openbiz-business** | Customer / Account / Ledger / Item / Order（账户扣款交易；Java package `com.openbiz.biz.*`） | **Phase 1 COMPLETE / FROZEN** |
| **openbiz-service** | WorkOrder（assign / accept / complete / cancel + 状态机） | **Phase 1 COMPLETE / FROZEN** |
| **openbiz-agent** | PromptTemplate / AgentInvocation / ModelPort / OpenAI-compatible adapter | **Phase 1 COMPLETE / FROZEN** |
| **openbiz-shop** | Product / Inventory CAS / ShopOrder | **Phase 1 COMPLETE / FROZEN** |
| **openbiz-foundation** | 跨模块薄基础（无业务对象） | Skeleton **FROZEN** |
| **openbiz-saas-core** | TenantContext / Tenant / Member | **LIVE**（独立于 Foundation） |

### Explicitly not implemented（Deferred / Future — 不算当前能力）

| Area | Deferred |
|------|----------|
| Service | CRM、完整售后、SLA、附件体系 |
| Agent | RAG、Memory、MCP、Workflow、Multi-Agent、Function Calling、完整 Agent OS |
| Shop | SKU、购物车、支付、退款、优惠券、物流、售后、营销 |

## Agent（诚实说明）

已实现：`PromptTemplate → AgentInvocation → ModelPort → OpenAiCompatibleModelAdapter`（JDK HttpClient，OpenAI-compatible `/chat/completions`）。`ToolPort` 仅为最小占位接口。

提供 OpenAI-compatible 模型适配能力；**Real Model E2E 为可选测试**，需配置环境变量后执行：

- `OPENBIZ_AGENT_BASE_URL`
- `OPENBIZ_AGENT_API_KEY`
- `OPENBIZ_AGENT_MODEL`

未配置时 `RealModelE2ETest` 会 **skip**（诚实跳过，非假绿）。**不得理解为“已完成真实大模型线上调用验收”。**

## 文档入口

| 文档 | 说明 |
|------|------|
| [docs/openbiz-final-audit.md](docs/openbiz-final-audit.md) | **最终封库审计** |
| [docs/architecture-sealed.md](docs/architecture-sealed.md) | **架构封存状态** |
| [docs/openbiz-architecture.md](docs/openbiz-architecture.md) | 五大模块总架构 |
| [docs/module-boundary.md](docs/module-boundary.md) | 模块边界与依赖规则 |
| [docs/business-phase1-report.md](docs/business-phase1-report.md) | Business Phase 1 |
| [docs/service-phase1-report.md](docs/service-phase1-report.md) | Service Phase 1 |
| [docs/agent-phase1-report.md](docs/agent-phase1-report.md) | Agent Phase 1 |
| [docs/shop-phase1-report.md](docs/shop-phase1-report.md) | Shop Phase 1 |
| [docs/coding-rules.md](docs/coding-rules.md) | 编码硬约束 |

## 已确认决策

| 项 | 决策 |
|----|------|
| L0 基座 | 官方 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) **`springboot3` 分支**（不采用 Plus） |
| 架构风格 | 薄核心 + 领域/行业插件；单体模块化 Maven |
| 复用率 | **不预设百分比目标**；以实测为准 |
| 领域依赖 | 五大业务母域默认互不依赖；只依赖 Foundation / saas-core |
| Customer | Business Customer ≠ IoT Member；不抽超级 Customer Core |
| Demo 配置 | `ruoyi-admin` 中 DB/JWT 为 **本地 Demo 默认值**，生产必须替换（见配置文件注释） |

## 原则（摘要）

```text
Reuse Before Abstraction
Evidence Before Claims
No Big-Bang Refactor
No Future-Proofing Without Evidence
Core Modification Must Be Justified
Stop When Validation Is Complete
```
