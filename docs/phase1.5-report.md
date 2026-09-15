# Phase 1.5 Report �� ��С�����Ž�ҵ��

> ���ڣ�2026-09-14  
> ���ۣ�**Phase 1.5 �������**����ҵ��ɾ�վ�� SaaS + IoT Core �ϣ�  
> **δ���н��� Phase 1.6 / С���� / �����**

---

## 1. ʵ�����

- ģ�� `openbiz-access`��ֻ���� saas-core + iot-core��**��**���� iot-mqtt��
- ����`openbiz_access_permission` / `openbiz_access_record`
- `AccessService.openDoor(deviceId)`����ǰ��¼�û� + TenantGuard �� Ȩ�� �� `DeviceCommandService.invoke("open_door")` �� д AccessRecord
- Probe��`POST /openbiz/test/access/open`��body �� `deviceId`��
- ���ӣ�admin��DOOR-001��ry��DOOR-002
- ���������ųɹ� + Mock OPEN + record.command_id ���� DeviceCommand

## 2. δ��ɣ����⣩

- С���� / ���� / NFC / �� / ��ά��
- RBAC / ���� / ʱ���Ȩ�� / �ÿ� / �ڰ�����
- �� MQTT / ǰ�˹���ҳ
- ����� / ��� / MES

## 3. AccessPermission

`tenant_id + user_id + device_id + status(0=allow)`��UK ��������ȫ���� role/ʱ�䴰�ֶΡ�

## 4. AccessRecord

`tenant_id, user_id, device_id, command_id, result(SUCCESS|FAILED)`��  
Ȩ�޾ܾ�**��**д��¼������ɹ�/ʧ�ܶ�д��`command_id` = IoT `DeviceCommand.command_id`��

## 5. AccessService

ֻ�� `DeviceService` + `DeviceCommandService` + Permission/Record Mapper��  
**��** ProtocolPort / MockDoor / MQTT ���á�

## 6. DeviceCommand ������

```text
LoginUser �� TenantGuard �� Device(tenant) �� Permission
�� DeviceCommandService.invoke(open_door)
�� ProtocolPortRegistry �� MockDoor �� OPEN
�� AccessRecord(command_id, SUCCESS)
```

## 7. Tenant ����

Device �Ȱ��⻧�飻���⻧�豸����Ϊ�������ڡ�(404)���޷��� permission �ƹ���

## 8. Ȩ�޲���

| ���� | ��� |
|------|------|
| admin �� DOOR-001 | 200 SUCCESS���� OPEN���� record |
| admin �� DOOR-002 | 404�����⻧�豸���ɼ��� |
| ry �� DOOR-002 | 200 SUCCESS |
| device 999999 | 404 |
| δ��¼ | 401 |
| ��Ԫ����Ȩ�� / ͣ��Ȩ�� | 403���� invoke������ record |
| ��Ԫ������ FAILED | �� FAILED record ���� 500 |

## 9. ���⻧����

admin �޷�����/���� DOOR-002��ry �ɿ� DOOR-002��

## 10. Command �ɹ�/ʧ��

�ɹ�·����������ʧ��·���ɵ�Ԫ���Ը��ǣ���д AccessRecord����

## 11. Regression Tests

```text
saas-core: 9
iot-core:  27
access:    6
ȫ����ɫ
```

## 12. Maven Build

`mvn -pl openbiz-access -am test` SUCCESS�������� jar �Ѻ� access������ͨ������δ����ɱ������ clean��

## 13. ������֤

18080��`/login`��`/captchaImage`��`/openbiz/test/access/open` ���á�

## 14. ���ݿ�仯

`sql/openbiz_access_1_5.sql`�����ű� + ���� permission ���ӡ�δ�� `sys_*` / IoT ���ṹ��

## 15. Git Diff

��Ҫ������`openbiz-access/**`��SQL 1.5���ĵ����� mqtt client����ǰ�ˡ��� RBAC��

## 16. ����֤��������

| ���� | �Ž� | �����/���/MES |
|------|------|-----------------|
| Tenant / Context / Device / Command / Protocol / Mock | ����֤ | Ԥ�� |
| AccessPermission / AccessRecord | **�Ž�����֤** | Ԥ�� / MES ������ |

> ���Ѵ����/���д������֤��

## 17. ����ծ��

1. Probe ����ʽ API  
2. Ȩ�޾ܾ�����Ʊ������⣩  
3. ͬ�⻧��������Ȩ������ 403 ����δ�������������ţ���Ԫ�Ѹ��ǣ�  
4. �ݵ�/�������� IoT ծ��

## 18. Phase 1.6 ���飨����������·�ߣ�

**����С�������ȡ�** ���飺

```text
���� 1.5
  ��
1.6 ���������֤���� Locker ģ�飩
  ��
1.7 ��縴����֤
```

�����ʣ�SaaS/IoT/Command/Protocol �Ƿ�ӽ� 100% ���ã���ҵ�Ƿ�ֻ����ģ�顣

---

**Phase 1.5 �Ƿ�������ɣ��ǡ�**  
**��ֹͣ�������� 1.6��**
