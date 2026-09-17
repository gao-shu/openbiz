# OpenBiz

## 可复用的 Java 业务能力组件

> OpenBiz 面向真实 Java 项目，提供可复用的业务能力，通过 Maven 组合到项目中，减少重复开发常见的业务基础能力。

Java 21 · Spring Boot 3 · MyBatis · MySQL · Maven

[快速开始](docs/getting-started.md) · [文档](docs/README.md) · [示例说明](docs/examples/workorder-demo.md) · [证据边界](docs/evidence.md)

---

## 关联 Demo

**[openbiz-workorder-demo](https://github.com/gao-shu/openbiz-workorder-demo)**

一个独立的 Spring Boot Demo，通过 Maven 引入 `com.openbiz:openbiz-service:3.9.2`，验证 OpenBiz 的工单能力可以脱离母仓库，被独立项目消费。

Demo 使用真实 HTTP + MySQL 验证：

- 工单创建
- 分配
- 接单
- 完成
- 取消
- 租户隔离
- 创建幂等

Demo：https://github.com/gao-shu/openbiz-workorder-demo

> OpenBiz 是能力仓库；`openbiz-workorder-demo` 是真实独立消费者，不是 OpenBiz 本身。

---

## 30 秒理解 OpenBiz

```text
OpenBiz Service
        ↓
mvn install  (local ~/.m2)
        ↓
com.openbiz:openbiz-service:3.9.2
        ↓
openbiz-workorder-demo
        ↓
独立 Spring Boot 项目  :18081
        ↓
真实 HTTP + MySQL
        ↓
工单生命周期 + 租户隔离 + 创建幂等
```

这条链路证明的是：OpenBiz 业务能力可以被独立项目通过 Maven 复用。

`openbiz-workorder-demo` 不是 OpenBiz 本身，而是 OpenBiz 的独立消费示例（不复制源码、不使用 Git submodule）。

> Local Maven Artifact **≠** Maven Central。

仓库内 `ruoyi-admin` 的 IoT Monitor Demo 是 **in-repo** 证据路径，证明级别不同于上述独立 Maven Consumer。

---

## 为什么做 OpenBiz

很多业务系统都会重复实现类似的能力，例如：

- 多租户
- 工单
- 客户与账户
- 设备与指令
- 库存扣减
- AI 模型调用

OpenBiz 尝试把这些能力沉淀成可以复用的 Java 模块，通过 Maven 组合到真实项目中，而不是每个项目重新实现一遍。

OpenBiz 不是另一个后台管理模板，也不是“AI 自动生成整个企业系统”的产品。

**OpenBiz 不是：**

| 不是这个 | 说明 |
| --- | --- |
| AI Coding 产品 / Agent 平台 | AI Coding 只是工程方法；模型调用是可选、最小能力 |
| RuoYi 替代品 | RuoYi 仍是运行时 / 认证 / 配置外壳；OpenBiz 是业务能力层 |
| 后台管理 UI 模板 | 前端不在本仓库定位内 |
| 微服务脚手架 | 能力单体仓，不是部署拓扑方案 |
| 通用 SDK 平台 | 当前是本地 Maven 产物，不是 Maven Central 产品化 |
| 工单 / IoT / 商城成品 | 那些是能力（以及一个 Hero Demo），不是产品身份 |

---

## 能力列表

| 能力 | 模块 | 当前状态 |
| --- | --- | --- |
| 多租户 | `openbiz-saas-core` | 可复用能力 |
| 工单 | `openbiz-service` | **已有独立 Demo 验证** |
| 客户 / 账户 | `openbiz-business` | MySQL 测试 / 母仓库验证 |
| IoT 设备 / 指令 | `openbiz-iot` | In-repo Demo / Mock 协议 |
| 库存 | `openbiz-shop` | MySQL 测试 |
| AI 模型调用 | `openbiz-agent` | 基础能力 / 可选真实模型 E2E |

按需选用即可，不必一次用完所有领域。

**目前只有 WorkOrder（工单）已经有独立 Maven Consumer**——其他行是能力本身与母仓库证据，证明级别不同。

---

## Examples

### WorkOrder Demo

- 链接：https://github.com/gao-shu/openbiz-workorder-demo
- 独立 Spring Boot 项目，通过 Maven 引入 OpenBiz Service，不复制 OpenBiz 源码，不使用 Git submodule。
- 证明链路：`OpenBiz Service → Maven Artifact → Independent Consumer → HTTP → MySQL`

| 示例 | 证明什么 |
| --- | --- |
| **[openbiz-workorder-demo](https://github.com/gao-shu/openbiz-workorder-demo)** | `openbiz-service` 的独立 Maven 消费者（Hero Example） |
| IoT Monitor Demo（`ruoyi-admin` 内） | 仓库内复用 `DeviceService` / `DeviceCommandService`（不是独立仓库） |

说明文档：[docs/examples/workorder-demo.md](docs/examples/workorder-demo.md)

---

## Quick Start

**默认路径 = WorkOrder Demo（不是完整管理后台）。**

在已安装 **JDK 21、Maven、MySQL、Redis** 的前提下，通常约 **5–10 分钟** 可跑通首个 API。

1. 在本仓库将产物安装到**本地** Maven 仓库：

```bash
mvn -pl openbiz-service,ruoyi-framework -am install -DskipTests
```

坐标：`com.openbiz:openbiz-service:3.9.2`

2. Clone 并配置 [openbiz-workorder-demo](https://github.com/gao-shu/openbiz-workorder-demo)
3. 指向 MySQL（默认库名 `ry-vue`）+ Redis
4. `mvn spring-boot:run` → 端口 **18081**
5. `POST /login` → `POST /api/work-orders` → `GET /api/work-orders/{id}`

完整步骤、SQL 文件名与排错说明：**[docs/getting-started.md](docs/getting-started.md)**

> 可选（非默认）：在 **:18080** 运行 `ruoyi-admin` 做仓库内探针。那是维护者路径，不是陌生人的第一路径。

---

## 证据边界

| 领域 | 证据级别 |
| --- | --- |
| 工单 + **独立** HTTP Consumer | **L3**（MySQL + 进程 `:18081`） |
| 业务账户 CAS / 幂等 | **L3**（母仓库 MySQL 测试 + 探针） |
| IoT 设备指令（MockDoor） | **L3** 仓库内 MySQL / HTTP Demo；协议 = **mock** |
| 商城库存 CAS | **L3** 母仓库 MySQL 测试 |
| Agent 模型调用 | **L2**（配置 API Key 时可做可选真实模型 E2E） |

**请认真读：**

| 说法 | 现实 |
| --- | --- |
| Local Maven Artifact | **≠** Maven Central |
| Independent Consumer | **≠** SDK 产品化 |
| Real MySQL | **≠** Production |
| Mock 协议 / MQTT stub | **≠** 真实设备 / 真实 MQTT |

详情与诚实边界：**[docs/evidence.md](docs/evidence.md)**

---

## 架构（简）

```text
RuoYi (auth / RBAC)     当前运行时 / 认证 / 配置外壳
        │
openbiz-saas-core       TenantContext
        │
 ┌──────┼──────────────┬─────────────┬──────────┐
iot  business  service  shop      agent
```

当前仓库保留 RuoYi 作为运行时、认证、配置及 Demo 外壳。

OpenBiz 的核心定位是业务能力层，而不是 RuoYi 替代品。

所有母域能力**继续留在本 monorepo**（`KEEP IN open-biz-platform`）。

产物可被消费 ≠ “每个领域必须拆成独立 Git 仓库”。

决策记录：**[docs/architecture-decision-mother-boundary.md](docs/architecture-decision-mother-boundary.md)**

---

## AI Coding

OpenBiz 使用 AI Coding 工作流辅助演进（见 [docs/ai-coding](docs/ai-coding/README.md)），但 **AI Coding 是工程方法，不是 OpenBiz 的产品定位**。

---

## 文档

入口：**[docs/README.md](docs/README.md)**

| 文档 | 用途 |
| --- | --- |
| [Getting Started](docs/getting-started.md) | Consumer-first 跑通手册 |
| [Evidence](docs/evidence.md) | L1–L4 矩阵与红线 |
| [Mother-boundary ADR](docs/architecture-decision-mother-boundary.md) | 不因“架构好看”提前拆仓 |
| [AI Coding workflow](docs/ai-coding/README.md) | 演进本仓库的工程方法（次要） |
| [Archive index](docs/archive/README.md) | 历史阶段 / 审计归档 |

---

## Roadmap

| 版本 | 重点 |
| --- | --- |
| **V0.1** | 开源入口：README、Quick Start、Example、Evidence |
| **V0.2** | 更多复用证明（例如额外 Consumer）——仅在有明确需要时 |
| **V1.0** | 社区包装：贡献指南、changelog、releases |

**不在路线图：** 为“架构好看”做物理拆域、行业灌水模块、把 MQTT 当营销话术、RAG/多智能体平台，或为刷 Stars 堆功能。

---

## Versioning

当前第一个公开 OpenBiz Release 是 **`v0.1.0`**。

当前 Maven 能力产物（如 `com.openbiz:openbiz-service:3.9.2`）仍沿用现有 reactor / upstream 对齐版本。

**`v0.1.0` 是 OpenBiz 的 GitHub Release 版本。**

**`3.9.2` 是当前 Maven artifact / reactor / upstream 对齐版本。**

两者不是同一个版本体系（也不是 “OpenBiz v3.9.2”）。

独立示例工程 `openbiz-workorder-demo` 使用自己的工程版本（`0.1.0-SNAPSHOT`），同时依赖上述 Maven 坐标。

`v0.1.0` **不代表** Maven Central 发布、生产就绪或 SDK 产品化。

---

## License / About

本仓库中的 OpenBiz 领域模块定位为可复用业务能力。

仓库保留基于 RuoYi 的运行时 / 认证 / Demo 外壳；详见 `LICENSE` 与 `NOTICE`。

- GitHub: https://github.com/gao-shu/openbiz
- Example: https://github.com/gao-shu/openbiz-workorder-demo
- Author site: https://gao-shu.github.io/my-vitepress-site
