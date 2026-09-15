# Phase 1.9 �� OpenBiz ������ƣ�Read-Only��

> ���ڣ�2026-09-14  
> **���׶Σ������������޸�**  
> ֤�ݻ��ߣ�Access / Locker / Charging / MES �� **Core Modification = 0**

---

## Phase 1.9 Status

```text
COMPLETE
Code Modification: NO
Core Modification: NO
Report: docs/phase1.9-subtraction-audit.md
```

---

## 1. ��Ʒ�Χ

| ��Χ | ���� |
|------|------|
| ģ�� | saas-core / iot-core / iot-mqtt / access / locker / charging / mes / ruoyi-admin װ�� |
| Mock | MockDoorDevice / Store / ProtocolPort |
| Probe | `/openbiz/test/**` |
| SQL | `sql/openbiz_*.sql`������ RuoYi/quartz �����ű�ϸ�� |
| ���� | `application.yml` / `application-druid.yml` |
| �ĵ� | `docs/*` + README + reuse-matrix |

**δ����** �ع���ɾ�����������������ܡ�commit/push��

---

## 2. ��ǰģ���ͼ

### openbiz-saas-core

| �� | ���� |
|----|------|
| Purpose | ���⻧��Tenant / Member / Context / Interceptor |
| Public API | `TenantService`, `MemberService`, `TenantContext`, `TenantResolver` |
| Dependencies | ruoyi-common��LoginUser / Security�� |
| Used By | ȫ�� OpenBiz ģ�飨�� Context������ҵ�� `TenantGuard` |
| Tests | TenantContext / MemberTenantResolver / TenantInterceptor��9�� |
| Runtime | Interceptor д Context��Live ���⻧ 404 ��֤ |

### openbiz-iot-core

| �� | ���� |
|----|------|
| Purpose | Product / Device / ThingModel / DeviceCommand + Protocol SPI + Mock Door |
| Public API | Product/Device/ThingModel/DeviceCommandService��`ProtocolPort` / Registry��`TenantGuard` |
| Dependencies | saas-core |
| Used By | access / locker / charging / mes��admin Probe |
| Tests | 27���� Mock ���� |
| Runtime | ����ҵ `invoke("open_door")` + Live Probe |

### openbiz-iot-mqtt

| �� | ���� |
|----|------|
| Purpose | `MqttProtocolAdapter` ռλ ProtocolPort |
| Public API | `protocol()=MQTT`��`sendCommand` ������־���� true |
| Dependencies | iot-core |
| Used By | Spring ע�᣻**��**��ҵ�����豸ʹ�� MQTT Э�� |
| Tests | **0** |
| Runtime | **Stub**���� broker�� |

### openbiz-access / locker / charging / mes

| ģ�� | Purpose | �� Core ��� | ��ҵר�� | Tests |
|------|---------|-------------|---------|------|
| access | ����Ȩ��+��¼ | DeviceService + DeviceCommandService | Permission / AccessRecord | 5 |
| locker | ����� | ͬ�� | Locker / Slot / Record | 7 |
| charging | ���� | ͬ�� | Station / Connector / Record | 7 |
| mes | ������ | ͬ�� | Material/BOM/Process/WO/ProductionRecord | 6 |

���������Ѻ��飩��

```text
Industry �� saas-core + iot-core
Core ? Industry   ���޷���������
Industry ? mqtt
```

### ruoyi-admin

| �� | ���� |
|----|------|
| Purpose | ������װ�� |
| Runtime | `OpenBizBootstrapConfiguration` Import ȫ�� OpenBiz ģ�� |

---

## 3. SaaS Core ���

