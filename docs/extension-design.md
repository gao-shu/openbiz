# 扩展设计：储物柜 / 充电 / MES

> Spec v0.1 · 证明「母项目复用」而非复制代码  
> 原则：**只规划差异点**；不提前实现这些模块

---

## 0. 复用判定标准

某个能力算「可复用」，必须同时满足：

1. 落在 `saas-core` / `iot-core`（或未来 `business-order`）公共模块内  
2. 行业模板 **只依赖接口与物模型 ID**，不复制命令/租户/设备表  
3. 新增行业时，diff 以「新表 + 新物模型 + 新业务服务」为主，而不是 fork 一套 IoT

若发现要复制 `iot_device` / `DeviceCommandGateway`，说明边界失败，先修架构再扩行业。

---

## 1. MVP-1 已沉淀的可复用资产

| 资产 | 模块 | 后续谁用 |
|------|------|----------|
| 租户 / 会员 / 客户 | saas-core | 全部模板 |
| RBAC / 菜单 / 日志 | RuoYi | 全部模板 |
| Product / Device / ThingModel | iot-core | 全部 IoT 类模板 |
| Command 幂等 / 超时 / 快照 | iot-core | 全部 IoT 类模板 |
| MQTT Adapter | iot-mqtt | 门禁/柜/充电（协议未换前） |
| 模拟器骨架 | device-simulator | 各行业换物模型即可 |
| 小程序登录模式 | saas + 模板 | 柜/充电可复用登录，不复用开门 API |

---

## 2. Smart Locker（智能储物柜）

### 2.1 业务差异（相对门禁）

| 点 | 门禁 | 储物柜 |
|----|------|--------|
| 设备拓扑 | 1 设备 ↔ 1 门点 | 1 设备 ↔ **N 格口** |
| 核心动作 | `open_door` | `open_cell`（带 `cellNo`） |
| 业务状态 | 门开/关 | 格口 空闲/占用/故障 + 柜体在线 |
| 权限模型 | 人 ↔ 门 | 人 ↔ 柜/格口（常为「扫码开自己的格」） |
| 订单 | 通常无 | **可选**：租用/超时计费（可后置） |

### 2.2 直接复用

- `saas-core`：Tenant、Member、Customer  
- `iot-core`：Product、Device、ThingModel、Command、Snapshot、EventLog  
- `iot-mqtt`：Topic 约定与 Adapter（payload 多一个 `cellNo`）  
- RuoYi 管理端壳、租户隔离拦截器  

### 2.3 需要新增（仅模板层 + 少量 IoT 配置）

| 新增 | 归属 | 说明 |
|------|------|------|
| `lck_cabinet` | smart-locker | 柜体业务视图（可 1:1 绑 `iot_device`） |
| `lck_cell` | smart-locker | 格口：cell_no、status、绑定 member/order |
| `lck_open_record` | smart-locker | 开格记录（类似 AccessRecord） |
| 物模型 | IoT 配置数据 | 产品「智能储物柜」：属性 `cells[]` 或按格上报；服务 `open_cell` |
| 小程序 API | smart-locker | 开格 / 我的格口 / 记录 |

### 2.4 刻意不改 / 少改

- **不** 复制 `iot_device` 表  
- **不** 让 locker 依赖 MQTT 客户端  
- AccessPoint「一门一设备」约束 **留在门禁模块内**；储物柜用自己的 cell 模型，不要硬改 `acc_*` 成万能点位表（避免过早通用化）

### 2.5 复用观察（假说，非 KPI）

| 层 | 说明 |
|----|------|
| 平台 + SaaS + IoT Core | 预期大量直接复用；**以实测为准** |
| 管理端基础设施 | 预期高复用 |
| 行业业务 / 小程序 | 预期多为新增 |
| 对外数字 | 禁止在 reuse-matrix 实测前宣称百分比 |

验证成功标志：新增模块主要是 `lck_*` + 物模型 JSON + 开格 AppService，**零** 新建命令通道。

---

## 3. Charging（充电桩）

### 3.1 业务差异

| 点 | 储物柜 | 充电 |
|----|--------|------|
| 会话 | 开格即短事务 | **长会话**：启动 → 充电中 → 结束 |
| 计量 | 可选 | **必选**：电量 / 时长 |
| 订单支付 | 可后置 | **MVP 核心**：Order + 计费 +（可先模拟）Payment |
| 协议 | MQTT 够用 | 长期可能 OCPP；**第一版仍可 MQTT 模拟桩** |
| 设备状态 | 开/关/占用 | IDLE / CHARGING / FAULT / OFFLINE 等 |

### 3.2 直接复用

- 全部 MVP-1 IoT / SaaS / 租户隔离  
- 储物柜阶段若已有「设备多点位」经验，可类比「一桩多枪」：`chg_connector` ≈ `lck_cell`  
- Command 幂等：`start_charge` / `stop_charge`  

### 3.3 需要新增

| 新增 | 归属 | 说明 |
|------|------|------|
| `openbiz-business-order`（或 `ob_order*`） | L1 Business | 订单头/明细、状态机、金额 |
| `ob_payment*`（可先 Mock） | L1 | 支付单、回调；第一版允许「余额/模拟支付」 |
| `chg_station` / `chg_pile` / `chg_connector` | charging 模板 | 站/桩/枪 |
| `chg_session` | charging | 充电会话，关联 order_id、起止 SOC/电量 |
| `chg_tariff` | charging | 简单计费规则（先固定单价） |
| 物模型 | IoT 配置 | `start_charge` / `stop_charge`；属性 power、energy、status |
| （更后）`openbiz-iot-ocpp` | Adapter | **仅当** 真实 OCPP 桩接入时；业务不依赖 OCPP |

