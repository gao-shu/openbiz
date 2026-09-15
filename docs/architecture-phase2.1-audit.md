# OpenBiz Architecture Phase 2.1 �� Boundary Audit

> Date: 2026-09-14  
> Nature: **read-only** (this file is the only deliverable)  
> Verdict: **ARCHITECTURE STATUS: PASS WITH WARNING**

---

## Method

- Maven: every `openbiz-*` `pom.xml` + parent + `ruoyi-admin`
- Java imports: Foundation / IoT / Business / Service / Agent / Shop / `ruoyi-admin`
- Skeleton file inventory (excluding `target/`)
- SQL tree under `sql/`
- Git: repo has **no commits**; `git diff` on IoT paths is empty. Frozen conclusion is **source-tree**, not tag-vs-HEAD.

No Java / POM / DDL was changed in this phase.

---

## 1. Module Matrix

| Module | Type | Current status | Business logic | DB |
| ------ | ---- | -------------- | -------------- | -- |
| Foundation (`openbiz-foundation`) | base | Skeleton | **NO** | **NO** |
| SaaS (`openbiz-saas-core`) | shared infra (Tenant) | Complete (legacy) | Tenant/Member mapping only �� **not** a fifth business domain | YES (`openbiz_saas_1_2.sql`) |
| IoT (`iot-core` + mqtt + access/locker/charging/mes) | domain | **v1.0 FROZEN** | YES | YES |
| Business (`openbiz-biz-core`) | domain | **Phase 1 COMPLETE** | YES | YES (`openbiz_biz_1.sql`) |
| Service (`openbiz-service`) | domain | Skeleton | **NO** | **NO** |
| Agent (`openbiz-agent`) | domain | Skeleton | **NO** | **NO** |
| Shop (`openbiz-shop`) | domain | Skeleton | **NO** | **NO** |
| `ruoyi-admin` | boot assembly | RuoYi + `@Import` | RuoYi system only; **no** OpenBiz domain Service calls | uses RuoYi + OpenBiz SQL |

---

## 2. Dependency Matrix (Maven, code truth)

`?` = direct Maven dependency. `?` = none.  
`saas` is listed separately because it is **not** `openbiz-foundation` and **not** IoT Core.

| From \\ To | foundation | saas-core | IoT* | Business | Service | Agent | Shop |
| ---------- | ---------: | --------: | ---: | -------: | ------: | ----: | ---: |
| IoT* | ? | ? | �� | ? | ? | ? | ? |
| Business | ? | ? | ? | �� | ? | ? | ? |
| Service | ? | ? | ? | ? | �� | ? | ? |
| Agent | ? | ? | ? | ? | ? | �� | ? |
| Shop | ? | ? | ? | ? | ? | ? | �� |
| foundation | �� | ? | ? | ? | ? | ? | ? |
| saas-core | ? | �� | ? | ? | ? | ? | ? |
| ruoyi-admin | ? | ? | ? | ? | ? | ? | ? |

\*IoT* = `openbiz-iot-core` plus industries. Industries also depend on `openbiz-iot-core`. `openbiz-iot-mqtt` �� `openbiz-iot-core` only.

### Expected diagram vs actual

Intended:

```text
Foundation
    ��
    ������ IoT / Business / Service / Agent / Shop
```

Actual:

```text
openbiz-foundation  ��  Service, Agent, Shop only
openbiz-saas-core   ��  IoT (+ industries), Business
ruoyi-admin         ��  all of the above (classpath assembly)
```

This is **not** a domain-to-domain edge. Business does **not** depend on `openbiz-iot-core`.

### Full OpenBiz Maven graph (direct)

```text
openbiz-foundation          �� spring-context
openbiz-saas-core           �� ruoyi-common, spring-webmvc, mybatis
openbiz-iot-core            �� saas-core, ruoyi-common, spring, mybatis
openbiz-iot-mqtt            �� iot-core
openbiz-access|locker|
  charging|mes              �� saas-core + iot-core + ruoyi-common + mybatis
openbiz-biz-core            �� saas-core, ruoyi-common, spring, mybatis
openbiz-service             �� foundation
openbiz-agent               �� foundation
openbiz-shop                �� foundation
ruoyi-admin                 �� foundation + saas + iot* + biz + service + agent + shop
                              + ruoyi-framework/quartz/generator
```

### Indirect / runtime

- **No** Business �� IoT, Service �� Business, Agent �� Business/IoT, Shop �� Business (compile).
- **Yes** runtime co-location: one Spring Boot app (`scanBasePackages` includes `com.openbiz`) loads every module on the admin classpath. That is **assembly**, not a domain import of `BizService` / `DeviceCommandService`.

---

## 3. Foundation Audit

### Foundation contains

```text
openbiz-foundation/
  pom.xml                         spring-context only
  README.md
  OpenBizFoundationAutoConfiguration.java   empty @Configuration
  package-info.java               "do not put Customer/Product/Order/Device here"
```

No Mapper, Entity, Controller, SQL, Redis, MQ.

Mentions of Customer / Product / Order exist **only as ��do not put these here�� comments**.