| ���� | ����ҵʵ��ʹ�� | ���� |
|------|----------------|------|
| TenantContext | �ǣ��� TenantGuard / Interceptor�� | **VERIFIED CORE** |
| TenantResolver / MemberTenantResolver | �ǣ���¼��������� | **VERIFIED CORE** |
| Member �� + Mapper | �ǣ������⻧�� | **VERIFIED CORE** |
| TenantInterceptor | �ǣ�HTTP ·���� | **VERIFIED CORE** |
| TenantGuard���� iot-core�� | ����ҵ + IoT Service | **VERIFIED CORE**������ iot �����£������� SaaS Э���� |
| TenantService | �� Probe | **�� API**����ҵ���� |
| MemberService | **���ⲿ���÷�**��Resolver ֱ�� Mapper�� | **POTENTIAL / ����Ϊ���� API ��** |

�ش�ժҪ��

1. ����ҵ���ã�Context + Resolver + Member ���� + Guard��  
2. ƫδ��/����װ��`MemberService` �ӿڲ㡣  
3. Tenant У�飺Interceptor д Context + Guard/Mapper ��ʽ `tenant_id`��**��** MyBatis �Զ��⻧���������֪ծ�����ظ����󣩡�  
4. ��ɾ����Ӱ����ҵ��ĺ�ѡ������¼����`MemberService` ��ǡ�  
5. �޲��Գ��󣺻������ǣ�MQTT ģ���޲⡣

---

## 4. IoT Core ��ƣ�����֤�ݾ���

> �ж��ھ���**��ҵҵ�����**�Ƿ�ֱ�ӻ���������������ɱջ���  
> ����ӡ�= �� `DeviceCommandServiceImpl` �ڲ�������ҵ import��

| ���� | Access | Locker | Charging | MES | �Ƿ��Ҫ | ֤�ݿھ� |
|------|:------:|:------:|:--------:|:---:|:--------:|----------|
| Product | ?��� | ?��� | ?��� | ?��� | **��** | Command �� product.protocol ·�� |
| Device | ? | ? | ? | ? | **��** | ����ҵ `deviceService.get` |
| ThingModel | ? | ? | ? | ? | **��ǰ�����ݲ��У�** | ��ҵ**��**�� ThingModelService��invoke **��**У�� model_json |
| DeviceCommand | ? | ? | ? | ? | **��** | ����ҵ invoke + commandId �� Record |
| ProtocolPort | ?��� | ?��� | ?��� | ?��� | **��** | Command �� Registry �� Port |
| ProtocolPortRegistry | ?��� | ?��� | ?��� | ?��� | **��** | ͬ�� |
| Mock Door | ?��� | ?��� | ?��� | ?��� | **��֤��Ҫ** | ��ǰΨһ��ʵִ���� |

**reuse-matrix ����˵�����ĵ�ծ����**  
ThingModel �� VERIFIED ������ҵ**ƫ�ֹ�**����׼ȷ��

```text
ThingModel = POTENTIAL CORE / seed ���ڣ���������ҵ invoke ��·��
```

���׶�**����** reuse-matrix ����������ļ����ݲ��ԣ����ڱ���������ھ���������ƽ׶θľ����������ۣ�����һ���ĵ�����ʱ�ɸġ�

---

## 5. ProtocolPort ���

��·������ҵһ�£���

```text
Industry Service
  �� DeviceCommandService.invoke
  �� ProtocolPortRegistry.require(product.protocol)
  �� ProtocolPort.sendCommand
  �� MockDoor��protocol=MOCK��
```

| ��� | ���� |
|------|------|
| **VERIFIED CORE ABSTRACTION** | ProtocolPort + Registry + MOCK ʵ�� |
| **POTENTIAL / Stub** | MqttProtocolAdapter������չ�㣬������֤�ݣ� |

��ҵ��**��**���� ProtocolPort ���͡����߽���ȷ��

---

## 6. DeviceCommand ���

