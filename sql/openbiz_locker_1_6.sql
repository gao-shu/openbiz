-- Phase 1.6: minimal Locker tables for cross-industry reuse validation
-- Reuses existing IoT devices (DOOR-001 / DOOR-002) and open_door command. No Core schema change.

CREATE TABLE IF NOT EXISTS openbiz_locker (
  id            bigint(20)      NOT NULL AUTO_INCREMENT COMMENT 'pk',
  tenant_id     bigint(20)      NOT NULL                 COMMENT 'tenant id',
  device_id     bigint(20)      NOT NULL                 COMMENT 'openbiz_device.id',
  name          varchar(64)     NOT NULL                 COMMENT 'locker display name',
  status        varchar(16)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
  create_time   datetime                                COMMENT 'create time',
  update_time   datetime                                COMMENT 'update time',
  PRIMARY KEY (id),
  KEY idx_locker_tenant (tenant_id),
  KEY idx_locker_tenant_device (tenant_id, device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz locker cabinet';

CREATE TABLE IF NOT EXISTS openbiz_locker_slot (
  id            bigint(20)      NOT NULL AUTO_INCREMENT COMMENT 'pk',
  tenant_id     bigint(20)      NOT NULL                 COMMENT 'tenant id',
  locker_id     bigint(20)      NOT NULL                 COMMENT 'openbiz_locker.id',
  slot_no       int(11)         NOT NULL                 COMMENT 'slot number',
  status        varchar(16)     NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE/OCCUPIED',
  create_time   datetime                                COMMENT 'create time',
  update_time   datetime                                COMMENT 'update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_locker_slot_locker_no (locker_id, slot_no),
  KEY idx_locker_slot_tenant (tenant_id),
  KEY idx_locker_slot_locker (locker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz locker slot';

CREATE TABLE IF NOT EXISTS openbiz_locker_record (
  id            bigint(20)      NOT NULL AUTO_INCREMENT COMMENT 'pk',
  tenant_id     bigint(20)      NOT NULL                 COMMENT 'tenant id',
  user_id       bigint(20)      NOT NULL                 COMMENT 'sys_user.user_id',
  locker_id     bigint(20)      NOT NULL                 COMMENT 'locker id',
  slot_id       bigint(20)      NOT NULL                 COMMENT 'slot id',
  device_id     bigint(20)      NOT NULL                 COMMENT 'device id',
  command_id    varchar(64)     NOT NULL                 COMMENT 'DeviceCommand.command_id',
  result        varchar(16)     NOT NULL                 COMMENT 'SUCCESS/FAILED',
  create_time   datetime                                COMMENT 'create time',
  PRIMARY KEY (id),
  KEY idx_locker_record_tenant (tenant_id),
  KEY idx_locker_record_command (command_id),
  KEY idx_locker_record_user (tenant_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz locker open record';

-- Seed: tenant1 locker on DOOR-001(device 1); tenant2 locker on DOOR-002(device 2). No cross-tenant rows.
INSERT INTO openbiz_locker (id, tenant_id, device_id, name, status, create_time, update_time)
VALUES
  (1, 1, 1, 'Locker-A', 'ACTIVE', NOW(), NOW()),
  (2, 2, 2, 'Locker-B', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), update_time = NOW();

INSERT INTO openbiz_locker_slot (id, tenant_id, locker_id, slot_no, status, create_time, update_time)
VALUES
  (1, 1, 1, 1, 'AVAILABLE', NOW(), NOW()),
  (2, 2, 2, 1, 'AVAILABLE', NOW(), NOW())
ON DUPLICATE KEY UPDATE status = VALUES(status), update_time = NOW();
