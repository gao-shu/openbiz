# 路线图与 MVP 定义

> Spec v0.1 · 严格串行 · 前一步未证明复用则不进入下一步

---

## 0. 总原则

```text
先做可用的骨架 → 1 个完整母项目 → 验证复用 → 再扩展
```

- **不** 并行铺 10 个模板空壳  
- **不** 微服务  
- **不** 在本仓库 AI 项目上「改造」出 Java 平台  
- 工作区现状：`cangku` **无** RuoYi 源码；平台从 `open-biz-platform` **新建**（可先作为子目录，稳定后独立 GitHub 公开仓）

---

## 1. Phase 总览

| Phase | 名称 | 状态 | 退出条件 |
|-------|------|------|----------|
| 0 | 项目规范 + 选基座 | **完成** | Spec 齐套；官方 RuoYi SB3；reuse-matrix |
| 1 | RuoYi Base + OpenBiz 骨架 | **完成** | 见 phase1-report |
| 1.2 | SaaS Core 实现 | **完成** | 见 phase1.2-report |
| 1.3 | IoT Core 落库 | **完成** | 见 phase1.3-report |
| 1.4 | 模拟门禁设备 | **完成** | 见 phase1.4-report |
| 1.5 | 最小智能门禁业务 | **完成** | 见 phase1.5-report |
| 1.6 | Cross-Industry Reuse — Locker | **完成** | Core 0 改；复用 open_door；见 phase1.6-report |
| 1.7 | Cross-Industry Reuse — Charging | **完成** | Core 0；open_door；Connector↔Command；见 phase1.7-report |
| 1.8 | Cross-Industry Reuse — MES | **完成** | Core 0；Material/BOM/Process/WO；见 phase1.8-report |
| 1.9 | 减法审计（Read-Only） | **完成** | 零改代码；见 phase1.9-subtraction-audit |
| 2.0 | Java 21 + E2E 封板 | **完成** | v1.0 FROZEN；见 phase2.0-java21-e2e-report |
| 5 | Smart Locker | 锁定 | 柜模块无 MQTT 依赖；复用率达标 |
| 6 | Charging | 锁定 | 订单+启停充电闭环（可模拟支付） |
| 7 | MES Core | 锁定 | 工单与设备事件打通 |
| 8–10 | 停车 / 物流仓储 / 零售健身预约 | 远景 | 仅当 5–7 复用成功 |

**OpenBiz IoT v1.0 FROZEN（Java 21 + E2E 全绿）。封板后仅 Bug/Security/真实客户需求；不做 Telemetry/第五行业/小程序。**

---

## 2. Phase 0（现在）检查清单

- [x] 只读分析：确认无现成 Java/RuoYi  
- [x] `architecture.md`  
- [x] `module-boundary.md`  
- [x] `database-design.md`  
- [x] `iot-core-design.md`  
- [x] `smart-access-design.md`  
- [x] `extension-design.md`  
- [x] `roadmap.md`（本文）  
- [x] **决策**：官方 RuoYi-Vue（SB3）vs Plus → **已选官方 + 自研薄租户**  
- [x] **决策**：JDK 17 + Spring Boot 3（实际导入 3.5.16 / RuoYi 3.9.2）  
- [ ] 独立 GitHub 仓公开（可稍后；当前在 `cangku/open-biz-platform`）  
- [x] Coding rules + reuse-matrix  

**Phase 1 骨架完成后**：下一刀是 Phase 1.2 SaaS Core 实现，不是门禁业务。

---

## 3. MVP 定义与复用假设

> **不要把预估百分比当成 KPI。**  
> 假设目标：后续项目能复用大量 L0–L2；**具体复用率以 [reuse-matrix.md](reuse-matrix.md) 实测为准。**

### MVP-1：门禁闭环（骨架后的 1.2–1.6）

**只实现：**

1. RuoYi 原样：用户/角色/菜单/权限/部门/字典/日志/文件/定时任务  
2. SaaS：Tenant、TenantUser、Customer、Member、租户拦截  
3. IoT：Product、Device、ThingModel、Command、Snapshot、EventLog  
4. MQTT Adapter + Broker（本地 Mosquitto 即可）  
5. Device Simulator（门禁物模型）  
6. Smart Access：AccessPoint、Permission、Record、小程序开门 API  
7. 管理端：上述资源的最小 CRUD 页  

**不实现：** Order、Payment、Locker、Charging、MES、Rule、OTA、多协议。

| 评估项 | 说明 |
|--------|------|
| 目标 | 链路跑通 |
| 相对「从零门禁」 | 本阶段无历史复用（绿地） |
| 复用率 | **不预设数字**；门禁跑通后在 reuse-matrix 记第一行 |

---

### MVP-2：智能储物柜（Phase 5）

**在 MVP-1 之上仅增加：**

