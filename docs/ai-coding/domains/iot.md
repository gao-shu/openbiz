# OpenBiz IoT Domain AI Coding Guide

> 面向 Coding Agent：接到 IoT / 工业数字化 / 设备类需求时，如何判断、复用、禁止什么、何时改 Core、如何按证据验证。  
> 不是 IoT 模块说明书，不是 MQTT 教程，不是接口手册。  
> 事实以当前仓库代码为准；旧文档中的模块名可能过时。

与本文件一起阅读：`docs/ai-coding/README.md`、`docs/ai-coding/workflow.md`。

---

## 1. Domain 定位

IoT 是 OpenBiz 的 **IoT Mother Domain**。Maven 模块：`openbiz-iot`（**一个** jar）。

当前覆盖（包级逻辑边界，非独立 Maven module）：

| 逻辑层 | 包 | 角色 |
|--------|-----|------|
| Core | `com.openbiz.iot.core.*` | Product / Device / ThingModel / DeviceCommand / Protocol SPI / Mock |
| Protocol | `com.openbiz.iot.mqtt` | `MqttProtocolAdapter`（**Adapter Stub，无 Broker**） |
| Industry | `com.openbiz.access.*` | 门禁场景 |
| Industry | `com.openbiz.locker.*` | 储物柜场景 |
| Industry | `com.openbiz.charging.*` | 充电场景 |
| Industry | `com.openbiz.mes.*` | MES 场景 |

**Industry 是 Core 之上的业务场景，不是 Core 的一部分。**

禁止仅为「物理隔离更好看」而重新拆分 `openbiz-iot` Maven module。同 jar 内边界靠包约定 + 本指南维护。

旧文档中可能出现的名称（**已过时，不要按它们建目录**）：

`openbiz-iot-core` / `openbiz-iot-mqtt` / `openbiz-smart-access` / `smart-access`

当前正确入口：模块 `openbiz-iot`；包见上表。

---

## 2. Core / Industry / Demo 三层边界

### Core（`com.openbiz.iot.core` + `com.openbiz.iot.mqtt`）

| 负责 | 不负责 |
|------|--------|
| `ProductService` / `DeviceService` / `ThingModelService` / `DeviceCommandService` | Access / Locker / Charging / MES 业务策略 |
| 领域实体：`OpenbizProduct` / `OpenbizDevice` / `OpenbizThingModel` / `OpenbizDeviceCommand` | Demo 展示文案与页面编排 |
| `ProtocolPort` / `ProtocolPortRegistry` | 行业权限、柜格、充电会话、MES BOM |
| `MockDoorProtocolPort` / `MockDoorDeviceStore`（可测 mock） | 生产级 Broker 运维 |
| `TenantGuard` + tenant-scoped Mapper 查询 | 复制一套租户框架 |

### Industry（access / locker / charging / mes）

| 负责 | 默认不得 |
|------|----------|
| 行业实体、规则、场景编排 | 直接调用 `ProtocolPort` |
| 调用 Core API（尤其 `DeviceCommandService`） | 直接操作 MQTT / `MqttProtocolAdapter` |
| 行业 `*.api.*` Service | 直接操作 `MockDoor*` |
| | 绕过 `DeviceCommandService` 下发设备命令 |
| | 随意依赖其他 Industry 包 |

行业实现已有注释约定（如 Access）：permission / 业务校验后只走 `DeviceCommandService`。

### Demo（未来 `openbiz-iot-demo` 等）

| 负责 | 不应重新实现 |
|------|--------------|
| Controller、DTO、展示型 API | `DeviceService` / `DeviceCommandService` |
| Demo 场景编排、组装已有能力 | `ProtocolPort` / MQTT Client |
| 应用层组合 Industry + Core | 第二套设备命令链 |
| | Industry「内核」再造 |

Probe（如 `IotProbeController` → `/openbiz/test/iot/**`）仅为验证端点，**不是**正式业务 API，Demo 勿以其为产品契约扩展。

---

## 3. AI 面对 IoT 需求时的判断顺序

必须按序回答：

1. 这是 **Core / Industry / Demo** 哪一类？
2. 现有 Product / Device / ThingModel / Command 是否已支持？
3. 是否已有对应 `*.api.*` Service？
4. 能否直接复用？
5. 能否组合已有能力解决？
6. 是否只是 Demo 展示 / 场景问题？
7. 若都不能解决，是否**真的**需要改 `openbiz-iot`？
8. 修改前是否有明确真实需求与证据？
9. 能否用**最小改动**解决？
10. 是否有对应测试与验证方案？

