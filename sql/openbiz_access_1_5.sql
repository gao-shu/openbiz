-- Phase 1.5: minimal Smart Access tables (no RBAC / no time windows)

CREATE TABLE IF NOT EXISTS openbiz_access_permission (
  id            bigint(20)      NOT NULL AUTO_INCREMENT COMMENT 'pk',
  tenant_id     bigint(20)      NOT NULL                 COMMENT 'tenant id',
  user_id       bigint(20)      NOT NULL                 COMMENT 'sys_user.user_id',
  device_id     bigint(20)      NOT NULL                 COMMENT 'openbiz_device.id',
  status        char(1)         NOT NULL DEFAULT '0'     COMMENT '0=allow 1=disabled',
  create_time   datetime                                COMMENT 'create time',
  update_time   datetime                                COMMENT 'update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_access_perm_tenant_user_device (tenant_id, user_id, device_id),
  KEY idx_access_perm_tenant_user (tenant_id, user_id),
  KEY idx_access_perm_tenant_device (tenant_id, device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz access permission';

CREATE TABLE IF NOT EXISTS openbiz_access_record (
  id            bigint(20)      NOT NULL AUTO_INCREMENT COMMENT 'pk',
  tenant_id     bigint(20)      NOT NULL                 COMMENT 'tenant id',
  user_id       bigint(20)      NOT NULL                 COMMENT 'sys_user.user_id',
  device_id     bigint(20)      NOT NULL                 COMMENT 'device id',
  command_id    varchar(64)     NOT NULL                 COMMENT 'DeviceCommand.command_id',
  result        varchar(16)     NOT NULL                 COMMENT 'SUCCESS/FAILED',
  create_time   datetime                                COMMENT 'create time',
  PRIMARY KEY (id),
  KEY idx_access_record_tenant (tenant_id),
  KEY idx_access_record_command (command_id),
  KEY idx_access_record_user (tenant_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz access open record';

-- Seed: admin(1) -> DOOR-001(1); ry(2) -> DOOR-002(2). No cross-tenant grant.
INSERT INTO openbiz_access_permission (id, tenant_id, user_id, device_id, status, create_time, update_time)
VALUES
  (1, 1, 1, 1, '0', NOW(), NOW()),
  (2, 2, 2, 2, '0', NOW(), NOW())
ON DUPLICATE KEY UPDATE status = VALUES(status), update_time = NOW();
