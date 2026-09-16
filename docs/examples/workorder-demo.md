# Example: WorkOrder Independent Consumer

| Item | Value |
| --- | --- |
| Repo | [gao-shu/openbiz-workorder-demo](https://github.com/gao-shu/openbiz-workorder-demo) |
| Depends on | `com.openbiz:openbiz-service:3.9.2` |
| Process | Independent Spring Boot app |
| Port | **18081** |
| Persistence | Real MySQL |
| Evidence | **L3** Independent HTTP + MySQL |

---

## What it proves

```text
Mother openbiz-service
  �� mvn install (local artifact)
  �� demo pom dependency (no source copy, no submodule, no relativePath to mother sources)
  �� WorkOrderController �� WorkOrderService
  �� ServiceTenantGuard / transitions / mapper inside the artifact
  �� MySQL
```

Verified lifecycle (Phase 3):

- List / Get / Create
- Create idempotency (same tenant + idempotentKey)
- Assign / Accept / Complete
- Cancel (CREATED / ASSIGNED �� CANCELLED; COMPLETED �� reject)
- Cross-tenant isolation

---

## What it does not prove

- Maven Central release
- Removing RuoYi thin shell
- Production hardening
- Business / Shop / Agent / IoT artifact consumers
- Real devices or MQTT

---

## How to run

From the mother repo, install Local Maven artifacts (not Maven Central):

```bash
mvn -pl openbiz-service,ruoyi-framework -am install -DskipTests
```

Follow **[Getting Started](../getting-started.md)** or the Example��s own [README](https://github.com/gao-shu/openbiz-workorder-demo/blob/master/README.md).

---

## Related in-repo Demo (not independent)

`ruoyi-admin` contains an IoT Monitor Demo Controller that calls `DeviceService` / `DeviceCommandService` **inside** the mother process.

That proves **in-repo reuse** (Phase 2), not an independent Maven consumer.
