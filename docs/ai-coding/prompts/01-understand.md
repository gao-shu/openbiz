# 01-understand

## Purpose

在任何设计与实现之前，把用户需求变成：**可执行、可验收、边界明确**的问题定义。

本闸门产出是 `02-plan` 的输入，不是方案本身。

原则：先理解，再设计。**没有明确 Problem + AC，不进入 Plan。**

---

## When to Use

新功能、Bug、Demo、业务变更、Core 修改请求、重构请求——开工前均可使用。

需求已很清楚时：快速走完并产出固定报告，**不要**变成冗长需求分析项目。

Understand = **轻量闸门**。

---

## Type

`分析型`

**默认不修改任何代码。**

---

## Inputs

| 输入 | 说明 |
|------|------|
| 自然语言需求 / Issue / Bug 描述 | 必需至少一种 |
| 用户已给的 AC | 可选；需复核是否可验证 |
| 相关文件/代码上下文 | 可选；用于校准范围，非实现 |

信息不足时：列出 Known / Unknown / 阻塞问题。**禁止编造。**

---

## Instructions

### Step 1：复述问题

用自己的话回答：用户真正要解决什么？  
禁止只粘贴用户原话。

### Step 2：确认业务目标

区分 **业务目标** vs **技术手段**。

- 坏例：把「加 MQTT Adapter」「加一张表」当成目标
- 好例：某类设备能通过既有命令链路完成某业务动作

技术手段留给 `02-plan`。

### Step 3：影响范围（初判）

只做初步定位，**不做最终 Architecture**：

- 可能涉及哪个 Mother Domain / Demo / RuoYi？
- 可能相关的模块或包？
- 是否像在碰已有能力？

最终归域、复用清单、改 Core 判断 → `02-plan`。

### Step 4：Acceptance Criteria（核心）

写成可验证条目（Given/When/Then 或等价形式）。

禁止不可测措辞：代码质量好、设计合理、支持高并发、生产可用、完整平台……

能力类声明（并发、幂等、真实设备、REAL MySQL 等）**不得**仅因用户口头提到就写入 AC；写入前须在 Unknowns 或 AC 中标明验证要求与证据级别。

### Step 5：Out of Scope

明确本次不做。至少警惕：顺手重构、升级依赖、拆 module、加未来能力、未批准改 Core。

### Step 6：未知信息

例如：是否要真设备 / REAL MySQL / 并发 / 幂等 / 跨租户？  
不知则写 `UNKNOWN`，不猜。

### Step 7：风险初筛

只打标，不解题：租户、并发、幂等、事务、权限、一致性、外部依赖等。  
禁止在此展开复杂架构。

---

## Boundary

```text
01-understand  →  Problem + AC + Scope + Unknowns + Risk Flags
02-plan        →  Architecture + Reuse + 改 Core? + 实现方案 + 验证计划
```

本闸门禁止：选具体实现、新建类/接口/表结构、完整代码方案、修改 Core、最终定案。

若任务命中某母域，可**点名**应读的 Domain Guide（如 IoT → `docs/ai-coding/domains/iot.md`），但本闸门不代替 Plan 做域内决策。

---

## Constraints

- 不改仓库任何文件（含 Java / POM / SQL / 文档）
- 不扩大用户未确认的 Scope
- 不把 Deferred / 未来需求写入本次 AC
- 不复制 README / workflow 长文；需要原则时引用路径即可

---

## Evidence Rules

- 用户未提供的事实 → 不得当 Known Facts
- 不确定 → `UNKNOWN`
- 「高并发 / 幂等 / 生产可用」等 → 无明确验证约定不得写入 AC 当已承诺能力
- AC 若要求真实 DB / 设备 / 外部模型 → 必须写进 AC 或 Unknowns，供后续 Verify
- Mock / Stub / 单测 **不是** Understand 阶段的生产证据；本闸门也不宣称任何验证已完成

---

## Expected Output

固定输出：

```text
# Understand Report

## 1. Problem
## 2. Business Goal
## 3. Scope
## 4. Acceptance Criteria
## 5. Out of Scope
## 6. Known Facts
## 7. Unknowns
## 8. Risk Flags
## 9. Handoff to 02-plan
```

`Handoff to 02-plan` **只列待判断项**（归属、待搜能力、是否改 Core、验证方式），**禁止**提前给最终答案。

---

## Stop Condition

| 结论 | 条件 |
|------|------|
| **PASS** | Problem、Business Goal、Scope 明确；AC 可验证；Out of Scope 明确；Unknowns 已显式列出 → 可进入 `02-plan` |
| **BLOCKED** | 关键业务目标或 AC 无法确定 → **禁止**进入 Plan；向人列出阻塞问题 |

---

## Do Not

1. 不写业务代码、不修改代码
2. 不设计完整架构、不创建接口/类/库表方案
3. 不为「完整」自行补需求、不扩大 Scope
4. 不把技术手段当成业务目标
5. 不把 `UNKNOWN` 猜成事实
6. 不提前修改 Core
7. 不把未来需求写入本次 AC
8. 不输出实现代码或伪代码方案
9. 不把 Probe / Mock 能力写成生产验收标准（除非用户明确只要验证级行为）
10. 不跳过本闸门直接 Implement
