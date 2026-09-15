# OpenBiz Final Audit

> Date: 2026-09-15  
> Scope: **Final Read-Only Audit** (封库验收)  
> Mode: evidence-only · **no code / POM / SQL / config / README changes**  
> Only artifact allowed: this file

---

## 1. Executive Summary

| Gate | Result |
|------|--------|
| Module Structure | **PASS** (WARN: git 尚未首次提交) |
| Five Mother Domains | **PASS** |
| Foundation thinness | **PASS** |
| SaaS Core purity | **PASS** |
| Cross-domain deps | **PASS** (无非法 Maven / Entity 跨母域 import) |
| `mvn clean test` | **PASS** (1 skipped = Agent RealModelE2E，无 key，预期) |
| `mvn clean package -DskipTests` | **PASS** |
| Business 并发扣款 | **REAL MYSQL** |
| Shop stock=1 并发 | **REAL MYSQL** |
| Hardcoded LLM API Key | **PASS** (未发现) |
| P0 | **无** |

**Verdict line:** 五大母域 MVP 与锁定架构一致，全仓测试与并发证据真实；文档有过期夸大/状态漂移（P1），仓库尚无 git commit（开源卫生 WARN）。**可以冻结开发；公开前建议对齐文档与提交卫生，无需为封库继续写业务代码。**

---

## 2. Module Structure

### Observed layout

```text
open-biz-platform
├── openbiz-foundation          (极薄 marker)
├── openbiz-saas-core           (Tenant / Member)
├── openbiz-iot-core
├── openbiz-iot-mqtt
├── openbiz-access / locker / charging / mes
├── openbiz-biz-core            (Business)
├── openbiz-service
├── openbiz-agent
├── openbiz-shop
└── ruoyi-admin                 (装配启动)
```

Parent `pom.xml` modules 完整；`ruoyi-admin` 依赖全部 OpenBiz 模块做装配。

### Maven dependency direction (mother domains)

| Module | Depends on | Illegal peer domains? |
|--------|------------|------------------------|
| openbiz-biz-core | saas-core | NO |
| openbiz-service | saas-core | NO |
| openbiz-shop | saas-core | NO |
| openbiz-agent | foundation only | NO |
| openbiz-iot-core | saas-core | NO |
| industries (access/locker/charging/mes) | saas-core + iot-core | NO (IoT 行业插件，符合设计) |

### Structure score

**Module Structure = PASS**

WARN:

- Git: `No commits yet on master`（全量 untracked）。不影响代码正确性，但影响“可立即 push 开源”的卫生结论。
- 工作区存在 `.codegraph/`（索引工具产物）；`.gitignore` 未覆盖。

---

## 3. Five Mother Domains

### IoT

| Item | Evidence |
|------|----------|
| Status claim | v1.0 FROZEN — Device / Product / ThingModel / DeviceCommand / Protocol / Mock / Access / Locker / Charging / MES 模块均存在 |
| Command path | `DeviceCommandServiceImpl` → `ProtocolPortRegistry.require(protocol)` → `ProtocolPort.sendCommand` |
| Tenant | Mapper SQL：`id + tenant_id` |
| Core pollution by later domains | 无 Biz/Service/Shop/Agent import 进入 iot-core |
| Tests | iot-core 27 + access 5 + locker 7 + charging 7 + mes 6 = **52** |
| IoT Core Modification = 0 | 本次审计未改代码；跨域审计未见后续母域反向依赖 Core |

**Domain = PASS**

### Business

| Item | Evidence |
|------|----------|
| Positioning | 客户 + 账户 + 账本 + Item + Order（账户扣款交易） |
| Tables | `openbiz_customer/account/account_ledger/item/order/order_item`（≠ Shop 表） |
| Money | BigDecimal / Money 测试存在 |
| Idempotency / optimistic lock / TX | `BizMysqlTest` + unit tests |
| Concurrent debit | `concurrentConsumeNeverGoesNegative` → **REAL MYSQL** |
| Tests | **21** |

**Domain = PASS** — Business Order ≠ Shop Order（表名与语义分离明确）。

### Service

