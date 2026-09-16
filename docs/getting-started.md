# Getting Started

**Default path:** run the independent WorkOrder Example��not the full `ruoyi-admin` mother app.

Goal: understand that OpenBiz capabilities can be consumed via **Maven artifacts** in a separate project.

> **5�C10 minutes** only means: JDK 21, Maven, MySQL, and Redis are **already** installed and running.

---

## Prerequisites

| Requirement | Notes |
| --- | --- |
| JDK **21** | Required |
| Maven 3.8+ | Required |
| MySQL | Default schema name `ry-vue` |
| Redis | Required by the Example��s RuoYi thin shell |
| Local Maven repo | Artifacts are **not** on Maven Central yet |

---

## Step 1 �� Install `openbiz-service` locally

Clone / open the mother repo [openbiz](https://github.com/gao-shu/openbiz), then:

```bash
mvn -pl openbiz-service -am install -DskipTests
```

Installs `com.openbiz:openbiz-service:3.9.2` into `~/.m2` (or your configured local repository).

**Local Maven Artifact �� Maven Central.**

---

## Step 2 �� Clone the Example

```bash
git clone https://github.com/gao-shu/openbiz-workorder-demo.git
cd openbiz-workorder-demo
```

Example README: [openbiz-workorder-demo/README.md](https://github.com/gao-shu/openbiz-workorder-demo/blob/master/README.md)

---

## Step 3 �� Prepare MySQL (minimum)

Import from mother `sql/` (adjust filenames to what exists in your checkout):

1. RuoYi base script (`ry_*.sql`)
2. `openbiz_saas_*.sql` (tenant / member)
3. `openbiz_service_*.sql` (work order tables)

You do **not** need Access / Locker / Charging / MES / Shop scripts for this first run.

Edit `src/main/resources/application-druid.yml`:

- JDBC URL / username / **password** (sample `123456` is for local demo only)

---

## Step 4 �� Redis

Ensure Redis listens where `application.yml` points (default `localhost:6379`).

Without Redis, the thin shell typically fails at login/token storage.

---

## Step 5 �� Start Example (:18081)

```bash
mvn spring-boot:run
```

Look for: `Started WorkOrderDemoApplication`  
Port: **18081**

---

## Step 6 �� Login

```http
POST http://127.0.0.1:18081/login
Content-Type: application/json

{ "username": "admin", "password": "admin123", "code": "", "uuid": "" }
```

Copy `token` �� `Authorization: Bearer <token>`.

---

## Step 7 �� Create & get a WorkOrder

```http
POST http://127.0.0.1:18081/api/work-orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Hello OpenBiz",
  "content": "first reuse",
  "contactName": "Dev",
  "contactPhone": "13800000001",
  "idempotentKey": "qs-hello-001"
}
```

```http
GET http://127.0.0.1:18081/api/work-orders/{id}
Authorization: Bearer <token>
```

Success = you consumed mother **Service** capability from an independent process.

---

## Common failures

| Symptom | Check |
| --- | --- |
| Dependency `openbiz-service` not found | Re-run mother `mvn -pl openbiz-service -am install` |
| Cannot connect MySQL | URL/DB name/password; schema imported |
| Login / token errors | Redis up? Correct password? Captcha settings? |
| Wrong port | Example is **18081**, mother admin often **18080** |

---

## Optional: mother admin (not default)

Maintainers may still run `ruoyi-admin` on `:18080` for in-repo probes (`/openbiz/test/**`).  
That path is **secondary** and does not replace the Example consumer story.

---

## Next reading

- [Evidence](evidence.md)
- [WorkOrder Example details](examples/workorder-demo.md)
- [Mother-boundary ADR](architecture-decision-mother-boundary.md)
