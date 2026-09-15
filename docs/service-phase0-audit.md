# Service Phase 0 Audit

> Date: 2026-09-14  
> Nature: **read-only** �� design only, no code/DB/API changes  
> Module today: `openbiz-service` = AutoConfiguration + README + pom (Foundation only)

```text
Phase 0 Status: COMPLETE
Code Modification: 0
Database Modification: 0
API Modification: 0
```

---

## 1. ��ǰ���뿼�Ž��

### 1.1 `openbiz-service` ��״

| Item | Evidence |
|------|----------|
| Source | `OpenBizServiceAutoConfiguration` (empty `@Import` Foundation) |
| Maven | depends on `openbiz-foundation` only |
| Tables / Mapper / Entity / Controller | **none** |
| README candidates | ServiceRequest, WorkOrder, Assignment, ServiceRecord, AfterSale �� **not implemented** |

### 1.2 Platform context (sealed)

```text
IoT v1.0 FROZEN | Business Phase 1 COMPLETE | Agent Phase 1 COMPLETE
Architecture SEALED (docs/architecture-sealed.md)
```

Service is the **fourth mother domain** to design, not a place to reopen Foundation / Customer Core extraction.

### 1.3 Stack facts (relevant to Service)

| Layer | Fact |
|-------|------|
| Java / Boot | 21 / Spring Boot 3.5.16 |
| ORM | MyBatis (no MyBatis-Plus) |
| Money/concurrency patterns | Proven in Business (optimistic lock, idempotency) �� **reuse pattern, not dependency** |
| Redis | RuoYi login/cache only; OpenBiz domains do not require it for Core |

---

## 2. ��ֱ�Ӹ�������

### A. RuoYi base (do not rebuild)

| Capability | Where | Service use |
|------------|-------|-------------|
| Login / JWT / Security | `ruoyi-framework` + `SysLoginController` | Staff auth for WO handlers |
| User | `SysUser` / `SysUserServiceImpl` | **Assignee** = `sys_user.user_id` |
| Dept | `SysDept` | Optional later filter by dept; Phase 1 not required |
| Role / Menu / Permission | `SysRole` / `SysMenu` | Probe/API `@PreAuthorize` later; Phase 1 can use login + tenant only |
| Dict | `SysDictType` / `SysDictData` | WO status labels later; Phase 1 enum/string OK |
| Config | `SysConfig` | Optional feature flags later |
| Notice | `SysNotice` | **not** WO notification engine |
| Oper log / Login log | monitor controllers | Audit of admin actions; not WO timeline |
| File upload | `CommonController` `/common/upload` | Attachments later; Phase 1 skip |
| Quartz | `ruoyi-quartz` | SLA timers later; Phase 1 skip |

**Conclusion:** Service must **not** reimplement User/Role/Dept/File/Login. Assignee points at RuoYi `sys_user`.

### B. SaaS Core (reuse as IoT/Business do)

| Capability | Evidence | Service need |
|------------|----------|--------------|
| `TenantContext` ThreadLocal | `openbiz-saas-core/.../TenantContext.java` | **YES** �� every WO row `tenant_id` |
| `TenantInterceptor` + `TenantResolver` | resolves `sys_user` �� tenant via `OpenbizMember` | **YES** �� HTTP probe path |
| `OpenbizTenant` / `OpenbizMember` | Member = **user?tenant map**, not CRM customer | Reuse for isolation only |
| `TenantService` / `MemberService` | CRUD for membership | Phase 1: read context only |

**Design note (not implemented this phase):** today��s `openbiz-service` pom �� Foundation only.  
When Phase 1 starts, Service should depend on **`openbiz-saas-core`** for TenantContext (same as Business/IoT), **not** invent a second tenant.  
Do **not** put Tenant into Foundation for Service.

### C. Patterns to copy (not modules to depend on)

| Pattern | Source | Apply to Service |
|---------|--------|------------------|
| Local `*TenantGuard` | `BizTenantGuard` | Service-local guard over `TenantContext` |
| Idempotent key on create | Business recharge/order | Optional on WO create |
| Probe under `/openbiz/test/...` | Biz/IoT probes | Phase 1 probe, not product UI |
| Domain AutoConfiguration | all OpenBiz modules | Keep |

---

## 3. ��Ӧ���ظ����������

| Do not rebuild | Why |
|----------------|-----|
| User / Role / Menu / Dept | RuoYi already owns staff identity |
| Tenant / Member mapping | `openbiz-saas-core` |
| Prepaid Account / Ledger / Item / Order | `openbiz-biz-core` |
| Device / Command / MES WorkOrder | IoT / `openbiz-mes` �� **different** �������� (production), not CRM |
| LLM ModelPort / PromptTemplate | `openbiz-agent` �� call later via Tool/API, not import |
| Shop cart / inventory | Shop skeleton |
| Redis / MQ / workflow engine / Camunda | No evidence of need for Phase 1 |
| Shared ��Customer Core�� in Foundation | Architecture seal forbids premature extraction |

---

## 4. Customer �����ж�

### 4.1 Three ��people�� already in the repo