| Item | Evidence |
|------|----------|
| Scope | 最小 WorkOrder only |
| Transitions | `WorkOrderTransitions` 唯一规则源：CREATED→ASSIGNED/CANCELLED；ASSIGNED→ACCEPTED/CANCELLED；ACCEPTED→COMPLETED |
| Assignee | accept/complete 经 `CurrentUserPort` |
| Tenant | `openbiz_work_order` + id/tenant 查询模式 |
| Tests | transitions 3 + mysql 3 + service 8 = **14** |
| Deferred (not defects) | CRM / AfterSale / SLA / Attachment |

**Domain = PASS**

### Agent

| Item | Evidence |
|------|----------|
| Positioning | Java AI Application Core（非 Agent OS） |
| Pieces | PromptTemplate · AgentInvocation/AgentInvoker · ModelPort · OpenAiCompatibleModelAdapter · ToolPort（空接口占位） |
| Stack | Java 21 · JDK HttpClient · Jackson · OpenAI-compatible `/chat/completions` |
| Credentials | `AgentModelProperties`；bean `@ConditionalOnProperty(...api-key)`；无硬编码 key |
| Persistence | **No persistent business data**（无 Agent 业务表） |
| Tests | adapter/prompt/invoker **13 run** + RealModelE2E **1 skipped**（assume env） |
| Deferred (not defects) | RAG / Memory / MCP / Workflow / Multi-Agent / Function Calling |

**Domain = PASS**（WARN: 无 `/openbiz/test/agent` Probe，闭环靠单测 + 可选 Real E2E）

### Shop

| Item | Evidence |
|------|----------|
| Positioning | 商品 + 库存 + 订单 |
| Tables | `openbiz_shop_*`（与 IoT `openbiz_product`、Biz `openbiz_order` 分离） |
| PlaceOrder | validate → price → total → stable product order → CAS deduct → SUCCESS order + items → commit |
| Concurrent | `concurrentBuyStockOne`：SUCCESS=1 FAIL=1 inventory=0 → **REAL MYSQL** |
| Tests | **11** |
| Deferred (not defects) | SKU / Cart / Payment / Refund / Coupon / Logistics / AfterSale |

**Domain = PASS**

---

## 4. Foundation Audit

Files: `OpenBizFoundationAutoConfiguration` + `package-info`（明确禁止 Customer/Product/Order/Device）。

无业务 Entity / Mapper / Service。

**Foundation = PASS**

---

## 5. SaaS Core Audit

Present and live:

- `TenantContext` / `TenantInterceptor` / `TenantResolver` (`MemberTenantResolver`)
- `MemberService` / `OpenbizTenant` / `OpenbizMember`
- SQL: `openbiz_tenant` / `openbiz_member`
- Probe: `GET /openbiz/test/tenant`
- Tests: **9**

无 Shop / Business / Service / IoT / Agent 业务对象污染。

母域通过 `TenantContext` + Guard 使用租户能力（Biz/Service/Shop/IoT）。

**SaaS Core = PASS**

---

## 6. Cross-domain Dependency Audit

Java import 扫描（母域互引 Entity/Mapper/Service）：**未发现**。

显式禁止边：

| Edge | Result |
|------|--------|
| Shop → Business/IoT/Service/Agent | **X（无）** |
| Service → Business/IoT/Agent/Shop | **X（无）** |
| Business → IoT/Service/Agent/Shop | **X（无）** |
| Agent → Business/IoT/Service/Shop | **X（无）** |

Agent → Foundation only：符合锁定设计。

**Cross-domain = PASS** · 无 Severity 记录项

---

## 7. Database Audit

| Table | Domain | tenant_id | Notable unique / index | Cross-domain FK |
|-------|--------|-----------|------------------------|-----------------|
| openbiz_tenant / openbiz_member | SaaS | N/A / yes | membership | No |
| openbiz_product / device / thing_model / device_command | IoT | yes | tenant scoped | No |
| openbiz_access_* / locker_* / charging_* / mes_* | IoT industries | yes (per SQL) | — | No |
| openbiz_customer / account / account_ledger / item / order / order_item | Business | yes | phone / ledger idem / order idem | No |
| openbiz_work_order | Service | yes | — | No |
| openbiz_shop_product / inventory / order / order_item | Shop | yes | product code / inv product / order idem | No |

确认：

