# 数据库设计（MVP-1）

> Spec v0.1 · 逻辑模型，非最终 DDL 细节  
> 原则：能少表就少表；能用 JSON 先 JSON；不为 MES/充电预埋宽表

---

## 1. 多租户策略

### 1.1 选定方案：**共享库 + 共享表 + `tenant_id` 行级隔离**

| 方案 | 第一阶段 | 原因 |
|------|----------|------|
| 独立库 per tenant | ❌ | 运维重，私活/演示不划算 |
| 独立 schema | ❌ | 迁移成本高 |
| 共享表 + tenant_id | ✅ | 与 RuoYi 单体最合拍 |

### 1.2 约定（Phase 1.2 已拍板部分）

1. **方案 A**：不改 `sys_user`；用 `openbiz_member(user_id, tenant_id)` 表达归属。  
2. 业务表未来用 `tenant_id` 行级隔离；**Phase 1.2 尚未做 SQL 自动过滤**。  
3. 无租户上下文时 `TenantContext.getTenantId()` 为 `null`，禁止默默落到某租户。  
4. 表名落地为 `openbiz_tenant` / `openbiz_member`（见 `sql/openbiz_saas_1_2.sql`）。

### 1.3 数据隔离进度

| 阶段 | 能力 |
|------|------|
| Phase 1.2 | 请求级 TenantContext 正确 |
| Phase 1.3（当前） | IoT 四表全部含 `tenant_id`；Service/Mapper **显式**带 tenant；仍无 SQL Rewrite |
| 更后 | 再评估 MyBatis 自动追加（仅 openbiz_*） |

### 1.4 Phase 1.3 IoT 表（已落地）

见 `sql/openbiz_iot_1_3.sql`：

| 表 | 要点 |
|----|------|
| `openbiz_product` | tenant 内 product_code 唯一；protocol 提示，非行业枚举 |
| `openbiz_device` | 属 product；tenant 内 device_code 唯一 |
| `openbiz_thing_model` | 每 product 一份 model_json |
| `openbiz_device_command` | command_id 业务唯一；PENDING/SUCCESS/FAILED |

---

## 2. 核心实体 ER（逻辑）

```text
ob_tenant 1───* ob_member
ob_tenant 1───* ob_customer
ob_tenant 1───* iot_product 1───* iot_device
iot_product 1───1 iot_thing_model
iot_device 1───* iot_device_command
iot_device 1───1 iot_device_property_snapshot
iot_device 1───* iot_device_event_log
iot_device 1───1 acc_access_point
ob_member *───* acc_access_point （经 acc_access_permission）
ob_member / sys_user ───* acc_access_record
```

---

## 3. 表清单（MVP-1）

### 3.1 SaaS

#### `ob_tenant`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | |
| tenant_code | varchar | 唯一，登录/域名用 |
| name | varchar | |
| status | tinyint | 0停用 1正常 |
| expire_at | datetime | 可选 |
| created_at / updated_at | | |

#### `ob_tenant_user`（方案 A）

| 字段 | 说明 |
|------|------|
| tenant_id | |
| user_id | 关联 `sys_user.user_id` |
| is_admin | 租户管理员标记 |

#### `ob_customer`

| 字段 | 说明 |
|------|------|
| tenant_id | |
| name | 客户/门店名 |
| contact / phone | 可空 |
| status | |

#### `ob_member`

| 字段 | 说明 |
|------|------|
| tenant_id | |
| mobile | 可空，租户内唯一（有则） |
| open_id | 小程序，租户内唯一 |
| nickname / avatar | |
| status | 正常/禁用 |
| customer_id | 可选归属客户 |

---

### 3.2 IoT

#### `iot_product`

| 字段 | 说明 |
|------|------|
| tenant_id | 也可做「平台级产品模板」tenant=0 + 租户实例化；MVP 直接租户自建 |
| product_key | 租户内唯一 |
| name | 如「智能门禁」 |
| protocol | `MQTT`（枚举预留，MVP 只写 MQTT） |
| node_type | DEVICE（预留 GATEWAY） |
| status | |

#### `iot_thing_model`

| 字段 | 说明 |
|------|------|
| product_id | UK |
| model_json | 属性/事件/服务定义（见 iot-core-design） |
| version | |

