# 02-plan

## Purpose

Architecture + Reuse + **Core Change Gate**。

在实现前判定：归属何处、已有能力、复用/组合/新增、Demo 能否解决、是否改 Core、最小改动、如何验证。

原则：**先复用，再新增。先计划并获人批准，再实现。**

无可靠 Understand Report → 退回 `01-understand`，不进入 Plan。

## When to Use

`01-understand` = PASS 之后、任何 `03-implement` 之前。

## Type

`分析型` — **默认不修改任何代码。** 输出是实施计划，不是实施本身。

## Inputs

| 输入 | 要求 |
|------|------|
| Understand Report | **必需**；缺失则 BLOCKED |
| 原始需求 | 必需 |
| 代码/文档上下文 | 必需用于搜索已有能力 |

## Instructions

严格按序，禁止跳过搜索直接给实现方案：

```text
Understand → 归属 → Domain Guide → 搜索能力 → Reuse → Composition
→ Demo? → New? → Core Gate → 最小方案 → 验证计划 → 等人批准
```

### Step 1：业务归属

判定其一：IoT / Business / Service / Agent / Shop / RuoYi·基础设施 / Demo·Presentation / 暂无法判断。

五大母域模块：`openbiz-iot` · `openbiz-business` · `openbiz-service` · `openbiz-agent` · `openbiz-shop`。  
**禁止**因复杂而新建第六 Mother Domain。

命中域则读 `docs/ai-coding/domains/<domain>.md`。当前仅有 `iot.md`；其他**不存在则勿创建**，按 README/workflow + 代码事实继续。

### Step 2：搜索已有能力

提出新增前必须搜：API · Service · Entity · Mapper · Port/Adapter · Test · Demo · 文档。  
除类名外搜：业务动作、方法名、表名、状态、API、测试、关键领域词。  
核心问：是否已有能力直接解决？

### Step 3：Reuse / Composition / New

- **Reuse**：现有能力直接可用 → 优先。
- **Composition**：多能力组合即可 → 禁止为组合新建大抽象。
- **New**：确无能力才新增；须说明为何不足、新增什么、为何最小、落 Domain 还是 Demo。

### Step 4：Demo vs Core

若仅为 Demo/场景编排/DTO/展示 API：优先只改 Demo，组合已有能力。  
禁止因 Demo 缺组合逻辑就改 OpenBiz Core。

### Step 5：Core Change Gate（默认不改 Core）

```text
已有能力? YES→Reuse
  NO → 组合? YES→Composition
    NO → Demo能解? YES→Demo
      NO → 真实重复需求? NO→STOP
        YES → 明确Core缺口? NO→STOP
          YES → 最小 Core Change → Human Approval
```

允许提议改 Core **仅当同时**：真实需求；现有不足；Demo/Industry 组合不行；缺口有证据；范围可控；有测试方案；**人批准**。

禁止理由：未来可能用、更优雅、更完整、方便扩展。

### Step 6：最小实现方案

写清：改/增哪些模块文件；复用什么；为何最小；**明确不应改**什么。  
禁止顺手：重构、升依赖、整理、拆 module、统一命名、补未来能力。

### Step 7：验证计划

事先标明哪些要做/不做：Unit · Integration · REAL MySQL · Concurrent · Tenant · Idempotency · Device/Broker/Model · HTTP/API。  
**Test Plan ≠ Test Result。** 本闸门只计划，不声称已验证。

### Mini example（判断方式，非教程）

需求「工业设备远程开机」→ IoT → 读 `domains/iot.md` → 搜 Device / DeviceCommandService  
→ 复用 `invoke` → 若仅 Demo 场景则 Demo 调已有能力 → 不建 MQTT Client、不直调 ProtocolPort、默认不改 IoT Core。

## Constraints

- 不改任何仓库文件；不实现代码
- 原则引用：`docs/ai-coding/README.md`（Reuse Before Abstraction、Evidence Before Claims、No Big-Bang、No Future-Proofing、Smallest Valid Change）与 `workflow.md`；不复制长文
- Domain 规则以对应 `domains/*.md` 为准（IoT 见 `iot.md`）

## Evidence Rules

- 未跑测试 ≠ PASS；无 REAL MySQL 证据 ≠ 已 REAL MySQL 验证
- Mock ≠ 真设备；Stub ≠ 生产能力；Unit ≠ 并发验证
- 有字段 ≠ 已实现幂等；Plan 中的验证项 ≠ 已完成
- AC 含高并发/幂等/生产 MQTT/真模型等 → 必须进入 Verification Plan

## Expected Output

固定输出：

```text
# Plan Report

## 1. Requirement
## 2. Domain
## 3. Domain Guide
## 4. Existing Capability Search
## 5. Reuse
## 6. Composition
## 7. Gap
## 8. Demo vs Core
## 9. Core Change Decision
   NO / PROPOSED / BLOCKED（PROPOSED 须说明原因）
## 10. Implementation Plan
## 11. File Scope
## 12. Explicitly Out of Scope
## 13. Verification Plan
## 14. Risks
## 15. Human Approval Required
   APPROVAL REQUIRED — 批准前禁止 Implement
```

## Stop Condition

Domain 明确；Guide 已读（若存在）；已搜索；Reuse / Composition / Demo vs Core / Core Gate 已判断；最小方案、File Scope、Verification Plan、Risks 明确。

结束态：`02-plan = READY FOR HUMAN APPROVAL`（**不是** IMPLEMENT）。

缺 Understand / 归属无法判断 / 未搜索就给方案 → `BLOCKED`。

## Do Not

1. 不修改代码、不实现方案
2. 不跳过搜索直接新建 Service/能力
3. 不重复造已有轮子；不为未来/优雅扩大范围或改 Core
4. 不创建新 Mother Domain；不创建 Domain Guide
5. 不把 Demo 问题升级成 Core 问题
6. 不把验证计划写成验证结果；不把 UNKNOWN 猜成事实
7. 不跳过 Human Approval 进入 Implement
