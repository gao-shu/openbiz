-- OpenBiz Shop Phase 1. Does NOT alter IoT / Business / Service / sys_* tables.

CREATE TABLE IF NOT EXISTS openbiz_shop_product (
  id            bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id     bigint(20)      NOT NULL,
  product_code  varchar(64)     NOT NULL,
  product_name  varchar(128)    NOT NULL,
  price         decimal(12,2)   NOT NULL,
  status        varchar(16)     NOT NULL DEFAULT 'ACTIVE',
  create_time   datetime,
  update_time   datetime,
  PRIMARY KEY (id),
  UNIQUE KEY uk_shop_product_tenant_code (tenant_id, product_code),
  KEY idx_shop_product_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz Shop product';

CREATE TABLE IF NOT EXISTS openbiz_shop_inventory (
  id            bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id     bigint(20)      NOT NULL,
  product_id    bigint(20)      NOT NULL,
  quantity      int(11)         NOT NULL DEFAULT 0,
  version       int(11)         NOT NULL DEFAULT 0,
  update_time   datetime,
  PRIMARY KEY (id),
  UNIQUE KEY uk_shop_inv_tenant_product (tenant_id, product_id),
  KEY idx_shop_inv_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz Shop inventory';

CREATE TABLE IF NOT EXISTS openbiz_shop_order (
  id              bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id       bigint(20)      NOT NULL,
  buyer_name      varchar(64)     NOT NULL,
  buyer_phone     varchar(32)     NOT NULL,
  total_amount    decimal(12,2)   NOT NULL,
  status          varchar(16)     NOT NULL COMMENT 'SUCCESS only in Phase 1',
  idempotent_key  varchar(64)     NOT NULL,
  create_time     datetime,
  update_time     datetime,
  PRIMARY KEY (id),
  UNIQUE KEY uk_shop_order_tenant_idem (tenant_id, idempotent_key),
  KEY idx_shop_order_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz Shop order';

CREATE TABLE IF NOT EXISTS openbiz_shop_order_item (
  id            bigint(20)      NOT NULL AUTO_INCREMENT,
  tenant_id     bigint(20)      NOT NULL,
  order_id      bigint(20)      NOT NULL,
  product_id    bigint(20)      NOT NULL,
  quantity      int(11)         NOT NULL,
  unit_price    decimal(12,2)   NOT NULL,
  line_amount   decimal(12,2)   NOT NULL,
  PRIMARY KEY (id),
  KEY idx_shop_oi_order (order_id),
  KEY idx_shop_oi_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OpenBiz Shop order line';
