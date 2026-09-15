# 编码约束（Phase 1 起）

> 与 `roadmap.md` §6 一致；违反即视为架构回退。

## 硬禁止

1. 行业模块（`smart-access` / 未来 locker、charging、mes）**禁止** 引入 MQTT 客户端依赖。  
2. **禁止** 再实现 User / Role / Menu / Permission / Dict / OperLog（用 RuoYi）。  
3. **禁止** 复制 `iot_device` / `iot_device_command` 为行业私有命令表。  
4. **禁止** 第一阶段拆 IoT/SaaS 为独立微服务。  
5. **禁止** 为未开始的 Phase 创建空 Maven 占位模块（locker/charging/mes 等）。  
6. **禁止** 在未更新 Spec 的情况下「顺手」加 Rule Engine、OTA、工作流、OCPP。

## 硬要求

1. 业务表含 `tenant_id`，读写走租户拦截；唯一索引带租户。  
2. 设备下行一律 `DeviceCommandGateway.invoke(..., idempotentKey)`。  
3. 协议细节只存在于 `openbiz-iot-*` Adapter。  
4. 单体模块化：依赖方向由 Maven 强制（见 `module-boundary.md`）。  
5. 能用字典/枚举表达的状态，不建多余状态引擎框架。

## 评审问题（每个 PR 自问）

- 门禁/柜/充电是否又能直接 new 了 Paho？  
- 是否改了 RuoYi 内核导致升级困难？  
- 是否出现第二个「设备表」？  
- 是否本 Phase 不该出现的模块进了仓库？
