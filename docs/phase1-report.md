# Phase 1 �������棨Foundation��

> ���ڣ�2026-09-14  
> ��Χ��RuoYi �������� + OpenBiz ��ģ��Ǽ� + �ɱ��������  
> **δʵ���κ���ҵҵ��**

---

## 1. ʵ��ʹ�õ� RuoYi �汾

| �� | ֵ |
|----|-----|
| ���� | https://gitee.com/y_project/RuoYi-Vue |
| ��֧ | `springboot3` |
| RuoYi | **3.9.2** |
| Spring Boot | **3.5.16** |
| Java������Ŀ�꣩ | 17���������� JDK 21�� |
| ǰ�� | ���׶�δ���� Vue ���̣�����˿������������� UI �ɺ����Խӹٷ� RuoYi-Vue3�� |
| ��¼�ļ� | `RUOYI_UPSTREAM.txt` |

**����ȷ�ϣ�** �ٷ� RuoYi-Vue��**������** RuoYi-Plus��

---

## 2. ����ģ��

| Maven ģ�� | groupId | ְ��Phase 1�� |
|------------|---------|-----------------|
| `openbiz-saas-core` | `com.openbiz` | `TenantService` / `MemberService` �ӿ� + ɨ������ |
| `openbiz-iot-core` | `com.openbiz` | Product/Device/ThingModel/Command/Message �ӿ� + `ProtocolPort` SPI |
| `openbiz-iot-mqtt` | `com.openbiz` | `MqttProtocolAdapter` **׮**���� MQTT �ͻ������������� Broker�� |

**δ������** smart-access��locker��charging��mes��business-order��10 ����ҵ��Ŀ¼��

---

## 3. ģ��������ϵ

```text
ruoyi-admin
  ������ ruoyi-framework / quartz / generator
  ������ openbiz-saas-core
  ������ openbiz-iot-core
  ������ openbiz-iot-mqtt
        ������ openbiz-iot-core
              ������ openbiz-saas-core
                    ������ ruoyi-common

Industry Template (δ��)
  �� openbiz-iot-core.api   (NOT mqtt)
  �� openbiz-saas-core.api
```

Ӳ���������ڴ���߽磺`openbiz-iot-core` **��** ���� `openbiz-iot-mqtt`��MQTT ģ��ʵ�� `ProtocolPort`��

����ɨ�裺`RuoYiApplication` ���� `scanBasePackages = {"com.ruoyi","com.openbiz"}`���� RuoYi ����С��Ҫ�Ķ�����

---

## 4. ���ݿ�仯

| �仯 | ˵�� |
|------|------|
| ����ٷ��ű� | `sql/ry_20260417.sql` �� �� `ry-vue`��Լ 20 �� `sys_*` ���� |
| OpenBiz ҵ��� | **δ����**��tenant/device ������ Phase 1.2 / 1.3���������ÿձ��� |
| �������� | `application-druid.yml`��`root` / �������룻�˿�Ӧ�ø�Ϊ **18080** |

---

## 5. Ϊʲô��������

1. **�����ģ�** ֻ�������Ž�/��/��綼�������� SaaS + IoT �߽硣  
2. **Э����滻��** ҵ����ֻ���� `DeviceCommandService` / `ProtocolPort`�������� Paho��  
3. **������** ���� CRM/ERP/MES/��ģ�壬����ʺɽ��  
4. **���������ȣ�** ��֤�� RuoYi + ��ģ��ͬ������������д�⻧���豸ʵ�֡�

---

## 6. `mvn clean package` ���

```text
BUILD SUCCESS
Reactor: ruoyi + 6 ruoyi modules + 3 openbiz modules + ruoyi-admin
```

---

## 7. ������֤���

| ����� | ��� |
|--------|------|
| `java -jar ruoyi-admin/target/ruoyi-admin.jar` | �ɹ� |
| ���� Banner | `OpenBiz / RuoYi �����ɹ�` |
| �˿� | `18080`������ 8080 ����������ռ�ã� |
| `GET /captchaImage` | HTTP 200������ uuid + ��֤��ͼ |
| ����Դ | Druid ���� `ry-vue` �ɹ������� config/dict/job |

Fat jar �ڰ��� `com/openbiz/**` �� `BOOT-INF/lib/openbiz-*-3.9.2.jar`��

---

## 8. ��ǰ�ֿ��ļ��仯����Խ� docs �׶Σ�

- �������� RuoYi ��˶�ģ��Դ�루`ruoyi-*`��`sql`��`bin`��`doc`����  
- ���� `openbiz-saas-core` / `openbiz-iot-core` / `openbiz-iot-mqtt`  
- ���� `docs/reuse-matrix.md`������  
- ���� `README.md`��`docs/roadmap.md`  
- ��������˵����`README-ruoyi.md`��`RUOYI_UPSTREAM.txt`  
- ��΢�޸ģ�`RuoYiApplication` ɨ�����`application.yml` �˿ڡ�`application-druid.yml` ���ؿ�����  

---

## 9. ����ծ�� / ����

1. **���� DB ����д�� `application-druid.yml`**������ GitHub ǰ�����Ϊ�������� / ���� profile��gitignore����  
2. **δ����ٷ�ǰ��**������ UI ������ RuoYi-Vue3�����׶�ֻ��֤��ˡ�  
3. **`MqttProtocolAdapter` ������־**����һ�׶ν� Broker ǰҵ�񲻿��淢���  
4. **�ӿ���ʵ����**��`TenantService` ����δ�� Spring Bean��ɨ�費��ʧ�ܣ���ע��ʱ��ȱ Bean������һ�׶β���  
5. **Parent POM ��Ϊ `com.ruoyi:ruoyi`**�������ٸ����Σ�OpenBiz �ö��� `groupId`��  
6. **����������**���ĵ�����ʷ��Ԥ�� %��������˵��**�� reuse-matrix ʵ��Ϊ׼**��

---

## 10. ��һ�׶�Ӧ����ʲô���Ƽ� Phase 1.2��

���û�ȷ�Ͻ��ࣨ�ȱ����Ž�ҵ�񣩣�

```text
Phase 1.2  SaaS Core ʵ��
  - ob_tenant / ob_tenant_user / ob_member����С����
  - TenantContext + MyBatis �⻧����
  - TenantService ʵ��
  - ��������С�⻧ CRUD���ɺ��� UI��

Ȼ��
Phase 1.3  IoT Core ʵ�֣��� + Command �����Կ��� stub Э�飩
Phase 1.4  MQTT ������ + ģ���豸
Phase 1.5  Smart Access ҵ��
Phase 1.6  С����
```

**���ڲ�Ҫ�� MVP-2������񣩡�**

---

## 11. ԭ������������

> ������ + ǿ���� + ��ҵ�����  
> ��Ϊ��80%������  
> �������þ���ʵ�⣬�����ںš�
