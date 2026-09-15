# OpenBiz Business �� Phase 0 ��Ʊ���

> ���ڣ�2026-09-14  
> �׶Σ�**ֻ����� + ��Сҵ��ģ�����**  
> **Code / Database / Core Modification��ȫ��Ϊ 0**�����׶ν������� Markdown��

---

## Phase 0 Status

```text
COMPLETE
Code Modification: 0
Database Modification: 0
Core Modification: 0
```

---

## 1. ��ǰ��Ŀ����ջ

���������� **OpenBiz IoT ĸ��Ŀ**���ѷ�壩�����ǿհײֿ⡣

| �� | ��״ |
|----|------|
| ���� | �ٷ� RuoYi-Vue `springboot3`���汾 **3.9.2** |
| Java | **21**��Phase 2.0 �Ѷ��棩 |
| Spring Boot | **3.5.16** |
| ORM | **MyBatis**��`mybatis-spring-boot-starter` 3.0.5�� |
| MyBatis-Plus | **δʹ��** |
| DB | **MySQL** + Druid |
| Redis | RuoYi ������������ Redis���Ự�ȣ���**OpenBiz ҵ���δ�����/������** |
| �ܹ� | ����ģ�黯 Maven |
| ״̬ | **OpenBiz IoT v1.0 FROZEN** |

���ۣ�OpenBiz Business **��ӦĬ������ IoT Core**������Ϊ**�ڶ���ĸ��Ŀ**����ͬ monorepo ��ģ�飬������ֿ⣩�����á�RuoYi + �⻧ϰ�� + �� Core + ��ҵ����������ۣ������Ǹ��� DeviceCommand��

---

## 2. ��ǰ��Ŀ�ܹ�

```text
ruoyi-admin
  ������ openbiz-saas-core     Tenant / Member(�⻧��Աӳ��)
  ������ openbiz-iot-core      Product / Device / Command / Protocol
  ������ openbiz-iot-mqtt      Stub
  ������ openbiz-access|locker|charging|mes   ��ҵ���
  ������ ruoyi-system/...      �û�����ɫ���˵����ֵ䡢��־
```

�ֲ�ϰ�ߣ�Controller / Service / Mapper / Domain����ҵֻ���� Core API��

---

## 3. ������������ Business �Ĺ�ϵ��

| ���� | ��״ | �� Business �ĺ��� |
|------|------|-------------------|
| ��¼ / RBAC | RuoYi `sys_user` / ��ɫ�˵� | ��Ա�˿��ã�**�����ڹ˿ͻ�Ա** |
| �⻧ | `openbiz_tenant` + `TenantContext` | **�ɽ��ģʽ**��tenant_id�� |
| OpenBiz Member | `user_id ? tenant_id` ӳ�� | **������ͻ����**�����ǡ��⻧��Ա�������ǡ���ֵ�˿͡� |
| �ֵ� / ���� / ������־ | RuoYi | �ɸ��û�����ʩ |
| ���� | Spring `@Transactional` | Business ��һ����·�� |
| �˻� / ��� / ��ֵ / ���� | **������** | Business �½� |
| IoT DeviceCommand | ����֤ | **��һ�� Business ������** |

**�ؼ��������飺** Business �˿�ʵ�岻Ҫ���� IoT SaaS ��ͬ�� `OpenbizMember` ���塣���飺

```text
BizCustomer / biz_customer
```

��ģ��ǰ׺ `openbiz_biz_member`�������ĵ�����ȷ����IoT Member �� Business Customer����

---

## 4. OpenBiz Business Ŀ��

��֤��

> һ���㹻�򵥡��������� Java ϰ�ߵ�ҵ�� Core���ܷ񸲸�������ˮվ��ϴ�������ݡ������**С���ֵ����**����Ŀ��

��һ��ֻ��֤��·��

```text
Customer �� Account �� Recharge �� Consume(via Order) �� Ledger
```

**����**����ȫϵͳ / ˮվȫϵͳ / �̳ǡ�

---

## 5. ��Сҵ��ģ�ͣ���һ�潨�飩

### ���� Core�����飩

| ���� | ˵�� |
|------|------|
| **Customer** | �ŵ�˿ͣ��ֻ���/������������ tenant |
| **Account** | �˿��ʽ��˻���**�����** |
| **AccountLedger** | �ʽ���ˮ����ֵ/���ѣ�����ǰ����� |
| **Item** | ��������������Ʒ���� type ���֣� |
| **Order** | ���ѵ��ݣ��� 1��N �У����ۿ����� |

