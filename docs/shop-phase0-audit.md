# Shop Phase 0 Audit

> Date: 2026-09-14  
> Nature: **read-only** �� design only  
> Module today: `openbiz-shop` skeleton only

```text
Shop Phase 0 = COMPLETE
Code Modification: 0
Database Modification: 0
API Modification: 0
POM Modification: 0
```

---

## 1. Current State

| Item | Evidence |
|------|----------|
| Source files | `OpenBizShopAutoConfiguration` only (`@Import` Foundation) |
| pom | depends on `openbiz-foundation` only |
| README | lists future Product/Category/Cart/Order/Inventory/Delivery; **none implemented** |
| SQL / Mapper / Entity / Service / Controller / Test | **none** |
| Old ecommerce code in repo | **none** (grep: only docs/skeleton mentions) |
| Admin wiring | already listed in parent pom + Bootstrap `@Import` (empty bean) |

**Verdict:** Shop is a clean empty domain shell. No hidden cart/SKU/payment code to salvage.

---

## 2. Existing Reusable Capability

### 2.1 Must reuse (same as Service/Business)

| Capability | Where | Shop Phase 1 use |
|------------|-------|------------------|
| TenantContext / interceptor | `openbiz-saas-core` | Every row `tenant_id` |
| Login / Security | RuoYi | Probe auth |
| MyBatis + DECIMAL money habits | Business patterns | Amounts as `BigDecimal`; copy `Money` **pattern**, not import biz class |
| Idempotent unique `(tenant_id, key)` | Business order/ledger, Service WO | Place order |
| Optimistic lock `version` + CAS update | Business `openbiz_account.version` | **Inventory stock** |
| MySQL test style | `BizMysqlTest` / `WorkOrderMysqlTest` | Concurrent debit proof |
| Probe style | `/openbiz/test/**` | `/openbiz/test/shop/**` |
| Local `*TenantGuard` | Biz / Service | Shop-local guard |

### 2.2 RuoYi �� reuse, do not rebuild

| Capability | Shop Phase 1 |
|------------|--------------|
| User / Role / Dept | Login only; no shop staff table |
| Dict / Config / File upload | **Defer** (images/category labels later) |
| Quartz | **No** |

### 2.3 Do **not** Maven-depend

```text
openbiz-biz-core   NO
openbiz-iot-*      NO
openbiz-service    NO
openbiz-agent      NO
```

Phase 1 pom should add **`openbiz-saas-core`** (+ ruoyi-common / mybatis / webmvc like Service), replacing empty Foundation-only dependency for real Tenant �� same move Service already made.

---

## 3. Business Boundary

Business already has overlapping **words**, different **jobs**:

| Business object | Real meaning (evidence) |
|-----------------|-------------------------|
| `OpenbizCustomer` | Prepaid store guest; creates **Account** |
| `OpenbizItem` | Priced SERVICE/PRODUCT catalog for **wallet consume** �� **no stock** |
| `OpenbizOrder` | Instant **SUCCESS** consume against Account; needs `customer_id` |
| `OpenbizOrderItem` | Lines of Item qty �� price |
| Account / Ledger | Money truth |

```text
Business Order = ��ֵ�ۿ�ƾ֤
Shop Order     = �п��Լ������Ʒ���׵�
```

**Do not reuse** `openbiz_item` / `openbiz_order` tables or Entities for Shop.

---

## 4. Shop Domain Boundary

### 4.1 Who owns which noun

| Noun | Owner | Notes |
|------|-------|-------|
| Customer / Account / Ledger | **Business** | Shop Phase 1: no customer wallet; buyer = contact fields or `buyer_user_id` optional �� prefer **no Customer table** |
| Item (prepaid SKU-less price list) | **Business** | Not inventory |
| Product (device template) | **IoT** | Thing/device model �� unrelated |
| **Product** (sellable goods) | **Shop** | Own table `openbiz_shop_product` |
| **Inventory** | **Shop** | Own table; CAS stock |
| **ShopOrder** / **ShopOrderItem** | **Shop** | Own tables; name clearly to avoid Biz Order confusion |
| WorkOrder / AfterSale | **Service** | Future link only via API |
| LLM | **Agent** | Future tool only |

