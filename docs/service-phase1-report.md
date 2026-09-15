# Service Phase 1 Report

> Date: 2026-09-14  
> Verdict: **OpenBiz Service Phase 1 COMPLETE**

```text
CREATED �� ASSIGNED �� ACCEPTED �� COMPLETED   PASS
CREATED|ASSIGNED �� CANCELLED                PASS
Illegal transitions                         REJECTED
Tenant isolation                            PASS (MySQL)
Assignee-only accept/complete               PASS
mvn clean test                              SUCCESS
mvn clean package -DskipTests               SUCCESS
```

---

## 1. Implemented

| Piece | Detail |
|-------|--------|
| Domain | `OpenbizWorkOrder`, `WorkOrderStatus`, `WorkOrderTransitions` (single rule source) |
| API | `WorkOrderService`: create / assign / accept / complete / cancel / get |
| Ports | `CurrentUserPort` (SecurityUtils), `StaffUserPort` (ISysUserService) |
| Persistence | MyBatis `OpenbizWorkOrderMapper` + XML |
| Probe | `/openbiz/test/service/work-orders/**` |
| Tenant | `ServiceTenantGuard` + `TenantContext` via **saas-core** |

Contact = `contact_name` + `contact_phone` on WorkOrder.  
Assignee = RuoYi `sys_user.user_id`.  
No Customer / ServiceParty / ServiceRequest / AfterSale.

---

## 2. Database

```text
sql/openbiz_service_1.sql
�� openbiz_work_order
```

Columns: id, tenant_id, title, content, contact_name, contact_phone, status,  
assignee_user_id, complete_note, idempotent_key, create_time, update_time  

Unique: `(tenant_id, idempotent_key)`

```text
DB Schema Change: 1 new table (Service only)
IoT/Business/sys_* tables: 0
```

---

## 3. API (Probe)

| Method | Path | Action |
|--------|------|--------|
| POST | `/openbiz/test/service/work-orders` | create |
| POST | `/openbiz/test/service/work-orders/{id}/assign` | assign |
| POST | `/openbiz/test/service/work-orders/{id}/accept` | accept |
| POST | `/openbiz/test/service/work-orders/{id}/complete` | complete |
| POST | `/openbiz/test/service/work-orders/{id}/cancel` | cancel |
| GET | `/openbiz/test/service/work-orders/{id}` | get |

Login required (RuoYi security default). Tenant from `TenantContext` (never client body).

---

## 4. State Machine

```text
CREATED  �� ASSIGNED, CANCELLED
ASSIGNED �� ACCEPTED, CANCELLED
ACCEPTED �� COMPLETED
COMPLETED / CANCELLED �� (none)
```

Rejected examples (unit-tested):  
`CREATED��COMPLETED`, `CREATED��ACCEPTED`, `ASSIGNED��COMPLETED`,  
`COMPLETED��*`, `ACCEPTED��CANCELLED`.

All transitions go through `WorkOrderTransitions` (+ CAS-style `WHERE status=from` updates).

---

## 5. Tenant Isolation

- Every row has `tenant_id` from `TenantContext`
- Select/update always `id + tenant_id`
- MySQL test: tenant 1 completes WO; tenant 2 get/complete �� **404**

---

## 6. Authentication

- `CurrentUserPort.requireUserId()` on every service method
- Accept/Complete: current user **must** be `assignee_user_id` else **403**
- Assign: `StaffUserPort.requireActiveUser` against `sys_user`
- Probe under authenticated paths (same as Biz); anonymous �� **401** at Spring Security (not re-implemented)

---

## 7. Tests

| Suite | Count | Result |
|-------|------:|--------|
| `WorkOrderTransitionsTest` | 3 | PASS |
| `WorkOrderServiceImplTest` | 8 | PASS |
| `WorkOrderMysqlTest` | 3 | PASS (real MySQL `ry-vue`) |
| **Service total** | **14** | PASS |
| Full reactor (IoT/Biz/Agent/��) | green | PASS |

Covers: happy path, illegal transition, non-assignee, no-tenant, cancel rules, idempotent create, cross-tenant.

---

## 8. Real HTTP / MySQL Validation

| Check | Result |
|-------|--------|
| MySQL schema + loop + cross-tenant | **PASS** (`WorkOrderMysqlTest`) |
| Live HTTP Probe on running admin | **NOT RUN this phase** (unit/MySQL sufficient; probe code present) |
| Unauthenticated 401 | Relies on RuoYi SecurityConfig (same as Biz probes) |

To HTTP-probe later: login �� set membership �� `POST /openbiz/test/service/work-orders` ��

---

## 9. Cross-domain Dependency Audit

```text
openbiz-service
  �� openbiz-saas-core     YES (Tenant)
  �� ruoyi-common          YES
  �� ruoyi-system          YES (ISysUserService for assignee only)
  �� openbiz-biz-core      NO
  �� openbiz-iot-*         NO
  �� openbiz-agent         NO
  �� openbiz-shop          NO
  �� openbiz-foundation    NO (saas-core is the real shared layer used)
```

```text
IoT Modification = 0
Business Modification = 0
Agent Modification = 0
Shop Modification = 0
Foundation Modification = 0
SaaS Core Modification = 0
```

Admin: already imported `OpenBizServiceAutoConfiguration` from Architecture Phase 2 �� **no Bootstrap edit** this phase. Parent `pom.xml` already listed `openbiz-service`.

---

## 10. Change Tally

| Metric | Count |
|--------|------:|
| Service module files added/updated | ~25 (src + test + pom + README) |
| New SQL file | 1 (`sql/openbiz_service_1.sql`) |
| Report | 1 (`docs/service-phase1-report.md`) |
| Deleted files | 0 |
| New business tables | 1 |
| New probe API endpoints | 6 |
| Other mother-domain code edits | **0** |

---

## 11. Known Limitations

- No Vue / product UI
- No ServiceRequest / AfterSale / CRM 360
- No SLA / appointment / attachment
- No `biz_customer_id` link
- HTTP live probe not executed in this run (MySQL + unit covered core rules)
- Assignee validity checked via `ISysUserService`; no cross-tenant staff membership rule beyond tenant on WO

---

## 12. Phase 2 Candidates (do not start)

- ServiceParty if contact reuse proven
- Optional opaque Business customer link
- Attachment via RuoYi upload
- Soft RBAC on assign vs create
- ServiceRecord timeline
- Agent Tool to summarize WO

---

```text
OpenBiz Service Phase 1 COMPLETE
STOP.
```

Do not implement CRM / AfterSale / ServiceRequest / frontend until a new explicit order after human review.
