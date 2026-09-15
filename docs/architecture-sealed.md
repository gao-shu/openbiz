# OpenBiz Architecture — SEALED

> Date: 2026-09-15  
> Source: Final Audit `docs/openbiz-final-audit.md` (supersedes Phase 2.1 skeleton status)

```text
OPENBIZ MVP = FINAL
ARCHITECTURE = SEALED
FIVE MOTHER DOMAINS = FROZEN
CODE DEVELOPMENT = STOP

Do not add modules, extract Tenant into Foundation, or invent Adapter/MQ/Event for cosmetics.
Do not open a sixth mother domain. Do not start Phase 2 feature work for packaging.
```

## Locked shape

```text
                    open-biz-platform
                           │
              ┌────────────┴────────────┐
              │                         │
      openbiz-saas-core          openbiz-foundation
        (real shared Tenant)        (thin marker)
              │                         │
        ┌─────┴─────┐              ┌────┴────┬────┐
        │           │              │    │    │    │
   openbiz-iot  openbiz-business  service agent shop
```

Maven layout (after consolidation): **5 mother domains + foundation + saas-core** (no `openbiz-common`).

IoT jar packages (unchanged): `iot.core` / `iot.mqtt` / `access` / `locker` / `charging` / `mes` — industries must not newly depend on each other.

| Module | Status | Next |
| ------ | ------ | ---- |
| openbiz-foundation | Skeleton (thin marker) | **FROZEN** |
| openbiz-saas-core | Tenant live | **do not merge into Foundation** |
| openbiz-iot | v1.0 **FROZEN** | **STOP** |
| openbiz-business | Phase 1 COMPLETE / **FROZEN** | **STOP** |
| openbiz-service | Phase 1 COMPLETE / **FROZEN** (WorkOrder) | **STOP** |
| openbiz-agent | Phase 1 COMPLETE / **FROZEN** (Model+Prompt+Invocation) | **STOP** |
| openbiz-shop | Phase 1 COMPLETE / **FROZEN** (Inventory CAS) | **STOP** |
| Overall | Final Audit PASS (90/100) | **CODE DEVELOPMENT = STOP** |

## Hard bans

- No Customer/Product Core in Foundation
- No Business ↔ IoT Maven dependency
- No Agent import of BizService / DeviceCommandService
- No cross-mother-domain Entity/Mapper/Service coupling
- No Redis/MQ/Nacos/ES “for the platform” without evidence
- No sixth mother domain
- No Shop Phase 2 / Agent Probe / RAG / MCP as packaging work