### 4.2 Cross-domain rules (Phase 1)

```text
Shop �� Business  NO
Shop �� IoT       NO
Shop �� Service   NO
Shop �� Agent     NO
Shop �� saas-core YES (Tenant)
```

Future (not now): return �� Service WO; AI recommend �� Agent Tool; device sell �� IoT �� **Adapter/API only**.

---

## 5. Phase 1 Minimal Model

### 5.1 Must / Defer / No

| Topic | Verdict | Reason |
|-------|---------|--------|
| Product (single SKU = product itself) | **����** | Catalog atom |
| SKU variants (color/size) | **��ȷ��Ҫ** Phase 1 | No evidence; product_id is enough |
| Category | **�����Ӻ�** | Not needed for stock proof |
| Inventory separate table | **����** | Stock + `version` is the interview core |
| Stock column only on Product | **���ԣ������Ƽ�** | Separate Inventory makes ��stock entity�� clearer; either OK �� **prefer separate Inventory 1:1 product** |
| ShopOrder | **����** | Transaction fact |
| ShopOrderItem | **����** | Multi-line qty deduction |
| Order state machine | **���루����** | Prove illegal jumps; not full ecommerce FSM |
| Idempotency on place order | **����** | Copy Business pattern |
| Inventory optimistic lock | **����** | Concurrent oversell prevention |
| Payment / WeChat / Alipay | **��ȷ��Ҫ** | Out of scope |
| Cart | **��ȷ��Ҫ** | Place order with lines directly |
| Refund / coupon / marketing / logistics | **��ȷ��Ҫ** | Future-proofing |
| Buyer = Business Customer | **��ȷ��Ҫ** | Same Chinese trap as Service |
| Buyer contact on order | **���루����** | `buyer_name` + `buyer_phone` on ShopOrder (like Service contact) |
| Image / CDN | **�����Ӻ�** | |

### 5.2 Recommended tables (design only)

```text
openbiz_shop_product
  id, tenant_id, product_code, product_name, price, status, create_time, update_time

openbiz_shop_inventory
  id, tenant_id, product_id, quantity, version, update_time
  UNIQUE(product_id) or UNIQUE(tenant_id, product_id)

openbiz_shop_order
  id, tenant_id, buyer_name, buyer_phone, total_amount, status,
  idempotent_key, create_time, update_time

openbiz_shop_order_item
  id, tenant_id, order_id, product_id, quantity, unit_price, line_amount
```

---

## 6. State Machine

Minimal ShopOrder statuses:

```text
CREATED  (optional internal �� or skip)
  ��
PAID_SUCCESS   // Phase 1: ��place order�� atomically means stock deducted + success
               // OR name it SUCCESS to mirror Business language carefully

CANCELLED      // only before success if CREATED is exposed
```

**Recommended Phase 1 simplification (strong preference):**

Do **not** expose a long CREATED��PAYING��SHIPPED chain.

```text
placeOrder (idempotent):
  validate stock
  �� CAS deduct inventory
  �� insert order + items with status = SUCCESS
  �� commit
On failure: rollback (no partial deduct)
```

Illegal ��transitions��: insufficient stock, concurrent CAS fail, replay idempotent key, cross-tenant get.

If a tiny FSM is desired for resume storytelling:

```text
SUCCESS  (terminal success)
CANCELLED only if you introduce CREATED �� otherwise skip CANCEL for Phase 1
```

**Recommendation:** Phase 1 order status = **`SUCCESS` only** (like Business), plus clear error paths. Save rich FSM for Shop Phase 2 if needed. Interview value sits in **inventory CAS + transaction**, not shipping states.

Alternative if human insists on FSM:

```text
CREATED �� SUCCESS
CREATED �� CANCELLED
SUCCESS �� (none)
```

placeOrder creates CREATED then immediately SUCCESS in one TX �� weak. Prefer single SUCCESS write.

---

## 7. Concurrency / Idempotency

