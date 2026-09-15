# openbiz-service

**Service** domain �� multi-tenant WorkOrder (Phase 1).

```text
CREATED �� ASSIGNED �� ACCEPTED �� COMPLETED
CREATED|ASSIGNED �� CANCELLED
```

Contact fields live on WorkOrder. Assignee = RuoYi `sys_user.user_id`.
Depends on `openbiz-saas-core` only among OpenBiz domains (not Business/IoT/Agent/Shop).

Probe: `/openbiz/test/service/**`