原则：

```text
Existing Capability First
Reuse Before Abstraction
Smallest Valid Change
```

「未来可能需要」**不能**作为改 Core 的理由。

---

## 4. 能力复用地图

先搜包：`com.openbiz.iot.core.api` → 对应 Industry `*.api` → 再判断 Gap。

| 需求类型 | 首先搜索 | 推荐复用 | 不应该做 |
|----------|----------|----------|----------|
| 产品/设备注册 | `ProductService` / `DeviceService` | `OpenbizProduct` / `OpenbizDevice` + `TenantGuard` + `*AndTenant` 查询 | 新建第二套设备体系 / 表 |
| 物模型 | `ThingModelService` | `OpenbizThingModel` | 在 Industry 重建物模型 |
| 设备下行命令 | `DeviceCommandService` | `invoke(deviceId, serviceId, paramsJson, idempotentKey)` | 直接调 `ProtocolPort` / MQTT |
| 协议适配 | `ProtocolPort` / `ProtocolPortRegistry` | 现有实现：`MockDoorProtocolPort`；`MqttProtocolAdapter`（stub） | Demo 内自建 MQTT Client 旁路 |
| 门禁 | `AccessService` | 行业服务 + `DeviceCommandService` | 自建开门命令链 |
| 储物柜 | `LockerService` | 行业服务 + Core | 新建 `DeviceCommandService` |
| 充电 | `ChargingService` | 行业服务 + Core | Industry 直接操作协议 |
| MES | `MesService` | MES 场景 + Core | 新造 Device/Command 模型 |
| HTTP 探测 | `IotProbeController` | 仅本地/联调验证 | 当正式业务 API 扩展 |

实体与命令表前缀以当前 SQL / domain 为准（如 `openbiz_device_command`）。

---

## 5. 必须理解的核心调用链

设备控制类需求，原则上必须进入 `DeviceCommandService`：

```text
Industry Service / Demo 编排
        ↓
DeviceCommandService.invoke(...)
        ↓
TenantGuard.requireTenantId()
+ Device / Product 按 tenant 校验
        ↓
ProtocolPortRegistry.require(product.protocol)
        ↓
ProtocolPort.sendCommand(...)
        ├─ MockDoorProtocolPort   （可测 mock）
        └─ MqttProtocolAdapter    （stub：无 Broker，accept 并 return true）
        ↓
OpenbizDeviceCommand 状态 PENDING → SUCCESS / FAILED
```

**禁止** Industry / Demo 建立旁路：

```text
Industry / Demo  ──?──→  ProtocolPort / MQTT / MockDoor
```

### MQTT 事实（勿夸大）

- `MqttProtocolAdapter`：**Adapter Stub**，代码注释写明 *Still no broker*。
- **当前没有**真实 MQTT Broker 验证。
- **禁止**写成：「OpenBiz 已支持生产级 MQTT」。

---

## 6. 禁止事项

### 绝对禁止

- 新建第二套 `DeviceService` / `DeviceCommandService`
- Industry 直接调用 `ProtocolPort` / MQTT / `MockDoor*`
- Demo 自己实现完整设备命令链
- 仅为「看起来更合理」修改 Maven 架构 / 拆 module
- 无证据增加基础设施（Broker、消息总线、新中间件等）
- 把 Mock 当真实设备；把 Adapter Stub 当生产 MQTT
- 把 Mockito 单元测试当成 REAL MySQL / 真设备验证
- 按过时文档名创建 `openbiz-iot-core` / `smart-access` 等目录

### 默认禁止（除非有明确证据与人批准）

- Industry 之间直接依赖（同 jar **编译挡不住**，靠约定）
- 新增抽象层 / 新增 Protocol 抽象「为了未来」
- 修改 Core；拆分 Maven module
- 增加新的基础设施

### 有真实需求和证据后允许

- 新增 `ProtocolPort` 实现（注册到 Registry）
- 新增 Industry 能力（包内实体 + 调 Core）
- 最小修改 Core；增加 REAL MySQL 集成测；增加真实协议/设备验证

流程必须是：真实需求 → 最小修改 → 测试 → 验证 → Review → Acceptance → STOP。