### ��һ�治�� Core

| ���� | ˵�� |
|------|------|
| **Package / �ο�** | ��������״̬���������**�ݻ�** |
| Payment / Coupon / Points / Inventory | ��֤�ݣ��ݻ� |
| Hair / WaterStation ר�ñ� | Industry ���������������� |

### �Ƽ�ģ���з�

```text
OpenBiz Business���������ĸ��Ŀ����� Maven ģ���飩
��
������ Infrastructure��RuoYi / ��ѡ���� Tenant ģʽ��
��
������ openbiz-biz-core
��   ������ Customer
��   ������ Account + AccountLedger
��   ������ Item
��   ������ Order (+ OrderItem)
��
������ Industry��Phase 1+ �ٿ���
    ������ openbiz-biz-hair      # ��ѡ������ʾ����/����
    ������ openbiz-biz-water     # ��ѡ������ʾ����/����
```

��һ���������ԣ�**ֻ�� Core + Probe**����ҵ���ò�ͬ Item ������֤���ã����� IoT���� Core ����ҵ���������ƣ���

---

## 6. Core / Industry �߽��

| ���� | �Ƿ� Core | ���� |
|------|-----------|------|
| Customer���˿ͣ� | **YES** | С��ͨ�ã�����ҵ���� |
| Account | **YES** | ����ǳ�ֵ�������� |
| Recharge | **YES��������** | ����Ϊ Ledger ���� + ���񷽷����ɲ�������ҵ����� |
| Consume | **YES��������** | �� Order �ۿ� + Ledger���Ƕ��������� |
| Item | **YES** | SERVICE / PRODUCT ͳһһ�ű����� |
| Order | **YES������** | ˮվ����Ʒ������������Ҫ�����ݡ�������ˮ���� |
| Package / �ο� | **NO���ݻ���** | ���Ӷȸߣ��������������� |
| Hair | Industry | ��ҵ��ʾ������ Core |
| WaterStation | Industry | ͬ�� |
| Payment��΢��/֧������ | �ݻ� | ��һ�桸����ʽ��ֵ������ |
| Coupon / Points / Level | �ݻ� | Ӫ���� |
| Inventory / ��Ӧ�� | �ݻ� | ˮվ�ɺ��� |
| ���ŵ� / ��� / CRM | �ݻ� | ��֤�� |
| IoT Device / Command | **���� Business Core** | ��һĸ��Ŀ���� |

---

## 7. Member / Customer ��ƽ���

### IoT �� Member �� Business �˿�

| | IoT `openbiz_member` | Business Customer |
|--|----------------------|-------------------|
| ���� | ϵͳ�û������⻧ | �������ѵĹ˿� |
| ���� | `sys_user.user_id` | �ֻ��� / ����Ϊ�� |
| ��� | �� | �� Account |

### �Ƿ�� Core��

**YES**���� Customer ��������

### ��һ���ֶΣ����鿳����С��

```text
id, tenant_id, mobile, name, status, create_time, update_time
```

### ��ȷ��������һ�棩

- ��Ա�ȼ������֡���ǩ����������Ӫ�����Ƽ��ˡ��࿨����ϵ

### �ȼ� / ���֣�

**������**

---

## 8. Account ��ƽ���

### �Ƿ������ Customer��

**YES��**  
Customer ���ˣ�Account ��Ǯ��1:1 �𲽣�һ�˿�һ����˻�����

### ��� vs ������

��һ�� **ֻ֧����DECIMAL��**��

������ / ��ʱ = Package��**�ݻ�**������ Account ��ɡ����+����+���֡��ϵ۶���

### �����ֶ�

```text
id, tenant_id, customer_id, balance, version, status, create_time, update_time
```

`version`���ֹ����������ƣ��� ��18����

---

## 9. Recharge / Consume ���

### ģ�͹�ϵ���Ƽ���

```text
Recharge(service)
  �� Account.balance += amount
  �� AccountLedger(type=RECHARGE, amount>0, balance_before/after)

Consume(service)  // �� placeOrderAndPay
  �� ���� Order (+ OrderItem)
  �� Account.balance -= total
  �� AccountLedger(type=CONSUME, amount<0 �� amount>0+direction, balance_before/after, order_id)
```

