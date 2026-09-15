# Phase 1.2 Report �� SaaS Core

> ���ڣ�2026-09-14  
> ���ۣ�**Phase 1.2 �������**����С���⻧�����Ŀ���֤��

---

## 1. ʵ�����

- `openbiz_tenant` / `openbiz_member` �� + �������ݣ�admin��demo-a��ry��demo-b��
- `TenantContext`��ThreadLocal��set / get / clear��
- `TenantResolver` + `MemberTenantResolver`��user �� active member �� normal tenant��
- `TenantInterceptor`��preHandle ������afterCompletion �� clear���ų� login/captcha �ȣ�
- `TenantService` / `MemberService` ʵ��
- ��֤�ӿ� `GET /openbiz/test/tenant`
- ��Ԫ���� 9 ��ȫ��ͨ��
- `mvn clean package` BUILD SUCCESS
- ������֤��admin��tenantId=1��ry��tenantId=2������ 401���� Member �� NO_TENANT_CONTEXT

## 2. δ��ɣ����ⲻ����

- MyBatis �Զ�׷�� `tenant_id` / SQL Rewrite
- �⻧�ײ� / �Ʒ� / �༶�⻧ / ��֯��
- C �� Member��openId���� ���׶� Member ������ `sys_user`
- IoT ��⡢ģ���豸���Ž���С����
- Redis Session / ΢���� / �ֿ�ֱ�
- ��ʽ�⻧���� CRUD UI

## 3. ����ģ��/��

���� `openbiz-saas-core`��

| �� | ְ�� |
|----|------|
| `context.TenantContext` | ThreadLocal |
| `resolve.TenantResolver` | SPI |
| `resolve.MemberTenantResolver` | Ĭ�Ͻ��� |
| `web.TenantInterceptor` | MVC ���� |
| `config.OpenBizSaasWebConfig` | ע�������� + `@MapperScan` |
| `domain.OpenbizTenant` / `OpenbizMember` | ʵ�� |
| `mapper.*` + XML | MyBatis |
| `service.impl.*` | API ʵ�� |
| `web.TenantProbeController` | ��֤�� |

δ�� RuoYi `sys_user` ���ṹ��δ�����޹� Maven �������� saas-core �� `spring-webmvc` / mybatis / test����

## 4. ���ݿ�仯

�ű���`sql/openbiz_saas_1_2.sql`

| �� | ˵�� |
|----|------|
| `openbiz_tenant` | id, tenant_code(UK), tenant_name, status, timestamps |
| `openbiz_member` | id, tenant_id, user_id, status������ tenant_id / user_id��UK(tenant_id,user_id) |

���ӣ�tenant `demo-a`(1)/`demo-b`(2)��member �� user 1/2��

## 5. TenantContext

- ThreadLocal&lt;Long&gt;
- `setTenantId` / `getTenantId` / `clear`��`remove`��
- ���⻧ʱΪ `null`��**��Ĭ��**�κ��⻧
- ������ `afterCompletion` ǿ�� clear�����̳߳�й©

## 6. TenantResolver

- �ӿ���ʵ�ַ���
- ��ǰ���ԣ�`userId` �� `openbiz_member(status=0)` �� `openbiz_tenant(status=0)` �� tenantId
- �޻�Ա / �⻧ͣ�� �� ���� null������Ĭ��Ĭ���⻧��
- δ�����滻 Header / �������Ȳ��Զ�����������������

## 7. TenantInterceptor

���̣�`clear` �� ȡ `LoginUser` �� `resolve` �� ��ѡ `set` �� Controller �� `afterCompletion clear`

�ų���`/login` `/register` `/captchaImage` `/logout` ��̬�� swagger/druid

���ƻ� RuoYi ��¼�������������⻧��

## 8. RuoYi �������

| ���� | �÷� |
|------|------|
| `LoginUser` + SecurityContext | ʶ��ǰ�û� |
| `SecurityUtils` | ���Խӿ�ȡ userId |
| JWT / TokenService | δ�� |
| `sys_user` admin / ry | ���ӹ������������û��� |
| MyBatis ��� | �� RuoYi һ�� |
| Spring Security 401 | δ��¼���� `/openbiz/**` |

�� RuoYi ���ģ������ü��� `scanBasePackages` �� `com.openbiz`��Phase 1 ���У������׶����ٸ� framework �����ࡣ

## 9. ���Խ��

��Ԫ��`openbiz-saas-core`����

```text
TenantContextTest          2 OK
MemberTenantResolverTest   5 OK
TenantInterceptorTest      2 OK
Tests run: 9, Failures: 0
```

���ɣ�

| ���� | ��� |
|------|------|
| admin ��¼ �� GET /openbiz/test/tenant | `tenantId=1, userId=1` |
| ry ��¼ �� ͬ�ӿ� | `tenantId=2, userId=2` |
| δ��¼ | HTTP 401 |
| admin ��Աͣ�� | `NO_TENANT_CONTEXT`��code 500 ҵ����� |
| /captchaImage | 200 |
| /login | 200��������֤��ʱ�ر���֤�룩 |

## 10. Maven Build

```text
mvn clean package  �� BUILD SUCCESS��ȫ reactor��
```

## 11. ������֤

```text
java -jar ruoyi-admin/target/ruoyi-admin.jar
Started RuoYiApplication �� port 18080
```

## 12. Git Diff

�ֿ���δ�״� commit����������Ϊ untracked�������׶���� Phase 1 ����Ҫ����·����

```text
openbiz-saas-core/src/main/java/com/openbiz/saas/**   (ʵ��)
openbiz-saas-core/src/test/java/**                    (����)
openbiz-saas-core/src/main/resources/mapper/saas/**
sql/openbiz_saas_1_2.sql
docs/phase1.2-report.md + �ĵ�����
```

δ�޸� `sys_user` / `sys_role` / `sys_menu` DDL������ҵģ�顢�� IoT ��⡣

## 13. ����ծ��

1. �������ر� `sys.account.captchaEnabled` ��������������ǰ�ָ� true����
2. DB �������� `application-druid.yml`������ǰ�ı��� profile����
3. `TenantProbeController` Ϊ��֤�ӿڣ�����Ӧ��Ȩ�޻��Ƴ���
4. һ�û����⻧��δ��ģ����ǰ LIMIT 1����
5. **��δ**�� SQL ���⻧���루Phase 1.3+����
6. Member �з�С���� C ��ģ�͡�

## 14. ��δ����ҵģ��ĸ��ü�ֵ

| ��ҵ | ��ֵ |
|------|------|
| �Ž� | Ԥ�Ƹ��� TenantContext дͨ�м�¼ / Ȩ�ޣ�**���Ž���غ�ʵ��** |
| ����� | Ԥ��ͬ�� |
| ��� | Ԥ�ƶ���/�Ự�� tenant_id |
| MES | Ԥ�ƹ��������ݴ��⻧ |

## 15. Phase 1.3 ����

1. IoT ����`iot_product` / `iot_device` / `iot_thing_model` / `iot_device_command`����С����  
2. ʵ�� `DeviceCommandService`�������� MQTT stub��  
3. **��**�����Ƿ��� MyBatis `tenant_id` �Զ����ˣ����� `iot_*` / `openbiz_*`��  
4. �Բ����Ž�ҵ����ģ�� Broker ������Broker �� 1.4��

---

**Phase 1.2 �Ƿ�������ɣ��ǡ�**
