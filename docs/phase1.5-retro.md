# Phase 1.5 ���̣����� Locker ������֤ǰ��

> ���ڣ�2026-09-14  
> Ŀ�ģ��տ���ҵ���һ����֤����ȷ��һ�׶�ֻ��������ҵ���á�������С����

---

## 1. 1.5 ֤����ʲô

| ���� | ��� |
|------|------|
| ��ҵģ���վ�� SaaS + IoT Core �� | ? Access �� DeviceCommand �� Mock Door |
| ��ҵ�㲻��֪Э�� | ? �� ProtocolPort / MQTT / MockDoor ���� |
| Tenant ��������ҵ�������Գ��� | ? admin �򲻿� DOOR-002 |
| ҵ���¼��׷�ݵ� Command | ? AccessRecord.command_id ? DeviceCommand |

**1.4**��ƽ̨�������豸��  
**1.5**����ҵҵ���ܸ���ƽ̨�����豸��

---

## 2. ����û�������ֿ��ƣ�

С����RBAC��ʱ���Ȩ�ޡ�����/NFC���� MQTT��ǰ�ˡ������/���ҵ��

---

## 3. ��һ�׶�����

```text
? Phase 1.6 = С����
? Phase 1.6 = Cross-Industry Reuse Validation �� Locker
```

�������ȣ�

1. **Core ���޸�**��saas / iot-core / iot-mqtt���� ������ģ�д�塸Ϊʲô��
2. Locker ֻ�� `DeviceCommandService.invoke(...)`�����ȸ��� `open_door`��
3. ����ҵģ�飺Locker / Slot / Record
4. �� reuse-matrix �ǣ�**������ʲô / ������ʲô / Core ���˼���**

---

## 4. �ɹ���ʲô��

```text
openbiz-saas-core      0 �޸�
openbiz-iot-core       0 �޸ģ����룩
openbiz-iot-mqtt       0 �޸�
DeviceCommandService   ����
ProtocolPort           ����
+ openbiz-locker       ��ģ��
```

�����ֱ����� IoT Core �� **��Ϊ�ܹ����֣���Ӳ�� 80%��**

---

## 5. ·�ߣ�������

```text
1.5 Access ?
 �� 1.6 Locker Reuse Validation
 �� 1.7 Charging Reuse Validation
 �� 1.8 MES Reuse Validation
 �� �پ��� Mini Program / �� MQTT / Frontend
```

---

**״̬��������ɡ�Phase 1.6 �� `phase1.6-report.md`��**
