-- OpenBiz Phase 1.2 SaaS Core (minimal)
-- Database: ry-vue
-- Does NOT alter RuoYi sys_* tables.

CREATE TABLE IF NOT EXISTS openbiz_tenant (
  id            bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT 'tenant id',
  tenant_code   varchar(64)     NOT NULL                    COMMENT 'unique code',
  tenant_name   varchar(128)    NOT NULL                    COMMENT 'display name',
  status        char(1)         NOT NULL DEFAULT '0'        COMMENT '0=normal 1=disabled',
  create_time   datetime                                   COMMENT 'create time',
  update_time   datetime                                   COMMENT 'update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_openbiz_tenant_code (tenant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz tenant';

CREATE TABLE IF NOT EXISTS openbiz_member (
  id            bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT 'member id',
  tenant_id     bigint(20)      NOT NULL                    COMMENT 'tenant id',
  user_id       bigint(20)      NOT NULL                    COMMENT 'sys_user.user_id',
  status        char(1)         NOT NULL DEFAULT '0'        COMMENT '0=normal 1=disabled',
  create_time   datetime                                   COMMENT 'create time',
  update_time   datetime                                   COMMENT 'update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_openbiz_member_tenant_user (tenant_id, user_id),
  KEY idx_openbiz_member_tenant (tenant_id),
  KEY idx_openbiz_member_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz member (user belongs to tenant)';

-- Seed: map existing RuoYi users admin(1) and ry(2) to two tenants
INSERT INTO openbiz_tenant (id, tenant_code, tenant_name, status, create_time, update_time)
VALUES
  (1, 'demo-a', 'Demo Tenant A', '0', NOW(), NOW()),
  (2, 'demo-b', 'Demo Tenant B', '0', NOW(), NOW())
ON DUPLICATE KEY UPDATE tenant_name = VALUES(tenant_name), status = VALUES(status), update_time = NOW();

INSERT INTO openbiz_member (id, tenant_id, user_id, status, create_time, update_time)
VALUES
  (1, 1, 1, '0', NOW(), NOW()),
  (2, 2, 2, '0', NOW(), NOW())
ON DUPLICATE KEY UPDATE tenant_id = VALUES(tenant_id), status = VALUES(status), update_time = NOW();