| ���� | ���� |
|------|------|
| ����ҵ�Ƿ񶼾����� | **YES** |
| commandId �Ƿ���ҵ���¼�� | **YES**��Access/Locker/Charging/MES Record�� |
| FAILED �Ƿ��в⣿ | **YES**������ҵ���� + iot-core�� |
| PENDING �Ƿ��Ҫ�� | ͬ�� invoke �ڶ���д��� SUCCESS/FAILED��**���첽��������** |
| �ݵȣ� | `idempotentKey` **ֻ�治����** �� **��֤�ݵ��ݵ����** |
| ģ���Ƿ��ã� | �Ե�ǰ����ҵͬ���ջ���**����** |

**��Ҫ**������� MQ / �첽�����ܡ�

---

## 7. ThingModel ���

| ά�� | ��ʵ |
|------|------|
| �� / Service / ���� | �� |
| ���� | DEMO-DOOR �� open_door/close_door JSON |
| ��ҵ���� | **��** |
| Command У�� serviceId��ģ�� | **��** |

���ࣺ

> **POTENTIAL CORE����������⣩+ ��ǰ����·������**  
> ���� DEAD���в��б�����Ҳ��������ҵ VERIFIED REUSE��

---

## 8. Industry Boundary ���

| ��ҵ | ֻ�����ڸ���ҵ�Ĵ��� |
|------|----------------------|
| Access | Permission��AccessRecord��AccessService |
| Locker | Locker��Slot��LockerRecord��LockerService |
| Charging | Station��Connector��ChargingRecord��ChargingService |
| MES | Material/BOM/BomItem/Process/WorkOrder/ProductionRecord��MesService |

���������飺

```text
iot-core / saas-core �� ��ҵģ����������
```

**�߽罡����**

MES ��ע��Material/BOM/Process **�б��� domain���� Mapper �� MesService ʹ��**������ʱֻ�� WorkOrder + ProductionRecord����������Ǽ� > ������·����������Ϊ��ҵ�� **schema ֤�� / �������**���� Core ���⡣

---

## 9. Mock / Probe ���

| ���� | ���� | ˵�� |
|------|------|------|
| MockDoorDevice / Store / ProtocolPort | **A** | ����ҵ Live �뵥������ |
| TenantProbeController | **B** | Phase 1.2 ��֤ |
| IotProbeController | **B** | Phase 1.3/1.4 ��֤ |
| Access/Locker/Charging/Mes Probe | **B** | ����ҵ��֤ |
| MqttProtocolAdapter | **C/Stub** | �� broker�������豸�� MOCK |
| ���޷��֣���·�� DemoController | �� | �� |

����˵����A=���뱣����֤������B=Phase ��֤������C=��ǰ����;��Stub����D/E �����飬���׶β�ɾ��

---

## 10. Dependency ��ƣ����� only��

| �۲� | ���� |
|------|------|
| ��ҵģ��������� mqtt | **����** |
| admin װ���� mqtt | ��ѡ������ǰ��Ϊ��ѡ Profile�������飩 |
| mqtt �޲��� | ���� Stub ����С���⣬���ĵ��� Stub |
| �� Kafka/Redis ҵ���������� OpenBiz | ���� |
| **��**��������� Boot/JDK | ��������������� 1.9 �� |

---

## 11. Database ���

| �� | Used By | Runtime | Required? |
|----|---------|---------|-----------|
| openbiz_tenant / member | SaaS | YES | YES |
| openbiz_product / device | IoT + ��ҵ | YES | YES |
| openbiz_thing_model | IoT Service/seed | ����·�� | **�������ݣ�����·��** |
| openbiz_device_command | IoT + ��ҵ Record | YES | YES |
| access_* / locker_* / charging_* / mes_* | ����ҵ | YES | YES��������� |
| mes material/bom/process | seed | MesService δ�� | ��ҵ�Ǽܣ��ɺ������� |
| quartz / ry_* | RuoYi | YES | ���� |

tenant_id��OpenBiz ҵ���һ����ʽЯ����  
δ���� OpenBiz δʹ�ÿձ���ThingModel ��д�����ѯ API����

---

## 12. Configuration ���

| �� | �ж� |
|----|------|
| server.port 18080 | ����Լ�� |
| Druid/MySQL | ���� |
| captcha DB ���� | ������֤������������Ĭ�������� |
| MQTT broker ר������ | **δ����������� MQTT ��������**���������� Stub�� |
| Redis��RuoYi�� | �����Ự�ȣ��� OpenBiz ҵ����·�� |

---

## 13. Documentation ���

| ���� | ���� |
|------|------|
| �滮���뵱ǰ���� | roadmap/extension �� Snapshot��EventLog��С���򡪡�**Planned** |
| ���ÿھ�ƫ�ֹ� | ThingModel ����ҵ VERIFIED |
| Stub ��д��֧�� MQTT�� | ��ȷ˵����**MQTT ProtocolPort Stub / δ�� broker** |
| ��������� | ���� phase �����������롪���ĵ�ծ |
| ֤����ʵ�� | Core=0 ����ҵ��reuse ̨�ˣ�phase1.5�C1.8 ���� |

�����ĵ���ǩͳһ��`Implemented` / `Verified` / `Stub` / `Planned`��

---

## 14. VERIFIED CORE

���뱣�����г��֤�ݣ�

1. TenantContext + TenantInterceptor + TenantResolver/Member ����  
2. TenantGuard���⻧�Ž���  
3. Product��������Ϊ protocol ·�����壩  
4. Device + DeviceService  
5. DeviceCommand + DeviceCommandService.invoke/get  
6. ProtocolPort + ProtocolPortRegistry  
7. MockDoor*����ǰ��ִ֤���壻����Ϊ����֤�оߡ������ڿ�Ǩ test ���ֲ���ɾ��  
8. RuoYi ��¼ / Security / LoginUser  

---

## 15. VERIFIED REUSE������ҵ��

1. SaaS �⻧�����������  
2. Device ��ѯ��tenant ��Χ��  
3. DeviceCommandService ͬ������  
4. ProtocolPort �����·��MOCK��  
5. commandId �� ��ҵ Record ��׷��  
6. ��ҵ�����ɾ�������ƻ� Core  

---

## 16. POTENTIAL CORE

��һ����ֵ��**��δ������ҵ��·�����֤��**��

1. **ThingModel**���� JSON��δ���� invoke У�飩  
2. ProductService ���� API����ҵ������Command �ڲ��� Mapper��  
3. TenantService / MemberService �������  
4. DeviceCommand.idempotentKey���洢�����أ�  
5. PENDING ��Ϊ�־�ҵ��̬����ǰ��˲̬��  
6. openbiz-iot-mqtt ģ����̬��SPI ռλ��  

---

## 17. FUTURE ONLY

��ǰ�����ɼ���Ͷ�루��������֤�׶Σ���

1. �� MQTT / �豸Ӱ�� / Telemetry  
2. PropertySnapshot / EventLog / DeviceLog��������ĵ���  
3. Workflow / Rule Engine / MQ / Redis Stream  
4. OCPP / �Ʒ� / ֧�� / С���� / ǰ��  
5. MyBatis �Զ��⻧�������ѡ��ǿ���ǵ�ǰ������  
6. ��ҵ���廯���start_charging / start_machine����������δ�� Core  

---

## 18. DEAD / UNUSED�����ƣ�

| �� | ˵�� |
|----|------|
| MemberService �޵��÷� | Resolver ֱ�� Mapper |
| MES Material/BOM/Process Mapper | **�� Mapper ʵ�ֱ�����ʹ��**���� domain+SQL seed�� |
| MqttProtocolAdapter ���м�ֵ | Stub�������豸 protocol=MOCK |
| ThingModel ��ҵ��·�� | �޵��� |

> �ǡ�����ɾ�����嵥�����Ǽ�����ѡ��

---

## 19. Over-engineering Risk�����װ�ƽ̨���ֵĵط���

1. **Ϊ����Ư���� Core ������/��ģ��У��/�첽����**  
2. **�� Mock ���ɶ�Э�����ƽ̨������ iot-core**  
3. **Telemetry + �� MQTT + �������桸һ�����롹**  
4. **�� Access/Locker/Charging/MES ����Ӳ��ɡ�������ҵ���ࡹ**  
5. **�ĵ����ơ�֧�� MQTT / 80% ���á�����֤��**  
6. **�� Core=0 ֤���ϼ����ѵ��塢������ IoT Demo**  

---

## 20. Minimum Core vs Current Core

### Current Core����״��

```text
SaaS: Tenant, Member, Context, Resolver, Interceptor, TenantService, MemberService
IoT:  Product, Device, ThingModel, DeviceCommand,
      ProtocolPort, Registry, TenantGuard,
      MockDoor*, (admin װ��) Mqtt Stub
```

### Minimum Core������Ϊ Access/Locker/Charging/MES ��д��

```text
SaaS
������ Tenant + Member�����������
������ TenantContext
������ TenantResolver��Member��Tenant��
������ TenantInterceptor

IoT
������ Product��protocol �ֶ��㹻��
������ Device
������ DeviceCommand + DeviceCommandService
������ ProtocolPort + ProtocolPortRegistry
������ һ�������е� Protocol ʵ�֣��ֽ� = MOCK��

��֤�оߣ��ɷǡ���Ʒ Core����
������ MockDoor* ��ȼ� test double
```

**�ɲ����� Minimum ��·����** ThingModel ����ʱУ�顢MQTT ģ�顢MemberService/TenantService ��ǡ��ݵȶ�·������ҵ�������

### �Ա�

```text
Current Core
    - ThingModel ��·���������ɽ���Ϊ��ѡ��
    - MQTT Stub ģ�飨�ɱ�� Stub / ��ѡװ�䣩
    - ���ɱ� Service API
    - idempotent δʵ���߼�
        ��
Minimum Core���ϱ���
```

---

## 21. ������һ�׶�

**��Ҫ**���� Telemetry / ������ҵ / С����

�Ƽ�˳��

1. **���������**���˹�ȷ�� A�CF �嵥��  
2. **���̶����ѡ��Java 21 ����**��������������д����ܣ�  
3. **���� Core API ����**����������/��ģ��ǿУ������ Phase ֤�ݣ�  
4. �ٿ�ҵ���򣺻�Ա��ֵ/�ŵ��**��һ��ĸ��Ŀ**����С��Χ�ĵ����루ThingModel/MQTT ��ǩ��

---

## 22. ���׶ν���

OpenBiz ��������ҵ֤����

```text
�� SaaS + IoT ������ �� ����ҵ�����Core = 0
```

���������ʾ������·�Ѿ����ݣ�**��Ҫ֬���ڡ�δ����·���� ThingModel��MQTT Stub���ݵ��ֶΡ��ĵ��ֹۿھ���MES δ�ùǼܱ���**��  

**�����ղ���ȱ���ܣ�������֤��֮��� Core ���֡�**

---

## ��¼��һҳֽժҪ

```text
Current Core     = SaaS�⻧ + IoT Product/Device/Command/Protocol(+Mock) + ThingModel���� + MQTT Stub
Minimum Core     = SaaS�⻧ + Product/Device/Command/Protocol(+һ��ʵ��)
Potential Core   = ThingModel��·�����ݵȡ���Service API��MQTTģ����̬
Dead / Unused    = MemberService�޵��÷���MES���ֹǼ�δ����MQTT��broker
Over-engineering = �첽����/Telemetry/���������Core/������ҵ����/����80%
Next Action      = ���ı����� �� Java 21 ���� �� ���� Core �� ��ҵ��ĸ��Ŀ
```

**ֹͣ��δ�޸��κ��������롣**