1. 产品物模型：储物柜 + `open_cell`  
2. 模板模块：柜、格口、开格记录、小程序开格  
3. 模拟器：多格口状态  

**不增加：** 支付（可人工占柜）、新协议、MES。

| 评估项 | 预估 |
|--------|------|
| 代码复用率 | **55–65%** |
| 新建占比 | 格口模型 + 模板 API/页面为主 |
| 失败信号 | 又写一套 device/command 表或业务直接调 Paho |

---

### MVP-3：充电桩（Phase 6）

**在 MVP-2 之上增加：**

1. `business-order` + 简单计费  
2. Payment Mock（真支付可再迭代）  
3. 站/桩/枪 + 充电会话  
4. 物模型：`start_charge` / `stop_charge` + 能量属性  
5. 仍用 MQTT 模拟桩（OCPP 不进本 MVP） |

| 评估项 | 预估 |
|--------|------|
| 代码复用率 | **45–55%** |
| 新建占比 | Order/Session/Tariff 为最大增量 |
| 失败信号 | 充电命令绕过 IoT Core；或未经验证就上 OCPP |

---

### MVP-4：MES（Phase 7）

**增加：**

1. `openbiz-mes-core`：物料、BOM、工艺、工单、任务、质量、追溯（OEE 可后置）  
2. WorkCenter ↔ Device 绑定  
3. 订阅 IoT 事件更新产量/停机  
4. 行业默认模板与看板（薄） |

| 评估项 | 预估 |
|--------|------|
| 平台复用率 | **30–40%**（正常偏低） |
| 价值 | 工业 IoT 采集不重做；MES 域新建 |
| 失败信号 | MES 模块内写 MQTT；或 IoT 表塞进工单字段 |

---

## 4. 建议排期（弹性，按人周量级）

> 单人兼职/求职期节奏；仅作量级感，不是承诺。

| 阶段 | 量级 | 备注 |
|------|------|------|
| Phase 0 | 0.5–1 周 | 文档 + 决策 + 空仓 |
| Phase 1 | 0.5 周 | 导入 RuoYi，跑通登录 |
| Phase 2 | 1 周 | 租户最小闭环 |
| Phase 3 | 1.5–2 周 | IoT + MQTT + 模拟器 |
| Phase 4 | 1.5–2 周 | 门禁 + 小程序 |
| **MVP-1 小计** | **约 5–7 周** | 求职期可并行简历项目包装 |
| Phase 5 柜 | 2–3 周 | 验证复用 |
| Phase 6 充电 | 3–4 周 | Order 是大头 |
| Phase 7 MES | 6–8 周+ | 明显更重 |

---

## 5. 独立仓库建议

```text
GitHub: open-biz-platform（工作名，可改）
内容: 本目录 Spec + 后续 RuoYi 多模块代码
许可: 建议 Apache-2.0（若代码含 RuoYi，遵循其许可并保留署名）
```

与 `cangku` 内 AI 项目：**物理分离**，避免再陷入「大仓百怪」无法演示。

---

## 6. Coding Rules（Phase 1 起生效，摘要）

完整细则可另拆 `coding-rules.md`；此处为门禁：

1. 业务模块禁止依赖 MQTT 客户端库。  
2. 禁止复制 RuoYi 已有 User/Role/Menu。  
3. 新表必须带 `tenant_id`（平台表除外）并走拦截器。  
4. 下行设备动作必须经 `DeviceCommandGateway` + `idempotent_key`。  
5. 第一阶段单体模块化；禁止拆独立 IoT 微服务。  
6. 未在本 roadmap 当前 Phase 出现的模块，禁止建空 Maven 工程「占位」。  
7. 接口先跑通再抽象；禁止未调用的「通用框架」代码。  
8. 文档与代码冲突时，先更新 Spec 再改代码（MVP 阶段）。

---

## 7. 下一步（给人与 Cursor 的唯一任务）

**现在不要写业务代码。**

可选立即动作（仍属 Phase 0）：

1. 人工确认：RuoYi 官方 vs Plus  
2. 创建空的 GitHub 仓并只提交 `docs/` + README  
3. 认可本 Spec 后，下一会话任务变为：  
   > 「按 roadmap Phase 1：导入 RuoYi-Vue，保持 openbiz 模块空壳可编译，不实现业务。」

---

## 8. MVP 复用率一览（假说，非 KPI）

| 阶段 | 内容 | 说明 |
|------|------|------|
| MVP-1 | RuoYi + SaaS + IoT + 门禁 | 绿地；跑通后写入 reuse-matrix |
| MVP-2 | + 储物柜 | **实测后再填**；历史口头预估勿当目标 |
| MVP-3 | + 充电（Order/支付） | 同上 |
| MVP-4 | + MES Core | 同上 |

对外宣称「平均复用率 XX%」的前提：至少完成门禁 → 储物柜 → 充电三次实测。
