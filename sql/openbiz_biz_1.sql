-- OpenBiz Business Phase 1. Does NOT alter IoT / sys_* tables.

CREATE TABLE IF NOT EXISTS openbiz_customer (
  id            bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id     bigint(20)      NOT NULL,
  name          varchar(64)     NOT NULL,
  phone         varchar(32)     NOT NULL,
  status        varchar(16)     NOT NULL DEFAULT 'ACTIVE',
  create_time   datetime,
  update_time   datetime,
  PRIMARY KEY (id),
  UNIQUE KEY uk_biz_customer_tenant_phone (tenant_id, phone),
  KEY idx_biz_customer_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz business customer';

CREATE TABLE IF NOT EXISTS openbiz_account (
  id            bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id     bigint(20)      NOT NULL,
  customer_id   bigint(20)      NOT NULL,
  balance       decimal(12,2)   NOT NULL DEFAULT 0.00,
  version       int(11)         NOT NULL DEFAULT 0,
  status        varchar(16)     NOT NULL DEFAULT 'ACTIVE',
  create_time   datetime,
  update_time   datetime,
  PRIMARY KEY (id),
  UNIQUE KEY uk_biz_account_customer (customer_id),
  KEY idx_biz_account_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz customer balance account';

CREATE TABLE IF NOT EXISTS openbiz_account_ledger (
  id               bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id        bigint(20)      NOT NULL,
  account_id       bigint(20)      NOT NULL,
  customer_id      bigint(20)      NOT NULL,
  txn_type         varchar(16)     NOT NULL COMMENT 'RECHARGE/CONSUME',
  amount           decimal(12,2)   NOT NULL COMMENT 'always positive',
  balance_before   decimal(12,2)   NOT NULL,
  balance_after    decimal(12,2)   NOT NULL,
  reference_type   varchar(32),
  reference_id     bigint(20),
  idempotent_key   varchar(64)     NOT NULL,
  create_time      datetime,
  PRIMARY KEY (id),
  UNIQUE KEY uk_biz_ledger_tenant_idem (tenant_id, idempotent_key),
  KEY idx_biz_ledger_account (account_id),
  KEY idx_biz_ledger_customer (tenant_id, customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz account ledger';

CREATE TABLE IF NOT EXISTS openbiz_item (
  id            bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id     bigint(20)      NOT NULL,
  item_code     varchar(64)     NOT NULL,
  item_name     varchar(128)    NOT NULL,
  item_type     varchar(16)     NOT NULL COMMENT 'SERVICE/PRODUCT',
  price         decimal(12,2)   NOT NULL,
  status        varchar(16)     NOT NULL DEFAULT 'ACTIVE',
  create_time   datetime,
  update_time   datetime,
  PRIMARY KEY (id),
  UNIQUE KEY uk_biz_item_tenant_code (tenant_id, item_code),
  KEY idx_biz_item_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz sellable item';

CREATE TABLE IF NOT EXISTS openbiz_order (
  id              bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id       bigint(20)      NOT NULL,
  customer_id     bigint(20)      NOT NULL,
  total_amount    decimal(12,2)   NOT NULL,
  status          varchar(16)     NOT NULL COMMENT 'SUCCESS',
  idempotent_key  varchar(64)     NOT NULL,
  create_time     datetime,
  update_time     datetime,
  PRIMARY KEY (id),
  UNIQUE KEY uk_biz_order_tenant_idem (tenant_id, idempotent_key),
  KEY idx_biz_order_tenant_customer (tenant_id, customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz consume order';

CREATE TABLE IF NOT EXISTS openbiz_order_item (
  id            bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id     bigint(20)      NOT NULL,
  order_id      bigint(20)      NOT NULL,
  item_id       bigint(20)      NOT NULL,
  quantity      int(11)         NOT NULL,
  unit_price    decimal(12,2)   NOT NULL,
  amount        decimal(12,2)   NOT NULL,
  PRIMARY KEY (id),
  KEY idx_biz_oi_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz order line';

-- Hair (tenant 1) SERVICE items
INSERT INTO openbiz_item (id, tenant_id, item_code, item_name, item_type, price, status, create_time, update_time)
VALUES
  (1, 1, 'CUT', 'Haircut', 'SERVICE', 50.00, 'ACTIVE', NOW(), NOW()),
  (2, 1, 'DYE', 'Hair dye', 'SERVICE', 200.00, 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE item_name = VALUES(item_name), price = VALUES(price), update_time = NOW();

-- WaterStation (tenant 2) PRODUCT items
INSERT INTO openbiz_item (id, tenant_id, item_code, item_name, item_type, price, status, create_time, update_time)
VALUES
  (3, 2, 'WATER', 'Barrel water', 'PRODUCT', 20.00, 'ACTIVE', NOW(), NOW()),
  (4, 2, 'MINERAL', 'Mineral water', 'PRODUCT', 3.00, 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE item_name = VALUES(item_name), price = VALUES(price), update_time = NOW();
