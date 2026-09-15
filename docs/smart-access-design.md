# Smart Access（智能门禁）设计

> Spec v0.1 · 第一个母项目 · 验证整条链路

---

## 1. 为什么先做门禁

| 验证点 | 门禁能否覆盖 |
|--------|----------------|
| 小程序 + JWT | ✅ |
| 租户 / 会员 / 权限 | ✅ |
| IoT 产品 / 设备 / 物模型 | ✅ |
| MQTT 上下行 | ✅ |
| 命令幂等 / 超时 | ✅ |
| 实时状态 + 日志 | ✅ |
| 业务复杂度 | **低**（适合当骨架） |

---

## 2. 角色与端

| 端 | 用户 | 能力 |
|----|------|------|
| 管理端（RuoYi Vue） | 平台超管 / 租户管理员 | 租户、产品、设备、门点、授权、记录查询 |
| 小程序 | Member | 登录、门列表、一键开门、我的记录 |
| 模拟器 | 开发者 | 假设备 |

---

## 3. 核心业务流程

```text
Member 小程序
  → 登录（微信 code → Member + JWT）
  → 选择 AccessPoint
  → POST /api/access/open { accessPointId, requestId }
       → 校验登录租户
       → 校验 AccessPermission + 时段
       → 校验门点绑定设备 ONLINE
       → DeviceCommandGateway.invoke(..., "open_door", requestId)
       → 写 AccessRecord(PENDING)
  → 轮询记录 / WebSocket（MVP 轮询即可）
       → SUCCESS / FAILED / TIMEOUT
设备侧
  → 执行 → 属性上报 door_status
  → IoT 更新 snapshot + work_status
  → 命令终态回调 → AccessRecord 终态
```

---

## 4. 模块内 API（示意）

### 4.1 小程序

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/wx/auth/login` | code2session → token |
| GET | `/access/points/mine` | 我有权限的门 |
| POST | `/access/open` | 开门 |
| GET | `/access/records/mine` | 我的记录 |

### 4.2 管理端

| 能力 | 说明 |
|------|------|
| 门点 CRUD | 绑定 device |
| 授权 CRUD | member ↔ point |
| 记录查询 | 租户隔离 |
| 代开门 | 管理员操作，记 operator_user_id |

权限字示例：`access:point:list`、`access:open:admin` 等，走 RuoYi 菜单。

---

## 5. 领域规则

1. **一门一设备（MVP）**：`acc_access_point.device_id` 唯一。储物柜再改「一设备多点位」。  
2. **无权限**：不调用 IoT，直接 `DENIED`。  
3. **设备离线**：`DENIED` 或 `FAILED`，原因码 `DEVICE_OFFLINE`。  
4. **幂等**：同一 `requestId` 重复提交返回同一结果，不产生第二次开门。  
5. **自动关门**：由模拟器/设备完成；云端不强制发 `close_door`（物模型可预留，MVP 可不实现服务）。  

---

## 6. 与 IoT 的契约

门禁产品物模型固定 service：

- `open_door`  

属性：

- `door_status`  
- `battery`（可展示，非开门硬依赖）  

事件（可选展示）：

- `door_open`  
- `door_forced` → 管理端告警列表可用简单查询代替告警引擎  

Smart Access **只依赖** 上述 ID 字符串 + `DeviceCommandGateway`。

---

## 7. 非功能（MVP 水位）

| 项 | 目标 |
|----|------|
| 开门接口 RT | 本地联调 < 200ms 到「命令已受理」 |
| 命令超时 | 默认 10s |
| 并发 | 单机演示级；不做压测优化 |
| 安全 | JWT；设备密钥；管理端 RBAC；租户隔离 |

---

## 8. 验收清单（跑通即过）

- [ ] 创建租户 A/B，数据互不可见  
- [ ] 注册门禁产品 + 物模型 + 设备，模拟器上线 ONLINE  
- [ ] 会员获得东门权限，可开门成功，记录 SUCCESS  
- [ ] 无权限会员开门 → DENIED，且 **无** MQTT 下行  
- [ ] 模拟器断开 → 开门 FAILED/DENIED DEVICE_OFFLINE  
- [ ] 同一 requestId 连点两次 → 仅一条命令  
- [ ] 命令超时 → TIMEOUT  
- [ ] `smart-access` 模块 pom **无** MQTT 客户端依赖  

---

## 9. 明确不做

- 人脸 / 指纹 / 卡片硬件协议  
- 访客审批流（工作流引擎）  
- 视频联动  
- 多门互锁 / 反潜回（anti-passback）  
- 真实微信商户支付门禁费  
