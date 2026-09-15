# Shop Phase 1 Report

> Date: 2026-09-14  
> Verdict: **Shop Phase 1 COMPLETE**

```text
Product + Inventory(CAS) + ShopOrder(SUCCESS) + OrderItem
Concurrent stock=1 �� exactly 1 SUCCESS, inventory=0
mvn clean test / package �� SUCCESS
Other mother domains Modification = 0
```

---

## 1. Implemented

| Piece | Detail |
|-------|--------|
| Product | `OpenbizShopProduct` + create/get |
| Inventory | `OpenbizShopInventory` + set + **CAS deduct** |
| Order | `OpenbizShopOrder` status **SUCCESS only** |
| OrderItem | price snapshot + line amounts |
| Service | `ShopServiceImpl.placeOrder` one TX |
| Money | `ShopMoney` (local copy of pattern, no biz-core dep) |
| Tenant | `ShopTenantGuard` + saas-core |
| Auth | `CurrentUserPort` / SecurityUtils |
| Probe | `/openbiz/test/shop/**` |

No CREATED/PAYING/SHIPPED fake FSM. No cart/pay/SKU.

---

## 2. Database

`sql/openbiz_shop_1.sql`

| Table | Uniques |
|-------|---------|
| `openbiz_shop_product` | `(tenant_id, product_code)` |
| `openbiz_shop_inventory` | `(tenant_id, product_id)` + `version` |
| `openbiz_shop_order` | `(tenant_id, idempotent_key)` |
| `openbiz_shop_order_item` | FK-like order_id + tenant_id |

```text
DB Schema Change: 4 new shop_* tables only
```

---

## 3. API (Probe)

| Method | Path |
|--------|------|
| POST | `/openbiz/test/shop/products` |
| GET | `/openbiz/test/shop/products/{id}` |
| POST | `/openbiz/test/shop/inventory/set` |
| POST | `/openbiz/test/shop/orders` |
| GET | `/openbiz/test/shop/orders/{id}` |

---

## 4. PlaceOrder Flow

```text
require login + tenant
validate lines / merge qty / sort productId
load products �� snapshot unit_price �� total
for each product (stable order): CAS deduct
insert SUCCESS order + items
commit
```

Any CAS fail / insufficient �� exception �� **full rollback** (no order, no partial deduct).

---

## 5. Inventory CAS

```sql
UPDATE openbiz_shop_inventory
SET quantity = quantity - ?, version = version + 1
WHERE tenant_id = ? AND product_id = ? AND version = ? AND quantity >= ?
```

Affected rows must be 1; limited retries (16). No Redis / SELECT-then-UPDATE in Java only.

---

## 6. Idempotency

- Unique `(tenant_id, idempotent_key)`
- Pre-check returns first SUCCESS order
- Concurrent same key: loser insert �� `IDEMPOTENT_RACE` �� TX rollback; retry loads winner

---

## 7. Transaction

`@Transactional` around createProduct, setInventory, placeOrder.  
Insufficient stock after CAS races: no SUCCESS row left behind.

---

## 8. Tenant Isolation

All selects/updates: `id/product_id + tenant_id`.  
MySQL: tenant-2 cannot get tenant-1 order (404).

---

## 9. Concurrency Test

`ShopMysqlTest.concurrentBuyStockOne`:

```text
stock = 1
2 threads placeOrder (different idempotent keys)
�� SUCCESS = 1, FAIL = 1, inventory = 0
�� SUCCESS order_item count for product = 1
```

---

## 10. Test Results

| Suite | Count | Result |
|-------|------:|--------|
| ShopMoneyTest | 1 | PASS |
| ShopServiceImplTest | 7 | PASS |
| ShopMysqlTest | 3 | PASS |
| **Shop total** | **11** | PASS |
| Full reactor | green | PASS |
| `mvn clean package -DskipTests` | | PASS |

---

## 11. Cross-domain Dependency Audit

```text
openbiz-shop
  �� openbiz-saas-core   YES
  �� ruoyi-common        YES
  �� openbiz-biz-core    NO
  �� openbiz-iot-*       NO
  �� openbiz-service     NO
  �� openbiz-agent       NO
  �� openbiz-foundation  NO
```

```text
IoT Modification = 0
Business Modification = 0
Service Modification = 0
Agent Modification = 0
Foundation Modification = 0
SaaS Core Modification = 0
```

Admin already imported Shop AutoConfiguration from Architecture Phase 2.

---

## 12. Change Tally

| Metric | Count |
|--------|------:|
| Shop module files | ~30 |
| New SQL | 1 |
| Report | 1 |
| README (shop + root status) | yes |
| Deleted | 0 |
| New probe endpoints | 5 |
| Other domain code edits | **0** |

---

## 13. Known Limitations

- No payment / cart / SKU / logistics / refund
- Order status = SUCCESS only
- HTTP live probe not run this session (MySQL + unit covered)
- Idempotent race returns CONFLICT requiring client retry (safe)

---

## 14. Explicitly Deferred

```text
SKU, Category, Cart, Payment, Coupon, Marketing, Logistics,
AfterSale, Business Customer checkout, Agent recommend, multi-warehouse
```

---

```text
Shop Phase 1 COMPLETE
STOP.
```

Five mother domains MVP now complete. Do not open a sixth domain or expand Shop ecommerce without a new explicit order.
