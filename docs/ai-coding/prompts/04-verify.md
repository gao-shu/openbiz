# 04-verify

## Purpose

对已完成 Implement 的变更做 **Test + Validate**：执行约定测试、记录证据、判断证据是否足以支撑 Claim。

本闸门**不负责**最终 Acceptance（属 `05-close`）。  
不负责 Debug / Refactor / Security / Performance / 重新 Architecture / 重新 Implement。

## When to Use

仅当 `03-implement` = **READY FOR VERIFY**，且 Understand / Plan / Human Approval / File Scope / Implementation Report 齐全。

## Type

`验证型`（Verification）

默认：**No Business Code Changes**（不改业务实现代码）。

允许：跑构建/测试、读日志与结果、核对修改范围、分析证据；必要时补充与 Plan 中 Verification Plan **一致**的测试。

## Inputs

| 输入 | 要求 |
|------|------|
| Original Requirement | 必需 |
| Understand Report | 必需 |
| Plan Report + Verification Plan | 必需 |
| Human Approval = YES | 必需 |
| Approved File Scope | 必需 |
| Implementation Report | 必需 |
| Actual Modified Files | 必需 |

关键输入缺失 → **BLOCKED**，禁止自行补全。

## Preconditions

```text
Understand = PASS
Plan = READY FOR HUMAN APPROVAL
Human Approval = YES
Implement = READY FOR VERIFY
Actual Modified Files 已提供
```

不满足 → **BLOCKED**。

## Instructions

### Step 1：Scope Verification

```text
Actual Modified Files ? Approved File Scope
```

否则：`IMPLEMENTATION SCOPE VIOLATION` → **立即 STOP**，不得把后续结果包装成正常 PASS。

### Step 2：Build / Compile

按项目做最小必要构建，例如 `mvn -pl <module> test` 或约定模块的 `mvn test`。  
勿无意义重复大量命令。

### Step 3：Test

按 Plan 的 Verification Plan 执行。优先：AC → 变更代码 → 受影响既有行为 → 相关回归。  
不为「测得多」而测无关内容。

**Test 只回答：** 测试有没有执行并通过？（Unit / Integration / MySQL / Build 等）

### Step 4：Validate（证据是否够）

**Validate 只回答：** 现有证据是否足以支撑需求与能力声明？

对每个 Claim 走：

```text
Claim → Required Evidence → Actual Evidence → Evidence Sufficiency
```

禁止：Unit/Mock PASS 自动推导 REAL MySQL 并发 / 真设备 / 生产 MQTT 等。

### Step 5：Evidence Level

| Level | 含义 | 可支撑的上限 |
|-------|------|--------------|
| 0 | No Evidence / NOT VERIFIED | 无 |
| 1 | Static / Compile / 路径检查 | 有限静态结论 |
| 2 | Unit / Mock / MockDoor / Mockito | 测试环境下逻辑 |
| 3 | Integration（真实 MySQL / HTTP / 组件集成） | 更强系统行为 |
| 4 | Real Env（真设备 / Broker / 外部模型 / 准生产） | 对应真实环境行为 |

未执行 → `NOT RUN`。失败 → `FAIL`。证据不够支撑 Claim → `INSUFFICIENT EVIDENCE`。充分 → `VERIFIED`。

禁止伪证据：「理论上应通过」「看起来没问题」「Mock ≡ REAL MySQL」。

### Step 6：IoT（若属 IoT）

读并遵守 `docs/ai-coding/domains/iot.md`。  
当前大量证据仍为 Mock/Mockito；REAL MySQL 集成/并发、真 Broker、真设备**不得因一般 Test PASS 自动声称已验证**。  
涉及 Device / Command / Protocol / MQTT / Tenant / Idempotency / DB concurrency 时，按**实际测试类型**定证据是否足够。

### Step 7：失败与回退（禁止隐形 Debug 循环）

需要改业务实现才能过测 → **STOP → `03-implement`**。  
设计/架构问题 → **STOP → `02-plan`**。

测试失败时先记录：Failure Location / Reason / Impact / Recommended Next Step。  
**禁止**自动「改代码→再测→再改」无限循环；须明确重新进入 `03-implement` 后才修。

## Constraints

- 默认不改业务代码、不改 Core、不升依赖、不改库表、不扩需求
- 原则引用：`docs/ai-coding/README.md`（Evidence Before Claims）、`workflow.md`；不复制长文
- 不 commit / push；不创建 `05-close` 或其他文件

## Evidence Rules

```text
Test PASS ≠ Claim VERIFIED
代码存在 ≠ 能力已验证
Mock PASS ≠ 真设备 / 真 Broker
Unit PASS ≠ REAL MySQL 并发
有字段 ≠ 幂等已证明
```

AC Verification 本闸门只标：`VERIFIED` / `NOT VERIFIED` / `INSUFFICIENT EVIDENCE` / `BLOCKED` — **不是**最终 Acceptance。

## Expected Output

```text
# Verify Report

## 1. Verification Preconditions
## 2. Scope Verification
## 3. Build Result
## 4. Test Result
## 5. Acceptance Criteria Verification
## 6. Evidence Inventory
## 7. Evidence Level
## 8. Claim vs Evidence
## 9. Validation Gaps
## 10. Failed Tests
## 11. Unresolved Issues
## 12. Recommended Next Step
## 13. Handoff to 05-close
```

Claim vs Evidence（核心）：

```text
| Claim | Required Evidence | Actual Evidence | Result |
|-------|-------------------|-----------------|--------|
| …     | …                 | … / NOT RUN     | VERIFIED / INSUFFICIENT EVIDENCE / NOT VERIFIED |
```

## Stop Condition

Scope 已核；约定测试已执行或诚实 NOT RUN；结果与 Evidence Level 已记；Claims 已对照证据；Gaps 已列。

结束态：`VERIFY = READY FOR CLOSE` → 交给 `05-close`。  
本闸门**不做**最终 Accepted / Rejected。

## Must STOP

1. Scope Violation  
2. 测失败且需改业务代码 → `03-implement`  
3. 新架构 / Plan 与代码严重不符 → `02-plan`  
4. 需未批准基础设施 / 改库表 / 升依赖 / 扩需求 → BLOCKED 或回 Plan  
5. 关键 Claim 证据不足 → 记 INSUFFICIENT EVIDENCE，勿伪造成 VERIFIED  

## Do Not

1. 不重设计、不重规划、不偷偷改 Core  
2. 不自动 Debug / Refactor；不为过测降低标准  
3. 不伪造结果；不把 Mock 包装成真实环境证据  
4. 不把 Test PASS 直接写成 Claim VERIFIED  
5. 不自行做最终 Acceptance；不 commit / push  
6. 不创建 `05-close.md` / `prompts/README.md`；不改其他 AI Coding 文档或 Java/POM/SQL  
