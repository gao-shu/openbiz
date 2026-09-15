-- OpenBiz Service Phase 1. Does NOT alter IoT / Business / sys_* tables.

CREATE TABLE IF NOT EXISTS openbiz_work_order (
  id                 bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id          bigint(20)      NOT NULL,
  title              varchar(128)    NOT NULL,
  content            varchar(1000)   DEFAULT NULL,
  contact_name       varchar(64)     NOT NULL,
  contact_phone      varchar(32)     NOT NULL,
  status             varchar(16)     NOT NULL COMMENT 'CREATED|ASSIGNED|ACCEPTED|COMPLETED|CANCELLED',
  assignee_user_id   bigint(20)      DEFAULT NULL COMMENT 'sys_user.user_id',
  complete_note      varchar(500)    DEFAULT NULL,
  idempotent_key     varchar(64)     NOT NULL,
  create_time        datetime,
  update_time        datetime,
  PRIMARY KEY (id),
  UNIQUE KEY uk_svc_wo_tenant_idem (tenant_id, idempotent_key),
  KEY idx_svc_wo_tenant_status (tenant_id, status),
  KEY idx_svc_wo_tenant_assignee (tenant_id, assignee_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz Service WorkOrder';
