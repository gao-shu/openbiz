-- Phase 1.7: minimal Charging tables for cross-industry reuse validation
-- Reuses existing IoT devices (DOOR-001 / DOOR-002) and open_door command. No Core schema change.

CREATE TABLE IF NOT EXISTS openbiz_charging_station (
  id            bigint(20)      NOT NULL AUTO_INCREMENT COMMENT 'pk',
  tenant_id     bigint(20)      NOT NULL                 COMMENT 'tenant id',
  device_id     bigint(20)      NOT NULL                 COMMENT 'openbiz_device.id',
  name          varchar(64)     NOT NULL                 COMMENT 'station display name',
  status        varchar(16)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
  create_time   datetime                                COMMENT 'create time',
  update_time   datetime                                COMMENT 'update time',
  PRIMARY KEY (id),
  KEY idx_chg_station_tenant (tenant_id),
  KEY idx_chg_station_tenant_device (tenant_id, device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz charging station';

CREATE TABLE IF NOT EXISTS openbiz_charging_connector (
  id            bigint(20)      NOT NULL AUTO_INCREMENT COMMENT 'pk',
  tenant_id     bigint(20)      NOT NULL                 COMMENT 'tenant id',
  station_id    bigint(20)      NOT NULL                 COMMENT 'openbiz_charging_station.id',
  connector_no  int(11)         NOT NULL                 COMMENT 'connector number',
  status        varchar(16)     NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE/CHARGING',
  create_time   datetime                                COMMENT 'create time',
  update_time   datetime                                COMMENT 'update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_chg_connector_station_no (station_id, connector_no),
  KEY idx_chg_connector_tenant (tenant_id),
  KEY idx_chg_connector_station (station_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz charging connector';

CREATE TABLE IF NOT EXISTS openbiz_charging_record (
  id            bigint(20)      NOT NULL AUTO_INCREMENT COMMENT 'pk',
  tenant_id     bigint(20)      NOT NULL                 COMMENT 'tenant id',
  user_id       bigint(20)      NOT NULL                 COMMENT 'sys_user.user_id',
  station_id    bigint(20)      NOT NULL                 COMMENT 'station id',
  connector_id  bigint(20)      NOT NULL                 COMMENT 'connector id',
  device_id     bigint(20)      NOT NULL                 COMMENT 'device id',
  command_id    varchar(64)     NOT NULL                 COMMENT 'DeviceCommand.command_id',
  result        varchar(16)     NOT NULL                 COMMENT 'SUCCESS/FAILED',
  create_time   datetime                                COMMENT 'create time',
  PRIMARY KEY (id),
  KEY idx_chg_record_tenant (tenant_id),
  KEY idx_chg_record_command (command_id),
  KEY idx_chg_record_user (tenant_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz charging start record';

-- Seed: tenant1 station on DOOR-001(device 1); tenant2 station on DOOR-002(device 2).
INSERT INTO openbiz_charging_station (id, tenant_id, device_id, name, status, create_time, update_time)
VALUES
  (1, 1, 1, 'Station-A', 'ACTIVE', NOW(), NOW()),
  (2, 2, 2, 'Station-B', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), update_time = NOW();

INSERT INTO openbiz_charging_connector (id, tenant_id, station_id, connector_no, status, create_time, update_time)
VALUES
  (1, 1, 1, 1, 'AVAILABLE', NOW(), NOW()),
  (2, 2, 2, 1, 'AVAILABLE', NOW(), NOW())
ON DUPLICATE KEY UPDATE status = VALUES(status), update_time = NOW();
