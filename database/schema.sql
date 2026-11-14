-- 独立项目数据库；不会删除其他数据库。

CREATE DATABASE IF NOT EXISTS commerce_erp CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE commerce_erp;

CREATE TABLE IF NOT EXISTS tenant (
id BIGINT PRIMARY KEY, name VARCHAR(80) NOT NULL, enabled BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS category (
id BIGINT PRIMARY KEY, name VARCHAR(80) NOT NULL, parent_id BIGINT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS brand (
id BIGINT PRIMARY KEY, name VARCHAR(80) NOT NULL, country VARCHAR(30) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS supplier (
id BIGINT PRIMARY KEY, name VARCHAR(80) NOT NULL, province VARCHAR(30) NOT NULL, rating INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS warehouse (
id BIGINT PRIMARY KEY, name VARCHAR(80) NOT NULL, city VARCHAR(30) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS customer (
id BIGINT PRIMARY KEY, tenant_id BIGINT NOT NULL, name VARCHAR(80) NOT NULL,
email VARCHAR(120) NOT NULL, phone VARCHAR(24) NOT NULL, tier VARCHAR(16) NOT NULL,
province VARCHAR(30) NOT NULL, registered_at DATETIME NOT NULL,
KEY idx_customer_tenant_tier(tenant_id,tier), KEY idx_customer_email(email), KEY idx_customer_registered(registered_at),
FOREIGN KEY(tenant_id) REFERENCES tenant(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS customer_address (
id BIGINT PRIMARY KEY, customer_id BIGINT NOT NULL, province VARCHAR(30) NOT NULL,
city VARCHAR(30) NOT NULL, street VARCHAR(160) NOT NULL, postal_code VARCHAR(12) NOT NULL,
KEY idx_address_customer(customer_id), FOREIGN KEY(customer_id) REFERENCES customer(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS product (
id BIGINT PRIMARY KEY, tenant_id BIGINT NOT NULL, category_id BIGINT NOT NULL,
brand_id BIGINT NOT NULL, supplier_id BIGINT NOT NULL, name VARCHAR(120) NOT NULL,
status VARCHAR(16) NOT NULL, created_at DATETIME NOT NULL,
KEY idx_product_category(category_id), KEY idx_product_brand(brand_id), KEY idx_product_supplier(supplier_id),
FOREIGN KEY(tenant_id) REFERENCES tenant(id), FOREIGN KEY(category_id) REFERENCES category(id),
FOREIGN KEY(brand_id) REFERENCES brand(id), FOREIGN KEY(supplier_id) REFERENCES supplier(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sku (
id BIGINT PRIMARY KEY, product_id BIGINT NOT NULL, code VARCHAR(40) NOT NULL UNIQUE,
specification VARCHAR(100) NOT NULL, price DECIMAL(12,2) NOT NULL, cost DECIMAL(12,2) NOT NULL,
weight_grams INT NOT NULL, KEY idx_sku_product(product_id), FOREIGN KEY(product_id) REFERENCES product(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS inventory (
id BIGINT PRIMARY KEY, warehouse_id BIGINT NOT NULL, sku_id BIGINT NOT NULL,
available_qty INT NOT NULL, reserved_qty INT NOT NULL, reorder_point INT NOT NULL,
updated_at DATETIME NOT NULL, UNIQUE KEY uk_inventory_warehouse_sku(warehouse_id,sku_id),
KEY idx_inventory_sku(sku_id), KEY idx_inventory_sku_availability(sku_id,available_qty,reorder_point), FOREIGN KEY(warehouse_id) REFERENCES warehouse(id), FOREIGN KEY(sku_id) REFERENCES sku(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sales_order (
id BIGINT PRIMARY KEY AUTO_INCREMENT, order_no VARCHAR(40) NOT NULL UNIQUE, tenant_id BIGINT NOT NULL,
customer_id BIGINT NOT NULL, address_id BIGINT NOT NULL, warehouse_id BIGINT NOT NULL,
status VARCHAR(16) NOT NULL, channel VARCHAR(16) NOT NULL, ordered_at DATETIME NOT NULL,
paid_at DATETIME NULL, total_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
discount_amount DECIMAL(12,2) NOT NULL, shipping_fee DECIMAL(12,2) NOT NULL,
remark VARCHAR(200) NOT NULL,
KEY idx_order_customer(customer_id), KEY idx_order_time(ordered_at), KEY idx_order_status_time(status,ordered_at),
KEY idx_order_tenant_time(tenant_id,ordered_at), FOREIGN KEY(tenant_id) REFERENCES tenant(id),
FOREIGN KEY(customer_id) REFERENCES customer(id), FOREIGN KEY(address_id) REFERENCES customer_address(id),
FOREIGN KEY(warehouse_id) REFERENCES warehouse(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sales_order_item (
id BIGINT PRIMARY KEY AUTO_INCREMENT, order_id BIGINT NOT NULL, sku_id BIGINT NOT NULL,
quantity INT NOT NULL, unit_price DECIMAL(12,2) NOT NULL, discount_amount DECIMAL(12,2) NOT NULL,
line_amount DECIMAL(14,2) NOT NULL, created_at DATETIME NOT NULL,
fulfillment_status VARCHAR(16) NOT NULL, promotion_code VARCHAR(40) NULL,
KEY idx_item_order(order_id), KEY idx_item_sku(sku_id), KEY idx_item_created(created_at),
FOREIGN KEY(order_id) REFERENCES sales_order(id), FOREIGN KEY(sku_id) REFERENCES sku(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS payment (
id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, method VARCHAR(16) NOT NULL,
status VARCHAR(16) NOT NULL, amount DECIMAL(14,2) NOT NULL, paid_at DATETIME NOT NULL,
transaction_no VARCHAR(64) NOT NULL UNIQUE, KEY idx_payment_order(order_id), KEY idx_payment_time(paid_at),
FOREIGN KEY(order_id) REFERENCES sales_order(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS shipment (
id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, warehouse_id BIGINT NOT NULL,
carrier VARCHAR(20) NOT NULL, tracking_no VARCHAR(48) NOT NULL UNIQUE,
status VARCHAR(16) NOT NULL, shipped_at DATETIME NOT NULL, delivered_at DATETIME NULL,
freight_cost DECIMAL(12,2) NOT NULL, KEY idx_shipment_order(order_id), KEY idx_shipment_time(shipped_at),
FOREIGN KEY(order_id) REFERENCES sales_order(id), FOREIGN KEY(warehouse_id) REFERENCES warehouse(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS shipment_event (
id BIGINT PRIMARY KEY, shipment_id BIGINT NOT NULL, event_type VARCHAR(24) NOT NULL,
occurred_at DATETIME NOT NULL, location VARCHAR(80) NOT NULL,
KEY idx_event_shipment_time(shipment_id,occurred_at), FOREIGN KEY(shipment_id) REFERENCES shipment(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS purchase_order (
id BIGINT PRIMARY KEY, supplier_id BIGINT NOT NULL, warehouse_id BIGINT NOT NULL,
status VARCHAR(16) NOT NULL, ordered_at DATETIME NOT NULL, expected_at DATETIME NOT NULL,
KEY idx_purchase_supplier(supplier_id), FOREIGN KEY(supplier_id) REFERENCES supplier(id), FOREIGN KEY(warehouse_id) REFERENCES warehouse(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS purchase_order_item (
id BIGINT PRIMARY KEY, purchase_order_id BIGINT NOT NULL, sku_id BIGINT NOT NULL,
quantity INT NOT NULL, received_qty INT NOT NULL, unit_cost DECIMAL(12,2) NOT NULL,
KEY idx_purchase_item_order(purchase_order_id), KEY idx_purchase_item_sku(sku_id),
FOREIGN KEY(purchase_order_id) REFERENCES purchase_order(id), FOREIGN KEY(sku_id) REFERENCES sku(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS return_order (
id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, customer_id BIGINT NOT NULL,
reason VARCHAR(24) NOT NULL, status VARCHAR(16) NOT NULL, requested_at DATETIME NOT NULL,
refund_amount DECIMAL(12,2) NOT NULL, KEY idx_return_order(order_id), KEY idx_return_customer(customer_id),
FOREIGN KEY(order_id) REFERENCES sales_order(id), FOREIGN KEY(customer_id) REFERENCES customer(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS return_item (
id BIGINT PRIMARY KEY, return_order_id BIGINT NOT NULL, order_item_id BIGINT NOT NULL,
quantity INT NOT NULL, disposition VARCHAR(16) NOT NULL,
KEY idx_return_item_return(return_order_id), KEY idx_return_item_sale(order_item_id),
FOREIGN KEY(return_order_id) REFERENCES return_order(id), FOREIGN KEY(order_item_id) REFERENCES sales_order_item(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS stock_movement (
id BIGINT PRIMARY KEY, warehouse_id BIGINT NOT NULL, sku_id BIGINT NOT NULL,
movement_type VARCHAR(16) NOT NULL, quantity INT NOT NULL, occurred_at DATETIME NOT NULL,
reference_no VARCHAR(40) NOT NULL, KEY idx_movement_sku_time(sku_id,occurred_at),
KEY idx_movement_warehouse_time(warehouse_id,occurred_at), FOREIGN KEY(warehouse_id) REFERENCES warehouse(id), FOREIGN KEY(sku_id) REFERENCES sku(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS coupon (
id BIGINT PRIMARY KEY, tenant_id BIGINT NOT NULL, code VARCHAR(40) NOT NULL UNIQUE, minimum_amount DECIMAL(12,2) NOT NULL, discount_amount DECIMAL(12,2) NOT NULL, valid_until DATETIME NOT NULL, FOREIGN KEY(tenant_id) REFERENCES tenant(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS customer_coupon (
id BIGINT PRIMARY KEY, customer_id BIGINT NOT NULL, coupon_id BIGINT NOT NULL, status VARCHAR(16) NOT NULL, issued_at DATETIME NOT NULL, KEY idx_customer_coupon(customer_id,status), FOREIGN KEY(customer_id) REFERENCES customer(id), FOREIGN KEY(coupon_id) REFERENCES coupon(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS product_review (
id BIGINT PRIMARY KEY, order_item_id BIGINT NOT NULL, customer_id BIGINT NOT NULL, sku_id BIGINT NOT NULL, rating INT NOT NULL, content VARCHAR(300) NOT NULL, created_at DATETIME NOT NULL, KEY idx_review_sku(sku_id), KEY idx_review_sku_rating(sku_id,rating), KEY idx_review_customer(customer_id), KEY idx_review_item(order_item_id), FOREIGN KEY(order_item_id) REFERENCES sales_order_item(id), FOREIGN KEY(customer_id) REFERENCES customer(id), FOREIGN KEY(sku_id) REFERENCES sku(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS shopping_cart (
id BIGINT PRIMARY KEY, customer_id BIGINT NOT NULL UNIQUE, updated_at DATETIME NOT NULL, FOREIGN KEY(customer_id) REFERENCES customer(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS shopping_cart_item (
id BIGINT PRIMARY KEY, cart_id BIGINT NOT NULL, sku_id BIGINT NOT NULL, quantity INT NOT NULL, added_at DATETIME NOT NULL, KEY idx_cart_item_cart(cart_id), KEY idx_cart_item_sku(sku_id), FOREIGN KEY(cart_id) REFERENCES shopping_cart(id), FOREIGN KEY(sku_id) REFERENCES sku(id)
) ENGINE=InnoDB;
