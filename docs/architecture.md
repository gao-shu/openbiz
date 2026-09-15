# OpenBiz 总体架构

> 状态：Phase 1.5 最小门禁业务已完成（见 `phase1.5-report.md`）  
> 工作名：`open-biz-platform`  
> 基座：**官方 RuoYi-Vue `springboot3` = 3.9.2 / Spring Boot 3.5.16**（不用 Plus）

---

## 0. 当前代码事实（必须先认清）

| 项 | 结论 |
|----|------|
| 本地是否已有 RuoYi | **否**。`cangku` 全库无 `pom.xml` / `.java` |
| 是否可从 haode / manju 改造 | **否**。技术栈为 FastAPI / Electron / Vue 创作链路，与 Java 二开平台无关 |
| 本阶段交付物 | **仅文档 Spec**；禁止提前写业务代码 |
| L0 来源 | 引入官方 RuoYi-Vue（建议 `springboot3` 分支，JDK 17），再叠加自有 Core |

因此：下文「复用 RuoYi」指 **即将引入的官方框架能力**，不是「当前仓库已有代码」。

---

## 1. 产品目标（一句话）

建设一套 **基于 RuoYi / Spring Boot 的单体模块化二开母平台**，用真实母项目（门禁 → 储物柜 → 充电 → MES）验证：

> SaaS + IoT Core 是否真的能降低后续项目重复开发。

**不是** 一上来做 10 个行业模板的大而全平台。

---

## 2. 分层架构（最终愿景 vs 第一阶段）

### 2.1 长期四层（愿景，不全做）

```text
L3  Industry Templates   Smart Access / Locker / Charging / MES / ...
L2  Domain Cores         IoT Core  |  Business Core  |  MES Core
L1  Platform Cores       SaaS / Tenant / Message / File（薄封装）
L0  RuoYi Base           用户/角色/菜单/权限/部门/字典/日志/文件/定时任务
```

### 2.2 第一阶段只允许出现的层

```text
L3  smart-access（唯一行业模板）
L2  iot-core（MQTT only）
L1  saas-core（Tenant + 最小 Customer/Member）
L0  ruoyi-base（原样复用，不重写）
```

**明确不做（第一阶段）**：Payment、Inventory、Workflow、Rule Engine、OTA、Gateway 集群、微服务拆分、MES、充电、停车等。

---

## 3. 推荐仓库形态（单体模块化）

```text
open-biz-platform/
├── ruoyi-admin                 # 启动入口（现成）
├── ruoyi-framework
├── ruoyi-system
├── ruoyi-common
├── ruoyi-quartz
├── ruoyi-generator
├── openbiz-saas-core           # 新增：租户 / 客户 / 会员（MVP 最小）
├── openbiz-iot-core            # 新增：产品/设备/物模型/命令/日志 + 接口
├── openbiz-iot-mqtt            # 新增：MQTT Adapter（可替换实现）
├── openbiz-smart-access        # 新增：门禁业务（依赖 iot-core 接口）
├── openbiz-device-simulator    # 新增：模拟门禁设备（HTTP/MQTT）
└── docs/                       # 本 Spec
```

原则：

1. **单体部署**：一个 `ruoyi-admin` 进程加载各模块。  
2. **模块边界靠 Maven 依赖强制**，不是靠微服务。  
3. `smart-access` **禁止** 依赖 `openbiz-iot-mqtt`；只依赖 `openbiz-iot-core` 的接口/服务 API。

---

## 4. 运行时拓扑（MVP-1）

```text
小程序 / 管理端 Vue
        │  HTTPS + JWT
        ▼
   ruoyi-admin（单体）
   ├── SaaS Core（租户上下文）
   ├── Smart Access（开门业务）
   ├── IoT Core（设备/命令/物模型）
   └── MQTT Adapter ──► MQTT Broker（Mosquitto / EMQX 单机即可）
                              ▲
                              │
                     Device Simulator（模拟门禁）
```

可选：模拟器同时暴露 `POST /sim/devices/{id}/open`，便于无 Broker 时联调。

---

## 5. L0：RuoYi 直接复用清单

基于官方 RuoYi-Vue 内置能力（v3.9.x）：

| 能力 | 模块 | 策略 |
|------|------|------|
| 用户 / 角色 / 菜单 / 按钮权限 | system | **直接复用** |
| 部门 / 岗位 / 数据范围 | system | **直接复用**（租户内再套部门） |
| 字典 / 参数 / 通知 | system | **直接复用** |
| 操作日志 / 登录日志 | system | **直接复用** |
| 文件上传 | common/framework | **直接复用**，第一阶段不另做 file-core |
| 定时任务 | quartz | **直接复用**（命令超时扫描可挂这里） |
| 代码生成 | generator | 开发期复用 |
| JWT 多终端 | framework | **直接复用**（小程序同 Token 体系） |

**禁止**：再写一套 User/Role/Menu/Permission。

### 5.1 RuoYi 缺口（需自建）

| 缺口 | 说明 | 落点 |
|------|------|------|
| 多租户 | 官方 RuoYi **无** 租户模型 | `saas-core` |
| 设备 / 物模型 / MQTT | 无 | `iot-core` + `iot-mqtt` |
| C 端会员 | `sys_user` 偏 B 端运营账号 | `saas-core` 的 Member（可关联 openId） |
| 行业业务 | 无 | `smart-access` |

> 备选：若希望「租户开箱」，可评估 [RuoYi-Vue-Plus](https://gitee.com/dromara/RuoYi-Vue-Plus)。第一阶段建议仍用官方 RuoYi + 自研薄租户，避免 Plus 全家桶过重、边界不清。是否切 Plus 放在 Phase 0 决策点，见 `roadmap.md`。

---

## 6. 依赖方向（硬规则）

```text
smart-access  →  iot-core (API)  →  iot-mqtt (Adapter)
      │               │
      └────→ saas-core ←┘
                 │
              ruoyi-*
```

- 下层 **禁止** import 上层包。  
- 业务层只调用「发命令 / 查影子 / 订阅事件」等 **领域接口**。  
- 协议实现仅存在于 Adapter；未来 TCP/HTTP/OCPP 替换时，业务模块零改或极少改。

---

## 7. 绝对不要提前设计的东西

1. 微服务 / 服务网格 / 多注册中心  
2. 多协议全家桶（TCP/Modbus/OPC-UA/OCPP/BLE）  
3. 完整 CRM / ERP / 财务 / 采购  
4. 通用工作流引擎（Flowable/Activiti）— 门禁用不到  
5. 规则引擎 / 复杂告警编排 — MVP 用代码 if/else + 日志即可  
6. Device Shadow 完整 AWS 风格实现 — 先做「最新属性快照表」即可  
7. 分库分表 / 多数据源读写分离  
8. 10 个行业模板的空壳模块  

---

## 8. 成功标准（架构是否合格）

第一阶段结束时，只问四个问题：

1. 能否在 **模拟设备** 上跑通「小程序开门 → MQTT → 状态回传 → 日志」？  
2. Smart Access 是否 **零依赖** MQTT 客户端库？  
3. 租户 A 的设备/记录是否对租户 B 不可见？  
4. 文档是否足够让下一阶段 **只加储物柜格口模型**，而不是复制一套 IoT？

四个「是」→ 架构过关；否则先修边界，不扩展行业。
