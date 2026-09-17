# Evidence & Limitations

Evidence levels used across OpenBiz docs:

| Level | Meaning |
| --- | --- |
| **L0** | No evidence |
| **L1** | Static code / config |
| **L2** | Unit / Mock tests |
| **L3** | Real MySQL and/or real HTTP (may be mother process or independent process��say which) |
| **L4** | Real external production / real devices |

**Code exists ≠ verified.**  
**Unit/Mock ≠ Real MySQL.**  
**Real MySQL ≠ production.**  
**Independent consumer ≠ SDK productization.**

---

## Capability matrix (honest)

| Capability | L1 | L2 | L3 MySQL | L3 Independent HTTP | L4 |
| --- | --- | --- | --- | --- | --- |
| SaaS Tenant / Member | ? | ? | ? | via consumers | ? |
| WorkOrder Service API | ? | ? | ? | **?** `openbiz-workorder-demo` `:18081` | ? |
| Business Customer/Account/Order (CAS) | ? | ? | ? | ? (no independent consumer yet) | ? |
| IoT DeviceCommand | ? | ? (MockDoor) | ? | in-repo Demo only | ? device/MQTT |
| Shop Inventory CAS | ? | ? | ? | ? | ? |
| Agent ModelPort | ? | ? | optional E2E w/ key | ? | ? |

---

## Red lines (do not over-claim)

| Tempting claim | Actual |
| --- | --- |
| ��Published on Maven Central�� | **Local** `mvn install` → `~/.m2` only |
| ��Production-ready platform�� | Demo/local configs, sample secrets |
| ��Supports real MQTT devices�� | MQTT adapter is a **stub**; commands use **Mock** protocol in validated paths |
| ��Full AI Agent platform�� | Prompt → single model invoke; `ToolPort` not a product |
| ��Every domain is independently released�� | Domains **KEEP IN** one Git monorepo; Service has one external Example consumer |

---

## Independent Consumer (Service)

Proven chain:

```text
openbiz-service:3.9.2
  ↓ openbiz-workorder-demo
  ↓ :18081
  ↓ HTTP + MySQL WorkOrder lifecycle + tenant isolation
```

See [examples/workorder-demo.md](examples/workorder-demo.md).

---

## How to re-check locally

Mother tests (when DB configured for integration tests):

```bash
mvn clean test
```

Example smoke (manual): Getting Started create/get after `spring-boot:run` on `:18081`.
