# OpenBiz AI Coding Guide

> 如何使用 AI Coding 开发 OpenBiz 生态中的真实业务项目。  
> 这不是 Cursor 通用教程，也不是「用 AI 写任意 Java」手册。

---

## 1. 定位

OpenBiz 采用 AI Coding（Cursor / Claude Code / 其他 Coding Agent）作为主要开发方式之一。

核心分工一句话：

> **AI 提高实现速度；人负责边界、架构与验证。**

OpenBiz 当前处于：

```text
Core Frozen / Demand-Driven Evolution
```

- **Core**：五大母域 MVP + Foundation + SaaS Core 已冻结  
- **演进**：真实 Demo / 真实项目需求驱动，按需最小修改  
- **禁止**：无证据地继续堆模块、堆抽象、堆「看起来高级」的基础设施

---

## 2. 为什么 OpenBiz 适合 AI Coding

OpenBiz 已经具备 AI 可消费的结构：

| 已有条件 | 对 AI Coding 的意义 |
|----------|---------------------|
| 清晰 Mother Domain | 需求先归域，减少乱写位置 |
| 模块职责明确 | Foundation ≠ SaaS ≠ 业务母域 |
| 可复用能力 + 测试 | 优先搜索与复用，而非从零生成 |
| REAL MySQL / 并发证据 | 关键能力有验证标准 |
| Evidence Before Claims | 禁止把「写了代码」说成「已完成」 |

因此 AI **不应该**从零生成整个系统，而应该：

```text
理解已有能力
    ↓
选择正确 Mother Domain
    ↓
复用已有能力
    ↓
最小化实现
    ↓
测试
    ↓
真实验证（必要时 REAL MySQL / 可选 Model E2E）
    ↓
STOP
```

---

## 3. AI 与人的职责

### AI 负责

- 阅读代码与文档
- 分析需求、拆解任务
- 搜索已有 Entity / Service / Mapper / Port / Adapter / Test
- 提出实现方案（含风险与复用选项）
- 在批准范围内生成代码与测试
- 分析测试失败、辅助修复
- 辅助 Review 与文档草稿

### 人负责

- 需求值不值得做
- Mother Domain 归属是否正确
- 架构与边界决策
- 是否抽象、是否新增能力、是否改 Core
- 风险判断（安全、并发、数据）
- 最终验证与验收
- **STOP** 决策

核心原则：

> **AI 可以写代码，但不能自行决定 OpenBiz 的边界。**

---

## 4. 接到需求时的强制判断顺序

AI 收到任何新需求，必须先按顺序回答（写进方案，不可跳过）：

```text
1. 这是什么业务问题？
2. 属于哪个 Mother Domain？（或 Demo 层 / RuoYi 基座）
3. 是否已经存在对应能力？
4. 能否直接复用？
5. 能否通过组合已有能力解决？
6. 如果不能，最小新增是什么？
7. 是否需要修改 OpenBiz Core？（默认：否）
8. 如何验证？（单测 / MySQL / 幂等 / 租户 / 并发……）
```

母域速查：

| Domain | 放什么 | 不放什么（示例） |
|--------|--------|------------------|
| Foundation | 极薄技术基础 | Customer / Order / Device |
| SaaS Core | Tenant / Member / TenantContext | 行业业务、账户扣款、库存 |
| IoT | 设备 / 命令 / 协议 / Access·Locker·Charging·MES | Business 客户账户、Shop 订单 |
| Business | Customer / Account / Ledger / Item / 账户交易 Order | Shop 库存订单、工单状态机 |
| Service | WorkOrder 状态流程 | CRM / 完整售后（Deferred） |
| Agent | Prompt / Invocation / ModelPort | RAG / MCP / Multi-Agent（Deferred） |
| Shop | Product / Inventory / ShopOrder | 支付 / 购物车（Deferred） |
| RuoYi | 登录 / 用户角色菜单 | OpenBiz 业务表逻辑 |

**Demo 优先消费 OpenBiz，而不是为了 Demo 改造 OpenBiz。**

---

## 5. Core Freeze

普通 Demo / 业务交付：

```text
Demo 或客户项目
        ↓
复用 OpenBiz（依赖模块、调用能力）
        ↓
Demo 层自己的代码（若需要）
```

而不是：

```text
Demo
 ↓
修改 OpenBiz Core
 ↓
新增大量基础能力 / 第六母域 / 提前抽象
```

只有同时满足时，才允许提议修改 Core：

1. 真实需求（不是想象）  
2. 已有能力确实不足（已搜索过）  
3. 问题可重复验证  
4. 最小修改方案明确  
5. **人批准**后再改  

---

## 6. 工程原则（必须遵守）

1. **Reuse Before Abstraction** — 先复用，再抽象  
2. **Evidence Before Claims** — 无测试/验证不声称支持（尤其并发、幂等、租户、事务、Model E2E）  
3. **No Big-Bang Refactor** — 禁止借小需求重构整仓  
4. **No Future-Proofing Without Evidence** — 无真实需求不造平台件  
5. **Existing Capability First** — 先搜代码再新增  
6. **Smallest Valid Change** — 最小有效改动  
7. **Test Before Claim** — 代码完成 ≠ 功能完成  
8. **Stop When Validation Complete** — 验收通过后 STOP，不为「更完整」继续加功能  

详细步骤见 [workflow.md](workflow.md)。

---

## 7. 明确不在本方法范围内

当前 AI Coding 方法**不包含**、也不引导去建设：

RAG · MCP · Multi-Agent · Workflow Engine · Plugin System · Event Bus · 微服务 · Kubernetes · Prompt Marketplace · 自动代码生成平台

Agent 能力以仓库现状为准：`ModelPort` + `PromptTemplate` + `AgentInvocation` + OpenAI-compatible Adapter。Real Model E2E 无 API Key 时 skip，不得写成「已完成真实模型验收」。

AI Coding 方法会随真实 Demo / 项目验证迭代；**Prompt 模板库（`prompts/`）暂不创建**，等真实开发过程再沉淀。

---

## 8. 相关文档

| 文档 | 用途 |
|------|------|
| [workflow.md](workflow.md) | 标准工作流（Understand → … → STOP） |
| [../../README.md](../../README.md) | 项目定位与工程证据 |
| [../architecture-sealed.md](../architecture-sealed.md) | 架构封存与冻结状态 |
| [../module-boundary.md](../module-boundary.md) | 模块边界（部分历史命名，以当前 Maven 模块为准） |
| [../coding-rules.md](../coding-rules.md) | 编码硬约束 |
