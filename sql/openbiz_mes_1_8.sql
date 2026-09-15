-- Phase 1.8: minimal MES tables for cross-industry reuse validation
-- Reuses existing IoT devices + open_door. No Core schema change.

CREATE TABLE IF NOT EXISTS openbiz_mes_material (
  id              bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id       bigint(20)      NOT NULL,
  material_code   varchar(64)     NOT NULL,
  material_name   varchar(128)    NOT NULL,
  material_type   varchar(32)     NOT NULL DEFAULT 'RAW',
  create_time     datetime,
  update_time     datetime,
  PRIMARY KEY (id),
  UNIQUE KEY uk_mes_mat_tenant_code (tenant_id, material_code),
  KEY idx_mes_mat_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz MES material';

CREATE TABLE IF NOT EXISTS openbiz_mes_bom (
  id              bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id       bigint(20)      NOT NULL,
  bom_code        varchar(64)     NOT NULL,
  material_id     bigint(20)      NOT NULL,
  version         varchar(32)     NOT NULL DEFAULT 'V1',
  status          varchar(16)     NOT NULL DEFAULT 'ACTIVE',
  create_time     datetime,
  update_time     datetime,
  PRIMARY KEY (id),
  UNIQUE KEY uk_mes_bom_tenant_code (tenant_id, bom_code),
  KEY idx_mes_bom_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz MES BOM';

CREATE TABLE IF NOT EXISTS openbiz_mes_bom_item (
  id              bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id       bigint(20)      NOT NULL,
  bom_id          bigint(20)      NOT NULL,
  material_id     bigint(20)      NOT NULL,
  quantity        decimal(12,2)   NOT NULL DEFAULT 1.00,
  PRIMARY KEY (id),
  KEY idx_mes_bom_item_bom (bom_id),
  KEY idx_mes_bom_item_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz MES BOM item';

CREATE TABLE IF NOT EXISTS openbiz_mes_process (
  id              bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id       bigint(20)      NOT NULL,
  process_code    varchar(64)     NOT NULL,
  process_name    varchar(128)    NOT NULL,
  sequence_no     int(11)         NOT NULL DEFAULT 10,
  device_id       bigint(20)      NOT NULL COMMENT 'openbiz_device.id',
  create_time     datetime,
  update_time     datetime,
  PRIMARY KEY (id),
  UNIQUE KEY uk_mes_proc_tenant_code (tenant_id, process_code),
  KEY idx_mes_proc_tenant (tenant_id),
  KEY idx_mes_proc_device (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz MES process';

CREATE TABLE IF NOT EXISTS openbiz_mes_work_order (
  id              bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id       bigint(20)      NOT NULL,
  work_order_no   varchar(64)     NOT NULL,
  material_id     bigint(20)      NOT NULL,
  bom_id          bigint(20)      NOT NULL,
  quantity        int(11)         NOT NULL DEFAULT 1,
  status          varchar(16)     NOT NULL DEFAULT 'CREATED',
  device_id       bigint(20)      NOT NULL COMMENT 'openbiz_device.id',
  create_time     datetime,
  update_time     datetime,
  PRIMARY KEY (id),
  UNIQUE KEY uk_mes_wo_tenant_no (tenant_id, work_order_no),
  KEY idx_mes_wo_tenant (tenant_id),
  KEY idx_mes_wo_device (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz MES work order';

CREATE TABLE IF NOT EXISTS openbiz_mes_production_record (
  id              bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id       bigint(20)      NOT NULL,
  work_order_id   bigint(20)      NOT NULL,
  device_id       bigint(20)      NOT NULL,
  quantity        int(11)         NOT NULL DEFAULT 0,
  result          varchar(16)     NOT NULL,
  command_id      varchar(64)     NOT NULL COMMENT 'DeviceCommand.command_id',
  create_time     datetime,
  PRIMARY KEY (id),
  KEY idx_mes_pr_tenant (tenant_id),
  KEY idx_mes_pr_wo (work_order_id),
  KEY idx_mes_pr_command (command_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz MES production record';

-- Tenant1 on device 1; Tenant2 on device 2
INSERT INTO openbiz_mes_material (id, tenant_id, material_code, material_name, material_type, create_time, update_time)
VALUES
  (1, 1, 'MAT-001', 'Aluminum Shell', 'RAW', NOW(), NOW()),
  (2, 2, 'MAT-001', 'Aluminum Shell', 'RAW', NOW(), NOW())
ON DUPLICATE KEY UPDATE material_name = VALUES(material_name), update_time = NOW();

INSERT INTO openbiz_mes_bom (id, tenant_id, bom_code, material_id, version, status, create_time, update_time)
VALUES
  (1, 1, 'BOM-001', 1, 'V1', 'ACTIVE', NOW(), NOW()),
  (2, 2, 'BOM-001', 2, 'V1', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE status = VALUES(status), update_time = NOW();

INSERT INTO openbiz_mes_bom_item (id, tenant_id, bom_id, material_id, quantity)
VALUES
  (1, 1, 1, 1, 1.00),
  (2, 2, 2, 2, 1.00)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);

INSERT INTO openbiz_mes_process (id, tenant_id, process_code, process_name, sequence_no, device_id, create_time, update_time)
VALUES
  (1, 1, 'PROCESS-001', 'CNC Machining', 10, 1, NOW(), NOW()),
  (2, 2, 'PROCESS-001', 'CNC Machining', 10, 2, NOW(), NOW())
ON DUPLICATE KEY UPDATE device_id = VALUES(device_id), update_time = NOW();

INSERT INTO openbiz_mes_work_order (id, tenant_id, work_order_no, material_id, bom_id, quantity, status, device_id, create_time, update_time)
VALUES
  (1, 1, 'WO-001', 1, 1, 10, 'CREATED', 1, NOW(), NOW()),
  (2, 2, 'WO-002', 2, 2, 10, 'CREATED', 2, NOW(), NOW())
ON DUPLICATE KEY UPDATE status = VALUES(status), update_time = NOW();