#### `iot_device`

| 字段 | 说明 |
|------|------|
| tenant_id | |
| product_id | |
| device_sn | 租户内唯一 |
| device_name | |
| secret | 设备密钥（加密存储） |
| online_status | ONLINE / OFFLINE |
| work_status | 业务状态：见状态机 |
| last_online_at | |
| enabled | |

#### `iot_device_property_snapshot`

| 字段 | 说明 |
|------|------|
| device_id | PK/UK |
| properties_json | `{ "door_status":"CLOSED", "battery":86 }` |
| reported_at | |

#### `iot_device_command`

| 字段 | 说明 |
|------|------|
| tenant_id / device_id | |
| service_id | 如 `open_door` |
| params_json | |
| idempotent_key | **租户+设备内唯一** |
| status | PENDING / SENT / ACKED / SUCCESS / FAILED / TIMEOUT |
| timeout_at | |
| result_json | |
| created_at / finished_at | |

#### `iot_device_event_log`

| 字段 | 说明 |
|------|------|
| device_id | |
| event_id | 如 `door_open` |
| payload_json | |
| event_time | 设备侧时间（可空） |
| received_at | 服务端时间 |
| msg_id | 上行去重 |

#### `iot_device_log`（运维可选，可与 event 合并）

连接、鉴权失败、解码错误等。MVP 可先只用 `event_log` + 应用日志。

---

### 3.3 Smart Access

#### `acc_access_point`

| 字段 | 说明 |
|------|------|
| tenant_id | |
| name | 如「东门」 |
| device_id | UK：MVP 一门一设备 |
| location | 可空 |
| status | |

#### `acc_access_permission`

| 字段 | 说明 |
|------|------|
| tenant_id | |
| member_id | |
| access_point_id | |
| effect | ALLOW（MVP 不做复杂拒绝链） |
| time_window_json | 如工作日 08:00-22:00；可空=全天 |
| valid_from / valid_to | |

UK：`(tenant_id, member_id, access_point_id)` 简化版即可。

#### `acc_access_record`

| 字段 | 说明 |
|------|------|
| tenant_id | |
| member_id | 可空（管理员代开） |
| operator_user_id | 可空 |
| access_point_id / device_id | |
| command_id | 关联 `iot_device_command` |
| request_id | 与幂等键对齐 |
| result | PENDING / SUCCESS / DENIED / FAILED / TIMEOUT |
| deny_reason | 权限/离线等 |
| created_at | |

---

## 4. 设备工作状态（库字段枚举）

存于 `iot_device.work_status`（字符串或字典）：

```text
ONLINE      # 仅表示连接层时不要与 work 混用 —— 连接用 online_status
OFFLINE
OPENING
OPEN
CLOSING
CLOSED
ERROR
```

**建议拆分两个字段**（避免语义打架）：

- `online_status`: `ONLINE | OFFLINE`  
- `work_status`: `OPENING | OPEN | CLOSING | CLOSED | ERROR | UNKNOWN`

详细转移见 `iot-core-design.md` / `smart-access-design.md`。

---

## 5. 索引与保留策略（MVP）

| 表 | 关键索引 |
|----|----------|
| iot_device | (tenant_id, device_sn), (tenant_id, product_id) |
| iot_device_command | (tenant_id, device_id, idempotent_key) UK, (status, timeout_at) |
| iot_device_event_log | (device_id, received_at), (tenant_id, msg_id) UK 可空 |
| acc_access_record | (tenant_id, created_at), (member_id, created_at) |

日志表 MVP **不做** 分表；数据量大时再按月归档。

---

## 6. 明确不建的表（防过度设计）

- `ob_order*` / `pay_*`（充电再加）  
- `iot_rule*` / `iot_alarm*`  
- `iot_ota*`  
- `iot_gateway*` / `iot_device_group*`  
- `mes_*` 全部  
- `acc_face*` / 生物识别  

---

## 7. 与 RuoYi 字典的关系

建议字典类型（`sys_dict_type`）：

- `iot_online_status`  
- `iot_work_status`  
- `iot_command_status`  
- `acc_access_result`  
- `ob_tenant_status`  

业务代码用枚举；字典供管理端下拉。
