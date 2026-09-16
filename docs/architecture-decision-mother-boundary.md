# Architecture Decision �� Mother Repo Boundary

**Status:** Accepted (OpenBiz Phase 3 Close / Phase 4 documentation)  
**Date:** 2026-09

---

## Decision

The following modules **KEEP IN** the monorepo `open-biz-platform`:

```text
openbiz-foundation
openbiz-saas-core
openbiz-iot
openbiz-business
openbiz-service
openbiz-agent
openbiz-shop
```

Plus RuoYi base modules used as the admin / thin-shell foundation.

**No physical Git split** of mother domains at this time.

---

## Domain ownership (not the same as Git ownership)

| Domain | Owns (examples) | Location |
| --- | --- | --- |
| Business | Customer, Account, AccountLedger, Item, Order, OrderItem | `openbiz-business` |
| Service | WorkOrder + transitions | `openbiz-service` |
| IoT | Product, Device, ThingModel, DeviceCommand, industry packages | `openbiz-iot` |
| Shop | Shop product / inventory / shop order | `openbiz-shop` |
| Agent | PromptTemplate, ModelPort, AgentInvoker | `openbiz-agent` |
| SaaS | Tenant, Member, TenantContext | `openbiz-saas-core` |

Customer / Account are **Business**, not IoT. There is nothing to ��move out of IoT�� for those entities.

---

## What Phase 3 proved (and did not prove)

**Proved:**

> `openbiz-service` capabilities can be consumed by an independent project through a **local Maven artifact**, with real HTTP + MySQL.

**Did not prove:**

> Every mother domain must become its own Git repository.

```text
Business domain boundary
        ��
Maven module boundary
        ��
Git repository boundary
```

---

## When a physical split may be reconsidered

All of the following should exist **before** planning a split:

1. A real external consumer need beyond in-repo demos  
2. Independent release / versioning pressure  
3. Clear ownership boundary  
4. Full process: Understand �� Plan �� **Human Approval** �� Implement �� Verify �� Close  

Until then:

```text
NO MOVE
NO COPY
NO DELETE
NO RENAME (for split purposes)
NO NEW REPOSITORY for mother domains
NO MODULE SPLIT ��for architecture aesthetics��
```

---

## Related

- [Evidence](evidence.md)
- [Getting Started](getting-started.md)
