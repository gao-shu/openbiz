# 模块复用矩阵（Reuse Matrix）

> 用途：长期记录「哪些 Core 能力被哪些行业真正用到」。  
> **禁止虚构复用率**；未落地的行业栏用规划符号，实测后改为统计。

状态约定：

| 标记 | 含义 |
|------|------|
| VERIFIED | 该行业代码路径已实测复用 |
| EXPECTED | 规划预期，尚未用代码验证 |
| INDUSTRY-SPECIFIC | 行业专属，不进 Core |
| - | 本行业不需要 |

---

## 1. 能力矩阵（截至 Phase 1.8）

| Capability | Access | Locker | Charging | MES | Parking |
|------------|:------:|:------:|:--------:|:---:|:-------:|
| RuoYi Foundation | VERIFIED | VERIFIED | VERIFIED | VERIFIED | EXPECTED |
| SaaS Tenant | VERIFIED | VERIFIED | VERIFIED | VERIFIED | EXPECTED |
| SaaS Member | VERIFIED | VERIFIED | VERIFIED | VERIFIED | EXPECTED |
| TenantContext | VERIFIED | VERIFIED | VERIFIED | VERIFIED | EXPECTED |
| TenantResolver | VERIFIED | VERIFIED | VERIFIED | VERIFIED | EXPECTED |
| TenantInterceptor | VERIFIED | VERIFIED | VERIFIED | VERIFIED | EXPECTED |
| IoT Product | VERIFIED | VERIFIED | VERIFIED | VERIFIED | EXPECTED |
| IoT Device | VERIFIED | VERIFIED | VERIFIED | VERIFIED | EXPECTED |
| IoT ThingModel | VERIFIED | VERIFIED | VERIFIED | VERIFIED | EXPECTED |
| IoT DeviceCommand | VERIFIED | VERIFIED | VERIFIED | VERIFIED | EXPECTED |
| ProtocolPort | VERIFIED | VERIFIED | VERIFIED | VERIFIED | EXPECTED |
| Mock Protocol / Device | VERIFIED | VERIFIED | VERIFIED | VERIFIED | EXPECTED |
| AccessPermission / AccessRecord | INDUSTRY-SPECIFIC | - | - | - | - |
| Locker / Slot / Record | - | INDUSTRY-SPECIFIC | - | - | - |
| ChargingStation / Connector / Record | - | - | INDUSTRY-SPECIFIC | - | - |
| Material / BOM / Process / WorkOrder / ProductionRecord | - | - | - | INDUSTRY-SPECIFIC | - |
| MQTT Adapter | EXPECTED | EXPECTED | EXPECTED | EXPECTED | EXPECTED |

说明：四行业 Protocol/Mock = **间接复用**（经 DeviceCommandService）。均复用 `open_door` 作链路验证命令（MES 语义债务见 phase1.8-report）。

---

## 2. 实测复用成本

| 项目 | Core 修改 | Core 新增 API | 新增行业模块 | 新增行业表 | 统计日 | 备注 |
|------|----------:|--------------:|-------------:|-----------:|--------|------|
| Access | 0 | 0 | 1 | 2 | 2026-09-14 | Phase 1.5 |
| Locker | 0 | 0 | 1 | 3 | 2026-09-14 | Phase 1.6 |
| Charging | 0 | 0 | 1 | 3 | 2026-09-14 | Phase 1.7 |
| MES | 0 | 0 | 1 | 6 | 2026-09-14 | Phase 1.8（含 BomItem） |

删除行业模块后 Core 仍可运行：Access/Locker/Charging/MES = **YES**

### Core Modification 台账

| 行业 | Core 业务修改 | Core 新增 API | 为何能复用 / 为复用改了什么 |
|------|--------------:|--------------:|---------------------------|
| Access | 0 | 0 | DeviceCommand；自管 Permission/Record；**未改 Core** |
| Locker | 0 | 0 | 同栈 + open_door；自管 Locker/Slot/Record；**未改 Core** |
| Charging | 0 | 0 | 同栈 + open_door；Connector↔Command；**未改 Core** |
| MES | 0 | 0 | 同栈 + open_door；Process/WorkOrder.device_id→Device；WO↔Command；**未改 Core** |

> Access、Locker、Charging、MES 四个行业均在不修改 Core 的情况下完成最小业务验证。  
> **禁止**宣称「80%」。

---

## 3. 使用规则

1. 每完成可运行行业模板，更新 §1–§2。  
2. 发现需改 Core → 先停编码，写 phase 报告原因。  
3. 四行业 IoT 验证完成后，优先做减法审计，再决定 Telemetry / MQTT / Frontend。

---

## 4. 与路线图

- Phase 1.5–1.8：门禁 / 储物柜 / 充电 / MES 均 Core=0。  
- 下一阶段：架构减法审计（非第五个 IoT Demo）。