### 3.4 明确仍不做（充电第一版）

- 完整财务 / 发票 / 分账  
- 互联互通平台对接  
- 一上来就上 OCPP 全家桶（先 MQTT 模拟跑通订单闭环）

### 3.5 复用率评估

| 层 | 预估复用 |
|----|----------|
| SaaS + IoT Core | **65–75%** |
| 相对门禁+柜已有代码 | **50–60%** |
| 新建（订单/支付/会话/计费） | **40–50% 增量代码** |
| **整体有效复用** | **约 45–55%** |

验证成功标志：充电业务只新增 Order/Session/Tariff；启停充电仍走 `DeviceCommandGateway`；若日后换 OCPP，**只加 Adapter**。

---

## 4. MES（制造执行）

### 4.1 为何放到第四阶段

- 主数据（物料/BOM/工艺）与 IoT 设备模型正交且更重  
- 工单、质量、追溯状态机复杂，过早做会拖死「先跑通门禁」的节奏  
- 真正优势在 **IoT Core + MES Core 粘合**，前提是 IoT 已在前三个母项目打磨稳定

### 4.2 直接复用

| 资产 | MES 用法 |
|------|----------|
| iot Device / 属性快照 / 事件 | 产线设备采集、停机、计数 |
| saas Tenant | 工厂/租户隔离 |
| RuoYi 权限与字典 | 工单状态等字典 |
| （若充电已建）Order 模式 | **不要** 强行复用充电订单；MES 用工单，至多借鉴状态机写法 |

### 4.3 需要新增（MES Core，不是模板里糊）

| 模块/表 | 说明 |
|---------|------|
| Material / BOM | 物料与物料清单 |
| Process / Operation | 工艺路线与工序 |
| WorkCenter | 工作中心（可绑定 Device） |
| WorkOrder / Task | 工单与生产任务 |
| Quality | 质检记录 |
| Traceability | 批次/序列号追溯 |
| OEE（可后置） | 开动率等，依赖设备事件 |

模板 `templates/mes` 只做行业默认工艺与看板；**核心表在 `openbiz-mes-core`**。

### 4.4 IoT ↔ MES 粘合方式（保持解耦）

```text
MES WorkOrder / Task
        │  绑定 workCenter.deviceId（可选）
        ▼
IoT DeviceEvent / PropertySnapshot
        │  应用服务订阅 DeviceEventPort
        ▼
更新工序实际产量 / 停机记录
```

- MES **不** 解析 MQTT  
- IoT **不** 知道工单号（关联放在 MES 侧映射表）

### 4.5 复用率评估

| 层 | 预估复用 |
|----|----------|
| SaaS + RuoYi | **40–50%** |
| IoT Core（采集与命令） | **30–40%** 的 MES 总工作量依赖它，但 MES 自身代码增量大 |
| MES 新建代码占比 | **60–70%** |
| **整体有效复用（相对平台已有）** | **约 30–40%** |

说明：MES 复用率数字会低于柜/充电，这是正常的——价值在「设备层不重做」和「工业 IoT + MES」组合能力，而不在 CRUD 抄表。

---

## 5. 对照总表：复用 vs 新增

| 能力 | 门禁 | 储物柜 | 充电 | MES |
|------|------|--------|------|-----|
| RuoYi RBAC | 复用 | 复用 | 复用 | 复用 |
| Tenant / Member | 复用 | 复用 | 复用 | 复用（角色偏 B 端） |
| Product/Device/Model | 复用 | 复用 | 复用 | 复用（产线设备） |
| MQTT Adapter | 复用 | 复用 | 复用（模拟） | 复用（采集） |
| AccessPoint/Permission | 本模板 | — | — | — |
| Cell/Cabinet | — | **新增** | 可参考枪模型 | — |
| Order/Payment/Billing | — | 可选 | **新增** | 不复用充电订单 |
| OCPP Adapter | — | — | 后期新增 | — |
| BOM/工单/质量/追溯 | — | — | — | **新增 MES Core** |
| Rule Engine / OTA | 不做 | 按需 | 按需 | 按需 |

---

## 6. 扩展时的禁止事项

1. **禁止** 为了柜/充电去改门禁表成「万能业务点位」大表（过早抽象）。  
2. **禁止** 在模板模块内 new MQTT client。  
3. **禁止** 复制 `iot_device_command` 为 `lck_command` / `chg_command`。  
4. **禁止** 第一阶段就建空的 `mes-*`、`parking-*` Maven 模块。  
5. **禁止** 未验证门禁→柜复用前，并行开工充电与 MES。

---

## 7. 阶段性证明链（架构是否成立）

```text
① 门禁跑通     → 证明 L0+L1+L2+模板 能落地
② 柜复用 IoT   → 证明「多点位」只加模板表，不 fork IoT
③ 充电加 Order → 证明 Business Core 可挂到同一 IoT
④ MES 挂采集   → 证明工业场景吃同一 Device/Event
```

任一步失败：停扩行业，先修边界与文档，再编码。
