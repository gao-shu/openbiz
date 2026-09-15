-- OpenBiz Phase 1.3 IoT Core (minimal)
-- Database: ry-vue
-- All IoT rows are tenant-scoped. No change to sys_* tables.

CREATE TABLE IF NOT EXISTS openbiz_product (
  id            bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT 'product id',
  tenant_id     bigint(20)      NOT NULL                    COMMENT 'tenant id',
  product_code  varchar(64)     NOT NULL                    COMMENT 'code within tenant',
  product_name  varchar(128)    NOT NULL                    COMMENT 'display name',
  protocol      varchar(32)     NOT NULL DEFAULT 'MQTT'    COMMENT 'protocol hint',
  status        char(1)         NOT NULL DEFAULT '0'        COMMENT '0=normal 1=disabled',
  description   varchar(500)    DEFAULT NULL               COMMENT 'description',
  create_time   datetime                                   COMMENT 'create time',
  update_time   datetime                                   COMMENT 'update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_openbiz_product_tenant_code (tenant_id, product_code),
  KEY idx_openbiz_product_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz IoT product';

CREATE TABLE IF NOT EXISTS openbiz_device (
  id                bigint(20)      NOT NULL AUTO_INCREMENT COMMENT 'device id',
  tenant_id         bigint(20)      NOT NULL                 COMMENT 'tenant id',
  product_id        bigint(20)      NOT NULL                 COMMENT 'product id',
  device_code       varchar(64)     NOT NULL                 COMMENT 'code within tenant',
  device_name       varchar(128)    NOT NULL                 COMMENT 'display name',
  status            char(1)         NOT NULL DEFAULT '0'     COMMENT '0=normal 1=disabled',
  online_status     varchar(16)     NOT NULL DEFAULT 'OFFLINE' COMMENT 'ONLINE/OFFLINE',
  last_online_time  datetime        DEFAULT NULL            COMMENT 'last online',
  last_offline_time datetime        DEFAULT NULL            COMMENT 'last offline',
  metadata          varchar(2000)   DEFAULT NULL            COMMENT 'json metadata',
  create_time       datetime                                COMMENT 'create time',
  update_time       datetime                                COMMENT 'update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_openbiz_device_tenant_code (tenant_id, device_code),
  KEY idx_openbiz_device_tenant (tenant_id),
  KEY idx_openbiz_device_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz IoT device';

CREATE TABLE IF NOT EXISTS openbiz_thing_model (
  id            bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT 'thing model id',
  tenant_id     bigint(20)      NOT NULL                    COMMENT 'tenant id',
  product_id    bigint(20)      NOT NULL                    COMMENT 'product id',
  model_json    mediumtext      NOT NULL                    COMMENT 'properties/services/events json',
  version       varchar(32)     NOT NULL DEFAULT '1.0'     COMMENT 'model version',
  status        char(1)         NOT NULL DEFAULT '0'        COMMENT '0=normal 1=disabled',
  create_time   datetime                                   COMMENT 'create time',
  update_time   datetime                                   COMMENT 'update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_openbiz_thing_model_product (tenant_id, product_id),
  KEY idx_openbiz_thing_model_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz thing model';

CREATE TABLE IF NOT EXISTS openbiz_device_command (
  id              bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT 'pk',
  tenant_id       bigint(20)      NOT NULL                  COMMENT 'tenant id',
  device_id       bigint(20)      NOT NULL                  COMMENT 'device id',
  command_id      varchar(64)     NOT NULL                  COMMENT 'business command uuid',
  command_name    varchar(64)     NOT NULL                  COMMENT 'thing-model service id',
  payload         varchar(2000)   DEFAULT NULL              COMMENT 'json params',
  status          varchar(16)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
  request_time    datetime                                  COMMENT 'request time',
  response_time   datetime        DEFAULT NULL              COMMENT 'response time',
  error_message   varchar(500)    DEFAULT NULL              COMMENT 'error message',
  idempotent_key  varchar(64)     DEFAULT NULL              COMMENT 'client idempotent key',
  create_time     datetime                                  COMMENT 'create time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_openbiz_command_id (command_id),
  KEY idx_openbiz_command_tenant (tenant_id),
  KEY idx_openbiz_command_device (device_id),
  KEY idx_openbiz_command_idem (tenant_id, device_id, idempotent_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz device command';

-- Seed for tenant 1 (demo-a) and tenant 2 (demo-b)
INSERT INTO openbiz_product (id, tenant_id, product_code, product_name, protocol, status, description, create_time, update_time)
VALUES
  (1, 1, 'DEMO-DOOR', 'Demo Door Controller', 'MOCK', '0', 'Phase 1.3/1.4 seed for tenant A', NOW(), NOW()),
  (2, 2, 'DEMO-DOOR', 'Demo Door Controller', 'MOCK', '0', 'Phase 1.3/1.4 seed for tenant B', NOW(), NOW())
ON DUPLICATE KEY UPDATE product_name = VALUES(product_name), protocol = VALUES(protocol), update_time = NOW();

INSERT INTO openbiz_device (id, tenant_id, product_id, device_code, device_name, status, online_status, create_time, update_time)
VALUES
  (1, 1, 1, 'DOOR-001', 'Door 001', '0', 'OFFLINE', NOW(), NOW()),
  (2, 2, 2, 'DOOR-002', 'Door 002', '0', 'OFFLINE', NOW(), NOW())
ON DUPLICATE KEY UPDATE device_name = VALUES(device_name), update_time = NOW();

INSERT INTO openbiz_thing_model (id, tenant_id, product_id, model_json, version, status, create_time, update_time)
VALUES
  (1, 1, 1, '{"properties":[{"id":"door_status","type":"enum"}],"services":[{"id":"open_door","params":[]},{"id":"close_door","params":[]}],"events":[{"id":"door_open"}]}', '1.0', '0', NOW(), NOW()),
  (2, 2, 2, '{"properties":[{"id":"door_status","type":"enum"}],"services":[{"id":"open_door","params":[]},{"id":"close_door","params":[]}],"events":[{"id":"door_open"}]}', '1.0', '0', NOW(), NOW())
ON DUPLICATE KEY UPDATE model_json = VALUES(model_json), update_time = NOW();
