# IoT Core 设计

> Spec v0.1 · MQTT only · 面向可替换 Adapter

---

## 0. Phase 1.3 / 1.4 落地状态

已实现最小四表 + Service（见 `sql/openbiz_iot_1_3.sql`、`phase1.3-report.md`）：

```text
openbiz_product → openbiz_device
       ↓
openbiz_thing_model (model_json)

openbiz_device → openbiz_device_command
                      ↓
              ProtocolPortRegistry
                 ├── MOCK → MockDoorProtocolPort → MockDoorDevice (内存状态)
                 └── MQTT → MqttProtocolAdapter (仍 Stub)
```

Phase 1.4 已验证：**Command → ProtocolPort → Device State**（见 `phase1.4-report.md`）。

行业业务只应调用 `DeviceCommandService`，不直接调 MQTT / Mock。

**仍未做**：真 Broker、SQL 自动 tenant 过滤、命令超时重试、物模型拆表、完整门禁业务。

---

## 1. 设计目标

用最小集合支撑门禁验证，并为储物柜 / 充电 / MES 预留 **扩展点（不是预留空实现）**：

1. 产品 + 物模型定义业务能力  
2. 设备实例 + 在线 + 属性快照  
3. 命令下行 + 回执 + 超时  
4. 协议细节关在 Adapter 内  

---

## 2. 物模型（Thing Model）

### 2.1 存储

`iot_thing_model.model_json`，MVP Schema 示例（门禁）：

```json
{
  "properties": [
    { "id": "door_status", "type": "enum", "enum": ["OPEN", "CLOSED", "OPENING", "CLOSING", "ERROR"] },
    { "id": "battery", "type": "int", "unit": "%", "min": 0, "max": 100 }
  ],
  "events": [
    { "id": "door_open", "params": [{ "id": "method", "type": "string" }] },
    { "id": "door_forced", "params": [] }
  ],
  "services": [
    { "id": "open_door", "params": [], "returns": [{ "id": "accepted", "type": "bool" }] }
  ]
}
```

### 2.2 规则

- 业务模块通过 **serviceId / propertyId** 字符串调用，不写死 MQTT。  
- 校验：调用前按物模型检查 service 是否存在（简单 Map 查找即可）。  
- **不做** 完整 TSL 可视化编辑器；管理端可先 JSON 文本编辑。

---

## 3. 领域接口（业务唯一入口）

```text
DeviceCommandGateway
  invoke(deviceId, serviceId, params, idempotentKey) -> CommandId
  getCommand(commandId) -> CommandView

DeviceQueryPort
  getDevice(deviceId)
  getSnapshot(deviceId)

DeviceEventPort（应用内事件，非 MQTT）
  onCommandFinished / onPropertyChanged / onDeviceOnline
```

实现类在 `iot-core`；`iot-mqtt` 只实现 `ProtocolPort`：

```text
ProtocolPort
  sendCommand(device, serviceId, params, commandId, idempotentKey)
  // 上行由 Adapter 回调 core 的 InboundHandler
```

---

## 4. MQTT Topic 约定（Adapter 私有）

建议（可与阿里云 IoT 风格接近，降低学习成本）：

```text
下行服务调用：
  /sys/{productKey}/{deviceSn}/c/service/{serviceId}

上行服务回执：
  /sys/{productKey}/{deviceSn}/s/service/reply

上行属性上报：
  /sys/{productKey}/{deviceSn}/s/property/report

上行事件：
  /sys/{productKey}/{deviceSn}/s/event/{eventId}

在线：
  使用 MQTT Last Will + Connect；或
  /sys/{productKey}/{deviceSn}/s/status
```

Payload 统一 envelope：

```json
{
  "msgId": "uuid",
  "ts": 1690000000000,
  "commandId": "optional",
  "idempotentKey": "optional",
  "data": {}
}
```

**业务模块禁止** 拼接上述 Topic。

---

## 5. 设备状态

### 5.1 在线状态

```text
OFFLINE --connect--> ONLINE --lwt/disconnect/heartbeat超时--> OFFLINE
```

心跳：MQTT keepalive + 可选应用层 60s property 心跳。MVP：Broker keepalive + LWT 即可。

### 5.2 工作状态（门禁类设备）

是否需要状态机？**需要（轻量）**。

推荐转移：

```text
                    open_door
  CLOSED ─────────────────────► OPENING
                                  │
                     ack/success  │  timeout/error
                                  ▼
                                 OPEN
                                  │
                     自动关门/close │
                                  ▼
                               CLOSING ──► CLOSED

  任意状态 --fault--> ERROR
  ERROR --reset/report--> CLOSED 或 OPEN（以设备上报为准）
```

规则：

1. **以设备上报属性为准** 收敛 `work_status`；服务端乐观更新仅作 UX。  
2. 重复 `open_door`：若已是 `OPENING/OPEN`，返回原 `commandId` 或业务「已打开」（见幂等）。  
3. `OFFLINE` 时：命令直接 `FAILED`（或入队，MVP **直接失败**，简单可预测）。

---

## 6. 幂等与可靠性

| 场景 | 策略（MVP） |
|------|-------------|
| 重复开门请求 | 客户端 `requestId` = `idempotent_key`；DB UK；命中则返回已有命令 |
| MQTT 消息重复 | 上行 `msgId` 去重表/缓存（TTL 24h）；重复丢弃 |
| 设备离线 | `invoke` 前查 `online_status`；离线 → 业务 DENIED/FAILED，不发 Broker |
| 网络异常 | Adapter 发送失败 → 命令 FAILED；可人工重试新 key |
| 命令超时 | `timeout_at`（默认 10s）；Quartz/延迟任务扫 PENDING/SENT → TIMEOUT |
| 无响应 | 同超时；记录 `acc_access_record=TIMEOUT` |
| 消息乱序 | 属性带 `ts`；仅当 `ts >= snapshot.reported_at` 才更新；旧包丢弃 |
| 回执丢失但门已开 | 下一属性 `door_status=OPEN` 仍可修正工作状态；通行记录保持 TIMEOUT（可人工标记） |

**不做（MVP）**：分布式事务、Exactly-once 跨系统、命令自动重试风暴。

---

## 7. 设备认证（MVP）

1. 注册设备时生成 `device_sn` + `secret`。  
2. 模拟器 / 真机用 `username=device_sn` `password=secret` 连 Broker（或 Token 插件）。  
3. 第一阶段可用 **Mosquitto 明文 + 内网**；文档标明生产需 TLS。  

---

## 8. 模拟设备

`openbiz-device-simulator`：

- 订阅下行 Topic，收到 `open_door` 后：  
  1. 回执 accepted  
  2. 延迟 200–500ms 上报 `door_status=OPENING` → `OPEN`  
  3. 可选 N 秒后 `CLOSING` → `CLOSED`  
- 同时提供 HTTP：`POST /sim/devices/{sn}/force-status` 便于联调。  
- 可模拟：离线（断开）、ERROR、强制闯入事件 `door_forced`。

---

## 9. 与未来协议扩展的关系

```text
iot-core
   └── ProtocolPort
         ├── MqttProtocolAdapter      ← MVP
         ├── HttpProtocolAdapter      ← 后期
         ├── TcpProtocolAdapter       ← 后期
         └── OcppProtocolAdapter      ← 充电后期
```

物模型 `services/properties` 保持稳定；Adapter 负责映射到协议帧。

---

## 10. 第一阶段不做清单（再强调）

- Rule Engine、告警引擎、场景联动  
- OTA、远程配置中心  
- 网关子设备拓扑  
- 多 Broker 集群联邦  
- 时序库（Influx/TDengine）— 属性历史先可空，只要快照 + 事件日志  