**��Ҫ**�ѡ���ֵ�������ѡ������� Order ƽ�е����׶������ġ�

### Ledger �����ֶ�

```text
id, tenant_id, account_id, customer_id,
txn_type (RECHARGE|CONSUME|ADJUST),
amount,                  -- Լ�������������� direction ����Ų��Զ�ѡһ���ĵ�д��
balance_before, balance_after,
biz_type, biz_id,        -- ORDER / RECHARGE_REQUEST
idempotent_key,          -- ���ظ��ύ��Ӧ�ò���أ������ֲ�ʽ��
operator_user_id,        -- ��Ա��sys_user��
remark, create_time
```

���ȣ�`DECIMAL(12,2)`����ֹ `double`��

---

## 10. Item ���

### ͳһ Item + type������ Product/Service �ֱ���

**�Ƽ���һ�� `Item` + `item_type`��SERVICE | PRODUCT����**

| ά�� | �ж� |
|------|------|
| ���Կɽ����� | ���������������������֡����� |
| ����ϰ�� | С��ϵͳ����һ����Ʒ/����� |
| ������ | ����=SERVICE��ˮվ=PRODUCT��ͬ CRUD |
| ���Ӷ� | �������ױ� + ���� Service |

��һ���ֶΣ�

```text
id, tenant_id, item_code, item_name, item_type, price, status, create_time, update_time
```

������SKU����񡢿�桢�ɱ�����Ӧ�̡�

---

## 11. Order ���

### �����Ƿ���Ҫ������

��Ҫ�����ݡ�����˭����ʱ��ʲô���񡢶���Ǯ���Ƿ��ѿۿ���Ժܱ���

### ˮվ�Ƿ���Ҫ������

��Ҫ��������Ʒ�ϼơ�

### Order vs Consume

| | Order | Consume / Ledger |
|--|-------|------------------|
| ��ʲô | ��ҵ���ݣ�����ʲô�� | �ʽ�䶯��ʵ |
| ��ϵ | 1 Order �� 1 �οۿ���ˮ����һ�棩 | Ledger.biz_id �� order_id |

**��Ҫ**Ĭ�ϡ���������ֻ����ˮû�ж�������  
**��Ҫ**Ĭ�ϡ�Order = Consume ͬһ�ű�����

��һ�潨�飺

```text
Order          -- header: customer_id, total_amount, status(PAID), ...
OrderItem      -- item_id, qty, price, line_amount
```

״̬������`CREATED` ��ʡ�ԣ�ֱ�� `PAID`���µ����ۿ�ɹ�����ʧ�ܻع���

---

## 12. Package / �ο� �� �Ƿ�����һ�� Core��

**���ۣ��ݻ���**

���ɣ��ο��漰ʣ����������� Item�����ڡ����ֺ�������� Account �ӡ�������ɶ��ʲ�����������������Ϊ Phase 2+ ��֤��**��Ϊ���ǽ�������ǰ���ӻ�**��

---

## 13. ������֤������Phase 1 Ŀ�꣩

```text
��Ա��¼��tenant A��
  �� �����˿� ����
  �� ���� Account balance=0
  �� ��ֵ 1000 �� balance=1000��Ledger RECHARGE
  �� Item��������price=50 type=SERVICE
  �� �µ����Ѽ��� ��1 �� Order PAID��balance=950��Ledger CONSUME
```

���⻧��tenant B ���ɼ������˻���

---

## 14. ˮվ��֤������Phase 1 Ŀ�꣩

```text
��Ա��¼��tenant A��
  �� �˿� + �˻�
  �� ��ֵ 500
  �� Item��Ͱװˮ��price=20 type=PRODUCT
  �� �µ� Ͱװˮ ��2 �� total=40 �� balance=460
```

������ **����ͬһ�� Core API**��������� Item ����/չʾ��

---

## 15. ��һ���ֹ���� Core

- ΢��֧�� / ֧���� / �κε�����֧���ص�
- �Ż�ȯ�����֡���Ա�ȼ���Ӫ����������CRM
- ���ŵ���֯����Ա�����
- ��桢��Ӧ������������
- �˿���м���ծ����һ��ɲ�����
- �����ײ� / �ο� / ��ϴ���
- Workflow��MQ���ֲ�ʽ����Redis �ֲ�ʽ����΢������
- IoT �豸���ơ�MES���Ž�

�Ժ������**��֤�ݲ��� Core**��

---

## 16. ���ݱ����飨��Ƽ������׶β�������

```text
biz_customer
biz_account
biz_account_ledger
biz_item
biz_order
biz_order_item
```

ȫ���� `tenant_id`��  
**���޸�** IoT �� `openbiz_*` �� `sys_*`��

---

## 17. API ���飨Probe ������������

```text
POST /openbiz/biz/test/customers
POST /openbiz/biz/test/accounts/{customerId}/recharge   { amount, idempotentKey }
POST /openbiz/biz/test/orders                           { customerId, items:[{itemId,qty}] }
GET  /openbiz/biz/test/accounts/{customerId}
GET  /openbiz/biz/test/ledgers?customerId=
```

���ݣ���Ա LoginUser���⻧��TenantContext��**��ֹ** body �� tenantId ��Ϊ�����⻧��

---

## 18. ���� / ��������

### ջ

```text
@Transactional + MySQL InnoDB
```

�������ֲ�ʽ����MQ ����һ�¡�Redisson ����

### �ۿ��

���ֶ����ϡ���ͨ Java ��Ŀ�����Ƽ���һд���淶��

**���� A���Ƽ���ѧ���������ֹ���**

```text
UPDATE biz_account SET balance=?, version=version+1
WHERE id=? AND version=? AND balance>=?
```

ʧ������ҵ���쳣 / ���޴����ԡ�

**���� B��ͬ����ʵ����������**

```text
SELECT ... FROM biz_account WHERE id=? FOR UPDATE
```

ͬ�����ڸ���д��ˮ��д������

**��һ�潨����÷��� A��version��**�������������鶼�ɾ���С����Ҳ���á�

### �ݵ�

��ֵ / �µ��� `idempotent_key` Ψһ�������ظ����󷵻�ԭ�����  
����ȡ IoT Command��ֻ�� key �����ء��Ľ�ѵ����Business **Ҫ��������**����

---

## 19. ��������֤���������� IoT �����ۣ�

```text
Phase 1  Biz Core ��С�ջ������⻧��ʾ���ɣ������� tenant_id��
Phase 1.5  Hair ������֤��SERVICE Item��
Phase 1.6  Water ������֤��PRODUCT Item��
Ҫ��Biz Core Modification = 0
```

������

```text
ͬһ Customer/Account/Order API
  �� ������ˮվ�� Item ��ͬ
```

��ֹ���ơ�80%������ reuse-matrix �� VERIFIED��

---

## 20. Ǳ�ڹ�����Ƶ�

1. �� IoT `Member` �� Business Customer ���һ�ű�  
2. Account ͬʱ�������+����+����  
3. Ϊ֧����ǰ�� MQ / ����  
4. Product �� Service ������ Core  
5. ��һ���� Package/�ο�/�Ż�ȯ  
6. �� Business ���� `openbiz-iot-core`  
7. DDD �ۺϸ���CQRS��Event Sourcing  
8. ������ Order ���ġ�״̬��������  

---

## 21. Phase 1 ����

1. **�½�** `openbiz-biz-core`��������ֿ� `open-biz-business`����**��Ҫ��** IoT �ѷ�� Core��  
2. ʵ�֣�Customer / Account / Ledger / Item / Order(+Item)��  
3. ������`recharge`��`createPaidOrder`���µ����ۿ��  
4. ���� + version �ֹ��� + idempotent_key��  
5. Probe + ���� + ����/ˮվ�������ӣ������⻧����  
6. ���⻧����ع顣  
7. Package / Payment��**����**��  
8. ���� `docs/business-phase1-report.md` �� business reuse-matrix��

---

## ��¼���� IoT ĸ��Ŀ�Ĺ�ϵ

| | OpenBiz IoT | OpenBiz Business |
|--|-------------|------------------|
| ��֤���� | �豸�����Ƿ�ɿ���ҵ���� | ��ֵ�����Ƿ�ɿ�С�긴�� |
| Core | DeviceCommand / Protocol | Account / Order / Item |
| ״̬ | **v1.0 FROZEN** | **Phase 0 �����ɣ��� Phase 1** |
| ���� | ������� bug | **�¿�ģ��/�ֿ��ƽ�** |

---

**���׶ν������ȴ� Phase 1 ִ�� Prompt��**
