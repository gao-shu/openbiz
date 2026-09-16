# 03-implement

## Purpose

按**已批准**的 Plan，在明确 **File Scope** 内做**最小实现**。

原则：**Plan 决定做什么；Implement 只把它做出来。**  
禁止在 Implement 阶段重新设计架构或自行扩大范围。

## When to Use

仅当：`01-understand` = PASS，且 `02-plan` = READY FOR HUMAN APPROVAL，且 **Human Approval = YES**，且 File Scope 明确。

之后进入 `04-verify`。

## Type

`执行型`

允许：改/增代码与测试；在批准范围内改配置。  
全部修改受 Plan 的 Human Approval + File Scope 约束。

## Inputs

| 输入 | 要求 |
|------|------|
| 原始需求 | 必需 |
| Understand Report | 必需 |
| Plan Report | 必需 |
| Human Approval = YES | **必需** |
| Approved File Scope | **必需** |

缺 Plan / Approval / File Scope → **BLOCKED**，不得改代码。

## Preconditions

改代码前确认：

```text
Understand = PASS
Plan = READY FOR HUMAN APPROVAL
Human Approval = YES
File Scope = 明确
```

并检查工作区：无未预期脏改；Plan 与当前代码无重大偏差。  
偏差严重 → **STOP → 返回 `02-plan`**，禁止自行重设计。

## Instructions

### Step 1：确认目标

只实现 Understand 的 Problem / Business Goal / **AC**；遵守 Out of Scope。

### Step 2：锁定 File Scope

只改 Plan 批准的文件/模块。  
必须改范围外文件 → **STOP**，不得自行扩 scope。

### Step 3：按复用方案执行

执行 Plan 的 Reuse / Composition / New。  
不得重造已有能力。Plan 指定能力实际不存在 → **STOP → Plan**。

### Step 4：最小变更

遵守 Smallest Valid Change：最少文件、最少新代码、复用优先、不改无关行为。

禁止顺手：重构、统一命名、升依赖、拆 module、优化无关代码、清旧代码、加未来扩展点。

### Step 5：架构无决策权

若产生新架构判断（拆模块、加抽象、改 Core、合并 Service、重做流程等）：

```text
STOP → 返回 02-plan → 重评 → Human Approval → 再 Implement
```

### Step 6：Core 修改

仅当 Plan **明确批准**某 Core 模块范围时可改（foundation / saas-core / iot / business / service / agent / shop）。  
未批准 → 即使「顺手更合理」也禁止。

### Step 7：IoT（若属 IoT）

遵守 `docs/ai-coding/domains/iot.md`。命令链默认：

```text
Industry / Demo → DeviceCommandService → ProtocolPortRegistry → ProtocolPort
```

未批准不得：第二套 Device/Command Service、MQTT Client、旁路 Protocol、改写 IoT Core 命令主链路。

### Step 8：测试

可增改测试，须对齐 Plan 的 Verification Plan；勿顺便扩大测试体系。  
REAL MySQL 等按计划执行；环境不可跑 → 记 `NOT RUN`，禁止伪造 PASS。

### Step 9：范围核对

开始/结束记录 Allowed vs Actual。要求：

```text
Actual Modified Files ? Approved File Scope
```

否则：`IMPLEMENTATION SCOPE VIOLATION`，不得宣称完成。

## Constraints

- 无 Approval / Scope → 零改动
- 原则引用：`docs/ai-coding/README.md`、`workflow.md`；IoT 引用 `domains/iot.md`；不复制长文
- 发现新问题 → STOP 回 Plan，不自行加功能

## Evidence Rules

代码写完 ≠ 功能验证完成。

```text
编译成功 ≠ 业务正确
Unit PASS ≠ REAL MySQL PASS
Mock PASS ≠ 真设备 PASS
Stub PASS ≠ 生产能力 PASS
```

本闸门只报告：改了什么、测了什么、编译如何。  
能力是否成立 → 交给 `04-verify`。禁止无证据写「生产可用 / 高并发已验证 / 幂等已保证」。

## Expected Output

```text
# Implementation Report

## 1. Approved Plan
## 2. Approved File Scope
## 3. Actual Modified Files
## 4. Implementation Summary
## 5. Reuse
## 6. New Code
## 7. Tests Added / Changed
## 8. Test Execution
## 9. Scope Check
   PASS / VIOLATION
## 10. Plan Deviations
## 11. Unresolved Issues
## 12. Handoff to 04-verify
```

## Stop Condition

AC 已按 Plan 实现；File Scope 未越界；无未批准架构变化；测试已按计划准备/执行；Plan Deviations 已记录。

结束态：`IMPLEMENT = READY FOR VERIFY` → 进入 `04-verify`。

## Must STOP（回 `02-plan`）

1. Plan 与代码冲突  
2. 须改范围外文件 / 未批准 Core  
3. 新架构问题 / 需求重大歧义  
4. Reuse 与 Plan 不符  
5. 测试需新基础设施且 Plan 未批  
6. 须扩 Scope / 改库表 / 升依赖且未批  

输出 Blocked / Plan Deviation，**禁止自行扩任务**。

## Do Not

1. 不在 Implement 重设计架构  
2. 不自行扩 File Scope / 改 Core  
3. 不顺手重构、升依赖、拆模块、加未来扩展点  
4. 不绕过已有能力  
5. 不夸大测试为生产能力；不伪造结果；不隐藏 Deviation  
6. 不因「更优雅」偏离批准方案  