- Business `openbiz_order` ≠ Shop `openbiz_shop_order`
- IoT `openbiz_product` ≠ Shop `openbiz_shop_product`
- MES `openbiz_mes_work_order` ≠ Service `openbiz_work_order`

SQL 文件存在于 `sql/openbiz_*.sql`。

**Database = PASS**

---

## 8. Tenant Isolation Audit

| Domain | Pattern | Evidence |
|--------|---------|----------|
| IoT | select/update `id + tenant_id` | Device/Product/Command mappers |
| Business | Customer/Account/Order + tenant | BizMysqlTest 跨租户拒绝 |
| Service | WorkOrder id + tenant_id | WorkOrder service + mysql tests |
| Shop | Product/Inventory/Order/OrderItem + tenant | ShopServiceImpl + ShopTenantGuard |
| Agent | N/A | No persistent business data — **不强制 tenant table** |

**Tenant Isolation = PASS**

---

## 9. Authentication / Security Audit

- 使用 RuoYi Security + JWT filter；`anyRequest().authenticated()`
- `/openbiz/test/**` **未**列入 `permitAll` → Probe 依赖正常登录
- Service/Shop 使用 `CurrentUserPort` / SecurityUtils 模式（与 RuoYi LoginUser 集成）
- 未发现第二套自研 JWT

**Auth = PASS**

WARN（开源配置卫生，非自研安全旁路）:

- `ruoyi-admin/.../application-druid.yml`：本地演示库密码 `123456`（RuoYi 上游常见默认）
- `application.yml`：JWT `secret: abcdefghijklmnopqrstuvwxyz`（RuoYi 默认演示 secret）

---

## 10. Test Audit

### Commands (this audit run)

```text
mvn clean test                 → BUILD SUCCESS
mvn clean package -DskipTests  → BUILD SUCCESS
```

### Counts (module Totals from Surefire)

| Area | Tests run | Skipped | Failures |
|------|-----------|---------|----------|
| SaaS | 9 | 0 | 0 |
| IoT core | 27 | 0 | 0 |
| Access | 5 | 0 | 0 |
| Locker | 7 | 0 | 0 |
| Charging | 7 | 0 | 0 |
| MES | 6 | 0 | 0 |
| Business | 21 | 0 | 0 |
| Service | 14 | 0 | 0 |
| Agent | 14 | **1** | 0 |
| Shop | 11 | 0 | 0 |

Agent skip: `RealModelE2ETest` — `assumeTrue` 要求 env 中的 baseUrl/apiKey/model；**非假绿，属诚实跳过**。

未发现 `@Disabled` 掩盖核心业务；MySQL 并发测试非 mock 核心路径。

**Test Audit = PASS**

---

## 11. Concurrency Evidence

| Domain | Scenario | Marker | File |
|--------|----------|--------|------|
| Business | 余额并发消费不超扣 / 余额非负 | **REAL MYSQL** | `BizMysqlTest.concurrentConsumeNeverGoesNegative` (`jdbc:mysql://localhost:3306/ry-vue`) |
| Shop | stock=1 · 两并发下单 → SUCCESS=1 FAIL=1 inventory=0 | **REAL MYSQL** | `ShopMysqlTest.concurrentBuyStockOne` |

Unit 层另有 mock/in-memory 行为测试；**不以 mock 冒充 MySQL 证据**。

**Concurrency = PASS**

---

## 12. Sensitive Information Audit

| Check | Result |
|-------|--------|
| Hardcoded OpenAI / LLM API key (`sk-…`) | **未发现** |
| `.env` committed content | `.env` 在 `.gitignore`；未见提交型密钥文件 |
| Agent docs | 仅示例 `set OPENBIZ_AGENT_API_KEY=...`（占位，非真 key） |
| DB/JWT demo defaults in yml | 见 §9 WARN（RuoYi 演示配置） |

**Sensitive = PASS**（无 P0 密钥泄漏）  
公开前应对演示密码/JWT secret 做环境覆盖说明（P1 卫生，非本次代码缺陷修复范围）。

---

## 13. Git / Open-source Hygiene

| Item | Status |
|------|--------|
| `.gitignore` | 含 `target/`、`.env`、IDE、`*.log`、local yml overrides |
| LICENSE | 存在 |
| First commit | **无**（全部 `??`） |
| `.codegraph/` | 未 ignore |
| Large non-target binaries | 本次抽查未见明显 >5MB 业务二进制（排除 target/.git） |
| Personal absolute paths in code | 未作为阻断项检出 |

**Hygiene = WARN**（可公开，但需先完成首次干净提交与文档对齐）

未执行 `git clean` / 未删除任何文件。

---

## 14. Documentation Consistency

| Doc | Issue | Severity |
|-----|-------|----------|
| `docs/architecture-sealed.md` | Service/Agent/Shop 仍写 **Skeleton**；与 Phase 1 COMPLETE 事实不符 | **P1** |
| `README.md` 模块表 | Service 写「CRM / 工单 / 售后」；Agent 写「AI / RAG / Agent」——**夸大已完成范围** | **P1** |
| `README.md` | 「下一母域」仍链 Agent Phase 0 —— 过期导航 | **P1** |
| `docs/architecture-phase2.1-audit.md` 等 | 历史骨架态描述残留 | **P2** |
| Phase 1 reports (biz/service/agent/shop) | 与代码大体一致 | OK |
| 第六母域 | 未发现正式“已完成第六母域”宣称 | OK |

原则：**文档说完成的，代码基本存在**；问题主要是 **旧封存文档 / README 标签未随 Phase 1 更新**，以及 **范围用词过宽**。

**Docs = WARN**（只记录，未修改）

---

## 15. Probe API Audit

| Mother Domain | Endpoint | Purpose | Auth | Status |
|---------------|----------|---------|------|--------|
| SaaS | `GET /openbiz/test/tenant` | 租户上下文 | RuoYi login | OK |
| IoT | `/openbiz/test/iot/**` | devices/products/invoke/commands/mock | login | OK |
| Access | `POST /openbiz/test/access/open` | 门禁探测 | login | OK |
| Locker | `POST /openbiz/test/locker/open` | 柜门 | login | OK |
| Charging | `POST /openbiz/test/charging/start` | 充电 | login | OK |
| MES | `POST /openbiz/test/mes/start-production` | 生产 | login | OK |
| Business | `/openbiz/test/biz/**` | customer/recharge/order/ledger | login | OK |
| Service | `/openbiz/test/service/work-orders/**` | WO 全流程 | login | OK |
| Shop | `/openbiz/test/shop/**` | product/inventory/order | login | OK |
| Agent | — | **无 Probe** | — | **WARN / P2** |

Probe 目标是验证闭环，非正式对外 REST —— **不因“不够正式”判 FAIL**。

---

## 16. Capability Matrix

| 母域 | 核心能力 | 真实测试 | 并发 | 租户 | 状态机 | AI |
|------|----------|----------|------|------|--------|-----|
| IoT | Device/Command/Protocol + 行业插件 | YES (52) | Mock 协议路径实测；非库存式并发 | YES | Command 状态 PENDING/SUCCESS/FAILED | - |
| Business | Customer/Account/Ledger/Order 扣款 | YES (21) | **REAL MYSQL** | YES | 订单 SUCCESS 语义（非复杂 FSM） | - |
| Service | WorkOrder assign/accept/complete/cancel | YES (14) | MySQL 验证存在 | YES | **YES** (`WorkOrderTransitions`) | - |
| Agent | Prompt→Invoker→ModelPort | YES (13) + E2E skip | N/A | N/A (无业务表) | - | **YES** (可替换 ModelPort) |
| Shop | Product/Inventory CAS/Order | YES (11) | **REAL MYSQL stock=1** | YES | Phase1 SUCCESS-only（非假 CREATED FSM） | - |

未虚构未实现能力。

---

## 17. Interview Value

| Question | Code + test evidence? | Result |
|----------|----------------------|--------|
| IoT：命令如何下发到设备？ | DeviceCommand → ProtocolPortRegistry → ProtocolPort + tests | **PASS** |
| Business：并发扣款如何避免超扣？ | 乐观锁/版本 + REAL MYSQL 并发 | **PASS** |
| Service：非法状态如何拒绝？ | `WorkOrderTransitions` + tests | **PASS** |
| Agent：如何接入大模型且可替换？ | `ModelPort` + OpenAI-compatible adapter + unit tests | **PASS** |
| Shop：库存=1 双请求如何防超卖？ | CAS + REAL MYSQL 断言 | **PASS** |

