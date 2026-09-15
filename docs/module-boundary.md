# 模块边界与依赖

> Spec v0.1 · 与 `architecture.md` 配套

---

## 1. 边界总表（第一阶段）

| 层级 | 模块 | 职责 | 不负责 |
|------|------|------|--------|
| L0 | `ruoyi-*` | 账号体系、RBAC、部门数据权限、字典、参数、日志、文件、定时任务 | 租户、设备、行业业务 |
| L1 | `openbiz-saas-core` | Tenant、Member、TenantContext、Resolver、Interceptor（Phase 1.2 已落地）；未来 Customer / C 端 Member | IoT、开门业务、支付、SQL 自动租户过滤（尚未做） |
| L2 | `openbiz-iot-core` | Product / Device / ThingModel / Command / PropertySnapshot / DeviceLog / 领域接口 | MQTT 细节、门禁策略 |
| L2 | `openbiz-iot-mqtt` | MQTT 连接、Topic 约定、上下行编解码、在线感知 | 业务鉴权、行业实体 |
| L3 | `openbiz-smart-access` | 门点、通行权限、开门申请、通行记录、小程序开门 API | 直接操作 Broker |
| 工具 | `openbiz-device-simulator` | 模拟设备状态机与回执 | 生产部署依赖 |

---

## 2. RuoYi 复用边界

### 2.1 直接复用（不要再包一层「假 Core」）

- `SysUser` / `SysRole` / `SysMenu` / `SysDept` / `SysDict*` / `SysConfig`
- `@PreAuthorize` / 数据权限 `@DataScope`
- `SysOperLog` / `SysLogininfor`
- 通用文件上传 API
- Quartz 任务注册

### 2.2 允许的薄扩展（放 saas-core，不改坏 RuoYi 内核优先）

- 表增加 `tenant_id`（或通过拦截器自动注入条件）
- `LoginUser` 扩展租户字段
- 租户管理员 vs 平台超管的角色约定（字典/角色编码）

### 2.3 禁止

- 复制一套 `sys_user` 叫 `biz_user` 做后台运营账号  
- 在 IoT 模块里做菜单权限  
- 在 Smart Access 里解析 MQTT Payload

---

## 3. SaaS Core 边界（MVP 最小集）

### 3.1 必须有

| 能力 | 说明 |
|------|------|
| Tenant | 租户 CRUD、启用/停用、编码 |
| TenantContext | 请求线程内当前 `tenantId`；平台超管可切换 |
| 数据隔离 | MyBatis 拦截器或 Wrapper 自动带 `tenant_id` |
| Customer | 客户（企业/门店主体，可极简：名称+联系人） |
| Member | C 端会员：绑定租户、openId/手机号、状态 |

### 3.2 第一阶段不做

- 套餐计费、配额、租户套餐商城  
- 完整组织多法人、多品牌  
- Order / Payment / Inventory / Reservation / Workflow  

> Order 在门禁 MVP **不是刚需**（开门不是下单）。放到充电 MVP 再引入 `business-order`（或 saas 下 order 子包）。避免「为了架构完整」空建 Order。

---

## 4. IoT Core 边界

### 4.1 必须有（MVP）

| 能力 | 说明 |
|------|------|
| Product | 产品定义、协议类型（暂仅 MQTT） |
| Device | 设备、SN、归属产品/租户、在线状态、业务状态 |
| Thing Model | 属性 / 事件 / 服务 定义（JSON Schema 级即可） |
| DeviceCommand | 下行命令、幂等键、超时、状态 |
| PropertySnapshot | 最新属性（轻量 Shadow） |
| DeviceEventLog / DeviceLog | 上行事件与运维日志 |
| DeviceAuth | deviceName + secret / 证书占位（MVP 用密钥） |
| `DeviceCommandGateway` | **接口**：`invoke(serviceId, params, idempotentKey)` |

### 4.2 明确推迟

| 能力 | 推迟到 |
|------|--------|
| Device Group / Gateway 拓扑 | 储物柜或充电站有多级设备时 |
| Alarm / Rule Engine | 有真实告警需求时 |
| OTA | 有固件升级需求时 |
| 多协议 Adapter | 充电/工业阶段按需加 |
| 完整 Device Shadow 文档合并 | 有冲突合并需求时 |

---

## 5. Smart Access 边界

### 5.1 属于门禁业务的实体

- `AccessPoint`：门点（通常 1 设备 ↔ 1 门；可挂 `deviceId`）  
- `AccessPermission`：会员/角色 ↔ 门点 ↔ 时段  
- `AccessRecord`：开门结果流水（谁、哪扇门、何时、成功/失败原因）  
- 开门应用服务：鉴权 → 调 IoT `open_door` → 记记录  

### 5.2 不属于门禁、必须下沉的

| 表象 | 正确归属 |
|------|----------|
| door_status / battery | 物模型属性（IoT） |
| open_door 服务 | 物模型服务（IoT） |
| MQTT Topic | Adapter |
| 租户隔离 | SaaS |
| 后台登录账号 | RuoYi |

---

## 6. 依赖规则（可检查清单）

### 6.1 允许的依赖

```text
ruoyi-admin
  → smart-access, iot-mqtt, iot-core, saas-core, ruoyi-*

smart-access
  → iot-core, saas-core, ruoyi-common（工具/注解）

iot-mqtt
  → iot-core, （可选）saas-core 仅用于租户感知路由

iot-core
  → saas-core, ruoyi-common

saas-core
  → ruoyi-system / ruoyi-common
```

### 6.2 禁止的依赖（CI 可用 ArchUnit / 手工 Review）

```text
iot-core      ↛  smart-access
iot-core      ↛  iot-mqtt
saas-core     ↛  iot-* / smart-access
smart-access  ↛  iot-mqtt / Eclipse Paho / HiveMQ client
ruoyi-system  ↛  openbiz-*   （保持上游干净；扩展用扩展点）
```

### 6.3 替换协议时的期望 diff

增加 `openbiz-iot-http` 或 `openbiz-iot-tcp` 时：

- **改**：`ruoyi-admin` 的 Maven 依赖与配置  
- **不改**：`smart-access` 业务代码  
- **可能改**：物模型 service 的超时参数（配置级）

---

## 7. 包名约定（建议）

```text
com.openbiz.saas.*
com.openbiz.iot.core.*
com.openbiz.iot.mqtt.*
com.openbiz.access.*
com.openbiz.sim.*
com.ruoyi.*          # 不改官方包名
```

---

## 8. 数据库 schema 边界

| 前缀 | 归属 |
|------|------|
| `sys_*` | RuoYi |
| `ob_tenant*` / `ob_customer` / `ob_member` | SaaS |
| `iot_*` | IoT Core |
| `acc_*` | Smart Access |

同一 MySQL 库、同一数据源即可（第一阶段）。**不做** 按租户分库。

---

## 9. 模块间协作时序（开门）

```text
AccessAppService
  1. 校验 Member + AccessPermission + 时段
  2. 生成 clientRequestId（幂等）
  3. deviceCommandGateway.invoke(deviceId, "open_door", {}, clientRequestId)
  4. 写 acc_access_record(PENDING)
  5. 监听 DeviceCommand 终态 / 超时 → 更新 record
```

IoT 内部：

```text
CommandService → 持久化 iot_device_command
             → ProtocolPort.publish(...)
MqttAdapter  → broker
Simulator    → 回执 event/property
MqttAdapter  → CommandService.onAck / PropertyService.onReport
```

业务层只关心第 1–5 步，不关心 Topic。
