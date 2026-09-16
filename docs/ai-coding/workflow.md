# OpenBiz AI Coding Workflow

标准流程（Coding Agent 与人类共用）：

```text
需求
 ↓
1. Understand
 ↓
2. Architecture
 ↓
3. Reuse
 ↓
4. Implement
 ↓
5. Test
 ↓
6. Validate
 ↓
7. Review
 ↓
8. Acceptance
 ↓
STOP
```

任一环节证据不足或越界 → **停在该步，升级给人决策**，禁止自行跳步「先写了再说」。

原则全文见 [README.md](README.md)。

---

## 1. Understand

**目的：** 把口语需求变成可验证的业务问题陈述。

| | |
|--|--|
| **AI** | 复述需求；列出成功标准、非目标、风险；标出未知点 |
| **人** | 确认「要做什么 / 不做什么」；拒绝含糊的「顺便做完整」 |
| **输出** | 1 段问题陈述 + Acceptance Criteria（可测）+ Out of Scope |
| **进入下一步** | 人确认问题陈述与验收标准 |

禁止：把 Deferred 能力（支付、RAG、CRM…）默认塞进范围。

---

## 2. Architecture

**目的：** 决定归属与是否允许动 Core。

| | |
|--|--|
| **AI** | 回答强制判断顺序 1～8（见 README §4）；给出推荐 Mother Domain / Demo 层 / RuoYi |
| **人** | 拍板归属；默认 **不改 OpenBiz Core** |
| **输出** | 归属结论 + 依赖方向 +「改 Core？YES/NO」及理由 |
| **进入下一步** | 归属明确且人同意「改 Core」结论（通常为 NO） |

速查：

```text
RuoYi Admin     = bootstrap / auth / RBAC assembly
Foundation      = 极薄技术基础
SaaS Core       = Tenant / Member / TenantContext
IoT             = 设备与命令及行业 package
Business        = 客户 / 账户 / 交易
Service         = WorkOrder 状态流
Agent           = Model / Prompt / Invocation
Shop            = 商品 / 库存 / 商城订单
```

跨母域：默认 **禁止** Entity/Mapper/Service 互 import。组合通过应用层编排或 API，不通过 Maven 耦合。

---

## 3. Reuse

**目的：** Existing Capability First。

| | |
|--|--|
| **AI** | 搜索 Entity / Service / Mapper / Port / Adapter / Test / Probe / SQL；列出可复用点与缺口 |
| **人** | 确认「够用 / 需组合 / 需最小新增」 |
| **输出** | Reuse 清单 + Gap 清单（每项对应验收标准） |
| **进入下一步** | 已穷尽合理复用；Gap 为真正缺口 |

禁止：未搜索就新建「公共层 / 工具类 / 第二套 XXX Core」。

---

## 4. Implement

**目的：** Smallest Valid Change。

| | |
|--|--|
| **AI** | 仅在批准范围内改代码；保持包名与边界；不顺手重构无关文件 |
| **人** | 批准文件范围；拒绝 Big-Bang 与 Future-Proofing |
| **输出** | 最小 diff + 必要时配套测试代码 |
| **进入下一步** | 实现覆盖 Gap，且无越界文件 |

默认落点：

- Demo 需求 → Demo / 应用装配层优先  
- 确需改母域 → 单母域内最小改动  
- 租户能力 → 用 SaaS Core，不复制 TenantContext  

禁止：升级依赖、改无关 POM、改 SQL 表结构「顺便优化」、引入 MQ/微服务/插件体系。

---

## 5. Test

**目的：** Test Before Claim。

| | |
|--|--|
| **AI** | 补充/运行相关单测；解释失败原因；在范围内修复 |
| **人** | 规定必须覆盖的场景（尤其钱、库存、状态、租户） |
| **输出** | `mvn` 相关模块或全仓测试结果（PASS/FAIL） |
| **进入下一步** | 约定范围内测试 PASS |

至少自问：

- 非法状态 / 非法参数是否拒绝？  
- 无租户 / 跨租户是否拒绝？  
- 幂等重复请求行为是否正确？  

---

## 6. Validate

**目的：** Evidence Before Claims（真实验证层）。

| | |
|--|--|
| **AI** | 按人要求跑 MySQL 集成测、并发场景、Probe；记录命令与结果 |
| **人** | 决定是否需要 REAL MySQL / 并发 / 登录后 Probe；Agent 是否跑 Real Model E2E |
| **输出** | 验证记录（环境、命令、结论）；未跑项明确标 **NOT RUN** |
| **进入下一步** | 人要求的验证项全部有结论（PASS 或诚实 NOT RUN） |

硬规则：

- 并发 / 超卖 / 超扣 → 无 REAL MySQL 证据不得声称「已验证」  
- Agent Real Model E2E → 无 Key 则 **skip / NOT RUN**，不得写成已完成线上验收  
- Probe → 开发验证用，≠ 正式业务 API  

---

## 7. Review

**目的：** 边界与风险复核。

| | |
|--|--|
| **AI** | 对照 checklist 自检；标出可疑跨域依赖、过大 diff、未测路径 |
| **人** | 最终 Review；可要求缩小范围或补测 |
| **输出** | Review 结论（PASS / 退回某步） |
| **进入下一步** | Review PASS |

Checklist（节选）：

- [ ] 未新建第六母域 / 未把 Deferred 写成已完成  
- [ ] 未跨母域 import Entity/Mapper/Service  
- [ ] 未改 Foundation 塞业务对象  
- [ ] 未无批准改 Core  
- [ ] 无 Big-Bang Refactor  
- [ ] 文档/注释未夸大  

---

## 8. Acceptance

**目的：** 对照 Understand 的 Acceptance Criteria 验收。

| | |
|--|--|
| **AI** | 逐条对照 AC，给出证据链接（测试名、命令、结果） |
| **人** | 最终验收签字（口头/书面均可） |
| **输出** | Accepted / Rejected |
| **进入下一步** | Accepted → STOP；Rejected → 退回相应步骤 |

---

## STOP

验收通过后：

```text
STOP
```

- 不继续「顺手」加功能  
- 不开始未批准的 Phase  
- 不把 Demo 里的一次性逻辑未经证据沉淀进 Core  

若后续真实项目再次碰到同一缺口 **≥ 可重复次数**，再走完整流程，由人决定是否最小回写 OpenBiz Core。

---

## 快速对照表

| 步骤 | 一句话 |
|------|--------|
| Understand | 问题与验收标准说清 |
| Architecture | 归域；默认不改 Core |
| Reuse | 先搜后写 |
| Implement | 最小 diff |
| Test | 测试 PASS |
| Validate | 关键证据诚实记录 |
| Review | 边界复核 |
| Acceptance | AC 通过 |
| STOP | 停 |

---

## 与 Demo 开发的关系

```text
Phase 1.1（本文档）→ 定义方法
Phase 1.2          → 沉淀 Prompt（尚未开始）
Phase 2            → 真实 IoT Demo 验证方法
```

Demo 开发时：把本 workflow 当作强制清单；有效 Prompt 再写入 `prompts/`（当前目录**不存在，且本阶段不创建**）。
