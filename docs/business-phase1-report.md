# OpenBiz Business �� Phase 1 Report

> ���ڣ�2026-09-14  
> ���ۣ�**OpenBiz Business Phase 1 COMPLETE**  
> IoT Core Modification��**0**���� admin װ�� / �� POM ���أ��� integration-only��

---

## �����嵥

```text
[x] Customer PASS
[x] Account PASS
[x] Recharge PASS
[x] Consume PASS
[x] Ledger PASS
[x] Order PASS
[x] Idempotency PASS
[x] Concurrency PASS
[x] Tenant Isolation PASS
[x] Unauthenticated PASS
[x] Hair PASS
[x] WaterStation PASS
[x] Transaction Consistency PASS
[x] Unit Test PASS
[x] Package PASS
[x] IoT Core Modification = 0
[x] No unnecessary infrastructure
```

---

## 1. ʵ������ģ��

```text
openbiz-biz-core
```

- ������`openbiz-saas-core` + `ruoyi-common` + MyBatis + spring-tx  
- **������** `openbiz-iot-core` / mqtt / �κ���ҵ IoT ���  
- Hair / WaterStation��**�� Item ����**���޶�����ҵģ�飨Core ���޸���֤��

integration-only��

- �� `pom.xml` module + dependencyManagement  
- `ruoyi-admin` ����  
- `OpenBizBootstrapConfiguration` Import  

---

## 2. ʵ��������

SQL��`sql/openbiz_biz_1.sql`

| �� | ��; |
|----|------|
| `openbiz_customer` | ҵ��˿ͣ��� IoT `openbiz_member`�� |
| `openbiz_account` | 1:1 ����˻� + `version` |
| `openbiz_account_ledger` | RECHARGE / CONSUME ��ˮ |
| `openbiz_item` | SERVICE / PRODUCT |
| `openbiz_order` | ���ѵ��� |
| `openbiz_order_item` | ����Ŀ������ unit_price / amount�� |

���ӣ�

- Tenant1 Hair��`CUT` 50 SERVICE��`DYE` 200 SERVICE  
- Tenant2 Water��`WATER` 20 PRODUCT��`MINERAL` 3 PRODUCT  

---

## 3. Core ���սṹ

```text
openbiz-biz-core
������ Customer
������ Account (+ version)
������ AccountLedger
������ Item
������ Order / OrderItem
������ BizService (recharge / placeOrder / queries)
������ Money (BigDecimal scale=2)
������ Probe /openbiz/test/biz/**
```

---

## 4. Customer / Account / Ledger

```text
createCustomer �� Customer + Account(balance=0)
recharge �� balance�� + Ledger(RECHARGE, before/after)
placeOrder �� Order + OrderItems + balance�� + Ledger(CONSUME)
```

Ledger `amount` �������������� `txn_type` ���

---

## 5. Order / OrderItem

- Order��`total_amount`��`status=SUCCESS`��`idempotent_key`  
- OrderItem�������µ�ʱ `unit_price` / `amount`�����º�� Item �ּۣ�  

---

## 6. ����

- DB��`DECIMAL(12,2)`  
- Java���� `BigDecimal`���� `Money.of(...)` �̶� scale=2  
- ��ֹ double/float  

---

## 7. �������Ʒ���

**�ֹ��� `version`��Ψһ������**

```sql
UPDATE openbiz_account
SET balance = ?, version = version + 1
WHERE id = ? AND tenant_id = ? AND version = ?
```

ʧ�������޴����ԣ�16�����ľ��״���������ع���  

ѡ�����ɣ��� Phase 0 ����һ�¡����� Redis/FOR UPDATE��������������������

---

## 8. �ݵȷ���

- Recharge / Order��`UNIQUE (tenant_id, idempotent_key)`  
- �Ȳ��д��Order ������ `DuplicateKeyException` �������ж���  
- ͬһ key �ظ�����**���ֻ��һ�Ρ���ˮ/����ֻ�ɹ�һ��**  

---

## 9. Hair ��֤���

```text
Recharge 500 �� 500
Order CUT ��1 �� total 50 �� balance 450
Ledger: RECHARGE + CONSUME
```

Live��18085 / admin����`bal=450`���ݵ��µ�ͬ orderId��

---

## 10. WaterStation ��֤���

```text
Recharge 500 �� Order WATER ��2 �� total 40 �� balance 460
```

Live��ry����`water_total=40 bal=460`��  
**δ�޸� Biz Core ����**������ PRODUCT ���ӡ�

---

## 11. ���⻧

Tenant B��ry������ Tenant A �� customer / account / order �� **404**��  
MySQL ������ Live һ�¡�

---

## 12. δ��֤

�� Token ���� Probe �� body **`code=401`**��

---

## 13. ����һ����

���㣨50 �� DYE 200����

- ʧ�� `INSUFFICIENT_BALANCE`  
- ����� 50  
- �޳ɹ� CONSUME Ledger  
- �� Order ��  

`@Transactional` ��֤ Order ��������ۿ�ʧ�������ع������� + MySQL �⸲�ǣ���

---

## 14. ��������

��ʵ MySQL��`balance=100`�����̸߳����� 50��

- �ɹ� �� 2  
- `balance >= 0`  
- Ledger CONSUME ���� = �ɹ������� = �ɹ�����  
- ��� = 100 ? 50���ɹ���  

---

## 15. ��Ԫ / ���ɲ���

| �׼� | ��� |
|------|------|
| MoneyTest | 1 PASS |
| BizServiceImplTest��Mockito�� | 14 PASS |
| BizMysqlTest����ʵ MySQL�� | 6 PASS |
| **biz-core �ϼ�** | **21 PASS** |

---

## 16. Maven package

```text
mvn -pl ruoyi-admin -am package -DskipTests �� BUILD SUCCESS
mvn -pl openbiz-biz-core -am test �� BUILD SUCCESS
```

---

## 17�C18. Modification ����

| ��Χ | ���� |
|------|------|
| IoT Core / SaaS Core / Access / Locker / Charging / MES ҵ����� | **0** |
| ���� Business Core ģ�� | 1 |
| ����ҵ��� | 6 |
| Redis / MQ / �ֲ�ʽ�� / ֧�� | **0** |

---

## 19. ������Ƽ��

δ���룺Redis��MQ���ֲ�ʽ����֧�������ӻ��桢���������¼����ߡ����󹤳����������桢�� DDD��  

Hair/Water δ�������ҵģ�顪���� Item ������֤��Core ���޸ġ����㹻��

---

## 20. Phase 2 ����

1. ���� Business Core API ���棨�� IoT v1.0 ���ƣ�  
2. ��ѡ���� `openbiz-biz-hair` / `openbiz-biz-water` ������ʾ���ã��Խ�ֹ�� Core��  
3. **�ݻ�**��Package/�ο���֧����ȯ������  
4. �ĵ����룺Customer �� IoT Member  
5. �����п� Agent �����ӣ��Ȱ� IoT + Business ����ĸ��Ŀ�����  

---

## Live ժҪ���˿� 18085��

| Case | Result |
|------|--------|
| anon | 401 |
| Hair ��ֵ�ݵ� + ���� | 500��450�������ݵ� |
| Water ��Ͱˮ | 40 / ��� 460 |
| ���⻧ | 404 |
| ���� | 400 + ��� 50 |

---

**Phase 1 COMPLETE��ֹͣ���ȴ���һָ�**
