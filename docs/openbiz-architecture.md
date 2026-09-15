# OpenBiz Architecture

> Architecture Phase 2 �� total skeleton  
> Date: 2026-09-14

---

## 1. OpenBiz positioning

```text
��ҵӦ��ͨ��ĸƽ̨
```

Thin cores + industry / domain plugins on RuoYi (Spring Boot 3 / Java 21 / MyBatis).
Reuse before abstraction; evidence before claims; no big-bang refactor.

---

## 2. Five business modules + Foundation

```text
                     OpenBiz
                        ��
                   Foundation
                        ��
       �����������������������������������੤��������������������������������
       ��        ��       ��       ��        ��
      IoT    Business Service  Agent    Shop
```

| Module | Meaning |
|--------|---------|
| IoT | ��ҵ / IoT / MES |
| Business | ��Ա / �˻� / ���� |
| Service | CRM / ���� / �ۺ� |
| Agent | AI / RAG / Tool / Agent |
| Shop | ��Ʒ / ���� / ��� |

---

## 3. What each module owns

### Foundation

Cross-cutting **non-domain** base only (must stay thin).

Candidate: Tenant contract, Security glue, Exception/Response conventions, Audit, ID, tiny utils.

Today Tenant still lives in `openbiz-saas-core` (legacy, working) �� **not relocated** in Phase 2.

### IoT

Product / Device / ThingModel / DeviceCommand / ProtocolPort / MQTT stub  
Industry: Access / Locker / Charging / MES  

**Status: IoT v1.0 FROZEN**

### Business

Customer / Account / AccountLedger / Item / Order / OrderItem  
Recharge + consume with optimistic lock + idempotency  

**Status: Phase 1 COMPLETE**

### Service

CRM / work-order / after-sale / appointment **boundary only**

### Agent

LLM / RAG / Tool / Model **boundary only**

### Shop

Commerce catalog / cart / inventory / delivery **boundary only**

---

## 4. What each module does NOT own

| Module | Explicitly out of scope (for now) |
|--------|-----------------------------------|
| Foundation | Customer, Account, Order, Product, Device, Agent, RAG, Inventory, WorkOrder |
| IoT | Member prepaid balance, Shop cart, CRM tickets, Agent runtime |
| Business | Package/times-card, Payment, Coupon, Points, Inventory, Multi-store |
| Service | Full WO state machine, appointment product, tables in Phase 2 |
| Agent | LangChain stack, vector DB, MCP, Redis memory, multi-agent, tool-calling runtime |
| Shop | Payment channels, WeChat/Alipay, marketing, coupons, full catalog |

---

## 5. Foundation responsibilities

- Hold **proven** shared infrastructure contracts
- Be depended on by domain modules
- Remain extremely thin

## 6. Foundation non-responsibilities

- Do **not** become a trash bin for every shared-looking noun
- Do **not** create Customer Core / Product Core because two domains might need them later
- Keep domains independent until duplication is measured and justified

---

## 7. Module dependency rules

```text
Foundation  (+ saas-core legacy Tenant for IoT/Business)
    ��
    ��
���������੤�������Щ������������Щ�������������
��   ��    ��      ��      ��
IoT Biz Service Agent Shop
```

Maven reality (Phase 2):

```text
openbiz-foundation          �� spring-context only
openbiz-saas-core           �� ruoyi-common (Tenant; unchanged)
openbiz-iot-* / industry    �� saas-core (+ iot-core for industries)
openbiz-biz-core            �� saas-core only (NOT iot-core)
openbiz-service|agent|shop  �� foundation only
```

**Business modules must not depend on each other by default.**

Forbidden without a real requirement:

```text
Business �� IoT Core
IoT �� Business Core
Shop �� Business Core
Service �� Shop Core
```

**No cyclic dependencies.**

---

## 8. Inter-module communication

Prefer later:

```text
API / Tool / Adapter / Event
```

Never:

```text
Direct foreign Service import
Foreign Mapper
Foreign tables
```

Phase 2 does **not** implement cross-module business calls.

---

## 9. Core boundary principle

Each domain has its own Core. Industry plugins call that domain's Core API.
Adding an industry should prefer **Core Modification = 0**.

---

## 10. Why no super-Core

A shared mega-core mixes IoT DeviceCommand with Business money and Shop inventory.
That destroys reuse evidence and forces every change through one choke point.
OpenBiz prefers multiple thin cores under one platform shell.

---

## 11. Why Customer / Product are not extracted yet

- Business Customer �� IoT Member (sys_user?tenant)
- Shop Product �� IoT Product
- Service Customer may differ again

Extract only after **stable duplication is proven** across modules.

---

## 12. Recommended next development order

```text
1. Keep IoT frozen unless bug/security/real customer need
2. Keep Business Phase 1 frozen (no Package/Payment yet)
3. Pick ONE of Service / Agent / Shop for Phase 0 design �� Phase 1 validate �� freeze
4. Never open all three skeletons into full products in parallel without need
```

---

## 13. Agent special rule

Agent is an **intelligent entry**, not a parent of business cores.

```text
Agent �� Tool/Adapter �� Business API / IoT API
```

Not:

```text
Agent �� import BizServiceImpl / DeviceCommandService
```

---

## Maven module map (actual tree �� flat RuoYi style)

```text
open-biz-platform/
������ openbiz-foundation          Foundation skeleton
������ openbiz-saas-core           Tenant (legacy Foundation capability)
������ openbiz-iot-core            IoT
������ openbiz-iot-mqtt
������ openbiz-access|locker|charging|mes
������ openbiz-biz-core            Business
������ openbiz-service             Service skeleton
������ openbiz-agent               Agent skeleton
������ openbiz-shop                Shop skeleton
������ ruoyi-*                     RuoYi base + admin boot
```

Nested `Foundation/` / `IoT/` folders were **not** created �� Maven modules stay flat to avoid moving frozen code.