| Name | Module | Meaning (evidence) |
|------|--------|-------------------|
| `sys_user` | RuoYi | Staff who logs in, can be WO assignee |
| `OpenbizMember` | saas-core | Links `user_id` �� `tenant_id` only |
| `OpenbizCustomer` | biz-core | Prepaid **store guest**: name/phone, **always creates Account**, recharge/order/ledger |

`OpenbizCustomer` fields: `tenantId, name, phone, status` �� created with balance account (`BizServiceImpl.createCustomer`). Unique `(tenant_id, phone)`. Lifecycle is money-centric, not ticket-centric.

### 4.2 Is Service ��Customer�� the same?

| Dimension | Business Customer | Service requester (CRM) |
|-----------|-------------------|-------------------------|
| Primary job | Hold wallet + consume | Ask for help / get a ticket |
| Must have Account? | **Yes** (created with customer) | **No** |
| Orders | Paid `openbiz_order` | Not required |
| Typical industry | Hair / water / prepaid store | After-sale, install, repair, B2B ticket |

Same Chinese word ���ͻ��� �� same domain object. Architecture Phase 0/2 already warned: do not merge.

### 4.3 Verdict

```text
Shared Customer Core extraction: ��ʱ������
  Evidence: zero proven shared lifecycle; seal says no premature Customer Core.

Phase 1 implementation choice: Service �Լ�ά�� (thin contact)
  Evidence: WO needs name/phone (or party_id) without forcing Account creation;
  Maven-depending on openbiz-biz-core would couple Service �� Business (forbidden by default).

ֱ�Ӹ��� OpenbizCustomer / BizService: NO for Phase 1
  Evidence: createCustomer always opens Account; semantic mismatch.
```

Optional later (only with real product need): store `biz_customer_id` as **opaque reference** via API/Adapter �� still not Entity import.

---

## 5. Service ��С Core ����

Candidates from skeleton README, judged for **Phase 1**:

| Concept | Verdict | Reason |
|---------|---------|--------|
| **WorkOrder** | **������** | Core fact of Service domain; carries status machine |
| **Assignment** | **�����У��������ֶΣ�** | Prove assign to `sys_user`; separate `Assignment` table can wait |
| Contact / Party (thin) | **�����У�����** | name + phone on WO or tiny `ServiceParty`; **not** full CRM Customer |
| **Customer** (full CRM) | **�����Ӻ�** | Contacts + history/tags/360�� not needed to prove WO loop |
| **ServiceRequest** | **�����Ӻ�** | Collapse ��request�� into WO create for Phase 1 |
| **ServiceRecord** | **�����Ӻ�** | Completion note on WO enough for first loop |
| **AfterSale** | **��Ӧ���У�Phase 1��** | Extra product surface; no validation need yet |
| Appointment / SLA / Escalation | **��Ӧ���У�Phase 1��** | Future-proofing |
| Rating / CSAT | **�����Ӻ�** | Nice-to-have after complete |

**Minimal Core nouns for Phase 1:**

```text
WorkOrder (+ contact fields + assignee_user_id + status)
```

Everything else is optional packaging later.

---

## 6. ��Сҵ��ջ�

### Rejected as Phase 1 (too wide)

```text
Request �� WO �� Assign �� Accept �� Process �� Record �� Complete �� AfterSale �� Rate
```

### Recommended Phase 1 loop (what to **validate**)

```text
Staff (sys_user + tenant)
  �� Create WorkOrder (contact name/phone + title/content)
  �� Assign (assignee_user_id)
  �� Accept (assignee only)
  �� Complete
```

Hard checks:

1. Tenant isolation (cross-tenant 404)
2. Illegal status jump rejected (e.g. CREATED �� COMPLETED without ACCEPT if ACCEPT is required �� pick one linear path and enforce)
3. Unauthenticated 401 (probe style)
4. Assignee must be a real staff id (or at least non-null when ASSIGNED)
5. No Business/IoT Maven dependency

**Not** in Phase 1 loop: payment, device open_door, Agent summary, Shop return, Redis queue.

---

## 7. ����ģ�ͺ�ѡ����� only �� ��������

### Option A (preferred for Phase 1) �� single table

```text
openbiz_work_order
  id
  tenant_id
  title
  content
  contact_name
  contact_phone
  status          -- CREATED | ASSIGNED | ACCEPTED | COMPLETED | CANCELLED
  assignee_user_id  -- sys_user.user_id, nullable until assigned
  complete_note     -- optional short text
  idempotent_key    -- optional unique (tenant_id, key)
  create_time / update_time
```

### Option B (only if contact reuse across many WOs is proven later)

```text
openbiz_service_party (id, tenant_id, name, phone)
openbiz_work_order (... party_id ...)
```

Do **not** create `openbiz_customer` in Service. Do **not** reuse `openbiz_customer` table from Business.

### Status (minimal)

```text
CREATED �� ASSIGNED �� ACCEPTED �� COMPLETED
              �K CANCELLED (from CREATED/ASSIGNED only)
```

No QUEUED / RETRYING / SLA_BREACH in Phase 1.

---

## 8. Service ������ĸ��߽�

```text
                    Foundation (thin)
                           ��
              ���������������������������੤������������������������
              ��            ��            ��
         saas-core      (later)     (no edge)
              ��
           Service
```

| Peer | Relation | Default rule |
|------|----------|--------------|
| **IoT** | Field service may later open a door / check device | **No** Maven dep; future Tool/API to DeviceCommand |
| **Business** | Same human may have wallet + ticket | **No** Entity/Mapper import; optional opaque `biz_customer_id` later via API |
| **Agent** | Summarize WO / draft reply | Agent �� Tool �� Service API later; Service does not depend on Agent |
| **Shop** | Return/after-sale commerce | Shop owns return order; Service WO may reference later �� **no** Phase 1 link |

Current skeleton depends only on Foundation �� correct for empty module.  
Phase 1 should add **saas-core** (Tenant), still **not** biz/iot/agent/shop.

---

## 9. ���Լ�ֵ

What Service should demonstrate (aligned with what OpenBiz already proved elsewhere):

| Skill | Show in Service? | How |
|-------|------------------|-----|
| **Status machine** | **Primary** | Explicit allowed transitions |
| **Assignment** | **Primary** | assignee = RuoYi user |
| **Tenant isolation** | **Primary** | saas-core pattern |
| Transaction | Yes | Assign/accept/complete atomic |
| Idempotency | Optional | Create WO with key |
| Permission | Light | Login required; fine RBAC can wait |
| Audit | Light | update_time + optional oper log later |
| CRUD only | **Insufficient alone** | Must not be ��just CRUD table�� |
| Optimistic lock / money | Already in Business | Do **not** invent fake concurrency |
| Redis / MQ / workflow engine | **No** | Inflates without evidence |

Resume line (after Phase 1 green):

> Fourth OpenBiz domain: multi-tenant work-order state machine + staff assignment on RuoYi users, without coupling to prepaid Business or IoT.

---

## 10. Phase 1 ����

When an explicit Phase 1 order arrives (not now):

1. Add Maven dep: `openbiz-saas-core` (+ ruoyi-common as peers do).
2. One table `openbiz_work_order` (Option A).
3. Service API: create / assign / accept / complete / get (+ tenant guard).
4. Probe controller under `/openbiz/test/service/**` (same style as Biz).
5. Unit tests: transition matrix + tenant isolation; optional MySQL test.
6. **Stop.** No AfterSale, no Agent, no Biz Customer sync, no frontend.

Acceptance sketch:

```text
[x] Create WO
[x] Assign
[x] Accept
[x] Complete
[x] Illegal transition rejected
[x] Cross-tenant 404
[x] Unauthenticated 401
[x] IoT/Business/Agent/Shop Modification = 0
```

---

## 11. ��ȷ����ʲô

```text
- No full CRM (leads, opportunities, 360 profile, tags, marketing)
- No ServiceRequest / AfterSale / Appointment / SLA engine in Phase 1
- No shared Customer Core / Foundation Customer
- No Maven dependency on Business / IoT / Agent / Shop
- No Camunda / Activiti / Flowable
- No Redis queue / MQ for tickets
- No copying MES WorkOrder semantics into Service
- No Agent RAG over tickets
- No Shop return flow
- No Vue pages in Phase 1
- No architecture un-seal / Foundation fattening
```

---

## 12. �������ȷ����

| # | Item | Recommendation |
|---|------|----------------|
| 1 | Should Phase 1 require ACCEPT or allow ASSIGNED �� COMPLETED? | Prefer include ACCEPT (proves assignee action) |
| 2 | Contact as columns vs `ServiceParty` table | Prefer columns until multi-WO-per-party is needed |
| 3 | When to link `biz_customer_id` | Only after a real product story (e.g. prepaid shop + repair) |
| 4 | pom: Foundation-only vs saas-core | Phase 1 must add saas-core; record now, change only in Phase 1 |
| 5 | Overlap naming with MES ��WorkOrder�� | Keep package `com.openbiz.service` / table `openbiz_work_order`; docs say CRM WO �� MES WO |
| 6 | Priority vs Shop | Per product plan: Service next; Shop remains paused |

**����ȷ�Ϻ��ٿ� Phase 1��**

1. �ջ��Ƿ���� `CREATED �� ASSIGNED �� ACCEPTED �� COMPLETED`��  
2. Contact �Ƿ���� ��WO �� name/phone �ֶΡ� ���Ƕ��� Customer ����  
3. Phase 1 �Ƿ��������������Ӷ� `openbiz-saas-core` ��������

---

## Change tally (this phase)

| Metric | Count |
|--------|------:|
| �޸�ҵ������ļ� | **0** |
| �����ļ���������� Markdown�� | **1** (`docs/service-phase0-audit.md`) |
| ɾ���ļ� | **0** |
| DB Schema �޸� | **0** |
| API �޸� | **0** |
| IoT �޸� | **0** |
| Business �޸� | **0** |
| Agent �޸� | **0** |
| Foundation �޸� | **0** |
| SaaS Core �޸� | **0** |
| pom �޸� | **0** |

```text
STOP. Service Phase 0 COMPLETE.
Await human review before any Service Phase 1 implementation order.
```