---

## 7. 验证要求 · Core 修改门槛 · 当前证据

### Tenant（原则，不绑死某个 Mapper 名）

租户边界必须由现有 **`TenantGuard` + tenant-scoped query mechanism** 保证；优先复用现有实现。  
不得复制 `TenantContext` 逻辑，不得绕过 tenant 条件。  
当前常见形态：`TenantGuard.requireTenantId()` + Mapper 的 `selectByIdAndTenant` / `selectByTenant` 等——**以实际代码为准**。

### 当前已有证据

| 类型 | 说明 |
|------|------|
| Core Service 单元测试 | Product / Device / ThingModel / DeviceCommand（Mockito） |
| Protocol 路由 | `ProtocolPortRegistryTest` |
| MockDoor 命令链路 | `DeviceCommandMockDoorFlowTest` 等 |
| Industry | Access / Locker / Charging / MES 业务 Mockito 测试 |
| Tenant | 无租户拒绝、按 tenant 查询的单元层覆盖 |

### 当前没有 / NOT PROVEN

| 项 | 状态 |
|----|------|
| REAL MySQL IoT 集成 | **无** |
| REAL MySQL IoT 并发 | **无**（勿套用 Business/Shop 话术） |
| 真实 MQTT Broker | **无** |
| 真实设备 | **无** |
| 命令幂等完整闭环 | **NOT PROVEN** |

**幂等事实（只记录，本指南不要求修复）：**

- API 有 `idempotentKey` 参数；命令表有字段；存在普通索引。
- **没有** `(tenant_id, device_id, idempotent_key)` 级 UNIQUE 闭环。
- `DeviceCommandServiceImpl` **没有**完整「重复请求拦截 / 幂等返回」逻辑。
- 因此：**IoT 命令幂等性 = NOT PROVEN**。存在字段 ≠ 已实现幂等。

### 验证分级（写作与验收时必须诚实）

| 等级 | 含义 |
|------|------|
| Unit / Mockito | 逻辑与调用约定 |
| MockDoor 链路 | 命令经 Registry → Mock Port |
| REAL MySQL | IoT 当前 **未建立** 等同 Business/Shop 的套件 |
| 真 Broker / 真设备 | 当前 **未建立** |

### 何时允许修改 `openbiz-iot`

```text
IoT 需求
  → 已有 Core 能力？ ──YES──→ 直接复用
  → NO → Industry 能解决？ ──YES──→ 只改 Industry
  → NO → Demo 组合能解决？ ──YES──→ 只改 Demo
  → NO → 真实重复需求？ ──NO──→ STOP
  → YES → 明确架构缺口？ ──NO──→ STOP
  → YES → 最小修改 openbiz-iot
  → 测试 → 验证 → Review → Acceptance → STOP
```

优先扩展方式：新 `ProtocolPort` 实现，或 Industry 包内实体 + 调 `DeviceCommandService`；避免改命令主干「为了通用化」。

---

## 8. 常见 AI Coding 错误

1. 绕过 `DeviceCommandService` 直接调 `ProtocolPort`
2. Demo 新建 `DeviceService` / `DeviceCommandService`
3. Industry 直接写 MQTT / 碰 `MqttProtocolAdapter`
4. Access 直接依赖 Locker / Charging / MES（或反向）
5. 把 `IotProbeController`（`/openbiz/test/iot`）当正式业务 API
6. 把 `MqttProtocolAdapter` 当真实 MQTT 生产能力
7. 复制 `TenantContext` / `TenantGuard` 或绕过 tenant 查询
8. 无证据增加 Broker、总线、新 IoT 基础设施
9. 按旧文档 `openbiz-iot-core` / `smart-access` 建错目录
10. 用 Mockito 结果宣称 REAL MySQL / 真设备能力
11. 见到 `idempotentKey` 就宣称「已实现幂等」
12. 「未来多协议」驱动 Core 大抽象，而非按需加 Port 实现

---

## 一页速记

```text
搜 api → 复用 Service → 命令必须 DeviceCommandService
Industry 不碰 Protocol/MQTT/MockDoor
同 jar 禁跨 Industry 乱依赖
MQTT = stub；幂等 = NOT PROVEN；IoT REAL MySQL = 无
改 Core 最后手段；Demo 只编排不重造
证据分级诚实写；旧文档模块名勿信
```