---

## 18. Issues by Severity

### P0 — 阻断开源/面试

**无。**

### P1 — 建议在公开前处理（文档/卫生；非业务缺口）

1. **文档状态漂移**：`architecture-sealed.md` 仍称 Service/Agent/Shop 为 Skeleton。  
2. **README 夸大**：Service「CRM/售后」、Agent「RAG」与真实 Phase 1 不符。  
3. **README 导航过期**：「下一母域」仍指向 Agent Phase 0。  
4. **Git 无首次提交**：无法形成可审查历史；公开前需干净 initial commit。  
5. **演示凭据**：Druid 密码 / JWT secret 为 RuoYi 默认；公开仓库应依赖 local override / 环境变量并在 README 声明（**本次按规则未改配置**）。  
6. **`.codegraph/`** 建议加入 ignore，避免误提交索引目录。

### P2 — 可优化

1. Agent 缺少 `/openbiz/test/agent` Probe（能力已有单测证明）。  
2. 部分早期 architecture Phase 2.x 文档未标注“已被 Phase 1 报告取代”。  

### P3 — 明确延期 / 非缺陷（不得当成缺口）

SKU、Cart、Payment、Refund、Coupon、CRM、AfterSale、SLA、RAG、Memory、MCP、Workflow、Multi-Agent、Function Calling、第六母域、Shop Phase 2。

---

## 19. Final Score

| # | Dimension | Score /10 | Note |
|---|-----------|-----------|------|
| 1 | 架构边界 | **10** | 五大母域隔离干净 |
| 2 | 业务完整度 | **9** | 相对锁定 MVP，非完整 ERP/电商 |
| 3 | 技术深度 | **9** | CAS / 乐观锁 / 状态机 / ModelPort |
| 4 | 测试证据 | **9** | 全仓绿；Agent E2E 诚实 skip |
| 5 | 并发证据 | **10** | Biz + Shop REAL MYSQL |
| 6 | 多租户 | **9** | 一致 tenant_id 模式 |
| 7 | AI 能力 | **8** | 最小可替换 Core；非 Agent OS |
| 8 | 开源展示 | **7** | 无 commit + 文档漂移 + 演示默认配置 |
| 9 | 面试价值 | **9** | 五题均有代码+测试锚点 |
| 10 | 代码复杂度控制 | **10** | 边界优先，未堆平台件 |

### **Total: 90 / 100**

---

## 20. Final Verdict

| Question | Answer |
|----------|--------|
| 1. 是否可以冻结？ | **YES** — `OPENBIZ MVP = FINAL` · `ARCHITECTURE = SEALED` · `FIVE MOTHER DOMAINS = FROZEN` · `CODE DEVELOPMENT = STOP` |
| 2. 是否可以公开 GitHub/Gitee？ | **YES，附条件** — 先处理 P1 文档对齐与首次干净提交；无需为开源继续扩功能 |
| 3. 是否可以写入简历？ | **YES** — 以五大母域 MVP + 真实并发/状态机/ModelPort 表述，避免夸大 CRM/RAG/完整电商 |
| 4. 是否可以作为面试主项目？ | **YES** — 五母域各有可讲深的硬问题与证据 |
| 5. 是否存在 P0？ | **NO** |
| 6. 是否存在必须在公开前修复的问题？ | **无代码级 P0**；**建议公开前处理 P1（文档诚实度 + git 卫生 + 演示凭据说明）**。不要求修 P2、不开启第六母域、不开启 Shop Phase 2。 |

### Gate checklist (§二十八)

- [x] 五大母域边界正确  
- [x] 无异常跨域依赖  
- [x] Foundation 无业务污染  
- [x] SaaS Core 正常  
- [x] 全仓测试通过  
- [x] 并发证据真实（REAL MYSQL）  
- [x] 无敏感 API Key 提交  
- [x] 无 P0  

```text
OPENBIZ MVP = FINAL
ARCHITECTURE = SEALED
FIVE MOTHER DOMAINS = FROZEN
CODE DEVELOPMENT = STOP
```

---

*End of Final Read-Only Audit. No code was modified. STOP.*
