# Phase 1.3 Report �� IoT Core ���

> ���ڣ�2026-09-14  
> ���ۣ�**Phase 1.3 �������**����С IoT ����ģ�Ϳɿ��⻧��֤��  
> **δ���н��� Phase 1.4**

---

## 1. ʵ�����

- �ı���⣺`openbiz_product` / `openbiz_device` / `openbiz_thing_model` / `openbiz_device_command`
- Service��`ProductService` / `DeviceService` / `ThingModelService` / `DeviceCommandService`
- ȫ����ѯ/д������ʽ�� `tenant_id`��`TenantGuard.requireTenantId()`��
- `DeviceCommandService.invoke`���� PENDING �� `ProtocolPort` Stub �� SUCCESS/FAILED
- MQTT ģ����Ϊ Stub���� Broker���޿ͻ���������
- ���ӣ�Tenant A `DOOR-001`��Tenant B `DOOR-002`
- ��֤�ӿڣ�`/openbiz/test/iot/**`
- ��Ԫ���ԣ�iot-core **17** ͨ������ `-am` ���� saas-core��

## 2. δ��ɣ����⣩

- �� MQTT / Netty / Modbus / OTA / �������� / ʱ���
- SQL ȫ�� tenant ������
- �Ž� / ����� / ��� / MES ��ҵģ��
- ǰ���豸����ҳ
- �������� / TIMEOUT / ACKED �ȸ���״̬��
- ��ģ�Ͳ����property/service/event ��������

## 3. Product ģ��

һ���豸ģ�壨����ҵö�٣����ֶΣ�`tenant_id, product_code, product_name, protocol, status, description`��  
���ӣ�`DEMO-DOOR`��ÿ�⻧һ�ݣ���

## 4. Device ģ��

�����豸ʵ�������� `product_id`���� `online_status`��`metadata`��  
���ӣ�`DOOR-001`(tenant1) / `DOOR-002`(tenant2)��

## 5. ThingModel ģ��

���� + `model_json`��properties / services / events������ `product_id` һ��һ��  
δ��ʮ������ģ���ӱ���

## 6. DeviceCommand ģ��

��¼���`command_id`(ҵ�� UUID)��`command_name`��`payload`��`status`(PENDING/SUCCESS/FAILED)��`idempotent_key`��  
���׶�ֻ��¼ + Stub Э�飬�������ԡ�

## 7. ProtocolPort

�������� SPI��`sendCommand(...)` / `protocol()`��  
`openbiz-iot-mqtt.MqttProtocolAdapter` ���� `true`��ģ���������������� Broker��

## 8. Tenant ����

- �� `TenantContext` �� �ܾ�����Ĭ�� demo-a
- Mapper һ�� `...AndTenant(id, tenantId)` / `selectByTenant`
- **δ**�� MyBatis SQL Rewrite

## 9. ���⻧����

| ���� | ��� |
|------|------|
| admin �б� | �� `DOOR-001` |
| ry �б� | �� `DOOR-002` |
| admin ���� device 2 | `device not found in current tenant` |
| ��Ԫ��tenant1 get device2 | null / invoke ���쳣 |

## 10. DeviceCommand ����

��Ԫ���ɹ� + ProtocolPort FAILED + ���⻧�ܾ���  
������`POST /openbiz/test/iot/devices/1/invoke` �� `status=SUCCESS`��д�������С�

## 11. Maven Build

```text
mvn -pl openbiz-iot-core -am test  �� BUILD SUCCESS
  saas-core Tests run: 9
  iot-core  Tests run: 17, Failures: 0
```

## 12. ������֤

���н��� port **18080**��δǿ�� kill/restart����  
`/login` `/captchaImage` `/openbiz/test/iot/devices` ���á�

## 13. ���ݿ�仯

�ű���`sql/openbiz_iot_1_3.sql`���ı� + ���ӣ���δ�� `sys_*`��

## 14. Git Diff����� Phase 1.2��

��Ҫ������`openbiz-iot-core/**` ʵ������ԡ�`openbiz-iot-mqtt` stub ������`sql/openbiz_iot_1_3.sql`������ĵ���  
�� smart-access / locker / charging / mes ģ�飻�� MQTT �ͻ���������

## 15. ����ծ��

1. �ݵȼ������ѽ���ҵ�����δ����ͬ key ����ԭ�����·  
2. Probe Controller ����ʽ API  
3. ThingModel δУ�� serviceId �Ƿ��� JSON �д��ڣ����� 1.4/1.5��  
4. ���� SQL �Զ� tenant ���ˣ������Ӻ�

## 16. Reuse Matrix �仯

Product / Device / ThingModel / DeviceCommand / ProtocolPort �� ���Ž�����ҵ���Ա� **Ԥ�Ƹ���**��δ����ҵģ��ʵ�⣩��  
SaaS �������Ž��пɱ� **����֤��ƽ̨�ࣩ**��

## 17. ���Ž��ĸ��ü�ֵ

�Ž��ɣ�`AccessService �� DeviceCommandService.invoke(deviceId, "open_door", ...)`  
ҵ���� MQTT��Product/Device/ThingModel ��ֱ�ӹҡ������Ž������Ʒ�����踴���豸����

## 18. Phase 1.4 ����

ģ���Ž��豸��HTTP α�豸�򱾵� Mosquitto ��ѡһ�������� HTTP/�ڴ�ģ�����Խ� ProtocolPort����  
�Բ��������Ž�ҵ����С�������� 1.5/1.6����

---

**Phase 1.3 �Ƿ�������ɣ��ǡ�**  
**δ���н��� Phase 1.4��**
