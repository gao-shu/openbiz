# 05-close

## Purpose

整个 AI Coding 流程的**最终关闭门**：**Review + Acceptance + STOP**。

回答三问：实现是否符合已批准计划？AC 是否真正完成？若完成 → **STOP**（不再自行扩活）。

本闸门 ≠ Debug / Refactor / New Planning / New Implementation / Verify 重跑。

## When to Use

仅当 `04-verify` = **READY FOR CLOSE**，且 Understand → Plan → Approval → Implement → Verify 链条完整。

## Type

`验证型`（Close / Acceptance）

默认：**No Business Code Changes**。不重跑测试套件；基于既有 Reports 做 Review 与 Acceptance。

## Inputs

| 输入 | 要求 |
|------|------|
| Original Requirement | 必需 |
| Understand Report | 必需 |
| Plan Report | 必需 |
| Human Approval = YES | 必需 |
| Approved File Scope | 必需 |
| Implementation Report | 必需 |
| Verify Report | 必需 |
| Actual Modified Files | 必需 |

关键输入缺失 → **BLOCKED**，禁止自行补全。

## Preconditions

```text
Understand = PASS
Plan = READY FOR HUMAN APPROVAL
Human Approval = YES
Implement = READY FOR VERIFY
Verify = READY FOR CLOSE
```

任一不满足 → **BLOCKED**。

## Instructions

### Step 1：Requirement Review

Original Requirement vs Implemented Behavior。禁止偷换需求。

### Step 2：Acceptance Criteria Review（逐条）

对每条 AC 只允许：`ACCEPTED` / `REJECTED` / `BLOCKED`。  
禁止：「基本完成」「大体没问题」「应该可以」「看起来完成」。

### Step 3：Plan Deviation Review

Actual Implementation vs `02-plan`。偏差标为 Approved / **Unapproved**。  
重大未批准偏差 → **NOT ACCEPTED**（不得替人自动批准）。

### Step 4：Scope Review

```text
Actual Modified Files ? Approved File Scope
```

否则：`SCOPE VIOLATION` → **NOT ACCEPTED**。

### Step 5：Domain Rule Review

命中域则读 `docs/ai-coding/domains/<domain>.md`（IoT → `iot.md`；不存在则勿创建）。  
检查：Core/Industry/Demo 边界、是否绕过已有能力、命令链、租户、Evidence Rules、域内硬禁止。  
违反 → **NOT ACCEPTED**。

### Step 6：Evidence Review

不重新执行测试。判断 Verify 的 Evidence 是否足以支撑 Acceptance。

```text
Test PASS ≠ Evidence Sufficient ≠ Acceptance
Mock ≠ Real；Unit ≠ REAL MySQL 并发；有字段 ≠ 幂等已证明
```

证据不足的关键 Claim → 不得 ACCEPTED。

### Step 7：Acceptance Decision

仅四种：

| 结果 | 条件 |
|------|------|
| **ACCEPTED** | 关键 AC 满足 + 证据充分 + Scope/Plan/Domain 合规 |
| **REJECTED** | AC 不满足 / Scope 违规 / 未批架构或 Core / 关键验证失败 / 域规则违反 |
| **BLOCKED** | 缺审批、缺证据、缺明确 AC 等，无法继续判断 |
| **PARTIALLY ACCEPTED** | 部分 AC 满足但整体未完成；须列 Accepted/Rejected/Blocked AC + Remaining Work；**不得**标 DONE |

### Step 8：STOP（硬规则）

若 `Acceptance = ACCEPTED`（或 PARTIALLY ACCEPTED 仅等待人决定下一轮）：

```text
STOP → FINAL STATUS = CLOSED（或 PARTIALLY ACCEPTED）
```

禁止自动：优化、重构、加功能、扩测、升依赖、抽象、改 Core、开下一阶段、commit、push。

发现「还能更好」→ 只写入 **Future Follow-ups**，**不得执行**。

STOP 含义：当前需求已达批准完成条件，AI **不再自行扩大范围**；新需求须从 `01-understand` 重开。

### Step 9：Commit / Push

本闸门**不**执行 `git commit` / `git push`。  
即使 ACCEPTED，只输出：`READY FOR HUMAN COMMIT`（是否提交由人决定）。

## Constraints

- 职责边界：`01` 理解 · `02` 计划 · `03` 实现 · `04` 证据 · `05` 验收关闭 — 禁止污染
- 原则引用：`docs/ai-coding/README.md`、`workflow.md`；不复制长文
- 不改业务代码 / Core / POM / SQL / Domain / Workflow / Prompt Library

## Evidence Rules

- Acceptance = AC + Verification Evidence，≠「代码存在」或笼统「测试通过」
- 不以 Mock 证据接受需 Real 证据的 Claim
- 不以 Verify 的 VERIFIED 以外状态硬抬成 ACCEPTED

## Expected Output

```text
# Close Report

## 1. Preconditions
## 2. Requirement Review
## 3. Acceptance Criteria Review
## 4. Plan Deviation Review
## 5. File Scope Review
## 6. Domain Rule Review
## 7. Evidence Review
## 8. Acceptance Decision
## 9. Accepted Criteria
## 10. Rejected / Blocked Criteria
## 11. Future Follow-ups
## 12. Final Status
## 13. Human Action
```

AC 表：

```text
| AC | Requirement | Evidence | Result |
|----|-------------|----------|--------|
| AC-1 | … | … | ACCEPTED / REJECTED / BLOCKED |
```

```text
Final Acceptance: ACCEPTED | PARTIALLY ACCEPTED | REJECTED | BLOCKED
```

## Final Status

| Decision | Final Status | 动作 |
|----------|--------------|------|
| ACCEPTED | `CLOSED` | **STOP**；Human Action 可含 READY FOR HUMAN COMMIT |
| PARTIALLY ACCEPTED | `PARTIALLY ACCEPTED` | **STOP**；等人决定是否新开一轮 |
| REJECTED | `REJECTED` | 指明回 `03-implement` 或架构问题回 `02-plan` |
| BLOCKED | `BLOCKED` | 说明阻塞原因 |

## Stop Condition

Review 五项完成；AC 逐条有结果；Evidence 已对照；Decision 已给出；Follow-ups 仅记录。  
ACCEPTED / PARTIALLY ACCEPTED → **强制 STOP**，不自动进入下一轮。

## Do Not

1. 不改代码 / Core / POM / SQL / 方法论文档  
2. 不自动 Debug、Refactor、扩需求、批准 Deviation  
3. 不把 Test PASS / 代码存在等同于 Acceptance  
4. 不把 Mock 包装成 Real Evidence  
5. 不自动 commit / push / 开下一任务 / 「顺便优化」  
6. 不创建其他文件  
