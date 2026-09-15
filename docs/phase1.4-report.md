# Phase 1.4 Report �� ģ���Ž��豸

> ���ڣ�2026-09-14  
> ���ۣ�**Phase 1.4 �������**��Command �� Protocol �� Device State �ջ�����֤��  
> **δ���н��� Phase 1.5**

---

## 1. ʵ�����

- `MockDoorDevice`���ڴ棺OPEN / CLOSED��
- `MockDoorDeviceStore`��`ConcurrentHashMap`��
- `MockDoorProtocolPort`��`protocol=MOCK`��
- `ProtocolPortRegistry`���� `product.protocol` ·�ɣ�
- `DeviceCommandService` �� Registry �� ProtocolPort�����ٵ� Bean ע�룩
- DEMO-DOOR ��Ʒ `protocol=MOCK`��ThingModel �� `open_door` / `close_door`
- Probe��`GET /openbiz/test/iot/mock-devices/{deviceId}`
- ������CLOSED��OPEN��CLOSED���Ƿ����� FAILED�����⻧�Ծܾ�

## 2. δ��ɣ����⣩

- AccessService / �Ž���Ȩ / С���� / ǰ��
- �� MQTT / HTTP Device API / WebSocket
- �Ž�ҵ������豸����ʱ�����������澯
- LOCKING / ERROR �ȸ���״̬

## 3. MockDoorDevice

```text
deviceId + state(CLOSED|OPEN)
apply(open_door) �� OPEN
apply(close_door) �� CLOSED
apply(other) �� false��״̬���䣩
```

���ڴ棬�� DB ״̬����

## 4. MockDoorProtocolPort

ʵ�ּ��� `ProtocolPort.sendCommand(...)`������ SPI ǩ����  
δ֪����� `false` �� Command `FAILED`��

## 5. ProtocolPort ·��

`ProtocolPortRegistry` �ռ����� `ProtocolPort` Bean��

```text
MOCK �� MockDoorProtocolPort
MQTT �� MqttProtocolAdapter���� Stub��
```

`DeviceCommandService.invoke` ʹ�� `product.protocol` ������

## 6. DeviceCommand ִ����

```text
TenantGuard �� Device(tenant) �� Product �� Registry.require(protocol)
�� insert PENDING �� ProtocolPort.sendCommand �� SUCCESS/FAILED
```

Probe ֻ�� `DeviceCommandService`����ֱ�� MockDoorDevice.open()��

## 7. Device State

�ڴ�ɱ䣻`GET .../mock-devices/{id}` �� `DeviceService.get`���⻧У�飩�ٶ� Store��δ����������ʾ CLOSED��

## 8. Tenant ����

δ�ƹ� TenantGuard�����⻧ invoke / mock-state ��ʧ�ܡ�

## 9. ���⻧����

| ���� | ��� |
|------|------|
| admin open DOOR-001 | SUCCESS / OPEN |
| admin open DOOR-002 | rejected |
| ry open DOOR-002 | SUCCESS / OPEN |
| admin �� mock DOOR-002 | rejected |

## 10. Command ����

open/close �� SUCCESS + ״̬�仯��xxx �� FAILED + ״̬���䣻������д�� `openbiz_device_command`��

## 11. Regression Tests

```text
saas-core: 9 OK
iot-core:  27 OK���� Mock / Registry / Flow��
BUILD SUCCESS
```

## 12. Maven Build

`mvn package -DskipTests` SUCCESS��Ϊװ���´������ͣ��һ�� 18080����

## 13. ������֤

`Started RuoYiApplication` �� 18080 �� captcha/login/mock ��·���á�

## 14. ���ݿ�仯

`sql/openbiz_iot_1_4.sql`��`protocol=MOCK` + thing model ���� close_door��  
**��** `openbiz_door*` �±���

## 15. Git Diff����� 1.3��

��Ҫ��`mock/*`��`ProtocolPortRegistry`��`DeviceCommandServiceImpl` ·�ɡ�probe�����ԡ�SQL 1.4��  
�� AccessService / С���� / �� MQTT Client / HTTP Adapter��

## 16. ����֤��������

| ���� | ״̬ |
|------|------|
| Tenant / Member / Context | ����֤��ƽ̨�� |
| Product / Device / ThingModel | ����֤��ƽ̨�� |
| DeviceCommand | ����֤��ƽ̨�� |
| ProtocolPort | ����֤��ƽ̨�� |
| Device State Control��Mock �Ž��� | **�Ž���·����֤** |
| ��ʵ�Ž�ҵ�� / ����� / ��� / MES | ��ΪԤ�� |

## 17. ����ծ��

1. �ݵȶ�·��δ��  
2. ThingModel δǿ��У�� serviceId ������ JSON  
3. Probe ����ʽ API  
4. Mock ״̬����������ʧ�����⣩  
5. ProtocolPort �����첽��ִģ��

## 18. Phase 1.5 ����

��С��ʵ�Ž�ҵ��ջ����Ա�����

```text
Member Ȩ�� �� AccessPoint �� DeviceCommandService.open_door �� �� Mock ״̬ �� AccessRecord
```

��Ҫһ��������/��Ƭ/С����ȫ�ס���ɺ������ʣ����ɴ������Щ���ö���

---

**Phase 1.4 �Ƿ�������ɣ��ǡ�**  
**ֹͣ�ڴˣ������� 1.5��**