```text
Business leakage: NO
IoT leakage:      NO
Service leakage:  NO
Agent leakage:    NO
Shop leakage:     NO
```

### WARNING (non-blocking)

`openbiz-foundation` is **not** yet the shared base of IoT or Business. Tenant still lives in `openbiz-saas-core` (intentional: do not relocate frozen Tenant). Foundation is therefore thinner than the cartoon, and also **less used** than the cartoon.

---

## 4. Frozen Boundary Audit

| Gate | Result | Evidence |
| ---- | ------ | -------- |
| IoT Core Modification | **0** | No `com.openbiz.biz` / service / agent / shop / foundation in `openbiz-iot-core`. Industries still �� saas + iot-core only. |
| Business Core Modification | **0** (logic) | Domain set unchanged: Customer, Account, AccountLedger, Item, Order, OrderItem, probe, Money, `BizTenantGuard` �� `TenantContext` only. Phase 2 added `README.md` in the module; no service/model/SQL rewrite. |
| Database Schema Change | **0** this phase | No `openbiz_service_*.sql` / agent / shop. Existing IoT + biz SQL only. |
| Existing API Change | **0** domain APIs | Admin only `@Import` AutoConfigurations. No `ruoyi-admin` call to `BizService` / `DeviceCommandService`. |

Git cannot confirm freeze against a tag (`No commits yet on master`). Conclusion is filesystem/Maven, not `git diff origin`.

---

## 5. Circular Dependency

```text
Circular Dependency: NONE
```

No A��B��A among foundation / saas / iot / biz / service / agent / shop.

---

## 6. Domain checks (detail)

### Business independence

```text
Business �� saas-core     YES (TenantContext)
Business �� iot-core      NO
Business �� service/agent/shop/foundation  NO
```

`BizTenantGuard` is a **local** copy of the tenant gate; it does not import IoT `TenantGuard`.

Phase 1 capabilities still present in `src/main` (not re-audited for behavior): Customer, Account, Ledger, Item, Order, OrderItem, Recharge/Consume path in `BizServiceImpl`, optimistic lock / idempotent_key / transaction (as of Phase 1 report).

### IoT frozen

Internal IoT graph unchanged: Core �� saas; MQTT �� Core; industries �� saas + Core. No Business imports.

### Agent special boundary

`openbiz-agent` Java: one empty AutoConfiguration that `@Import`s Foundation only.

```text
Agent �� BusinessService / DeviceService / OrderService / CustomerMapper : ABSENT
```

Future rule (documented, not implemented): Agent �� Tool/Adapter/API �� other domains.

### Service / Agent / Shop skeletons

Each module (source, not `target/`):

```text
pom.xml + README.md + *AutoConfiguration.java
```

No Controller, Service impl, Mapper, Entity/Domain, Repository, DDL, HTTP business API.

### ruoyi-admin

- Allowed: Maven deps on all OpenBiz modules; `OpenBizBootstrapConfiguration` `@Import` of AutoConfigurations.
- `RuoYiApplication` `scanBasePackages = { "com.ruoyi", "com.openbiz" }` �� boot scan, not domain logic.
- Remaining `com.ruoyi.web.controller.*` are stock RuoYi.
- **No** injection of OpenBiz domain services in admin controllers.

**WARNING:** admin **Maven-depends on empty Service/Agent/Shop**. That does not create domain coupling, but it does put skeletons on the production classpath of the IoT+Business boot app. Acceptable for a single-process monorepo; do not treat it as ��Agent already talks to Business.��

---

## 7. Architecture Verdict

```text
ARCHITECTURE STATUS: PASS WITH WARNING
```

### Why not FAIL

- Foundation has no domain objects.
- Five business domains do not Maven-depend on each other.
- IoT Core / Business Core / schema / domain APIs were not rewritten for ��pretty architecture.��
- Service / Agent / Shop are empty markers.
- No cycles.

### Warnings (do not fix in an architecture pass)

1. **Foundation is unused by IoT and Business.** Real shared layer is still `openbiz-saas-core`. Do **not** move Tenant into Foundation without a later, justified extraction.
2. **Cartoon ��everything �� Foundation�� is false in Maven.** Report the saas-core fork honestly.
3. **`ruoyi-admin` assembles all five domains on one classpath.** Independence is compile-time (module POMs), not process isolation.
4. **Mental tree that puts `saas-core` under IoT is misleading.** saas-core is shared Tenant infra for IoT **and** Business.
5. **Git cannot freeze IoT** until there is a commit/tag.

### Why not a clean PASS

Item 1�C2 are small but they are the exact place a later ��just wire Foundation under IoT�� refactor would start. Leave them as warnings, not as a todo for this phase.

---

## 8. Stop line

Shape matches the locked target:

```text
One Monorepo
+ Thin Foundation (skeleton)
+ 5 independent business domains
+ IoT Frozen
+ Business Complete
+ Service / Agent / Shop Skeleton
+ saas-core as existing Tenant (not relocated)
```

**Stop architecture work.** Do not enter Phase 3 from this audit. Next work should be **one mother domain at a time** by business value, not more platform plumbing.