| Concern | Approach (pattern from Business) |
|---------|----------------------------------|
| Oversell | `UPDATE inventory SET quantity=quantity-?, version=version+1 WHERE product_id=? AND version=? AND quantity>=?` with retries |
| Double submit | Unique `(tenant_id, idempotent_key)` on shop_order; replay returns same order |
| Money | `BigDecimal` scale 2; no float |
| TX | `@Transactional`: deduct all lines + insert order/items or rollback |

Prove with MySQL test: concurrent placeOrder on stock=1 �� exactly one SUCCESS.

---

## 8. Tenant Isolation

Same proof as Service:

```text
Tenant A creates product/order
Tenant B get / buy �� 404 / fail
All queries: id + tenant_id
```

---

## 9. API Candidate (Probe only)

```text
POST /openbiz/test/shop/products          create product (+ init inventory qty)
POST /openbiz/test/shop/inventory/set     set/adjust stock (or include in create)
GET  /openbiz/test/shop/products/{id}
POST /openbiz/test/shop/orders            place order {lines[], buyer*, idempotentKey}
GET  /openbiz/test/shop/orders/{id}
```

No cart, no payment callback, no Vue.

---

## 10. Explicitly Deferred

```text
SKU / Category / Cart / Payment / Refund / Coupon / Marketing
Logistics / Tracking / After-sale (Service)
Business Customer wallet checkout
IoT device as product
Agent recommend
Multi-warehouse / reservation / soft lock beyond optimistic version
Rich order FSM (ship/deliver)
```

---

## 11. Cross-domain Dependency Rules

```text
                    saas-core (Tenant)
                         ��
                        Shop
                         ��
         ���������������������������������੤������������������������������
         ?               ?               ?
      Business          IoT           Service/Agent
```

| Rule | Status |
|------|--------|
| No Entity/Mapper import from other domains | **Hard** |
| No shared Product/Order Core in Foundation | **Hard** (architecture sealed) |
| Copy patterns, not modules | **Hard** |
| Naming: `openbiz_shop_*` / `ShopOrder` | **Hard** �� avoid `OpenbizOrder` clash |

---

## 12. Estimated Implementation Time

| Slice | Magnitude |
|-------|-----------|
| DDL + domain + mapper | 0.5�C1 h |
| Product + Inventory + placeOrder TX/CAS | 1.5�C2.5 h |
| Unit + MySQL concurrency test | 1�C1.5 h |
| Probe + report | 0.5 h |
| **Total** | **~���� (3�C5 h)** if stuck to this minimal model |

Inflates if: payment, SKU, cart, or Business coupling are added.

---

## 13. Acceptance Criteria (for future Phase 1)

```text
[ ] Create product + inventory
[ ] Place order deducts stock atomically
[ ] Concurrent orders on qty=1 �� exactly one success
[ ] Idempotent placeOrder replay
[ ] Insufficient stock �� fail, no order row
[ ] Cross-tenant isolation
[ ] Unauthenticated �� 401
[ ] Shop �� Business/IoT/Service/Agent Maven = NO
[ ] Other mother domains Modification = 0
[ ] mvn clean test / package green
[ ] docs/shop-phase1-report.md then STOP
```

---

## Final Phase 1 recommendation

```text
Product + Inventory(version) + ShopOrder + ShopOrderItem
placeOrder = TX(CAS stock ? qty for each line + insert SUCCESS order)
buyer = contact fields on order
Tenant via saas-core
No cart / pay / SKU / Business dependency
```

**Why this is the last mother domain worth building:** it closes the ��inventory concurrency�� story that Business (money CAS) and Service (state machine) do not cover.

---

## Change tally (this phase)

| Metric | Count |
|--------|------:|
| ҵ������޸� | **0** |
| �����ļ� | **1** (`docs/shop-phase0-audit.md`) |
| ɾ�� | **0** |
| DB / API / POM | **0** |
| IoT / Business / Service / Agent / Foundation / SaaS | **0** |

```text
Shop Phase 0 = COMPLETE
STOP.
Do not implement Phase 1 until explicit human order after review.
```
